package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.testrunner.output.OutputLine;
import org.openide.util.Parameters;

/**
 * Represents a single test case.
 */
public final class Testcase {

    private final String type;
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

    public Testcase(String type, TestSession session) {
        Parameters.notNull("session", session);
        this.session = session;
        this.type = type;
    }

    public TestSession getSession() {
        return session;
    }

    public String getType() {
        return type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addOutputLine(String line) {
        output.add(new OutputLine(line, false));
    }

    public void addOutputLines(List<String> lines) {
        for (String line : lines) {
            output.add(new OutputLine(line, false));
        }
    }

    public List<OutputLine> getOutput() {
        return output;
    }


    /**
     * Gets the location, i.e. the path to the file and line number of the test case.
     * May be null if such info is not available.
     * @return
     */
    public String getLocation() {
        return location;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        if (status != null) {
            return status;
        }
        if (trouble == null) {
            return Status.PASSED;
        }
        return trouble.isError() ? Status.ERROR : Status.FAILED;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the timeMillis
     */
    public int getTimeMillis() {
        return timeMillis;
    }

    /**
     * @param timeMillis the timeMillis to set
     */
    public void setTimeMillis(int timeMillis) {
        this.timeMillis = timeMillis;
    }

    /**
     * @return the trouble
     */
    public Trouble getTrouble() {
        return trouble;
    }

    /**
     * @param trouble the trouble to set
     */
    public void setTrouble(Trouble trouble) {
        this.trouble = trouble;
    }

    @Override
    public String toString() {
        return Testcase.class.getSimpleName() + "[class: " + className + ", name: " + name + "]"; //NOI18N
    }

}
