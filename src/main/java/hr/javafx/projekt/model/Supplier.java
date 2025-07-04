package hr.javafx.projekt.model;

/**
 * Predstavlja dobavljača u sustavu.
 * Klasa je `non-sealed`, što znači da može biti slobodno naslijeđena.
 */
public non-sealed class Supplier extends Entity {
    private String name;
    private String address;
    private String oib;

    /**
     * Konstruktor za kreiranje novog dobavljača.
     * @param id Jedinstveni identifikator dobavljača.
     * @param name Naziv dobavljača.
     * @param address Adresa dobavljača.
     * @param oib Osobni identifikacijski broj (OIB) dobavljača.
     */
    public Supplier(Long id, String name, String address, String oib) {
        super(id);
        this.name = name;
        this.address = address;
        this.oib = oib;
    }

    /**
     * Vraća naziv dobavljača.
     * @return Naziv dobavljača.
     */
    public String getName() { return name; }

    /**
     * Postavlja naziv dobavljača.
     * @param name Novi naziv dobavljača.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Vraća adresu dobavljača.
     * @return Adresa dobavljača.
     */
    public String getAddress() { return address; }

    /**
     * Postavlja adresu dobavljača.
     * @param address Nova adresa dobavljača.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Vraća OIB dobavljača.
     * @return OIB dobavljača.
     */
    public String getOib() { return oib; }

    /**
     * Postavlja OIB dobavljača.
     * @param oib Novi OIB dobavljača.
     */
    public void setOib(String oib) { this.oib = oib; }

    /**
     * Vraća string reprezentaciju dobavljača, formatiranu za lakše parsiranje i logiranje.
     * @return String s podacima o dobavljaču.
     */
    @Override
    public String toString() {
        return String.format("Supplier[name=%s, address=%s, oib=%s]", name, address, oib);
    }
}