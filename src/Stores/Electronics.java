package Stores;
import ColourCode.ColourCodes;

import java.sql.*;
import java.util.*;

public class Electronics extends Product {

    private String brand;
    private String specs;
    private String warrantyPeriod;

    LinkedList<Electronics> electronicsList = new LinkedList<>();
    public int vendor_id;
    Scanner sc = new Scanner(System.in);

    //Empty Constructor for Temporary object
    public Electronics() {}

    //with parameter constructor
    public Electronics(String brand, String specs, String warrantyPeriod, double price, int pid, String pname) { //For setting details
        this.product_id = pid;
        this.brand = brand;
        this.specs = specs;
        this.warrantyPeriod = warrantyPeriod;
        this.price = price;
        this.product_Name = pname;
    }

    //viewing all product details neatly formatted
    public void displayProduct(int stock) {
        System.out.println("***************");
        System.out.println("Product ID      : " + product_id);
        System.out.println("Name            : " + product_Name);
        System.out.println("Brand           : " + brand);
        System.out.println("Specifications  : " + specs);
        System.out.println("Warranty Period : " + warrantyPeriod);
        System.out.println("Price (₹)       : " + price);
        System.out.println("Stock Left      : " + stock);
        System.out.println("***************");
    }
    //Viewing all products related to searched item
    public void viewProducts(String productName, int cus_id) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        String query = "SELECT * FROM electronics WHERE pname LIKE ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + productName + "%");
        ResultSet rs = ps.executeQuery();

        // Store valid product IDs and stock
        Hashtable<Integer, Integer> productStockMap = new Hashtable<>();

        while (rs.next()) {
            product_id = rs.getInt("pid");
            brand = rs.getString("brand");
            specs = rs.getString("specs");
            warrantyPeriod = rs.getString("warranty_period");
            price = rs.getDouble("price");
            vendor_id = rs.getInt("vid");
            String pname = rs.getString("pname");
            stock = rs.getInt("stock");

            electronicsList.add(new Electronics(brand, specs, warrantyPeriod, price, product_id, pname));
            productStockMap.put(product_id, stock);
        }

        System.out.println("Electronics List available number " + electronicsList.size());
        if (electronicsList.isEmpty()) {
            System.out.println("No products available");
        } else {
            int index = 0;
            int batchsize = 6;
            boolean stay=true;

            while (index < electronicsList.size()||stay) {
                try {
                    int end = Math.min(index + batchsize, electronicsList.size());
                    for (int i = index; i < end; i++) {
                        Electronics e = electronicsList.get(i);
                        int stock = productStockMap.get(e.product_id);
                        e.displayProduct(stock); //  neat display
                    }

                    System.out.println("Press 1 to go to next page");
                    System.out.println("Press 2 to go to home page");
                    System.out.println("Press 3 to add an item to cart");
                    String choice = sc.next();
                    switch (choice) {
                        //go to next page
                        case "1":
                            index += batchsize;
                            if (index >= electronicsList.size()) {
                                System.out.println("No more products to display.");
                            }
                            break;
                        //  Returning to home page
                        case "2":
                            System.out.println("Returning to home page");
                            stay=false;
                            return;
                        // Adding items  to cart
                        case "3":
                            System.out.println("Enter product id to add to cart");
                            int product_id = sc.nextInt();

                            // Check if product ID exists
                            if (!productStockMap.containsKey(product_id)) {
                                System.out.println(ColourCodes.RED+"Invalid Product Id Please Try again"+ColourCodes.RESET);
                                break;
                            }

                            int stock = productStockMap.get(product_id);
                            System.out.println("Enter quantity to add to cart ");
                            int quantity = sc.nextInt();

                            if (quantity > stock || quantity <= 0) {
                                if (quantity <= 0) {
                                    System.out.println(ColourCodes.RED+"such amount cannot be added"+ColourCodes.RESET);
                                    break;
                                }
                                System.out.println(ColourCodes.RED+"Uh-oh, that's more than the stock available right now (" + stock + ")."+ColourCodes.RESET);
                                break;
                            }

                            // Insert into cart
                            query = "{call insert_cart(?,?,?,?)}";
                            CallableStatement ps1 = con.prepareCall(query);
                            ps1.setInt(1, cus_id);
                            ps1.setInt(2, product_id);
                            ps1.setString(3, "electronics");
                            ps1.setInt(4, quantity);
                            int n = ps1.executeUpdate();
                            if (n > 0) {
                                System.out.println(ColourCodes.GREEN+"Item Added To Cart Successfully.."+ColourCodes.RESET);
                                String query2 = "UPDATE electronics SET stock=? WHERE pid=?";
                                PreparedStatement preparedStatement = con.prepareStatement(query2);
                                preparedStatement.setInt(1, stock - quantity);
                                preparedStatement.setInt(2, product_id);
                                preparedStatement.executeUpdate();
                            }
                            break;

                        default:
                            System.out.println( ColourCodes.RED+"Invalid choice"+ColourCodes.RESET);
                            break;
                    }
                }catch(InputMismatchException i)
                {
                    System.out.println(ColourCodes.RED + "Exception Generated: " + ColourCodes.RESET + "Please Enter Valid input");
                    sc.nextLine();
                }
            }
        }
    }
}