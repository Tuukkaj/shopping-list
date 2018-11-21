package shoppingList;

public class Product {
    private int quantity;
    private String name;

    public Product(String name, int quantity) {
        setName(name);
        setQuantity(quantity);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
