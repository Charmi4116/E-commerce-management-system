package users;
import Stores.*;
import  ColourCode.*;

import java.util.*;
import java.sql.*;
import java.io.*;

interface User
{
    void showDashboard() throws Exception;
    void login() throws Exception;
    void logout() throws Exception;
    void register() throws Exception;

}
interface PaymentMethod
{
    boolean makePayment(double amount) throws Exception;
}
public class Customer implements User
{

    int customerId;
    private String password ;
    public String custName;
    Clothing cloth;
    Electronics electronics ;
    Footwear footwear ;
    Jewellery jewellery ;

    private String customerEmail;
    String customerContact;
    String customerAddress;
    CardPayment cardPayment;
    double grandTotal;
    Scanner sc=new Scanner(System.in);
    //Viewing cart and placing order
    public void viewCart(int customerId) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        con.setAutoCommit(false);

        // Step 1: Get all cart items for the customer
        String query = "{call display_cart(?)}";
        CallableStatement ps = con.prepareCall(query);
        ps.setInt(1, customerId);
        ResultSet rs = ps.executeQuery();

        boolean hasItems = false;
        grandTotal = 0.0;

        while (rs.next()) {
            hasItems = true;
            int pid = rs.getInt("pid");
            String productType = rs.getString("p_type");
            int quantity = rs.getInt("quantity");

            String details = "SELECT c.pname, c.price " +
                    "FROM " + productType + " c " +
                    "INNER JOIN cart t ON c.pid = t.pid " +
                    "WHERE t.cid = ? AND t.pid = ?";
            PreparedStatement ps2 = con.prepareStatement(details);
            ps2.setInt(1, customerId);
            ps2.setInt(2, pid);

            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                String productName = rs2.getString("pname");
                double price = rs2.getDouble("price");
                double total = price * quantity;
                grandTotal += total;

                System.out.println("--------------------------------------------------");
                System.out.println("Product Type: " + productType);
                System.out.println("Product ID  : " + pid);
                System.out.println("Product Name: " + productName);
                System.out.println("Price       : ₹" + price);
                System.out.println("Quantity    : " + quantity);
                System.out.println("Total       : ₹" + total);
            } else {
                System.out.println("Product not found in table: " + productType + " for PID " + pid);
            }
        }

        if (!hasItems) {
            System.out.println("Your cart is empty.");
        } else {

            System.out.println("==================================================");
            System.out.println("GRAND TOTAL: ₹" + grandTotal);
            System.out.println("==================================================");
            while (true)
            {
                query = "{call display_cart(?)}";
                ps = con.prepareCall(query);
                ps.setInt(1, customerId);
                rs = ps.executeQuery();
                if(!rs.next())
                {
                    break;
                }
                System.out.println("press 1 to place order  ");
                System.out.println("press 2 to Empty cart ");
                System.out.println("press 3 to return to home page ");

                int choice = sc.nextInt();

                switch (choice) {
                    //Placing order after Payment completion
                    case 1:
                        System.out.println("What payment method would you like to use");
                        System.out.println("1.Card Payment");
                        System.out.println("2.COD");
                        int paymentMethod = sc.nextInt();

                        String paymentType = "";
                        boolean paymentSuccess = true;

                        if (paymentMethod == 1) {
                            paymentType = "Card";
                            cardPayment = new CardPayment();
                            try {
                                if (!cardPayment.makePayment(grandTotal)) {
                                    paymentSuccess = false;
                                }
                            } catch (InvalidCardException e) {
                                paymentSuccess = false;
                            }
                            if (!paymentSuccess) {
                                System.out.println("Order not placed due to invalid card.");
                                break;
                            }
                        } else if (paymentMethod == 2) {
                            paymentType = "COD";
                        } else {
                            System.out.println("Invalid payment option.");
                            break;
                        }

                        // Insert order into orders table (auto increment order_id)

                        String orderId = customerId + "_" + System.currentTimeMillis();

                        // For each product in the cart, insert into orders table
                        String cartQuery = "SELECT pid, quantity, p_type FROM cart WHERE cid = ?";
                        PreparedStatement cartPs = con.prepareStatement(cartQuery);
                        cartPs.setInt(1, customerId);
                        ResultSet cartRs = cartPs.executeQuery();

                        while (cartRs.next()) {
                            int productId = cartRs.getInt("pid");
                            int quantity = cartRs.getInt("quantity");
                            String ptype = cartRs.getString("p_type");

                            // Fetch price safely
                            String q = "SELECT price FROM " + ptype + " WHERE pid = ?";
                            PreparedStatement pricePs = con.prepareStatement(q);
                            pricePs.setInt(1, productId);
                            ResultSet rs1 = pricePs.executeQuery();

                            if (rs1.next()) {
                                double price = rs1.getDouble("price");

                                // Insert into orders
                                String insertOrder = "INSERT INTO orders (order_id, customer_id, product_id, amount, payment_method) VALUES (?, ?, ?, ?, ?)";
                                PreparedStatement insertPs = con.prepareStatement(insertOrder);
                                insertPs.setString(1, orderId);
                                insertPs.setInt(2, customerId);
                                insertPs.setInt(3, productId);
                                insertPs.setDouble(4, price * quantity);//for each product
                                insertPs.setString(5, paymentType);
                                insertPs.executeUpdate();
                            }


                        }



                        System.out.println("Order placed! Your Order ID: " + orderId);
                        con.commit();
                        while (true) {

                            System.out.println("Would you like to download receipt (yes/no)?");
                            String ch = sc.next();
                            if (ch.equalsIgnoreCase("yes")) {
                                System.out.println("Downloading receipt...");
                                Thread.sleep(1500);

                                String filename = "Receipt_" + orderId + ".txt";
                                BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
                                bw.write("* DIGITAL MALL RECEIPT *\n");
                                bw.write("Order ID: " + orderId + "\n");
                                bw.write("Customer ID: " + customerId + "\n");
                                bw.write("Date: " + java.time.LocalDate.now() + "\n\n");
                                bw.write("Total Amount: ₹" + grandTotal + "\n");
                                bw.write("Payment Method: " + paymentType + "\n");
                                if (paymentType.equals("COD")) {
                                    bw.write("Payment Status: Pending\n");
                                } else {
                                    bw.write("Payment Status: Paid\n");
                                }
                                bw.write("**\n");
                                bw.close();
                                System.out.println("Receipt saved as " + filename);
                                File file = new File(filename);
                                System.out.println("saved at " + file.getAbsolutePath());
                                break;

                            } else if (ch.equalsIgnoreCase("no")) {
                                break;
                            } else {
                                System.out.println("invalid choice");
                            }
                        }
                        // Empty the cart after placing order
                        PreparedStatement clearCart = con.prepareStatement("DELETE FROM cart WHERE cid = ?");
                        clearCart.setInt(1, customerId);
                        clearCart.executeUpdate();
                        con.commit();
                        break;
                    //Deleting Products From Cart
                    case 2:

                        PreparedStatement emptyPs = con.prepareStatement("DELETE FROM cart WHERE cid = ?");
                        emptyPs.setInt(1, customerId);
                        emptyPs.executeUpdate();
                        System.out.println("Cart emptied successfully!");
                        con.commit();
                        break;
                    //Return to dashboard
                    case 3:
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }
    }

    public void showDashboard() throws Exception {
        boolean b = true;
        while (b) {
            try {
                System.out.println("1. View Footwear ");
                System.out.println("2. View Electronics section  ");
                System.out.println("3. View Clothing section ");
                System.out.println("4. View Jewellery catalog");
                System.out.println("5. View Cart");
                System.out.println("6. Cancel an Order");
                System.out.println("7. Logout");
                int ch = sc.nextInt();
                sc.nextLine();

                switch (ch) {
                    //Shop Footwear Products
                    case 1:
                        System.out.print("What do you want to see(  Shoes/Sneakers/etc...:");
                        String cat = sc.nextLine();
                        footwear = new Footwear();
                        footwear.viewProducts(cat, this.customerId);
                        break;
                    //Shop Electronics
                    case 2:
                        System.out.print("What do you want to see  (TV /fridge/Ac..etc ):");
                        cat = sc.nextLine();
                        electronics = new Electronics();
                        electronics.viewProducts(cat, this.customerId);
                        break;
                    //Shop Cloths
                    case 3:
                        cloth = new Clothing();
                        System.out.print("Enter What you want to see(   T shirt /shirt etc): ");
                        String pName = sc.nextLine();
                        cloth.viewProducts(pName, this.customerId);
                        break;
                    //Shop Jewellery
                    case 4:
                        System.out.print("What do you want to see (Necklace/Ring..etc) :");
                        cat = sc.nextLine();
                        jewellery = new Jewellery();
                        jewellery.viewProducts(cat, this.customerId);
                        break;
                    //Displaying cart of logged in customer
                    case 5:
                        viewCart(customerId);
                        break;
                    //Cancelling order  of logged in customer
                    case 6:
                        System.out.print("Enter the Order ID you want to cancel: ");
                        int orderId = sc.nextInt();
                        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
                        String deleteOrder = "{Call cancel_order(?,?)}";
                        CallableStatement ps = con.prepareCall(deleteOrder);
                        ps.setInt(1, orderId);
                        ps.setInt(2, customerId);
                        int rows = ps.executeUpdate();
                        if (rows > 0) {
                            System.out.println("Order " + orderId + " has been cancelled successfully.");
                        } else {
                            System.out.println("Order not found or does not belong to you.");
                        }
                        break;
                    //logging out
                    case 7:
                        logout();
                        b = false;
                        break;

                    default:
                        System.out.println("Invalid option.");

                }
            }
            catch(InputMismatchException i)
            {
                System.out.println(ColourCodes.RED + "Exception Generated: "  + "Please Enter Valid input (1-7)"+ ColourCodes.RESET);
                sc.nextLine();
            }
            catch(SQLException i)
            {
                i.printStackTrace();
            }
        }
    }
    //For existing Customer login
    public void login() throws Exception {
        Scanner sc = new Scanner(System.in);
        int attempts = 0;
        boolean loggedIn = false;

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");

        while (attempts < 3 && !loggedIn) {
            System.out.print("Enter your contact number: ");
            String contact = sc.next();

            System.out.print("Enter your password: ");
            String password = sc.next();

            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM customer WHERE Contact = ? AND Password = ?");
            stmt.setString(1, contact);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("Customer_Id");
                customerContact = contact;
                customerEmail = rs.getString("Email");
                loggedIn = true;
                showDashboard();
            }
            else {
                System.out.println("Incorrect contact number or password");
                attempts++;
            }
        }
        if (!loggedIn) {
            System.out.println("Too many failed attempts. Try again later.");
            Thread.sleep(10000);
        }
    }
    //Logging Out
    public void logout() throws  Exception
    {
        System.out.println("logging out");
        Thread.sleep(500);
    }
    //For Registering new Customer
    public void register() throws Exception {
        Scanner sc = new Scanner(System.in);
        int attempts = 0;
        boolean isRegistered = false;

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");

        while (attempts < 3 && !isRegistered) {
            System.out.println("Quick Signup Attempt " + (attempts + 1));

            System.out.print("Enter Contact Number: ");
            customerContact = sc.next();
            if (customerContact.length() != 10) {
                System.out.println("Invalid contact number");
                attempts++;
                continue;
            }
            boolean isNumeric = true;
            for (char ch : customerContact.toCharArray()) {
                if (!Character.isDigit(ch)) {
                    isNumeric = false;
                    break;
                }
            }

            if (!isNumeric) {
                System.out.println("Invalid contact number");
                attempts++;
                continue;
            }
            String num = String.valueOf(customerContact.charAt(0));
            int firstdigit=Integer.parseInt(num);
            if(!(firstdigit>=6&&firstdigit<=9))
            {
                System.out.println("Invalid Contact Number");
                attempts++;
                continue;
            }
            System.out.print("Enter Email: ");
            customerEmail = sc.next().toLowerCase();
            if (customerEmail.isEmpty()) {
                System.out.println("Invalid email");
                attempts++;
                continue;
            }
            else if(!(customerEmail.endsWith("@gmail.com")))
            {
                System.out.println("invalid email");
                attempts++;
                continue;
            }
            try{
                PreparedStatement checkStmt = con.prepareStatement("SELECT Email FROM customer WHERE Email = ?");
                checkStmt.setString(1, customerEmail);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    attempts++;
                    throw new EmailAlreadyExistsException("Email already registered");


                }}
            catch(Exception e){
                System.out.println(e.getMessage());
                continue;
            }
            System.out.print("Enter Password: ");
            password = sc.next();
            boolean hasLetter = false, hasDigit = false;
            for (char ch : password.toCharArray()) {
                if (Character.isLetter(ch)) hasLetter = true;
                if (Character.isDigit(ch)) hasDigit = true;
            }
            if (!(hasLetter && hasDigit)) {
                System.out.println(ColourCodes.RED+"Password is weak must be alphanumeric( alphabets and numbers)"+ColourCodes.RESET);
                attempts++;
                continue;
            }
            System.out.println("Enter  Fullname");
            sc.nextLine();
            custName = sc.nextLine();
            if(custName.isEmpty())
            {
                System.out.println(ColourCodes.RED+"Invalid fullname"+ColourCodes.RESET);
                attempts++;
                continue;
            }
            boolean containsdig=false;
            for(char ch:custName.toCharArray()){
                if(Character.isDigit(ch)){

                    containsdig=true;
                }
            }
            if(containsdig)
            {
                System.out.println(ColourCodes.RED+"Invalid fullname"+ColourCodes.RESET);
                attempts++;
                continue;
            }
            System.out.println("Enter address");
            customerAddress = sc.nextLine();
            if(customerAddress.isEmpty()){
                System.out.println("Invalid address cannot be empty");
                attempts++;
                continue;
            }

            PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO customer (Contact, Email, Password,name,Address) VALUES (?, ?, ?,?,?)");
            insertStmt.setString(1, customerContact);
            insertStmt.setString(2, customerEmail);
            insertStmt.setString(3, password);
            insertStmt.setString(4, custName);
            insertStmt.setString(5, customerAddress);
            int result = insertStmt.executeUpdate();
            if (result > 0) {
                System.out.println("Quick registration successful");
                isRegistered = true;
            }
        }
        if (!isRegistered) {
            System.out.println(ColourCodes.RED + "Exception Generated: "  + "Maximum Attempts Reached Please try again after 10 seconds"+ ColourCodes.RESET);
            Thread.sleep(10000);
        }
    }
}
//Cusomised  Exception
class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String s) {
        super(s);
    }
}