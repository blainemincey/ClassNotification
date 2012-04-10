/**
 * 
 */
package edu.kennesaw.msacs.classnotification.html5.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.kennesaw.msacs.classnotification.html5.model.Student;

/**
 * 
 * @author bmincey
 *
 */
@Path("/students")
@RequestScoped
@Stateful
public class StudentService
{
    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Student> studentEventSrc;

    @Inject
    private Validator validator;
    
    private static final String ATT = "@txt.att.net ";
    
    private static final String TMOBILE = "@tmomail.net";
    
    private static final String VERIZON = "@vtext.com";
    
    @GET
    @Produces("text/xml")
    public List<Student> listAllStudents()
    {
        @SuppressWarnings("unchecked")
        final List<Student> results = em.createQuery(
                "select s from Student s order by s.name").getResultList();
        return results;
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Student> listAllMembersJSON()
    {
        @SuppressWarnings("unchecked")
        final List<Student> results = em.createQuery(
                "select s from Student s order by s.name").getResultList();
        return results;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("text/xml")
    public Student lookupStudentById(@PathParam("id") long id)
    {
        return em.find(Student.class, id);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMemberGet(@QueryParam("name") String name,
            @QueryParam("className") String className,
            @QueryParam("phoneNumber") String phone,
            @QueryParam("phoneCarrier") String phoneCarrier)
    {
        return createNewStudent(name, className, phone, phoneCarrier);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMemberPost(@FormParam("name") String name,
            @FormParam("className") String className,
            @FormParam("phoneNumber") String phone,
            @FormParam("phoneCarrier") String phoneCarrier)
    {
        return createNewStudent(name, className, phone, phoneCarrier);
    }

    @GET
    @Path("/delete/{studentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteStudent(@PathParam("studentId") final int studentId)
    {
        Student student = this.lookupStudentById(studentId);
        if (student != null)
        {
            log.info("Deleting student: " + student.getId() + ", "
                    + student.getName());
            em.remove(student);
        }
    }

    /**
     * 
     * @param name
     * @param className
     * @param phone
     * @param phoneCarrier
     * @return
     */
    public Response createNewStudent(String name, String className,
            String phone, String phoneCarrier)
    {
        Response.ResponseBuilder builder = null;

        Student student = new Student();
        student.setName(name);
        student.setClassName(className);
        student.setPhoneNumber(phone);
        student.setPhoneCarrier(phoneCarrier);

        try
        {
            validateStudent(student);

            log.info("Registering " + student.getName());
            em.persist(student);

            studentEventSrc.fire(student);

            builder = Response.ok();
        }
        catch (ConstraintViolationException ce)
        {
            builder = createViolationResponse(ce.getConstraintViolations());
        }
        catch (ValidationException e)
        {
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("phoneNumber", "Phone Number already registered");
            builder = Response.status(Response.Status.CONFLICT).entity(
                    responseObj);
        }

        return builder.build();
    }

    /**
     * 
     * @param student
     * @throws ConstraintViolationException
     * @throws ValidationException
     */
    private void validateStudent(Student student)
            throws ConstraintViolationException, ValidationException
    {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Student>> violations = validator
                .validate(student);

        if (!violations.isEmpty())
        {
            throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the phone number
        if (phoneNumberAlreadyExists(student.getPhoneNumber()))
        {
            throw new ValidationException("Unique Phone Number Violation");
        }
    }

    /**
     * 
     * @param violations
     * @return
     */
    private Response.ResponseBuilder createViolationResponse(
            Set<ConstraintViolation<?>> violations)
    {
        log.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations)
        {
            responseObj.put(violation.getPropertyPath().toString(),
                    violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }


    /**
     * 
     * @param phoneNumber
     * @return
     */
    public boolean phoneNumberAlreadyExists(String phoneNumber)
    {
        Query checkPhoneNumberExists = em
                .createQuery(" SELECT COUNT(b.phoneNumber) FROM Student b WHERE b.phoneNumber=:phoneNumberparam");
        checkPhoneNumberExists.setParameter("phoneNumberparam", phoneNumber);
        long matchCounter = 0;
        matchCounter = (Long) checkPhoneNumberExists.getSingleResult();
        if (matchCounter > 0)
        {
            return true;
        }
        return false;
    }
    
    @POST
    @Path("/cancelClass")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelClass(@FormParam("className") String className)
    {
        log.info("Cancel class for class: " + className);
        Response.ResponseBuilder builder = null;

        this.cancelClassForClass(className);
        
        // Create an "ok" response
        builder = Response.ok();

        return builder.build();
    }
    
    /**
     * 
     * @param className
     */
    private void cancelClassForClass(String className)
    {
        Query query = em.createQuery(
                "SELECT s from Student s WHERE s.className=:classNameParam");
        query.setParameter("classNameParam", className);
        List<Student> students = query.getResultList();
        if(students != null && students.size() > 0)
        {
            log.info("Preparing to contact " + students.size() + " about cancelled class");
            for(Student student : students)
            {
                this.sendStudentMessage(student);
            }
        }
        else
        {
            log.info("No students registered to receive alerts for class: " + className);
        }
    }
    
    /**
     * 
     * @param student
     */
    private void sendStudentMessage(Student student)
    {
        log.info("Sending message to phone number " + student.getPhoneNumber() + " on " +
                student.getPhoneCarrier());
        if(student.getPhoneCarrier().equals("AT&T"))
        {
            log.info("AT&T");
            sendSMS(student.getPhoneNumber() + StudentService.ATT, student.getClassName());
        }
        else if(student.getPhoneCarrier().equals("T-Mobile"))
        {
            log.info("T-Mobile");
            sendSMS(student.getPhoneNumber() + StudentService.TMOBILE, student.getClassName());
        }
        else if(student.getPhoneCarrier().equals("Verizon"))
        {
            log.info("Verizon");
            sendSMS(student.getPhoneNumber() + StudentService.VERIZON, student.getClassName());
        }
    }
    
    /**
     * 
     * @param destination
     * @param className
     */
    private void sendSMS(final String destination, final String className)
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
 
        Session session = Session.getDefaultInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("USERID","PASSWORD");
                }
            });
 
        try {
 
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("EMAIL ADDRESS"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destination));
            message.setSubject("CLASS CANCELLED");
            message.setText("Today's " + className + " has been cancelled.");
 
            Transport.send(message);
 
            log.info("Mail has been sent to " + destination);
 
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
