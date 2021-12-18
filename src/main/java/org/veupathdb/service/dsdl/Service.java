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
  public GetDownloadByStudyIdResponse getDownloadByStudyId(String studyId) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByStudyIdResponse.respond200WithApplicationJson(
        FileRetrieval.getReleaseList(datasetHash));
  }

  @Override
  public GetDownloadByStudyIdAndReleaseResponse getDownloadByStudyIdAndRelease(String studyId, String release) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowStudyMetadata);
    return GetDownloadByStudyIdAndReleaseResponse.respond200WithApplicationJson(
        FileRetrieval.getFileList(datasetHash, release));
  }

  @Override
  public GetDownloadByStudyIdAndReleaseAndFileResponse getDownloadByStudyIdAndReleaseAndFile(String studyId, String release, String file) {
    String datasetHash = checkPermsAndFetchDatasetHash(studyId, StudyAccess::allowResultsAll);
    return GetDownloadByStudyIdAndReleaseAndFileResponse.respond200WithTextPlain(
        new FileContentResponseStream(FileRetrieval.getFileStreamer(datasetHash, release, file)));
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
