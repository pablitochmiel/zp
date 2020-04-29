package edu.agh.zp.hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity(name = "Committee")

public class CommitteeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="comID")
    private long comID;


    @NotNull
    @Column(name="comName")
    private String comName;


    @Column(name="comDescription")
    private String comDescription;


}