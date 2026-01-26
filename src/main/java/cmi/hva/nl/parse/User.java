package cmi.hva.nl.parse;

/**
 * Represents a participant in a chat conversation.
 *
 * <p>A {@code User} is uniquely identified by an immutable {@code id}. The id is
 * typically derived from the user's name or phone number, depending on which
 * information is available first in the log. Additional fields such as
 * {@code name} and {@code phoneNumber} may be filled in later as more details
 * appear during parsing.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code id} — canonical identifier for this user; never changes.</li>
 *   <li>{@code name} — optional display name extracted from the log.</li>
 *   <li>{@code phoneNumber} — optional phone number, normalized if present.</li>
 *   <li>{@code isCustomer} — whether this user is a customer or an internal employee.</li>
 * </ul>
 *
 * <p>Users may be created when only partial information is known (e.g., only a
 * phone number or only a name). The parser later merges users if both name and
 * phone number eventually refer to the same participant. The {@code id} field
 * determines uniqueness and is used as the map key inside {@link LogParser}.
 *
 * <p>The {@code hashCode()} implementation is based solely on {@code id},
 * ensuring stable uniqueness when stored in collections.
 *
 * <p>{@code toString()} provides a readable debug representation including all
 * user fields.
 */
public class User {

    public static final String USER_NOTIFICATION = "notification";
    public String phoneNumber;
    public String name;

    public final String id;

    public boolean isCustomer, isSystem;

    public User(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", isCustomer=" + isCustomer +
                ", isSystem=" + isSystem +
                '}';
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isCustomer() {
        return this.isCustomer;
    }
}
