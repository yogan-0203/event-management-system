import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MainFrame extends JFrame {

    private final EventDAO eventDAO = new EventDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private DefaultTableModel eventModel, bookingModel;
    private JTable eventTable, bookingTable;
    private JComboBox<Event> eventCombo;
    private JLabel infoLabel;

    public MainFrame() {
        super("Event Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Events", buildEventsTab());
        tabs.addTab("Book Tickets", buildBookingTab());
        tabs.addTab("Bookings", buildBookingsListTab());
        add(tabs);

        refreshAll();
    }

    // ---------- Tab 1: Events ----------
    private JPanel buildEventsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        eventModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Venue", "Date", "Price", "Total Seats", "Available"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(eventModel);
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);

        JTextField name = new JTextField(10), venue = new JTextField(10), date = new JTextField(8),
                   price = new JTextField(5), seats = new JTextField(4);
        JButton add = new JButton("Add Event");
        JButton delete = new JButton("Delete Selected");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Add / Remove Event"));
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel("Venue")); form.add(venue);
        form.add(new JLabel("Date (yyyy-mm-dd)")); form.add(date);
        form.add(new JLabel("Price")); form.add(price);
        form.add(new JLabel("Seats")); form.add(seats);
        form.add(add); form.add(delete);
        panel.add(form, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            try {
                if (name.getText().isBlank() || venue.getText().isBlank())
                    throw new IllegalArgumentException("Name and venue are required.");
                eventDAO.addEvent(name.getText().trim(), venue.getText().trim(),
                        Date.valueOf(date.getText().trim()),
                        Double.parseDouble(price.getText().trim()),
                        Integer.parseInt(seats.getText().trim()));
                name.setText(""); venue.setText(""); date.setText(""); price.setText(""); seats.setText("");
                refreshAll();
            } catch (NumberFormatException ex) {
                error("Price and seats must be numbers.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage().contains("required") ? ex.getMessage() : "Use the date format yyyy-mm-dd.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            int row = eventTable.getSelectedRow();
            if (row < 0) { error("Select an event first."); return; }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete this event and all its bookings?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                eventDAO.deleteEvent((int) eventModel.getValueAt(row, 0));
                refreshAll();
            } catch (Exception ex) { error(ex.getMessage()); }
        });

        return panel;
    }

    // ---------- Tab 2: Book tickets ----------
    private JPanel buildBookingTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Book Tickets"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        eventCombo = new JComboBox<>();
        JTextField customer = new JTextField(20), email = new JTextField(20);
        JSpinner seats = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        infoLabel = new JLabel(" ");
        JButton book = new JButton("Book Now");

        addRow(panel, g, 0, "Event", eventCombo);
        addRow(panel, g, 1, "Your Name", customer);
        addRow(panel, g, 2, "Email", email);
        addRow(panel, g, 3, "Seats", seats);
        g.gridx = 1; g.gridy = 4; panel.add(infoLabel, g);
        g.gridx = 1; g.gridy = 5; panel.add(book, g);

        Runnable updateInfo = () -> {
            Event ev = (Event) eventCombo.getSelectedItem();
            if (ev == null) { infoLabel.setText(" "); return; }
            int n = (int) seats.getValue();
            infoLabel.setText(String.format("Venue: %s | Available: %d | Total: Rs. %.2f",
                    ev.getVenue(), ev.getAvailableSeats(), ev.getPrice() * n));
        };
        eventCombo.addActionListener(e -> updateInfo.run());
        seats.addChangeListener(e -> updateInfo.run());

        book.addActionListener(e -> {
            Event ev = (Event) eventCombo.getSelectedItem();
            if (ev == null) { error("No event available."); return; }
            String n = customer.getText().trim(), m = email.getText().trim();
            if (n.isEmpty() || !m.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
                error("Enter a valid name and email."); return;
            }
            try {
                bookingDAO.bookEvent(ev.getId(), n, m, (int) seats.getValue());
                JOptionPane.showMessageDialog(this, "Booking confirmed!");
                customer.setText(""); email.setText(""); seats.setValue(1);
                refreshAll();
            } catch (Exception ex) { error(ex.getMessage()); }
        });

        return panel;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; p.add(new JLabel(label), g);
        g.gridx = 1; p.add(field, g);
    }

    // ---------- Tab 3: Bookings ----------
    private JPanel buildBookingsListTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        bookingModel = new DefaultTableModel(
                new String[]{"ID", "Event", "Customer", "Email", "Seats", "Amount", "Booked On", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel Selected Booking");
        JButton refresh = new JButton("Refresh");
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(cancel); bar.add(refresh);
        panel.add(bar, BorderLayout.SOUTH);

        cancel.addActionListener(e -> {
            int row = bookingTable.getSelectedRow();
            if (row < 0) { error("Select a booking first."); return; }
            try {
                bookingDAO.cancelBooking((int) bookingModel.getValueAt(row, 0));
                refreshAll();
            } catch (Exception ex) { error(ex.getMessage()); }
        });
        refresh.addActionListener(e -> refreshAll());

        return panel;
    }

    // ---------- Helpers ----------
    private void refreshAll() {
        try {
            List<Event> events = eventDAO.getAllEvents();
            eventModel.setRowCount(0);
            eventCombo.removeAllItems();
            for (Event ev : events) {
                eventModel.addRow(new Object[]{ev.getId(), ev.getName(), ev.getVenue(), ev.getDate(),
                        ev.getPrice(), ev.getTotalSeats(), ev.getAvailableSeats()});
                eventCombo.addItem(ev);
            }
            bookingModel.setRowCount(0);
            for (Booking b : bookingDAO.getAllBookings()) bookingModel.addRow(b.toRow());
        } catch (Exception ex) {
            error("Database error: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
