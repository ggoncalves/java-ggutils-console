package com.ggoncalves.ggutils.console.test.util;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AbstractAppender {
  private final List<String> messages = new ArrayList<>();

  public TestAppender() {
    super("TestAppender", null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
  }

  @Override
  public void append(LogEvent event) {
    messages.add(event.getMessage().getFormattedMessage());
  }

  public List<String> getMessages() {
    return new ArrayList<>(messages);
  }

  public void clear() {
    messages.clear();
  }

  public static TestAppender attachToLogger(Logger logger) {
    TestAppender appender = new TestAppender();
    appender.start();
    logger.addAppender(appender);
    return appender;
  }
}