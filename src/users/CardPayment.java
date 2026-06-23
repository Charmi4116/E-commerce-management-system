package users;

import ColourCode.ColourCodes;

import java.util.*;
import java.sql.*;

class CardPayment implements  PaymentMethod
{
    //Taking card details
    public boolean makePayment(double amount) throws Exception {
        Scanner sc = new Scanner(System.in);
        String cardNumber=null;
        String expirationMonth=null;
        try {
            System.out.println("Card Payment Method");
            System.out.println("*******************");
            System.out.print("Enter card number: ");
            cardNumber = sc.nextLine();
            System.out.print("Enter card expiration in MM/YYYY format: ");
            expirationMonth = sc.nextLine();
            System.out.print("Enter CVV: ");

            int cvv = sc.nextInt();
            if(cvv<0)
            {
                System.out.println("invalid Cvv");
                throw new InvalidCardException(ColourCodes.RED+"Cvv Invalid"+ColourCodes.RESET);
            }
        } catch (InvalidCardException e)
        {
            System.out.println(e.getMessage());
        }
        catch (InputMismatchException i)
        {
            throw new InvalidCardException(ColourCodes.RED+"Invalid Details"+ColourCodes.RESET);
        }

        String cardType = CheckCardNumber(cardNumber);
        boolean checkdate = checkExpiry(expirationMonth);

        try {
            if (cardType == null || !checkdate) {
                throw new InvalidCardException("Invalid card details");
            } else {
                System.out.println("Card type: " + cardType);
                System.out.println("Completing payment...");
                Thread.sleep(2000);
                System.out.println(ColourCodes.GREEN+"Order Placed Successfully of Amount" + amount+ColourCodes.RESET);
                return true;
            }
        } catch (InvalidCardException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //validating card number
    public  String CheckCardNumber(String cardNumber)
    {
        boolean isinvalid =false;
        if (cardNumber.length()!=16)
        {
            return null;
        }
        else{
            for(int i=0;i<cardNumber.length();i++)
            {
                if(!Character.isDigit(cardNumber.charAt(i)))
                {
                    isinvalid =true;
                    break;

                }
            }
            if(!(cardNumber.startsWith("4")||cardNumber.startsWith("5")||cardNumber.startsWith("34")))
            {
                isinvalid =true;
                return null;
            }
            else{
                if(cardNumber.startsWith("5"))
                {
                    return "mastercard";
                }
                else if(cardNumber.startsWith("4"))
                {
                    return "visa";

                }
                else{
                    return "Amex";
                }
            }

        }
    }
    //validating expiry of card
    public boolean checkExpiry(String expiryMonth) throws Exception {
        String[] parts = expiryMonth.split("/");
        if (parts.length != 2) return false;

        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        if (month < 1 || month > 12) return false;

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mall", "root", "");

        String query = "SELECT CASE " +
                "WHEN (? < MONTH(CURDATE()) AND ? = YEAR(CURDATE())) " +
                "OR (? < YEAR(CURDATE())) " +
                "THEN 'Expired' ELSE 'Valid' END AS status";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, month);
        ps.setInt(2, year);
        ps.setInt(3, year);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getString("status").equals("Valid");
        }

        return false; // fallback return
    }
}
//Customised Exception
class InvalidCardException extends RuntimeException
{
    public InvalidCardException(String message)
    {
        super(message);
    }
}