package org.hibernate.test;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class MessageWithLazyToOne {

    @Id
    private Long id;

    @LazyToOne(LazyToOneOption.NO_PROXY)
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @LazyToOne(LazyToOneOption.NO_PROXY)
    @ManyToOne(fetch = FetchType.LAZY)
    private Practitioner practitioner;

    public Long getId() {
        return id;
    }

    public MessageWithLazyToOne setId(Long id) {
        this.id = id;
        return this;
    }

    public Patient getPatient() {
        return patient;
    }

    public MessageWithLazyToOne setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public Practitioner getPractitioner() {
        return practitioner;
    }

    public MessageWithLazyToOne setPractitioner(Practitioner practitioner) {
        this.practitioner = practitioner;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageWithLazyToOne)) return false;
        MessageWithLazyToOne message = (MessageWithLazyToOne) o;
        return Objects.equals(getId(), message.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
