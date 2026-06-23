package Stores;
import ColourCode.ColourCodes;

import java.sql.*;
import java.util.*;

public class Clothing extends Product{

    private String brand;
    private String colour;
    private int size;

    LinkedList<Clothing> clothingList = new LinkedList<>();
    public int vendor_id;
    Scanner sc = new Scanner(System.in);

    //Empty Constructor for temporary object
    public Clothing() {}

    //with parameter constructor
    public Clothing(String brand, String colour, int size, double price, int pid, String pname) {//For setting product
        this.product_id = pid;
        this.brand = brand;
        this.colour = colour;
        this.size = size;
        this.price = price;
        this.product_Name = pname;
    }

    //  display method (neatly formatted product info)
    public void displayProduct(int stock) {
        System.out.println("***************");
        System.out.println("Product ID   : " + product_id);
        System.out.println("Name         : " + product_Name);
        System.out.println("Brand        : " + brand);
        System.out.println("Colour       : " + colour);
        System.out.println("Size         : " + size);
        System.out.println("Price (₹)    : " + price);
        System.out.println("Stock Left   : " + stock);
        System.out.println("***************");
    }
    //method for viewing products
    public void viewProducts(String productName,  int cus_id) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        String query = "SELECT * FROM clothing WHERE pname LIKE ? ";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + productName + "%");

        ResultSet rs = ps.executeQuery();

        // Store valid product IDs and stock
        Map<Integer, Integer> productStockMap = new HashMap<>();

        while (rs.next()) {
            product_id = rs.getInt("pid");
            brand = rs.getString("brand");
            size = rs.getInt("size");
            price = rs.getDouble("price");
            colour = rs.getString("color");
            vendor_id = rs.getInt("vid");
            String pname = rs.getString("pname");
            stock = rs.getInt("stock");

            clothingList.add(new Clothing(brand, colour, size, price, product_id, pname));
            productStockMap.put(product_id, stock);
        }

        System.out.println("Clothing List available number " + clothingList.size());
        if (clothingList.isEmpty()) {
            System.out.println("No products available");
        } else {
            int index = 0;
            int batchsize = 6;
            boolean stay=true;

            while (index < clothingList.size()||stay) {
                try {
                    int end = Math.min(index + batchsize, clothingList.size());
                    for (int i = index; i < end; i++) {
                        Clothing c = clothingList.get(i);
                        stock = productStockMap.get(c.product_id);
                        c.displayProduct(stock); //  display
                    }

                    System.out.println("Press 1 to go to next page");
                    System.out.println("Press 2 to go to home page");
                    System.out.println("Press 3 to add an item to cart");
                    String choice = sc.next();
                    switch (choice) {
                        // for going to next page
                        case "1":
                            index += batchsize;
                            if (index >= clothingList.size()) {
                                System.out.println("No more products to display.");
                            }
                            break;
                        //going to home page
                        case "2":
                            System.out.println("Returning to home page");
                            stay=false;
                            return;
                        //add items to cart
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
                            query = "INSERT INTO cart(cid, pid, p_type, quantity) VALUES (?, ?, ?, ?)";
                            PreparedStatement ps1 = con.prepareStatement(query);
                            ps1.setInt(1, cus_id);
                            ps1.setInt(2, product_id);
                            ps1.setString(3, "clothing");
                            ps1.setInt(4, quantity);
                            int n = ps1.executeUpdate();
                            if (n > 0) {
                                System.out.println(ColourCodes.GREEN+"Item Added To Cart Successfully.."+ColourCodes.RESET);
                                String query2 = "{call update_clothing(?,?)}";
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