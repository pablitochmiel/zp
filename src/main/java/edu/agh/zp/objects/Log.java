package edu.agh.zp.objects;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static edu.agh.zp.objects.Log.Status.ACCEPT;
import static edu.agh.zp.objects.Log.Status.FAILED;


@Entity
public class Log{
    public enum Status{ ACCEPT, FAILED}
    public enum Operation{ ADD, EDIT, LOGIN}
    public enum ElementType{ DOCUMENT, VOTING, USER }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name= "logid")
    private long id;

    @Column(name= "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "operation")
    private String operation;

    @Column(name = "logdescription", nullable = false)
    @NotEmpty
    private String logDescription;

    @Column(name= "elementType")
    private String elementType;

    @Column(name= "elementID")
    private long elementID;

    @ManyToOne
    @JoinColumn(name="citizenID")
    private CitizenEntity user;

    private String status;

    public Log(){}


    // constructor for all accepted operations on existing element, performed by signed in users (or accepted registration or sign in)
    public Log(Operation operation,
               @NotEmpty String logDescription,
               ElementType elementType,
               long elementID,
               CitizenEntity user,
               Status status) {
        this.operation = operation.toString();
        this.logDescription = logDescription;
        this.elementType = elementType.toString();
        this.elementID = elementID;
        this.user = user;
        this.status = status.toString();
        this.time = LocalDateTime.now();
    }

    // failed sign in or sign up
    public Log(Operation operation, @NotEmpty String logDescription, ElementType elementType, Status status) {
        this.operation = operation.toString();
        this.logDescription = logDescription;
        this.elementType = elementType.toString();
        this.user = null;
        this.status = status.toString();
        this.time = LocalDateTime.now();
    }

    static public Log failedSignInOrSignUp(Operation operation, @NotEmpty String logDescription){
        return new Log(operation, logDescription, ElementType.USER, Status.FAILED);
    }

    static public Log acceptSignInOrSignUp(Operation operation, @NotEmpty String logDescription, CitizenEntity user){
        return new Log(operation, logDescription, ElementType.USER, user.getCitizenID(), user, Status.ACCEPT);
    }

    public long getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getLogDescription() {
        return logDescription;
    }

    public void setLogDescription(String logDescription) {
        this.logDescription = logDescription;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public long getElementID() {
        return elementID;
    }

    public void setElementID(long elementID) {
        this.elementID = elementID;
    }

    public CitizenEntity getUser() {
        return user;
    }

    public void setUser(CitizenEntity user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}