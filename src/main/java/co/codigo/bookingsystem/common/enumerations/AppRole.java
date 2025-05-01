package co.codigo.bookingsystem.common.enumerations;

public enum AppRole {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private String role;

    AppRole(String role) {
        this.role = role;
    }

    public static AppRole fromName(String name) {
        for (AppRole role : values()) {
            if (role.name().equals(name)) return role;
        }

        throw new IllegalStateException("Invalid AppRole: " + name);
    }
}