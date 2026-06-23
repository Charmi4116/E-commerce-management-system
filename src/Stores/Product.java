package Stores;

public  abstract class Product
{
    int product_id;
    String product_Name;
    int stock ;
    double price;
    //implemented by inherited classes
    public abstract void viewProducts(String productName,  int cus_id) throws Exception;
}