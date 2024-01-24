package com.zinphraek.leprestigehall.domain.email;

import java.util.HashMap;
import java.util.Map;

public class Attachment {
  private String name;
  private String template;
  private String cssPath;
  private Map<String, Object> variables = new HashMap<>();

  public Attachment() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getCssPath() {
    return cssPath;
  }

  public void setCssPath(String cssPath) {
    this.cssPath = cssPath;
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
