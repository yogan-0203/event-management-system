import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public void addEvent(String name, String venue, Date date, double price, int seats) throws SQLException {
        String sql = "INSERT INTO events (name, venue, event_date, price, total_seats, available_seats) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, venue);
            ps.setDate(3, date);
            ps.setDouble(4, price);
            ps.setInt(5, seats);
            ps.setInt(6, seats);
            ps.executeUpdate();
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Event(rs.getInt("id"), rs.getString("name"), rs.getString("venue"),
                        rs.getDate("event_date"), rs.getDouble("price"),
                        rs.getInt("total_seats"), rs.getInt("available_seats")));
            }
        }
        return list;
    }

    public void deleteEvent(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
