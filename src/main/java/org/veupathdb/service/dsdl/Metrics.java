package org.veupathdb.service.dsdl;

import io.prometheus.client.Counter;

public class Metrics {
  private static final Counter DOWNLOADS_BY_DATASET = Counter.build()
      .name("dataset_download")
      .help("Total successfully uploaded datasets.")
      .labelNames("dataset_name")
      .register();

  public static void countDownload(String datasetName) {
    DOWNLOADS_BY_DATASET.labels(datasetName).inc();
  }
}
