package shoppingList;

/**
 * Used in TableView by DropboxDownload.
 *
 * @author Tuukka Juusela
 * @version 2018.1412
 * @since 1.8
 */
public class FileItem {
    /**
     * Name of the item.
     */
    private String name;

    /**
     * Constructor. Sets name of the item.
     * @param name name of the item.
     */
    public FileItem(String name) {
        setName(name);
    }

    /**
     * Returns name of the item.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of the item.
     * @param name of the item.
     */
    public void setName(String name) {
        this.name = name;
    }
}