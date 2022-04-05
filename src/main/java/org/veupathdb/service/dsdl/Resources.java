package org.veupathdb.service.dsdl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import jakarta.ws.rs.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.runtime.Environment;
import org.gusdb.fgputil.runtime.ProjectSpecificProperties;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.ContainerResources;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;

import static org.gusdb.fgputil.runtime.Environment.getRequiredVar;
import static org.gusdb.fgputil.runtime.ProjectSpecificProperties.PropertySpec.required;

/**
 * Service Resource Registration.
 *
 * This is where all the individual service specific resources and middleware
 * should be registered.
 */
public class Resources extends ContainerResources {

  private static final Logger LOG = LogManager.getLogger(Resources.class);

  public static final String DATASET_ACCESS_SERVICE_URL = getRequiredVar("DATASET_ACCESS_SERVICE_URL");
  public static final Path DOWNLOAD_FILES_MOUNT_PATH = getReadableDir(Paths.get(Environment.getRequiredVar("DOWNLOAD_FILES_MOUNT_PATH")));

  private static final String RAW_FILES_DIR_PROP = "RAW_FILES_DIR";
  private static Map<String,String> PROJECT_DIR_MAP;

  public Resources(Options opts) {
    super(opts);

    // log raw files location
    LOG.info("Will use parent raw files directory: " + DOWNLOAD_FILES_MOUNT_PATH);

    // check for valid project-specific props
    PROJECT_DIR_MAP = new ProjectSpecificProperties<>(
        new ProjectSpecificProperties.PropertySpec[] { required(RAW_FILES_DIR_PROP) },
        map -> map.get(RAW_FILES_DIR_PROP)
    ).toMap();
    LOG.info("Schema map: " + FormatUtil.prettyPrint(PROJECT_DIR_MAP, FormatUtil.Style.MULTI_LINE));

    enableJerseyTrace();
    DbManager.initAccountDatabase(opts);
    DbManager.initUserDatabase(opts);
    enableAuth();
  }

  public static Path getDatasetsParentDir(String projectId) {
    if (!PROJECT_DIR_MAP.containsKey(projectId)) {
      throw new NotFoundException("Invalid project ID: " + projectId);
    }
    return getReadableDir(DOWNLOAD_FILES_MOUNT_PATH.resolve(PROJECT_DIR_MAP.get(projectId)));
  }

  private static Path getReadableDir(Path dirPath) {
    if (Files.isDirectory(dirPath) && Files.isReadable(dirPath)) {
      return dirPath;
    }
    throw new RuntimeException("Configured data dir '" + dirPath + "' is not a readable directory.");
  }

  @Override
  protected Object[] resources() {
    return new Object[] {
      Service.class,
    };
  }
}
