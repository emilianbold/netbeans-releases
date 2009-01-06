package org.netbeans.modules.gsf.testrunner.api;

/**
 * Represents a cause for a test failure.
 */
public final class Trouble {

    private boolean error;
    private String message;
    private String exceptionClsName;
    private String[] stackTrace;
    private Trouble nestedTrouble;

    public Trouble(boolean error) {
        super();
        this.error = error;
    }

    /** */
    public boolean isError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the exceptionClsName
     */
    public String getExceptionClsName() {
        return exceptionClsName;
    }

    /**
     * @param exceptionClsName the exceptionClsName to set
     */
    public void setExceptionClsName(String exceptionClsName) {
        this.exceptionClsName = exceptionClsName;
    }

    /**
     * @return the stackTrace
     */
    public String[] getStackTrace() {
        return stackTrace;
    }

    /**
     * @param stackTrace the stackTrace to set
     */
    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * @return the nestedTrouble
     */
    public Trouble getNestedTrouble() {
        return nestedTrouble;
    }

    /**
     * @param nestedTrouble the nestedTrouble to set
     */
    public void setNestedTrouble(Trouble nestedTrouble) {
        this.nestedTrouble = nestedTrouble;
    }
}
