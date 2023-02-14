package org.veupathdb.service.dsdl;

import io.prometheus.client.Counter;

/**
 * Utility class for emitting domain-specific service metrics.
 */
public class ServiceMetrics {
  private static final String STUDY_LABEL = "study";
  private static final String USER_LABEL = "user";
  private static final String RESOURCE_LABEL = "resource";

  private static final Counter STUDY_FILE_DOWNLOAD_METRIC = Counter.build()
      .name("dataset_download_requested")
      .help("Dataset download request count")
      .labelNames(STUDY_LABEL, USER_LABEL, RESOURCE_LABEL)
      .register();

  public static DownloadReporter downloadReporter() {
    return new DownloadReporter();
  }

  /**
   * Builder-like interface for reporting download count metrics.
   */
  public static class DownloadReporter {
    private String studyName;
    private String userId;
    private String resourceName;

    public DownloadReporter withStudyName(String studyName) {
      this.studyName = studyName;
      return this;
    }

    public DownloadReporter withUserId(String userId) {
      this.userId = userId;
      return this;
    }

    public DownloadReporter withResourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
    }

    public void report() {
      STUDY_FILE_DOWNLOAD_METRIC.labels(studyName, userId, resourceName).inc();
    }
  }
}
