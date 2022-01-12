package org.veupathdb.service.dsdl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import org.gusdb.fgputil.FormatUtil;
import org.veupathdb.service.generated.model.FileImpl;

public class FileInfo extends FileImpl {

  private final Path _path;

  public FileInfo(Path filePath) {
    try {
      _path = filePath;
      BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
      setName(filePath.getFileName().toString());
      setSize(String.valueOf(attrs.size()));
      setModifiedDate(FormatUtil.formatDate(new Date(attrs.lastModifiedTime().toMillis())));
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to read file metadata", e);
    }
  }

  @JsonIgnore
  public Path getPath() {
    return _path;
  }
}
