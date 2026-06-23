package Stores;

import java.sql.*;
import java.util.*;
import ColourCode.*;

public class Footwear extends Product {

    private String brand;
    private String size;

    LinkedList<Footwear> footwearList = new LinkedList<>();
    Scanner sc = new Scanner(System.in);
    //Empty Constructor for Temporary Object
    public Footwear() {}
    //Constructor For Setting details of a product
    public Footwear(int pid, String name, String brand, String size, double price, int vendor_id) {
        this.product_id = pid;
        this.product_Name = name;
        this.brand = brand;
        this.size = size;
        this.price = price;

    }

    //  display all products properly
    public void displayProduct(int stock) {
        System.out.println("***************");
        System.out.println("Product ID      : " + product_id);
        System.out.println("Description     : " + product_Name);
        System.out.println("Brand           : " + brand);
        System.out.println("Size            : " + size);
        System.out.println("Price (₹)       : " + price);
        System.out.println("Stock Left      : " + stock);
        System.out.println("***************");
    }
    //Viewing all products related to searched item
    public void viewProducts(String productName, int cus_id) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        String query = "SELECT * FROM footwear WHERE pname LIKE ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + productName + "%");
        ResultSet rs = ps.executeQuery();

        // store stock per product_id
        Map<Integer, Integer> productStockMap = new HashMap<>();

        while (rs.next()) {
            int pid = rs.getInt("pid");
            String name = rs.getString("pname");
            String brand = rs.getString("brand");
            String size = rs.getString("size");
            double price = rs.getDouble("price");
            stock = rs.getInt("stock");
            int vid = rs.getInt("vid");

            footwearList.add(new Footwear(pid, name, brand, size, price, vid));
            productStockMap.put(pid, stock);
        }

        System.out.println("Footwear List available number " + footwearList.size());
        if (footwearList.isEmpty()) {
            System.out.println("No products available");
        } else {
            int index = 0;
            int batchsize = 6;
            boolean stay=true;

            while (index < footwearList.size()||stay) {//Paging max 6 products in one page
                try {
                    int end = Math.min(index + batchsize, footwearList.size());
                    for (int i = index; i < end; i++) {
                        Footwear f = footwearList.get(i);
                        int stock = productStockMap.get(f.product_id);
                        f.displayProduct(stock);
                    }

                    System.out.println("Press 1 to go to next page");
                    System.out.println("Press 2 to go to home page");
                    System.out.println("Press 3 to add an item to cart");
                    String choice = sc.next();
                    switch (choice) {
                        //viewing next batch of products if available
                        case "1":
                            index += batchsize;
                            if (index >= footwearList.size()) {
                                System.out.println("No more products to display.");
                            }
                            break;
                        //Return to home page
                        case "2":
                            System.out.println("Returning to home page");
                            stay=false;
                            return;
                        //Adding an item to cart
                        case "3":
                            System.out.println("Enter product id to add to cart");
                            int product_id = sc.nextInt();

                            if (!productStockMap.containsKey(product_id)) {
                                System.out.println(ColourCodes.RED+"Invalid Product Id Please Try again"+ColourCodes.RESET);
                                break;
                            }

                            stock = productStockMap.get(product_id);
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
                            query = "INSERT INTO cart(cid, pid, p_type, quantity) VALUES (?, ?, ?, ?)";
                            PreparedStatement ps1 = con.prepareStatement(query);
                            ps1.setInt(1, cus_id);
                            ps1.setInt(2, product_id);
                            ps1.setString(3, "footwear");
                            ps1.setInt(4, quantity);
                            int n = ps1.executeUpdate();
                            if (n > 0) {
                                System.out.println(ColourCodes.GREEN+"Item Added To Cart Successfully.."+ColourCodes.RESET);
                                String query2 = "{CALL update_footwear(?,?)}";
                                CallableStatement callableStatement = con.prepareCall(query2);
                                callableStatement.setInt(1, stock - quantity);
                                callableStatement.setInt(2, product_id);
                                callableStatement.executeUpdate();
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