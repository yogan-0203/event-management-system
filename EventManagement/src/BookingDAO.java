import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /** Books seats inside one transaction so seat count and booking row never go out of sync. */
    public void bookEvent(int eventId, String customer, String email, int seats) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int available;
                double price;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT available_seats, price FROM events WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Event not found.");
                        available = rs.getInt("available_seats");
                        price = rs.getDouble("price");
                    }
                }
                if (seats > available)
                    throw new SQLException("Only " + available + " seat(s) left for this event.");

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE events SET available_seats = available_seats - ? WHERE id = ?")) {
                    ps.setInt(1, seats);
                    ps.setInt(2, eventId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO bookings (event_id, customer_name, email, seats, total_amount) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, eventId);
                    ps.setString(2, customer);
                    ps.setString(3, email);
                    ps.setInt(4, seats);
                    ps.setDouble(5, price * seats);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cancels a booking and gives the seats back to the event. */
    public void cancelBooking(int bookingId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int eventId, seats;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT event_id, seats, status FROM bookings WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Booking not found.");
                        if (!"BOOKED".equals(rs.getString("status")))
                            throw new SQLException("Booking is already cancelled.");
                        eventId = rs.getInt("event_id");
                        seats = rs.getInt("seats");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE events SET available_seats = available_seats + ? WHERE id = ?")) {
                    ps.setInt(1, seats);
                    ps.setInt(2, eventId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.id, e.name, b.customer_name, b.email, b.seats, b.total_amount, b.booked_on, b.status "
                   + "FROM bookings b JOIN events e ON b.event_id = e.id ORDER BY b.booked_on DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Booking(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getInt(5), rs.getDouble(6), rs.getTimestamp(7), rs.getString(8)));
            }
        }
        return list;
    }
}
