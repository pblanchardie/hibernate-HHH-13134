package org.hibernate.test;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class MessageWithoutLazyToOne {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Practitioner practitioner;

    public Long getId() {
        return id;
    }

    public MessageWithoutLazyToOne setId(Long id) {
        this.id = id;
        return this;
    }

    public Patient getPatient() {
        return patient;
    }

    public MessageWithoutLazyToOne setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public Practitioner getPractitioner() {
        return practitioner;
    }

    public MessageWithoutLazyToOne setPractitioner(Practitioner practitioner) {
        this.practitioner = practitioner;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageWithoutLazyToOne)) return false;
        MessageWithoutLazyToOne message = (MessageWithoutLazyToOne) o;
        return Objects.equals(getId(), message.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
