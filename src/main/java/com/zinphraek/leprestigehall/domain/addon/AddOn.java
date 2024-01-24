package com.zinphraek.leprestigehall.domain.addon;


import com.zinphraek.leprestigehall.domain.media.AddOnMedia;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(name = "addons")
public class AddOn {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  private String category;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "media_id", referencedColumnName = "id")
  private AddOnMedia media;

  private String description;

  @NotNull
  private Double price;

  private boolean isActive;


  public AddOn() {
  }

  public AddOn(Long id, String name, String category, AddOnMedia media, String description, Double price, boolean isActive) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.media = media;
    this.description = description;
    this.price = price;
    this.isActive = isActive;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public AddOnMedia getMedia() {
    return media;
  }

  public void setMedia(AddOnMedia media) {
    this.media = media;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AddOn addOn)) return false;
    return isActive == addOn.isActive && Objects.equals(id, addOn.id) && name.equals(addOn.name)
        && category.equals(addOn.category) && Objects.equals(media, addOn.media)
        && Objects.equals(description, addOn.description) && price.equals(addOn.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, category, media, description, price, isActive);
  }

  @Override
  public String toString() {
    return "AddOn{" +
        "id=" + id +
        ", itemName='" + name + '\'' +
        ", category='" + category + '\'' +
        ", image=" + media +
        ", description='" + description + '\'' +
        ", itemPrice=" + price +
        ", active=" + isActive +
        '}';
  }
}
