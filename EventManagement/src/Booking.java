import java.sql.Timestamp;

public class Booking {
    private int id;
    private String eventName, customerName, email, status;
    private int seats;
    private double totalAmount;
    private Timestamp bookedOn;

    public Booking(int id, String eventName, String customerName, String email,
                   int seats, double totalAmount, Timestamp bookedOn, String status) {
        this.id = id; this.eventName = eventName; this.customerName = customerName;
        this.email = email; this.seats = seats; this.totalAmount = totalAmount;
        this.bookedOn = bookedOn; this.status = status;
    }

    public Object[] toRow() {
        return new Object[]{id, eventName, customerName, email, seats, totalAmount, bookedOn, status};
    }
}
