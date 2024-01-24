package com.zinphraek.leprestigehall.domain.addon;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.zinphraek.leprestigehall.domain.reservation.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "requested_add_ons")
public class RequestedAddOn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne
    private AddOn addOn;

    @ManyToOne
    @JsonBackReference
    private Reservation reservation;

    @NotNull
    private Double quantity;

    public RequestedAddOn() {
    }

    public RequestedAddOn(Long id, AddOn addOn, Reservation reservation, Double quantity) {
        this.id = id;
        this.addOn = addOn;
        this.reservation = reservation;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AddOn getAddOn() {
        return addOn;
    }

    public void setAddOn(AddOn addOn) {
        this.addOn = addOn;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestedAddOn that)) return false;
        return Objects.equals(id, that.id) && addOn.equals(that.addOn) && reservation
                .equals(that.reservation) && quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, addOn, reservation, quantity);
    }

    @Override
    public String toString() {
        return "RequestedAddOn{" +
                "id=" + id +
                ", addOn=" + addOn +
                ", reservation=" + reservation +
                ", quantity=" + quantity +
                '}';
    }
}
