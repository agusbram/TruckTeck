package ar.edu.iua.TruckTeck.auth.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a security role assigned to users within the system.
 * <p>
 * This entity is mapped to the {@code roles} table in the database and implements
 * {@link Serializable} to allow role instances to be serialized.
 * Roles are used to control access and permissions for {@link User} entities.
 * </p>
 *
 * <p>Common examples of roles include: "ADMIN", "USER", "MANAGER", etc.</p>
 *
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role implements Serializable {
	/** Serialization identifier for ensuring version compatibility. */
	private static final long serialVersionUID = -845420067971973620L;

	/**
     * Human-readable description of the role.
     * <p>Cannot be null and has a maximum length of 100 characters.</p>
     */
	@Column(nullable = false, length = 100)
	private String description;

	/**
     * Primary key identifier for the role.
     * <p>Automatically generated using the identity strategy.</p>
     */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
     * Unique name of the role, used for security checks and authorization.
     * <p>Cannot be null and must be unique.</p>
     */
	@Column(unique = true, nullable = false)
	private String name;
}
