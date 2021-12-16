package org.veupathdb.service.dsdl;

import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.generated.model.FileContentResponseStream;
import org.veupathdb.service.generated.resources.Download;

@Authenticated(allowGuests = true)
public class Service implements Download {

  @Override
  public GetDownloadByStudyIdResponse getDownloadByStudyId(String studyId) {
    return GetDownloadByStudyIdResponse.respond200WithApplicationJson(
        FileRetrieval.getReleaseList(studyId));
  }

  @Override
  public GetDownloadByStudyIdAndReleaseResponse getDownloadByStudyIdAndRelease(String studyId, String release) {
    return GetDownloadByStudyIdAndReleaseResponse.respond200WithApplicationJson(
        FileRetrieval.getFileList(studyId, release));
  }

  @Override
  public GetDownloadByStudyIdAndReleaseAndFileResponse getDownloadByStudyIdAndReleaseAndFile(String studyId, String release, String file) {
    return GetDownloadByStudyIdAndReleaseAndFileResponse.respond200WithTextPlain(
        new FileContentResponseStream(FileRetrieval.getFileStreamer(studyId, release, file)));
  }

}
