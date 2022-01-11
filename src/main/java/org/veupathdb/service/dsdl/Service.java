package org.veupathdb.service.dsdl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.service.eda.common.auth.StudyAccess;
import org.veupathdb.service.eda.common.client.DatasetAccessClient;
import org.veupathdb.service.eda.common.client.DatasetAccessClient.StudyDatasetInfo;
import org.veupathdb.service.generated.model.FileContentResponseStream;
import org.veupathdb.service.generated.resources.Download;

@Authenticated(allowGuests = true)
public class Service implements Download {

  @Context
  private ContainerRequestContext _request;

  @Override
  public GetDownloadByProjectAndStudyIdResponse getDownloadByProjectAndStudyId(String project, String studyId) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByProjectAndStudyIdResponse.respond200WithApplicationJson(
        FileRetrieval.getReleaseList(project, datasetHash));
  }

  @Override
  public GetDownloadByProjectAndStudyIdAndReleaseResponse getDownloadByProjectAndStudyIdAndRelease(String project, String studyId, String release) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByProjectAndStudyIdAndReleaseResponse.respond200WithApplicationJson(
        FileRetrieval.getFileList(project, datasetHash, release));
  }

  @Override
  public GetDownloadByProjectAndStudyIdAndReleaseAndFileResponse getDownloadByProjectAndStudyIdAndReleaseAndFile(String project, String studyId, String release, String file) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowResultsAll);
    return GetDownloadByProjectAndStudyIdAndReleaseAndFileResponse.respond200WithTextPlain(
        new FileContentResponseStream(FileRetrieval.getFileStreamer(project, datasetHash, release, file)));
  }

  private String checkPermsAndFetchDatasetHash(String studyId, Function<StudyAccess, Boolean> accessGranter) {
    Entry<String,String> authHeader = StudyAccess.readAuthHeader(_request, RequestKeys.AUTH_HEADER);
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
}
