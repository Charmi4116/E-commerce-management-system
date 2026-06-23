package users;

class DoublyLinkedList {
    class Node {
        int id;
        String name;
        int price;
        int stock;
        Node prev, next;
        //Customised Linked List For Vendor
        Node(int id, String name, int price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.prev = null;
            this.next = null;
        }
    }

    private Node head, tail;

    public void addProduct(int id, String name, int price, int stock) {
        Node newNode = new Node(id, name, price, stock);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }
    //display all products being sold by respective vendor
    public void displayProducts() {
        if (head == null) {
            System.out.println("No products found.");
            return;
        }
        Node temp = head;
        System.out.println("\n--- Products Sold by Vendor ---");
        while (temp != null) {
            System.out.println("Product ID: " + temp.id +
                    ", Name: " + temp.name +
                    ", Price: " + temp.price +
                    ", Stock: " + temp.stock);
            temp = temp.next;
        }
    }
    //Total worth of products being sold  by a particular vendor
    public int calculateTotalWorth() {
        Node temp = head;
        int totalWorth = 0;
        while (temp != null) {
            totalWorth += (temp.price * temp.stock);
            temp = temp.next;
        }
        return totalWorth;
    }
}