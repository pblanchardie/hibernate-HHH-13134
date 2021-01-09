package org.hibernate.test;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Patient {

    @Id
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Practitioner> practitioners = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Patient setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Patient setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Practitioner> getPractitioners() {
        return practitioners;
    }

    public Patient setPractitioners(Set<Practitioner> practitioners) {
        this.practitioners = practitioners;
        return this;
    }

    public Patient addPractitioner(Practitioner practitioner) {
        this.practitioners.add(practitioner);
        practitioner.getPatients().add(this);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient other = (Patient) o;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
