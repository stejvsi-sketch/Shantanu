package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import java.io.Serializable;

public class GuestBooking implements Serializable {
    private static final long serialVersionUID = 1L;

    private Customer customer;
    private Booking booking;

    public Customer getCustomer() {
        Customer c=this.customer;
        return c;
    }

    public void setCustomer(Customer customer) {
        Customer c=customer;
        this.customer = c;
    }

    public Booking getBooking() {
        Booking b=this.booking;
        return b;
    }

    public void setBooking(Booking booking) {
        Booking b=booking;
        this.booking = b;
    }
}
