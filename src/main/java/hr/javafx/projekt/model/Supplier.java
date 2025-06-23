package hr.javafx.projekt.model;

/**
 * Predstavlja dobavljača u sustavu.
 */
public final class Supplier extends BaseEntity {
    private String name;
    private String address;
    private String oib;

    public Supplier(Long id, String name, String address, String oib) {
        super(id);
        this.name = name;
        this.address = address;
        this.oib = oib;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOib() { return oib; }
    public void setOib(String oib) { this.oib = oib; }

    @Override
    public String toString() {
        return name;
    }
}