package co.codigo.bookingsystem.common.utils;

import jakarta.persistence.EntityNotFoundException;

public class CommonUtils {

    public static EntityNotFoundException createEntityNotFoundException(String entity, String propertyName, Object propertyValue) {
        return new EntityNotFoundException("%s with %s [%s] is not found.".formatted(entity, propertyName, propertyValue));
    }

}