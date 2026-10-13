import java.sql.Date;

public class Event {
    private int id;
    private String name, venue;
    private Date date;
    private double price;
    private int totalSeats, availableSeats;

    public Event(int id, String name, String venue, Date date, double price, int totalSeats, int availableSeats) {
        this.id = id; this.name = name; this.venue = venue; this.date = date;
        this.price = price; this.totalSeats = totalSeats; this.availableSeats = availableSeats;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getVenue() { return venue; }
    public Date getDate() { return date; }
    public double getPrice() { return price; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }

    @Override
    public String toString() { return name + " (" + date + ")"; } // shown in combo box
}
