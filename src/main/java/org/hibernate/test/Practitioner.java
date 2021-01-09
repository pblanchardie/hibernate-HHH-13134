package org.hibernate.test;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Practitioner {

    @Id
    private Long id;

    @LazyToOne(LazyToOneOption.NO_PROXY)
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "practitioner")
    private Set<MessageWithoutLazyToOne> messages = new HashSet<>();

    @ManyToMany(mappedBy = "practitioners")
    private Set<Patient> patients = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Practitioner setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Practitioner setUser(User user) {
        this.user = user;
        return this;
    }

    public Set<MessageWithoutLazyToOne> getMessages() {
        return messages;
    }

    public Practitioner setMessages(Set<MessageWithoutLazyToOne> messages) {
        this.messages = messages;
        return this;
    }

    public Set<Patient> getPatients() {
        return patients;
    }

    public Practitioner setPatients(Set<Patient> patients) {
        this.patients = patients;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Practitioner)) return false;
        Practitioner other = (Practitioner) o;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
