package Stores;

import ColourCode.ColourCodes;

import java.sql.*;
import java.util.*;

public class Jewellery extends Product {

    private String brand;
    private String material;
    private String size;
    private String history;

    LinkedList<Jewellery> jewelleryList = new LinkedList<>();
    Scanner sc = new Scanner(System.in);
    //Empty constructor for temporary usage
    public Jewellery() {}
    //for setting details
    public Jewellery(int pid, String name, String brand, String material, double price, String size, int vendor_id, String history) {
        this.product_id = pid;
        this.product_Name = name;
        this.brand = brand;
        this.material = material;
        this.price = price;
        this.size = size;
        this.history = history;
    }

    public void displayProduct(int stock) {//Displaying jewellery info in detail
        System.out.println("***************");
        System.out.println("Product ID      : " + product_id);
        System.out.println("Description     : " + product_Name);
        System.out.println("Brand           : " + brand);
        System.out.println("Material        : " + material);
        System.out.println("Size            : " + size);
        System.out.println("Price (₹)       : " + price);
        System.out.println("Stock Left      : " + stock);
        System.out.println("Heritage        : " + history);
        System.out.println("***************");
    }
    //Viewing all products related to searched item
    public void viewProducts(String productName, int cus_id) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        String query = "SELECT * FROM jewellery WHERE pname LIKE ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + productName + "%");
        ResultSet rs = ps.executeQuery();

        // Store product stock
        Map<Integer, Integer> productStockMap = new HashMap<>();

        while (rs.next()) {

            int pid = rs.getInt("pid");
            String name = rs.getString("pname");
            String brand = rs.getString("brand");
            String material = rs.getString("material");
            double price = rs.getDouble("price");
            String size = rs.getString("size");
            stock = rs.getInt("stock");
            int vid = rs.getInt("vid");
            String history = rs.getString("history");

            jewelleryList.add(new Jewellery(pid, name, brand, material, price, size, vid, history));
            productStockMap.put(pid, stock);
        }

        System.out.println("Jewellery List available number " + jewelleryList.size());
        if (jewelleryList.isEmpty()) {
            System.out.println("No products available");
        } else {
            int index = 0;
            int batchsize = 6;
            boolean stay=true;

            while (index < jewelleryList.size()||stay) {
                try {

                    int end = Math.min(index + batchsize, jewelleryList.size());
                    for (int i = index; i < end; i++) {
                        Jewellery j = jewelleryList.get(i);
                        int stock = productStockMap.get(j.product_id);
                        j.displayProduct(stock);
                    }

                    System.out.println("Press 1 to go to next page");
                    System.out.println("Press 2 to go to home page");
                    System.out.println("Press 3 to add an item to cart");
                    String choice = sc.next();
                    switch (choice) {
                        //going to next batch pof products if available
                        case "1":
                            index += batchsize;
                            if (index >= jewelleryList.size()) {
                                System.out.println("No more products to display.");
                            }
                            break;
                        //Returning to home page
                        case "2":
                            System.out.println("Returning to home page");
                            stay=false;
                            return;
                        //Adding item into cart
                        case "3":
                            System.out.println("Enter product id to add to cart");
                            int product_id = sc.nextInt();

                            // Check if product exists
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
                            ps1.setString(3, "jewellery");
                            ps1.setInt(4, quantity);
                            int n = ps1.executeUpdate();
                            if (n > 0) {
                                System.out.println(ColourCodes.GREEN+"Item Added To Cart Successfully.."+ColourCodes.RESET);
                                String query2 = "UPDATE jewellery SET stock=? WHERE pid=?";
                                PreparedStatement preparedStatement = con.prepareStatement(query2);
                                preparedStatement.setInt(1, stock - quantity);
                                preparedStatement.setInt(2, product_id);
                                preparedStatement.executeUpdate();
                            }
                            break;

                        default:
                            System.out.println(ColourCodes.RED+"Invalid choice"+ColourCodes.RESET);
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