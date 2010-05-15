package org.netbeans.modules.wsdlextensions.email.editor.wizard;


/**
 * Exception to indicate that a model change failed.
 *
 * @author Noel.Ang@sun.com
 */
public class ModelModificationException extends Exception {
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ModelModificationException() {
    }

    /**
     * Constructs a new exception with the specified detail message.  The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the {@link #getMessage()} method.
     */
    public ModelModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link
     * #getCause()} method).  (A <tt>null</tt> value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     *
     * @since 1.4
     */
    public ModelModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}