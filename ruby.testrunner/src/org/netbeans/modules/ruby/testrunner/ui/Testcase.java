package org.netbeans.modules.ruby.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;

/**
 * Represents a single test case.
 */
final class Testcase {

    private final TestType type;
    private String className;
    private String name;
    private int timeMillis;
    private Trouble trouble;
    private Status status;
    private final List<OutputLine> output = new ArrayList<OutputLine>();
    /**
     * The location, i.e. the file and line number of this test case.
     */
    private String location;
    private TestSession session;

    public Testcase(TestType type) {
        this.type = type;
    }

    public Testcase(TestType type, TestSession session) {
        this.session = session;
        this.type = type;
    }

    TestSession getSession() {
        return session;
    }

    TestType getType() {
        return type;
    }

    void setLocation(String location) {
        this.location = location;
    }

    void addOutputLine(String line) {
        output.add(new OutputLine(line, false));
    }

    void addOutputLines(List<String> lines) {
        for (String line : lines) {
            output.add(new OutputLine(line, false));
        }
//        output.addAll(lines);
    }

    List<OutputLine> getOutput() {
        return output;
    }


    /**
     * Gets the location, i.e. the path to the file and line number of the test case.
     * May be null if such info is not available.
     * @return
     */
    String getLocation() {
        return location;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    Status getStatus() {
        if (status != null) {
            return status;
        }
        if (trouble == null) {
            return Status.PASSED;
        }
        return trouble.isError() ? Status.ERROR : Status.FAILED;
    }

    /**
     * Gets the line from the stack trace representing the last line in the test class.
     * If that can't be resolved
     * then returns the second line of the stack trace (the
     * first line represents the error message) or <code>null</code> if there
     * was no (usable) stack trace attached.
     *
     * @return
     */
    String getTestCaseLineFromStackTrace() {
        if (trouble == null) {
            return null;
        }
        String[] stacktrace = trouble.stackTrace;
        if (stacktrace == null || stacktrace.length <= 1) {
            return null;
        }
        if (stacktrace.length > 2) {
            String underscoreName = RubyUtils.camelToUnderlinedName(className);
            for (int i = 0; i < stacktrace.length; i++) {
                if (stacktrace[i].contains(underscoreName) && stacktrace[i].contains(name)) {
                    return stacktrace[i];
                }
            }
        }
        return stacktrace[1];
    }

    /**
     * @return the className
     */
    String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the name
     */
    String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * @return the timeMillis
     */
    int getTimeMillis() {
        return timeMillis;
    }

    /**
     * @param timeMillis the timeMillis to set
     */
    void setTimeMillis(int timeMillis) {
        this.timeMillis = timeMillis;
    }

    /**
     * @return the trouble
     */
    Trouble getTrouble() {
        return trouble;
    }

    /**
     * @param trouble the trouble to set
     */
    void setTrouble(Trouble trouble) {
        this.trouble = trouble;
    }

    @Override
    public String toString() {
        return Testcase.class.getSimpleName() + "[class: " + className + ", name: " + name + "]"; //NOI18N
    }

}
