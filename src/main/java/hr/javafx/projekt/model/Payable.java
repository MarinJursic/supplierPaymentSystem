package hr.javafx.projekt.model;

import java.math.BigDecimal;

/**
 * Definira ponašanje za entitete koji imaju iznos za plaćanje.
 */
public interface Payable {
    /**
     * Vraća iznos koji je potrebno platiti.
     * @return Iznos za plaćanje.
     */
    BigDecimal getAmountPayable();
}