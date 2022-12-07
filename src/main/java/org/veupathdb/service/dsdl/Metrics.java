package org.veupathdb.service.dsdl;

import io.prometheus.client.Counter;

public class Metrics {
  private static final Counter DOWNLOADS_BY_STUDY_ID = Counter.build()
      .name("dataset_download")
      .help("Total successfully uploaded datasets.")
      .labelNames("study_id")
      .register();

  public static void countDownload(String studyId) {
    DOWNLOADS_BY_STUDY_ID.labels(studyId).inc();
  }
}
