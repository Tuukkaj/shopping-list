package shoppingList;

/**
 * Product is used by TableView in Components. Holds information for each row of TableView.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
public class Product {
    /**
     * Quantity of product in Components TableView.
     */
    private int quantity;
    /**
     * Name of product in Components TableView.
     */
    private String name;

    /**
     * Class constructor. Sets name and quantity to class parameters.
     * @param name of the product.
     * @param quantity of the product.
     */
    public Product(String name, int quantity) {
        setName(name);
        setQuantity(quantity);
    }

    /**
     * Gets products quantity.
     * @return quantity of product.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets quantity of the product.
     * @param quantity of the product.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets products name.
     * @return name of product.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the product.
     * @param name of the product.
     */
    public void setName(String name) {
        this.name = name;
    }
}
