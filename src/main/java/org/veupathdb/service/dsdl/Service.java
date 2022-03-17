package org.veupathdb.service.dsdl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.MapBuilder;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.server.middleware.CustomResponseHeadersFilter;
import org.veupathdb.service.eda.common.auth.StudyAccess;
import org.veupathdb.service.eda.common.client.DatasetAccessClient;
import org.veupathdb.service.eda.common.client.DatasetAccessClient.StudyDatasetInfo;
import org.veupathdb.service.generated.model.FileContentResponseStream;
import org.veupathdb.service.generated.resources.Download;

import static org.gusdb.fgputil.functional.Functions.cSwallow;

@Authenticated(allowGuests = true)
public class Service implements Download {

  private static final Logger LOG = LogManager.getLogger(Service.class);

  @Context
  ContainerRequest _request;

  @Override
  public GetDownloadByProjectAndStudyIdResponse getDownloadByProjectAndStudyId(String project, String studyId) {
    LOG.info("Get releases by " + project + " and " + studyId);
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByProjectAndStudyIdResponse.respond200WithApplicationJson(
        new FileStore(Resources.getDatasetsParentDir(project))
            .getReleaseNames(datasetHash)
            .orElseThrow(NotFoundException::new));
  }

  @Override
  public GetDownloadByProjectAndStudyIdAndReleaseResponse getDownloadByProjectAndStudyIdAndRelease(String project, String studyId, String release) {
    LOG.info("Get files by " + project + " and " + studyId + " and " + release);
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByProjectAndStudyIdAndReleaseResponse.respond200WithApplicationJson(
        new FileStore(Resources.getDatasetsParentDir(project))
            .getFiles(datasetHash, release)
            .orElseThrow(NotFoundException::new));
  }

  @Override
  public GetDownloadByProjectAndStudyIdAndReleaseAndFileResponse getDownloadByProjectAndStudyIdAndReleaseAndFile(String project, String studyId, String release, String fileName) {
    LOG.info("Get file content at " + project + " and " + studyId + " and " + release + " and " + fileName);
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowResultsAll);
    Path filePath = new FileStore(Resources.getDatasetsParentDir(project))
        .getFilePath(datasetHash, release, fileName)
        .orElseThrow(NotFoundException::new);
    String dispositionHeaderValue = "attachment; filename=\"" + fileName + "\"";
    _request.setProperty(CustomResponseHeadersFilter.CUSTOM_HEADERS_KEY,
        new MapBuilder<>(HttpHeaders.CONTENT_DISPOSITION, dispositionHeaderValue).toMap());
    return GetDownloadByProjectAndStudyIdAndReleaseAndFileResponse.respond200WithTextPlain(
        new FileContentResponseStream(cSwallow(
            out -> IoUtil.transferStream(out, Files.newInputStream(filePath)))));
  }

  private String checkPermsAndFetchDatasetHash(String studyId, Function<StudyAccess, Boolean> accessGranter) {
    try {
      Entry<String,String> authHeader = UserProvider.getSubmittedAuth(_request).orElseThrow();
      Map<String, StudyDatasetInfo> studyMap =
          new DatasetAccessClient(Resources.DATASET_ACCESS_SERVICE_URL, authHeader).getStudyDatasetInfoMapForUser();
      StudyDatasetInfo study = studyMap.get(studyId);
      if (study == null) {
        throw new NotFoundException("Study '" + studyId + "' cannot be found [dataset access service].");
      }
      if (!accessGranter.apply(study.getStudyAccess())) {
        throw new ForbiddenException("Permission Denied");
      }
      return study.getSha1Hash();
    }
    catch (Exception e) {
      LOG.error("Unable to check study permissions and convert studyId to dataset hash", e);
      throw e;
    }
  }
}
