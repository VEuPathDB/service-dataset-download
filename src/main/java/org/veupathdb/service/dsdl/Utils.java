package org.veupathdb.service.dsdl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gusdb.fgputil.runtime.Environment;

public class Utils {
  public static Path getReadableDir(Path dirPath) {
    if (Files.isDirectory(dirPath) && Files.isReadable(dirPath)) {
      return dirPath;
    }
    throw new RuntimeException("Configured data dir '" + dirPath + "' is not a readable directory.");
  }
}
