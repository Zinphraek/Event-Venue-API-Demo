package com.zinphraek.leprestigehall.domain.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mail {
  private String from;
  private String to;
  private String subject;
  private String emailTemplate;
  private String content;
  private List<Attachment> attachments = new ArrayList<>();
  private Map<String, Object> variables = new HashMap<>();

  public Mail() {}

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getEmailTemplate() {
    return emailTemplate;
  }

  public void setEmailTemplate(String emailTemplate) {
    this.emailTemplate = emailTemplate;
  }

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }

  public void addAttachment(Attachment attachment) {
    this.attachments.add(attachment);
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public void addVariable(String varName, Object varValue) {
    variables.put(varName, varValue);
  }
}
