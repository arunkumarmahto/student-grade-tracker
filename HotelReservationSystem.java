import java.io.*;
import java.time.LocalDate;
import java.util.*;

enum RoomType { STANDARD, DELUXE, SUITE }

class Room implements Serializable {
    int roomNumber;
    RoomType type;
    double pricePerNight;
    boolean isAvailable;

    Room(int roomNumber, RoomType type, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    boolean book() {
        if (isAvailable) {
            isAvailable = false;
            return true;
        }
        return false;
    }

    void cancel() {
        isAvailable = true;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " [" + type + "] - $" + pricePerNight + " per night - " +
               (isAvailable ? "Available" : "Booked");
    }
}

class Customer implements Serializable {
    String name;
    String email;
    String phone;

    Customer(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}

class Reservation implements Serializable {
    Customer customer;
    Room room;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    boolean paymentDone;

    Reservation(Customer customer, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.paymentDone = false;
    }

    void cancel() {
        room.cancel();
        paymentDone = false;
    }

    double getTotalCost() {
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        return nights * room.pricePerNight;
    }

    @Override
    public String toString() {
        return "Reservation for " + customer.name + " in " + room +
               "\nCheck-in: " + checkInDate + ", Check-out: " + checkOutDate +
               "\nPayment Status: " + (paymentDone ? "Paid" : "Pending") +
               "\nTotal Cost: $" + getTotalCost();
    }
}

class Hotel implements Serializable {
    List<Room> rooms = new ArrayList<>();
    List<Reservation> reservations = new ArrayList<>();

    void addRoom(Room room) {
        rooms.add(room);
    }

    List<Room> searchAvailableRooms(RoomType type) {
        List<Room> availableRooms = new ArrayList<>();
        for (Room r : rooms) {
            if (r.type == type && r.isAvailable) {
                availableRooms.add(r);
            }
        }
        return availableRooms;
    }

    Reservation makeReservation(Customer customer, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (room.book()) {
            Reservation reservation = new Reservation(customer, room, checkIn, checkOut);
            reservations.add(reservation);
            return reservation;
        }
        return null;
    }

    boolean cancelReservation(Reservation reservation) {
        if (reservations.remove(reservation)) {
            reservation.cancel();
            return true;
        }
        return false;
    }
}

// Simple Payment simulation
class Payment {
    static boolean processPayment(double amount) {
        System.out.println("Processing payment of $" + amount);
        // Simulate always successful
        System.out.println("Payment successful.");
        return true;
    }
}

// File IO for persistence
class FileHandler {
    static void saveHotelData(Hotel hotel, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(hotel);
        }
    }

    static Hotel loadHotelData(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Hotel) in.readObject();
        }
    }
}

// Main system controller and UI
public class HotelReservationSystem {
    private static Scanner scanner = new Scanner(System.in);
    private Hotel hotel;
    private String dataFile = "hotelData.ser";

    public static void main(String[] args) {
        new HotelReservationSystem().start();
    }

    void start() {
        try {
            hotel = FileHandler.loadHotelData(dataFile);
            System.out.println("Loaded existing hotel data.");
        } catch (Exception e) {
            System.out.println("Starting with new hotel data.");
            hotel = new Hotel();
            // Add sample rooms
            hotel.addRoom(new Room(101, RoomType.STANDARD, 75));
            hotel.addRoom(new Room(102, RoomType.DELUXE, 125));
            hotel.addRoom(new Room(103, RoomType.SUITE, 250));
        }
        while (true) {
            System.out.println("\n--- Hotel Reservation Menu ---");
            System.out.println("1. Search Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View All Reservations");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());
            try {
                switch (choice) {
                    case 1 -> searchRooms();
                    case 2 -> bookRoom();
                    case 3 -> cancelReservation();
                    case 4 -> viewReservations();
                    case 5 -> {
                        FileHandler.saveHotelData(hotel, dataFile);
                        System.out.println("Data saved. Exiting.");
                        return;
                    }
                    default -> System.out.println("Invalid choice, try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: "+e.getMessage());
            }
        }
    }

    void searchRooms() {
        System.out.print("Enter room type to search (STANDARD, DELUXE, SUITE): ");
        RoomType type = RoomType.valueOf(scanner.nextLine().toUpperCase());
        List<Room> available = hotel.searchAvailableRooms(type);
        if (available.isEmpty()) System.out.println("No available rooms found.");
        else {
            System.out.println("Available Rooms:");
            for (Room r : available) System.out.println(r);
        }
    }

    void bookRoom() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your phone: ");
        String phone = scanner.nextLine();
        Customer customer = new Customer(name, email, phone);

        System.out.print("Enter room type to book (STANDARD, DELUXE, SUITE): ");
        RoomType type = RoomType.valueOf(scanner.nextLine().toUpperCase());
        List<Room> available = hotel.searchAvailableRooms(type);
        if (available.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }
        System.out.println("Select room by number: ");
        for (Room r : available) System.out.println(r);
        int roomNum = Integer.parseInt(scanner.nextLine());
        Room selectedRoom = null;
        for (Room r : available) {
            if (r.roomNumber == roomNum) {
                selectedRoom = r;
                break;
            }
        }
        if (selectedRoom == null) {
            System.out.println("Invalid room selection.");
            return;
        }

        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        LocalDate checkIn = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter check-out date (YYYY-MM-DD): ");
        LocalDate checkOut = LocalDate.parse(scanner.nextLine());

        Reservation reservation = hotel.makeReservation(customer, selectedRoom, checkIn, checkOut);
        if (reservation == null) {
            System.out.println("Room booking failed.");
            return;
        }
        // Process payment
        double amount = reservation.getTotalCost();
        boolean paid = Payment.processPayment(amount);
        reservation.paymentDone = paid;

        if(paid)
            System.out.println("Booking confirmed:\n" + reservation);
        else
            System.out.println("Payment failed. Booking cancelled.");
    }

    void cancelReservation() {
        System.out.print("Enter your booking room number to cancel: ");
        int roomNum = Integer.parseInt(scanner.nextLine());
        Reservation toCancel = null;
        for (Reservation res : hotel.reservations) {
            if (res.room.roomNumber == roomNum) {
                toCancel = res;
                break;
            }
        }
        if (toCancel != null) {
            hotel.cancelReservation(toCancel);
            System.out.println("Reservation cancelled.");
        } else {
            System.out.println("No matching reservation found.");
        }
    }

    void viewReservations() {
        if(hotel.reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        System.out.println("All Reservations:");
        for (Reservation r : hotel.reservations) {
            System.out.println(r);
            System.out.println("-------------------------");
        }
    }
}
