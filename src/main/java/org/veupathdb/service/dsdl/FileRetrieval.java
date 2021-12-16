package org.veupathdb.service.dsdl;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.veupathdb.service.generated.model.File;

public class FileRetrieval {

  public static List<String> getReleaseList(String studyId) {
    return Collections.emptyList();
  }

  public static List<File> getFileList(String studyId, String release) {
    return Collections.emptyList();
  }

  public static Consumer<OutputStream> getFileStreamer(String studyId, String release, String file) {
    return out -> {};
  }

}
