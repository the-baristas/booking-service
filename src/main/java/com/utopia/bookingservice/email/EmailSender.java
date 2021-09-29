package com.utopia.bookingservice.email;

import javax.mail.MessagingException;

import com.utopia.bookingservice.entity.Booking;
import com.utopia.bookingservice.entity.Passenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.Comparator;

@Service
public class EmailSender {

    @Value("${aws.emailSender}")
    private String sender;

    private Region region = Region.US_EAST_2;

    public void sendBookingDetails(Booking booking) {
        String subject = "Utopia Airlines Booking Details";
        String recipient = booking.getUser().getEmail();

        //Format list of passengers into a list of tickets with seat and flight information
        StringBuffer passengers = new StringBuffer();
        booking.getPassengers().stream().sorted(Comparator.comparing(
                (Passenger passenger) -> {
                    return passenger.getFlight().getDepartureTime();
                }))
                .forEach((passenger) -> {
                    passengers.append(
                            "<p>Seat " + passenger.getSeatNumber() + " (" + passenger.getSeatClass() + "): "
                                + passenger.getGivenName() + " " + passenger.getFamilyName() + "</p>"
                                + "<p>Departing from: " + passenger.getFlight().getRoute().getOriginAirport().getCity()
                                + " [" + passenger.getFlight().getRoute().getOriginAirport().getAirportCode() + "]"
                                + " (" + passenger.getFlight().getDepartureTime().toString().replace('T', ' ') + ")</p>"
                                + "<p>Arriving at: " + passenger.getFlight().getRoute().getDestinationAirport().getCity()
                                + " [" + passenger.getFlight().getRoute().getDestinationAirport().getAirportCode() + "]"
                                + " (" + passenger.getFlight().getArrivalTime().toString().replace('T', ' ') + ")\n</p>"
                                + "--------------------------------------------------------------------\n"
                    );
        });


        String bodyHtml = "<html>"
                + "Your confirmation code: " + booking.getConfirmationCode() + "\n\n"
                + passengers
                + "</html>";

        String bodyText = "Your confirmation code: " + booking.getConfirmationCode() + "\n\n" + passengers;

        SesClient client = SesClient.builder().region(region).build();
        try {
            send(client, sender, recipient, subject, bodyHtml, bodyText);
            client.close();
            System.out.println("closed client........");
        }
        catch (MessagingException e){
            e.getStackTrace();
        }
    }

    private void send(SesClient client, String sender, String recipient,
                      String subject, String bodyHtml, String bodyText) throws MessagingException {

        Destination destination = Destination.builder().toAddresses(recipient).build();

        Content content = Content.builder().data(bodyHtml).build();

        Content sub = Content.builder().data(subject).build();

        Body body = Body.builder().html(content).build();

        Message message = Message.builder().subject(sub).body(body).build();

        SendEmailRequest request = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(sender)
                .build();

        try {
            System.out.println("Attempting to send email through SES");
            client.sendEmail(request);
        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }
}