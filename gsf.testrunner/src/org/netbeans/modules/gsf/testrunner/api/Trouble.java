package org.netbeans.modules.gsf.testrunner.api;

import org.openide.util.Parameters;

/**
 * Represents a cause for a test failure.
 */
public final class Trouble {

    private boolean error;
    private String[] stackTrace;
    private ComparisonFailure comparisonFailure;

    public Trouble(boolean error) {
        super();
        this.error = error;
    }

    /** returns true if error, false if failure */
    public boolean isError() {
        return error;
    }

    /**
     * @param error - true if error, false if failure
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the stackTrace
     */
    public String[] getStackTrace() {
        return stackTrace;
    }

    /**
     * @param stackTrace the stackTrace for the failure. The first
     * item in the array is treated as the failure message.
     */
    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * @return the comparison failure or <code>null</code>.
     */
    public ComparisonFailure getComparisonFailure() {
        return comparisonFailure;
    }

    /**
     * @param comparisonFailure the failure to set. May be <code>null</code>.
     */
    public void setComparisonFailure(ComparisonFailure comparisonFailure) {
        this.comparisonFailure = comparisonFailure;
    }

    /**
     * Represents a comparison failure for two Strings, e.g. an assert_equals failure.
     */
    public static final class ComparisonFailure {

        private final String expected;
        private final String actual;
        private final String mimeType;
        private static final String DEFAULT_MIME_TYPE = "text/plain"; //NOI18N

        /**
         * Constructs a new ComparisonFailure using the default mime type.
         * @param expected the expected value.
         * @param actual the actual value.
         */
        public ComparisonFailure(String expected, String actual) {
            this(expected, actual, DEFAULT_MIME_TYPE);
        }

        /**
         * Constructs a new ComparisonFailure.
         * @param expected the expected value.
         * @param actual the actual value.
         * @param mimeType the mime type for the comparison; must not be <code>null</code>
         * or an empty String.
         */
        public ComparisonFailure(String expected, String actual, String mimeType) {
            Parameters.notEmpty("mimeType", mimeType);
            this.expected = expected;
            this.actual = actual;
            this.mimeType = mimeType;
        }


        public String getActual() {
            return actual;
        }

        public String getExpected() {
            return expected;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

}
