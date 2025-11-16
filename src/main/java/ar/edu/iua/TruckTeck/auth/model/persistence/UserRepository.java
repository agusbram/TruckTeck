package ar.edu.iua.TruckTeck.auth.model.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.auth.model.User;

/**
 * Repository interface for performing CRUD operations on {@link User} entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing standard methods for
 * creating, reading, updating, and deleting users from the database.
 * </p>
 * <p>
 * It also includes a custom method for retrieving a user by either username or email,
 * which is commonly used during authentication and validation processes.
 * </p>
 * 
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>{ 
	/**
     * Finds a single {@link User} by matching either the username or the email.
     *
     * @param username The username to search for.
     * @param email    The email to search for.
     * @return An {@link Optional} containing the {@link User} if found, or empty if no user exists
     *         with the given username or email.
     */
	public Optional<User> findOneByUsernameOrEmail(String username, String email);
}
