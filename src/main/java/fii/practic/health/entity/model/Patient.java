package fii.practic.health.entity.model;

import javax.persistence.*;

@Entity
public class Patient extends Person{

    private Integer age;

    @ManyToOne
    @JoinColumn(nullable = true)
    private Doctor doctor;


    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
