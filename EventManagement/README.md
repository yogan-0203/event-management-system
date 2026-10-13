# Event Management System (Java Swing + JDBC + MySQL)

## Structure
- `schema.sql` – database + tables + sample events
- `src/DBConnection.java` – JDBC connection (edit user/password here)
- `src/Event.java`, `src/Booking.java` – model classes
- `src/EventDAO.java`, `src/BookingDAO.java` – all JDBC code (PreparedStatement, transactions)
- `src/MainFrame.java` – Swing GUI (Events / Book Tickets / Bookings tabs)

## Run
1. Run `schema.sql` in MySQL.
2. Download MySQL Connector/J (e.g. `mysql-connector-j-8.x.jar`) and put it in this folder.
3. Set your password in `DBConnection.java`.
4. Compile and run (Windows: use `;` instead of `:` in the classpath):

   javac -cp ".:mysql-connector-j-8.x.jar" -d out src/*.java
   java  -cp "out:mysql-connector-j-8.x.jar" MainFrame

In an IDE (IntelliJ/Eclipse/NetBeans): add the connector jar to the project libraries and run `MainFrame`.
