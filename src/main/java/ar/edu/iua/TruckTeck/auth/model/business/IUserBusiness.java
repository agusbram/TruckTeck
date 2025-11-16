package ar.edu.iua.TruckTeck.auth.model.business;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import ar.edu.iua.TruckTeck.auth.model.User;
import ar.edu.iua.TruckTeck.auth.model.business.exception.BadPasswordException;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

/**
 * Interface defining the business operations for managing {@link User} entities.
 * <p>
 * This interface abstracts the core user management functionalities such as loading,
 * enabling/disabling accounts, changing passwords, and listing all users. Implementations
 * should handle the necessary validations and business rules.
 * </p>
 * 
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
public interface IUserBusiness {
	/**
     * Loads a {@link User} entity based on the provided username or email.
     *
     * @param usernameOrEmail The username or email of the user to be retrieved.
     * @return The {@link User} corresponding to the provided identifier.
     * @throws NotFoundException   If no user is found with the given username or email.
     * @throws BusinessException   If a general business error occurs during the operation.
     */
	public User load(String usernameOrEmail) throws NotFoundException, BusinessException;

	/**
     * Changes the password of a user.
     *
     * <p>The method validates the old password and applies the new password
     * using the provided {@link PasswordEncoder}.</p>
     *
     * @param usernameOrEmail The username or email of the user.
     * @param oldPassword     The current password of the user.
     * @param newPassword     The new password to be set.
     * @param pEncoder        The password encoder used to hash the new password.
     * @throws BadPasswordException If the new password does not meet security requirements.
     * @throws NotFoundException    If the user cannot be found.
     * @throws BusinessException    If a business logic error occurs during password change.
     */
	public void changePassword(String usernameOrEmail, String oldPassword, String newPassword, PasswordEncoder pEncoder)
			throws BadPasswordException, NotFoundException, BusinessException;

	/**
     * Disables a user account.
     *
     * @param usernameOrEmail The username or email of the user to disable.
     * @throws NotFoundException If the user does not exist.
     * @throws BusinessException If a business rule or database error occurs.
     */			
	public void disable(String usernameOrEmail) throws NotFoundException, BusinessException;

	/**
     * Enables a previously disabled user account.
     *
     * @param usernameOrEmail The username or email of the user to enable.
     * @throws NotFoundException If the user does not exist.
     * @throws BusinessException If a business rule or database error occurs.
     */
	public void enable(String usernameOrEmail) throws NotFoundException, BusinessException;
	
	/**
     * Retrieves a list of all {@link User} entities.
     *
     * @return A {@link List} of all registered users.
     * @throws BusinessException If an error occurs during the retrieval process.
     */
	public List<User> list() throws BusinessException;

}
