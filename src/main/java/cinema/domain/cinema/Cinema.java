package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

public final class Cinema extends BaseEntity {

    private final String name;
    private final String address;

    public Cinema(String id, String name, String address) {
        super(id);
        this.name = requireText(name, "Cinema name");
        this.address = requireText(address, "Cinema address");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " must not be blank");
        }
        return value.trim();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
