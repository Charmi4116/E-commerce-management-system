import users.*;
import ColourCode.*;

import java.util.InputMismatchException;
import java.util.Scanner;

class Main
{
    public static void main(String[] args) throws Exception //Main Entry of our Digital Mall
    {
        Scanner sc = new Scanner(System.in);
        Customer customer = new Customer();
        Vendor vendor = new Vendor();
        boolean flag=true;

        System.out.println("******Welcome to Digital Mall******");
        while(flag) {
            try {
                System.out.println(ColourCodes.CYAN+"Press 1 for Vendor Login"+ColourCodes.RESET);
                System.out.println(ColourCodes.CYAN+"Press 2 to  customer login"+ColourCodes.RESET);
                System.out.println(ColourCodes.CYAN+"press 3 to register as a new vendor"+ColourCodes.RESET);
                System.out.println(ColourCodes.CYAN+"press 4 to create new Customer Account"+ColourCodes.RESET);
                System.out.println(ColourCodes.CYAN+"press 5 to Exit"+ColourCodes.RESET);
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice) {
                    // vendor login
                    case 1:
                        vendor.login();
                        break;
                    // customer login
                    case 2:
                        customer.login();
                        break;
                    // new vendor register
                    case 3:
                        vendor.register();
                        break;
                    // new customer register
                    case 4:
                        customer.register();
                        break;
                    // exit
                    case 5:
                        System.out.println(ColourCodes.PURPLE+"*****Thank You For Shopping With Us****** :)"+ColourCodes.RESET);
                        flag = false;
                        break;

                    default:
                        System.out.println(ColourCodes.RED + "Error: " + ColourCodes.RESET + "Please Enter Valid input(1 to 5)");
                        break;
                }
            }
            catch(InputMismatchException i)//Making sure valid input is given
            {
                System.out.println(ColourCodes.RED + "Error: " + ColourCodes.RESET + "Please Enter Valid input");
                sc.nextLine();
            }
        }
    }
}