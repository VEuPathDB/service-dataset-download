package org.veupathdb.service.dsdl;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gusdb.fgputil.runtime.Environment;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.ContainerResources;

import static org.gusdb.fgputil.runtime.Environment.getRequiredVar;

/**
 * Service Resource Registration.
 *
 * This is where all the individual service specific resources and middleware
 * should be registered.
 */
public class Resources extends ContainerResources {

  public static final String DATASET_ACCESS_SERVICE_URL = getRequiredVar("DATASET_ACCESS_SERVICE_URL");
  public static final Path DATA_FILES_PARENT_DIR = checkDataDir();

  private static Path checkDataDir() {
    Path dataDir = Paths.get(Environment.getRequiredVar("DATA_FILES_PARENT_DIR"));
    if (Files.isDirectory(dataDir) && Files.isReadable(dataDir)) {
      return dataDir;
    }
    throw new RuntimeException("Configured data dir '" + dataDir + "' is not a readable directory.");
  }

  public Resources(Options opts) {
    super(opts);
    enableAuth();
  }

  /**
   * Returns an array of JaxRS endpoints, providers, and contexts.
   *
   * Entries in the array can be either classes or instances.
   */
  @Override
  protected Object[] resources() {
    return new Object[] {
      Service.class,
    };
  }
}
