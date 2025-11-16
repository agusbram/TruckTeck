package ar.edu.iua.TruckTeck.auth.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import ar.edu.iua.TruckTeck.auth.model.User;
import ar.edu.iua.TruckTeck.auth.model.business.exception.BadPasswordException;
import ar.edu.iua.TruckTeck.auth.model.persistence.UserRepository;

/**
 * Service implementation of {@link IUserBusiness} that manages user operations.
 * <p>
 * This class provides business logic for user management, including loading users,
 * changing passwords, enabling/disabling accounts, and listing all users.
 * It integrates with {@link UserRepository} for persistence operations and uses
 * {@link PasswordEncoder} for secure password handling.
 * </p>
 *
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Service
@Slf4j
public class UserBusiness implements IUserBusiness {
	/** Repository for CRUD operations on {@link User} entities. */
	@Autowired
	private UserRepository userDAO;


	/**
     * Loads a {@link User} by username or email.
     *
     * @param usernameOrEmail The username or email of the user to load.
     * @return The {@link User} corresponding to the given identifier.
     * @throws NotFoundException If no user is found with the specified username or email.
     * @throws BusinessException If an unexpected business error occurs during the operation.
     */
	@Override
	public User load(String usernameOrEmail) throws NotFoundException, BusinessException {
		Optional<User> ou;
		try {
			ou = userDAO.findOneByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
		if (ou.isEmpty()) {
			throw NotFoundException.builder().message("No se encuentra el usuari@ email o nombre =" + usernameOrEmail)
					.build();
		}
		return ou.get();
	}

	/**
     * Changes the password of a user after validating the old password.
     *
     * @param usernameOrEmail The username or email of the user.
     * @param oldPassword     The current password of the user.
     * @param newPassword     The new password to set.
     * @param pEncoder        {@link PasswordEncoder} used to encode the new password.
     * @throws BadPasswordException If the old password does not match the stored password.
     * @throws NotFoundException    If the user cannot be found.
     * @throws BusinessException    If an unexpected error occurs during saving.
     */
	@Override
	public void changePassword(String usernameOrEmail, String oldPassword, String newPassword, PasswordEncoder pEncoder)
			throws BadPasswordException, NotFoundException, BusinessException {
		User user = load(usernameOrEmail);
		if (!pEncoder.matches(oldPassword, user.getPassword())) {
			throw BadPasswordException.builder().build();
		}
		user.setPassword(pEncoder.encode(newPassword));
		try {
			userDAO.save(user);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	/**
     * Disables a user account (sets enabled to false).
     *
     * @param usernameOrEmail The username or email of the user to disable.
     * @throws NotFoundException If the user cannot be found.
     * @throws BusinessException If an unexpected error occurs during saving.
     */
	@Override
	public void disable(String usernameOrEmail) throws NotFoundException, BusinessException {
		setDisable(usernameOrEmail, false);
	}

	/**
     * Enables a user account (sets enabled to true).
     *
     * @param usernameOrEmail The username or email of the user to enable.
     * @throws NotFoundException If the user cannot be found.
     * @throws BusinessException If an unexpected error occurs during saving.
     */
	@Override
	public void enable(String usernameOrEmail) throws NotFoundException, BusinessException {
		setDisable(usernameOrEmail, true);
	}

	/**
     * Sets the enabled state of a user account.
     *
     * @param usernameOrEmail The username or email of the user.
     * @param enable          {@code true} to enable, {@code false} to disable.
     * @throws NotFoundException If the user cannot be found.
     * @throws BusinessException If an unexpected error occurs during saving.
     */
	private void setDisable(String usernameOrEmail, boolean enable) throws NotFoundException, BusinessException {
		User user = load(usernameOrEmail);
		user.setEnabled(enable);
		try {
			userDAO.save(user);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	/**
     * Returns a list of all users in the system.
     *
     * @return {@link List} of {@link User} entities.
     * @throws BusinessException If an error occurs while retrieving the users.
     */
	@Override
	public List<User> list() throws BusinessException {
		try {
			return userDAO.findAll();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

}
