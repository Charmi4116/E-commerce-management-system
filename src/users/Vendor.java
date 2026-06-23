package users;

import ColourCode.ColourCodes;

import java.util.*;
import java.sql.*;

public class Vendor implements User {
    int vid;
    String shopName;
    String category;
    String vendorEmail;

    public void showDashboard() throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        Scanner sc = new Scanner(System.in);
        boolean b = true;

        while (b) {
            try {

                System.out.println("\n--- Vendor Dashboard ---");
                System.out.println("1. Add product");
                System.out.println("2. Remove product");
                System.out.println("3. View all products being sold by you");
                System.out.println("4. Update stock of a product");
                System.out.println("5. Log out");
                int ch = sc.nextInt();
                sc.nextLine();

                switch (ch) {
                    // Add Product
                    case 1:
                        System.out.println("Enter product name/description:");
                        String pname = sc.nextLine();

                        System.out.println("Enter price:");
                        double price = sc.nextDouble();
                        if (price < 0)
                        {
                            throw new InputMismatchException();
                        }

                        System.out.println("Enter stock:");
                        int stock = sc.nextInt();
                        if (stock < 0) stock = 0;
                        sc.nextLine();

                        if (category.equalsIgnoreCase("clothing")) {
                            System.out.println("Enter brand:");
                            String brand = sc.nextLine();
                            System.out.println("Enter color:");
                            String color = sc.nextLine();
                            System.out.println("Enter size (number):");
                            int size = sc.nextInt();
                            sc.nextLine();
                            System.out.println("Enter material:");
                            String material = sc.nextLine();

                            String sql = "INSERT INTO clothing (vid, pname, brand, color, size, price, stock, material) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement pst = con.prepareStatement(sql);
                            pst.setInt(1, vid);
                            pst.setString(2, pname);
                            pst.setString(3, brand);
                            pst.setString(4, color);
                            pst.setInt(5, size);
                            pst.setDouble(6, price);
                            pst.setInt(7, stock);
                            pst.setString(8, material);
                            pst.executeUpdate();
                            System.out.println("Clothing product added.");
                        } else if (category.equalsIgnoreCase("electronics")) {
                            System.out.println("Enter brand:");
                            String brand = sc.nextLine();
                            System.out.println("Enter specs:");
                            String specs = sc.nextLine();
                            System.out.println("Enter warranty period:");
                            String warranty = sc.nextLine();

                            String sql = "INSERT INTO electronics (vid, pname, brand, specs, warranty_period, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement pst = con.prepareStatement(sql);
                            pst.setInt(1, vid);
                            pst.setString(2, pname);
                            pst.setString(3, brand);
                            pst.setString(4, specs);
                            pst.setString(5, warranty);
                            pst.setDouble(6, price);
                            pst.setInt(7, stock);
                            pst.executeUpdate();
                            System.out.println("Electronics product added.");
                        } else if (category.equalsIgnoreCase("footwear")) {
                            System.out.println("Enter brand:");
                            String brand = sc.nextLine();
                            System.out.println("Enter size (number):");
                            int fsize = sc.nextInt();
                            sc.nextLine();

                            String sql = "INSERT INTO footwear (vid, pname, brand, size, price, stock) VALUES (?, ?, ?, ?, ?, ?)";
                            PreparedStatement pst = con.prepareStatement(sql);
                            pst.setInt(1, vid);
                            pst.setString(2, pname);
                            pst.setString(3, brand);
                            pst.setInt(4, fsize);
                            pst.setDouble(5, price);
                            pst.setInt(6, stock);
                            pst.executeUpdate();
                            System.out.println("Footwear product added.");
                        } else if (category.equalsIgnoreCase("jewellery")) {
                            System.out.println("Enter brand:");
                            String brand = sc.nextLine();
                            System.out.println("Enter material (e.g., Gold, Silver):");
                            String material = sc.nextLine();
                            System.out.println("Enter size (e.g., ring size / length):");
                            String jsize = sc.nextLine();
                            System.out.println("Enter history (dynasty/era or leave blank):");
                            String history = sc.nextLine();

                            String sql = "INSERT INTO jewellery (vid, pname, brand, material, price, size, stock, history) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement pst = con.prepareStatement(sql);
                            pst.setInt(1, vid);
                            pst.setString(2, pname);
                            pst.setString(3, brand);
                            pst.setString(4, material);
                            pst.setDouble(5, price);
                            pst.setString(6, jsize);
                            pst.setInt(7, stock);
                            pst.setString(8, history);
                            pst.executeUpdate();
                            System.out.println("Jewellery product added.");
                        } else {
                            System.out.println("Unknown category.");
                        }
                        break;
                    //  Remove Product
                    case 2:
                        System.out.println("Enter Product ID to remove:");
                        int pid = sc.nextInt();
                        sc.nextLine();
                        String table = category.toLowerCase();
                        String deleteSQL = "DELETE FROM " + table + " WHERE pid=? AND vid=?";
                        PreparedStatement delPst = con.prepareStatement(deleteSQL);
                        delPst.setInt(1, pid);
                        delPst.setInt(2, vid);
                        int rows = delPst.executeUpdate();
                        if (rows > 0) {
                            System.out.println("Product removed successfully.");
                        } else {
                            System.out.println("Product not found or not owned by you.");
                        }
                        break;
                    //  View Products via DLL
                    case 3:
                        DoublyLinkedList dll = new DoublyLinkedList();
                        String fetchSQL = "SELECT * FROM " + category.toLowerCase() + " WHERE vid=?";
                        PreparedStatement fetchPst = con.prepareStatement(fetchSQL);
                        fetchPst.setInt(1, vid);
                        ResultSet rs = fetchPst.executeQuery();

                        while (rs.next()) {
                            int id = rs.getInt("pid");
                            String name = rs.getString("pname");
                            int pr = rs.getInt("price");
                            int st = rs.getInt("stock");
                            dll.addProduct(id, name, pr, st);
                        }

                        dll.displayProducts();
                        System.out.println("Total Worth: " + dll.calculateTotalWorth());
                        break;
                    //  Update Stock
                    case 4:
                        System.out.println("Enter Product ID to update stock:");
                        int upid = sc.nextInt();
                        System.out.println("Enter new stock value:");
                        int newStock = sc.nextInt();
                        sc.nextLine();

                        String updateSQL = "UPDATE " + category.toLowerCase() + " SET stock=? WHERE pid=? AND vid=?";
                        PreparedStatement upPst = con.prepareStatement(updateSQL);
                        upPst.setInt(1, newStock);
                        upPst.setInt(2, upid);
                        upPst.setInt(3, vid);

                        int updated = upPst.executeUpdate();
                        if (updated > 0) {
                            System.out.println("Stock updated successfully.");
                        } else {
                            System.out.println("Product not found or not owned by you.");
                        }
                        break;

                    case 5: //  Logout
                        b = false;
                        logout();
                        break;

                    default:
                        System.out.println(ColourCodes.RED+"Invalid choice. Try again. press 1-5 only"+ColourCodes.RESET);
                }
            }catch(InputMismatchException i)
            {
                System.out.println(ColourCodes.RED + "Error: " + ColourCodes.RESET + "Please Enter Valid input");
                System.out.println();
                sc.nextLine();
            }
        }
    }
    //Existing Vendor Login
    public void login() throws Exception {
        Scanner sc = new Scanner(System.in);
        int attempts = 0;
        boolean loggedIn = false;

        String loginQuery = "{call vendor_login(?,?)}";
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");

        while (attempts < 3 && !loggedIn) {
            System.out.print("Enter your email: ");
            String email = sc.nextLine();

            System.out.print("Enter your password: ");
            String password = sc.nextLine();

            CallableStatement ps = con.prepareCall(loginQuery);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                vid = rs.getInt("Vendor_Id");
                shopName = rs.getString("Shop_Name");
                category = rs.getString("Category");
                vendorEmail = email;

                loggedIn = true;
                showDashboard();
            } else {
                System.out.println("Incorrect email or password");
                attempts++;
            }
        }

        if (!loggedIn) {
            System.out.println("Too many failed attempts. Please wait...");
            Thread.sleep(10000);
        }
    }

    public void logout()
    {
        System.out.println("Logging out");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted");
        }
    }
    //New Vendor Registration
    public void register() throws Exception {
        Scanner sc = new Scanner(System.in);
        int attempts = 0;
        boolean success = false;

        String checkQuery = "SELECT * FROM vendor WHERE Vendor_email = ?";
        String insertQuery = "INSERT INTO vendor (Shop_Name, Category, Vendor_email, Password) VALUES (?, ?, ?, ?)";

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");
        PreparedStatement psCheck = con.prepareStatement(checkQuery);
        PreparedStatement psInsert = con.prepareStatement(insertQuery);

        while (attempts < 3) {
            System.out.print("Enter Shop Name: ");
            shopName = sc.nextLine();
            boolean containsdig=false;
            for(char ch:shopName.toCharArray()){
                if(Character.isDigit(ch)){

                    containsdig=true;
                }
            }
            if(containsdig)
            {
                System.out.println("Invalid ShopName");
                attempts++;
                continue;
            }

            System.out.print("Enter Category: ");
            category = sc.nextLine();
            if(!(category.equalsIgnoreCase("Clothing")||category.equalsIgnoreCase("Electronics")||category.equalsIgnoreCase("Footwear")||category.equalsIgnoreCase("Jewellery"))){
                System.out.println("Sorry this category is not supported");
                System.out.println(ColourCodes.BLUE+"only Supported Category's are "+ColourCodes.RESET+ColourCodes.YELLOW+"Clothing/Electronics/Jewellery/Footwear"+ColourCodes.RESET);
                attempts ++;
                continue;
            }

            System.out.print("Enter Vendor Email: ");
            vendorEmail = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            if (shopName.isEmpty() || category.isEmpty() || vendorEmail.isEmpty() || password.isEmpty()) {
                System.out.println("Fields cannot be left empty. Try again.");
                attempts++;
                continue;
            }

            else if(!(vendorEmail.endsWith("@gmail.com")))
            {
                System.out.println("invalid email");
                attempts++;
                continue;
            }

            boolean hasLetter = false, hasDigit = false;
            for (char ch : password.toCharArray()) {
                if (Character.isLetter(ch)) hasLetter = true;
                if (Character.isDigit(ch)) hasDigit = true;
            }
            if (!(hasLetter && hasDigit)) {
                System.out.println("Weak password must be alphanumeric");
                attempts++;
                continue;
            }

            psCheck.setString(1, vendorEmail);
            ResultSet rs = psCheck.executeQuery();

            try {
                if (rs.next()) {
                    throw new Exception(ColourCodes.RED+"Email already exists. Try a different one."+ColourCodes.RESET);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                attempts++;
                continue;
            }

            psInsert.setString(1, shopName);
            psInsert.setString(2, category);
            psInsert.setString(3, vendorEmail);
            psInsert.setString(4, password);

            int rows = psInsert.executeUpdate();
            if (rows > 0) {
                System.out.println("Vendor registered successfully.");
                success = true;
            } else {
                System.out.println("Registration failed.");
            }
            break;
        }

        if (!success && attempts == 3) {
            System.out.println(ColourCodes.RED + "Exception Generated: "  + "Maximum Attempts Reached Please try again after 10 seconds"+ ColourCodes.RESET);
            Thread.sleep(10000);
        }
    }
}