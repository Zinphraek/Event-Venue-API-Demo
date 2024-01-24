package com.zinphraek.leprestigehall.domain.faq;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "faqs")
public class FAQ {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Category is required.")
  private String category;

  @NotBlank(message = "Question is required.")
  private String question;

  @Lob
  @NotBlank(message = "Answer is required.")
  private String answer;

  @Lob private String moreDetail;

  public FAQ() {}

  public FAQ(String category, String question, String answer, String moreDetail) {
    this.category = category;
    this.question = question;
    this.answer = answer;
    this.moreDetail = moreDetail;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCategory() {
    return category;
  }

  public String getQuestion() {
    return question;
  }

  public String getAnswer() {
    return answer;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getMoreDetail() {
    return moreDetail;
  }

  public void setMoreDetail(String moreDetail) {
    this.moreDetail = moreDetail;
  }
}
