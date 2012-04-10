/**
 * 
 */
package edu.kennesaw.msacs.classnotification.html5.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;


import org.hibernate.validator.constraints.NotEmpty;

/**
 * 
 * @author bmincey
 *
 */
@Entity
@XmlRootElement
@Table(name = "Student", uniqueConstraints = @UniqueConstraint(columnNames = "phoneNumber"))
public class Student implements Serializable
{
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Size(min = 1, max = 25, message = "1-25 letters and spaces")
    @Pattern(regexp = "[A-Za-z ]*", message = "Only letters and spaces")
    private String name;

    @NotNull
    @NotEmpty
    private String className;

    @NotNull
    @Size(min = 10, max = 12, message = "10-12 Numbers")
    @Digits(fraction = 0, integer = 12, message = "Not valid")
    @Column(name = "phoneNumber")
    private String phoneNumber;

    @NotNull
    @NotEmpty
    private String phoneCarrier;
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getPhoneCarrier()
    {
        return phoneCarrier;
    }

    public void setPhoneCarrier(String phoneCarrier)
    {
        this.phoneCarrier = phoneCarrier;
    }

    

}