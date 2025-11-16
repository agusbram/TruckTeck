package ar.edu.iua.TruckTeck.auth.model.business.exception;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Exception thrown to indicate that a provided password does not meet
 * the expected criteria or is invalid during authentication or validation processes.
 * <p>
 * This custom exception extends {@link Exception} and is typically used within
 * authentication or password management components to signal password-related issues.
 * </p>
 *
 * <p>Examples of scenarios that may trigger this exception include:</p>
 * <ul>
 *   <li>Incorrect user password during login.</li>
 *   <li>Password failing validation rules (e.g., complexity or length).</li>
 *   <li>Unexpected password hashing or encoding errors.</li>
 * </ul>
 *
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@NoArgsConstructor
public class BadPasswordException extends Exception {

    /** Serialization identifier for ensuring version compatibility. */
	private static final long serialVersionUID = -8582277206660722157L;

    /**
     * Constructs a new {@code BadPasswordException} with the specified detail message
     * and cause.
     *
     * @param message the detail message providing additional context about the error.
     * @param ex      the cause of this exception (may be {@code null}).
     */
	@Builder
	public BadPasswordException(String message, Throwable ex) {
		super(message, ex);
	}

    /**
     * Constructs a new {@code BadPasswordException} with the specified detail message.
     *
     * @param message the detail message describing the reason for the exception.
     */
	@Builder
	public BadPasswordException(String message) {
		super(message);
	}

    /**
     * Constructs a new {@code BadPasswordException} using the message from another
     * {@link Throwable} as the detail message, and associates that exception as the cause.
     *
     * @param ex the originating exception that caused this error.
     */
	@Builder
	public BadPasswordException(Throwable ex) {
		super(ex.getMessage(), ex);
	}
}
