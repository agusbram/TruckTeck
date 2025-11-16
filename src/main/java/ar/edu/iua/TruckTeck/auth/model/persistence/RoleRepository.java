package ar.edu.iua.TruckTeck.auth.model.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.iua.TruckTeck.auth.model.Role;

/**
 * Repository interface for performing CRUD operations on {@link Role} entities.
 * <p>
 * This interface extends {@link JpaRepository} providing standard methods for
 * saving, updating, deleting, and retrieving {@link Role} instances from the database.
 * It leverages Spring Data JPA's repository abstraction to reduce boilerplate code.
 * </p>
 *
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{ 
    // No additional methods are required; JpaRepository provides standard CRUD operations.
}