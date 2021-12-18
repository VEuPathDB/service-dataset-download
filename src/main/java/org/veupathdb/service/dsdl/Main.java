package org.veupathdb.service.dsdl;

import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.ContainerResources;
import org.veupathdb.lib.container.jaxrs.server.Server;

public class Main extends Server {
  public static void main(String[] args) {
    var server = new Main();
    server.enableUserDB();
    server.enableAccountDB();
    server.start(args);
  }

  @Override
  protected ContainerResources newResourceConfig(Options options) {
    return new Resources(options);
  }
}
