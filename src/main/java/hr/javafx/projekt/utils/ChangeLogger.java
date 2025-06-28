package hr.javafx.projekt.utils;

import hr.javafx.projekt.model.BaseEntity;
import hr.javafx.projekt.model.ChangeLogEntry;
import hr.javafx.projekt.repository.ChangeLogRepository;
import hr.javafx.projekt.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Uslužna klasa za generiranje i spremanje zapisa o promjenama entiteta.
 * Koristi refleksiju nad javnim getter metodama kako bi se poštovala enkapsulacija.
 */
public final class ChangeLogger {

    private static final Logger log = LoggerFactory.getLogger(ChangeLogger.class);
    private static final ChangeLogRepository changeLogRepository = new ChangeLogRepository();

    private ChangeLogger() {}

    /**
     * Logira dodavanje novog entiteta.
     * @param newEntity Novi entitet koji je dodan.
     * @param <T> Tip entiteta.
     */
    public static <T extends BaseEntity> void logAddition(T newEntity) {
        if (newEntity == null) return;
        logChange("ADD", newEntity.getClass().getSimpleName(), "N/A", "N/A", newEntity.toString());
    }

    /**
     * Logira brisanje postojećeg entiteta.
     * @param oldEntity Entitet koji je obrisan.
     * @param <T> Tip entiteta.
     */
    public static <T extends BaseEntity> void logDeletion(T oldEntity) {
        if (oldEntity == null) return;
        logChange("DELETE", oldEntity.getClass().getSimpleName(), "N/A", oldEntity.toString(), "N/A");
    }

    /**
     * Uspoređuje dva entiteta i logira svaku promjenu polja pozivanjem njihovih javnih getter metoda.
     * @param oldEntity Staro stanje entiteta.
     * @param newEntity Novo stanje entiteta.
     * @param <T> Tip entiteta.
     */
    public static <T extends BaseEntity> void logUpdate(T oldEntity, T newEntity) {
        if (oldEntity == null || newEntity == null) return;

        for (Method method : oldEntity.getClass().getMethods()) {
            // Zanima nas samo javna getter metoda bez parametara
            if (isGetter(method)) {
                try {
                    Object oldValue = method.invoke(oldEntity);
                    Object newValue = method.invoke(newEntity);

                    if (!Objects.equals(oldValue, newValue)) {
                        String fieldName = getFieldNameFromGetter(method.getName());
                        logChange(
                                "UPDATE",
                                oldEntity.getClass().getSimpleName(),
                                fieldName,
                                Objects.toString(oldValue, "null"),
                                Objects.toString(newValue, "null")
                        );
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Nije moguće pozvati metodu '{}' za logiranje promjena.", method.getName(), e);
                }
            }
        }
    }

    /**
     * Provjerava je li metoda javni getter.
     * @param method Metoda za provjeru.
     * @return True ako je metoda getter, inače false.
     */
    private static boolean isGetter(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) ||
                method.getParameterCount() != 0 ||
                method.getReturnType() == void.class) {
            return false;
        }
        String methodName = method.getName();
        return (methodName.startsWith("get") || methodName.startsWith("is")) && !methodName.equals("getClass");
    }

    /**
     * Pretvara naziv getter metode u naziv polja (npr. "getName" -> "name").
     * @param getterName Naziv getter metode.
     * @return Naziv polja.
     */
    private static String getFieldNameFromGetter(String getterName) {
        if (getterName.startsWith("get")) {
            return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
        }
        if (getterName.startsWith("is")) {
            return Character.toLowerCase(getterName.charAt(2)) + getterName.substring(3);
        }
        return getterName;
    }

    /**
     * Pomoćna metoda za kreiranje i spremanje zapisa o promjeni.
     */
    private static void logChange(String changeType, String entityName, String fieldName, String oldValue, String newValue) {
        ChangeLogEntry entry = new ChangeLogEntry(
                changeType,
                entityName,
                fieldName,
                oldValue,
                newValue,
                LocalDateTime.now(),
                SessionManager.getUserRole().name()
        );
        changeLogRepository.saveChange(entry);
    }
}