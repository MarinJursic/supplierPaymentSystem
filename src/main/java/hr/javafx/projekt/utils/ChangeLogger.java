package hr.javafx.projekt.utils;

import hr.javafx.projekt.model.ChangeLogEntry;
import hr.javafx.projekt.model.Entity;
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
 * Koristi refleksiju za automatsko praćenje promjena.
 */
public final class ChangeLogger {

    private static final Logger log = LoggerFactory.getLogger(ChangeLogger.class);
    private static final ChangeLogRepository changeLogRepository = new ChangeLogRepository();

    private ChangeLogger() {}

    /**
     * Bilježi dodavanje novog entiteta.
     */
    public static <T extends Entity> void logAddition(T newEntity) {
        if (newEntity == null) return;
        logChange("ADD", newEntity.getClass().getSimpleName(), "N/A", "N/A", newEntity.toString());
    }

    /**
     * Bilježi brisanje postojećeg entiteta.
     */
    public static <T extends Entity> void logDeletion(T oldEntity) {
        if (oldEntity == null) return;
        logChange("DELETE", oldEntity.getClass().getSimpleName(), "N/A", oldEntity.toString(), "N/A");
    }

    /**
     * Uspoređuje dva entiteta i bilježi svaku promjenu polja.
     */
    public static <T extends Entity> void logUpdate(T oldEntity, T newEntity) {
        if (oldEntity == null || newEntity == null) return;

        for (Method method : oldEntity.getClass().getMethods()) {
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