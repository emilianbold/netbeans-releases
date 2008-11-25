package org.netbeans.modules.ruby.testrunner.ui;

/**
 * Represents a cause for a test failure.
 */
final class Trouble {

    static final String COMPARISON_FAILURE_JUNIT3 = "junit.framework.ComparisonFailure";
    //NOI18N
    static final String COMPARISON_FAILURE_JUNIT4 = "org.junit.ComparisonFailure";
    //NOI18N
    boolean error;
    String message;
    String exceptionClsName;
    String[] stackTrace;
    Trouble nestedTrouble;

    Trouble(boolean error) {
        super();
        this.error = error;
    }

    /** */
    boolean isError() {
        return error;
    }

    /** */
    boolean isComparisonFailure() {
        return (exceptionClsName != null) && (exceptionClsName.equals(COMPARISON_FAILURE_JUNIT3) || exceptionClsName.equals(COMPARISON_FAILURE_JUNIT4));
    }

    /** */
    boolean isFakeError() {
        return error && isComparisonFailure();
    }
}
