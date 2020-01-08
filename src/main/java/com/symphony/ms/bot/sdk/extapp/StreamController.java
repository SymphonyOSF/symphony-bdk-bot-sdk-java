package com.symphony.ms.bot.sdk.extapp;

import com.symphony.ms.bot.sdk.internal.symphony.StreamType;
import com.symphony.ms.bot.sdk.internal.symphony.SymphonyService;
import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sample code. Implementation of an extension app endpoint for streams
 *
 * @author Gabriel Berberia
 */
@RestController
@RequestMapping("/appauth/stream")
public class StreamController {

  private final SymphonyService symphonyService;

  public StreamController(SymphonyService symphonyService) {
    this.symphonyService = symphonyService;
  }

  @GetMapping
  public List<SymphonyStream> getUserStreams() {
    return symphonyService.getUserStreams(StreamType.knownValues(), true);
  }

}
