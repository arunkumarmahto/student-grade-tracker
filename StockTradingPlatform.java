//Stock Trading Platform

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

class Stock implements Serializable {
    String symbol;
    String name;
    double price;

    Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    void updatePrice(double newPrice) { this.price = newPrice; }

    double getPrice() { return price; }

    @Override
    public String toString() {
        return symbol + " (" + name + ") - $" + String.format("%.2f", price);
    }
}

class Transaction implements Serializable {
    enum Type { BUY, SELL }

    Stock stock;
    int quantity;
    double price;
    Type type;
    LocalDateTime timestamp;

    Transaction(Stock stock, int qty, double price, Type type) {
        this.stock = stock;
        this.quantity = qty;
        this.price = price;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("%s %d shares of %s at $%.2f on %s",
                             type, quantity, stock.symbol, price, timestamp);
    }
}

class Portfolio implements Serializable {
    private Map<String, Integer> holdings = new HashMap<>();

    void addStock(Stock stock, int quantity) {
        holdings.put(stock.symbol, holdings.getOrDefault(stock.symbol, 0) + quantity);
    }

    boolean removeStock(Stock stock, int quantity) {
        int owned = holdings.getOrDefault(stock.symbol, 0);
        if (quantity > owned) return false;
        if (quantity == owned) holdings.remove(stock.symbol);
        else holdings.put(stock.symbol, owned - quantity);
        return true;
    }

    int getQuantity(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    Set<String> getSymbols() {
        return holdings.keySet();
    }

    double getValue(Market market) {
        double total = 0.0;
        for (String symbol : holdings.keySet()) {
            Stock stock = market.getStock(symbol);
            if (stock != null) {
                total += stock.getPrice() * holdings.get(symbol);
            }
        }
        return total;
    }

    void printPortfolio(Market market) {
        if (holdings.isEmpty()) {
            System.out.println("Portfolio is empty.");
            return;
        }
        System.out.println("Portfolio holdings:");
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            Stock stock = market.getStock(entry.getKey());
            if (stock != null) {
                System.out.printf("%s: %d shares @ $%.2f each (Total: $%.2f)%n",
                        stock.symbol, entry.getValue(), stock.getPrice(),
                        stock.getPrice() * entry.getValue());
            }
        }
        System.out.printf("Total portfolio value: $%.2f%n", getValue(market));
    }
}

class User implements Serializable {
    String username;
    double balance;
    Portfolio portfolio = new Portfolio();
    List<Transaction> transactions = new ArrayList<>();

    User(String username, double initialBalance) {
        this.username = username;
        this.balance = initialBalance;
    }

    boolean buyStock(Stock stock, int quantity) {
        double cost = stock.getPrice() * quantity;
        if (cost > balance) {
            System.out.println("Insufficient balance to buy.");
            return false;
        }
        balance -= cost;
        portfolio.addStock(stock, quantity);
        transactions.add(new Transaction(stock, quantity, stock.getPrice(), Transaction.Type.BUY));
        System.out.println("Bought " + quantity + " shares of " + stock.symbol);
        return true;
    }

    boolean sellStock(Stock stock, int quantity) {
        if (!portfolio.removeStock(stock, quantity)) {
            System.out.println("Not enough shares to sell.");
            return false;
        }
        double revenue = stock.getPrice() * quantity;
        balance += revenue;
        transactions.add(new Transaction(stock, quantity, stock.getPrice(), Transaction.Type.SELL));
        System.out.println("Sold " + quantity + " shares of " + stock.symbol);
        return true;
    }

    void printTransactions() {
        if (transactions.isEmpty()) System.out.println("No transactions yet.");
        else {
            System.out.println("Transaction History:");
            for (Transaction t : transactions) System.out.println(t);
        }
    }

    void printPortfolio(Market market) {
        portfolio.printPortfolio(market);
        System.out.printf("Available balance: $%.2f%n", balance);
    }
}

class Market {
    private Map<String, Stock> stocks = new HashMap<>();

    Market() {
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.00));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2800.00));
        stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 700.00));
        // Add more stocks as needed
    }

    Stock getStock(String symbol) {
        return stocks.get(symbol.toUpperCase());
    }

    void listStocks() {
        System.out.println("Market Stocks:");
        for (Stock s : stocks.values()) System.out.println(s);
    }

    // Simulate price update (optional)
    void fluctuatePrices() {
        Random rand = new Random();
        for (Stock s : stocks.values()) {
            double changePercent = (rand.nextDouble() - 0.5) * 0.1; // ±5%
            double newPrice = s.getPrice() * (1 + changePercent);
            s.updatePrice(Math.round(newPrice * 100.0) / 100.0);
        }
    }
}

class FileHandler {
    static void saveUser(User user) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(user.username + ".dat"))) {
            out.writeObject(user);
            System.out.println("Portfolio saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    static User loadUser(String username) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(username + ".dat"))) {
            User user = (User) in.readObject();
            System.out.println("Portfolio loaded successfully.");
            return user;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No saved portfolio found. Starting fresh.");
            return null;
        }
    }
}

public class StockTradingPlatform {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Market market = new Market();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        User user = FileHandler.loadUser(username);
        if (user == null) {
            user = new User(username, 10000.0); // Starting balance $10,000
        }

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. View market stocks");
            System.out.println("2. Buy stock");
            System.out.println("3. Sell stock");
            System.out.println("4. View portfolio");
            System.out.println("5. View transaction history");
            System.out.println("6. Save portfolio");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    market.listStocks();
                    break;
                case 2:
                    System.out.print("Enter stock symbol to buy: ");
                    String buySymbol = scanner.nextLine().toUpperCase();
                    Stock buyStock = market.getStock(buySymbol);
                    if (buyStock != null) {
                        System.out.print("Enter quantity: ");
                        int qty = scanner.nextInt();
                        scanner.nextLine();
                        user.buyStock(buyStock, qty);
                    } else {
                        System.out.println("Stock not found.");
                    }
                    break;
                case 3:
                    System.out.print("Enter stock symbol to sell: ");
                    String sellSymbol = scanner.nextLine().toUpperCase();
                    Stock sellStock = market.getStock(sellSymbol);
                    if (sellStock != null) {
                        System.out.print("Enter quantity: ");
                        int qty = scanner.nextInt();
                        scanner.nextLine();
                        user.sellStock(sellStock, qty);
                    } else {
                        System.out.println("Stock not found.");
                    }
                    break;
                case 4:
                    user.printPortfolio(market);
                    break;
                case 5:
                    user.printTransactions();
                    break;
                case 6:
                    FileHandler.saveUser(user);
                    break;
                case 7:
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
            // Optionally fluctuate prices over time
            // market.fluctuatePrices();
        }
        scanner.close();
    }
}
