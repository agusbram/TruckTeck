package ar.edu.iua.TruckTeck.auth.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a system user and implements {@link UserDetails} for Spring Security integration.
 * <p>
 * This entity is mapped to the {@code users} table in the database. Users may have multiple roles
 * defined by the {@link Role} entity, which are used for authorization checks and security enforcement.
 * </p>
 * 
 * <p>The class also provides validation methods for checking account status, such as expired,
 * locked, or disabled accounts.</p>
 *
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class User implements UserDetails {

	/** Flag indicating whether the account has expired. Defaults to true. */
	@Column(columnDefinition = "tinyint default 0")
	private boolean accountNonExpired = true;

	/** Flag indicating whether the account is locked. Defaults to true. */
	@Column(columnDefinition = "tinyint default 0")
	private boolean accountNonLocked = true;

	/** Flag indicating whether the credentials have expired. Defaults to true. */
	@Column(columnDefinition = "tinyint default 0")
	private boolean credentialsNonExpired = true;

	/** Flag indicating whether the account is enabled. Defaults to false. */
	@Column(columnDefinition = "tinyint default 0")
	private boolean enabled;

	/** Validation constants used in the {@link #validate()} method. */
	public static final String VALIDATION_OK = "OK";
	public static final String VALIDATION_ACCOUNT_EXPIRED = "ACCOUNT_EXPIRED";
	public static final String VALIDATION_CREDENTIALS_EXPIRED = "CREDENTIALS_EXPIRED";
	public static final String VALIDATION_LOCKED = "LOCKED";
	public static final String VALIDATION_DISABLED = "DISABLED";

	/**
     * Validates the account status of the user.
     *
     * @return One of the validation constants indicating the account status:
     *         {@link #VALIDATION_OK}, {@link #VALIDATION_ACCOUNT_EXPIRED},
     *         {@link #VALIDATION_CREDENTIALS_EXPIRED}, {@link #VALIDATION_LOCKED},
     *         or {@link #VALIDATION_DISABLED}.
     */
	public String validate() {
		if (!accountNonExpired)
			return VALIDATION_ACCOUNT_EXPIRED;
		if (!credentialsNonExpired)
			return VALIDATION_CREDENTIALS_EXPIRED;
		if (!accountNonLocked)
			return VALIDATION_LOCKED;
		if (!enabled)
			return VALIDATION_DISABLED;
		return VALIDATION_OK;
	}

	/** User email (unique and required). */
	@Column(length = 255, nullable = false, unique = true)
	private String email;

	/** Primary key of the user entity. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idUser;
	@Column(length = 100, unique = true)

	/** Username (unique). */
	private String username;

	/** User password (hashed). */
	private String password;


	/** Set of roles assigned to the user for authorization purposes. */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "userroles", joinColumns = {
			@JoinColumn(name = "idUser", referencedColumnName = "idUser") }, inverseJoinColumns = {
					@JoinColumn(name = "idRole", referencedColumnName = "id") })
	private Set<Role> roles;

	/**
     * Checks if the user has a specific {@link Role}.
     *
     * @param role The {@link Role} to check.
     * @return {@code true} if the user has the role, {@code false} otherwise.
     */
	@Transient
	public boolean isInRole(Role role) {
		return isInRole(role.getName());
	}

	/**
     * Checks if the user has a role by name.
     *
     * @param role Role name to check.
     * @return {@code true} if the user has the role, {@code false} otherwise.
     */
	@Transient
	public boolean isInRole(String role) {
		for (Role r : getRoles())
			if (r.getName().equals(role))
				return true;
		return false;
	}

	/**
     * Returns the authorities granted to the user for Spring Security.
     *
     * @return A collection of {@link GrantedAuthority} representing user roles.
     */
	@Transient
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());
		return authorities;
	}

	/**
     * Returns a list of authority names (role names) assigned to the user.
     *
     * @return List of role names.
     */
	@Transient
	public List<String> getAuthoritiesStr() {
		List<String> authorities = getRoles().stream().map(role -> role.getName()).collect(Collectors.toList());
		return authorities;
	}

}
