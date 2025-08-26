package com.hachimi.mamboaiplatform.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerationStatus {
  private String status; // running | built | failed | stopped
  private String sessionId;
  private String message; // build message or error message
  private long updatedAt; // epoch millis
}
