package nl.hva.cmi.lessen.discombobulator.model;

/**
 * Represents a participant in a chat conversation.
 *
 * <p>A {@code User} is uniquely identified by an immutable {@code id}, typically
 * derived from the user's name or phone number. Additional fields may be filled
 * in later as more details appear during parsing.
 *
 * <p>Users may be created when only partial information is known (e.g. only a
 * phone number or only a name). The parser merges users if both name and phone
 * number eventually refer to the same participant.
 *
 * <p>{@code hashCode()} and {@code equals()} are based solely on {@code id},
 * ensuring stable uniqueness when stored in collections.
 */
public class User {

    public final String id;
    public String name;
    public String phoneNumber;
    public String role;

    public User(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', phone='" + phoneNumber
                + "', role='" + role + "'}";
    }
}
