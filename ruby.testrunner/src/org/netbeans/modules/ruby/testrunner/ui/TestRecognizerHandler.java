package org.netbeans.modules.ruby.testrunner.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.RecognizedOutput;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FilteredOutput;

public abstract class TestRecognizerHandler {

    protected final Pattern pattern;
    protected Matcher matcher;

    public TestRecognizerHandler(String regex) {
        super();
        this.pattern = Pattern.compile(regex);
    }

    final Matcher match(String line) {
        this.matcher = pattern.matcher(line);
        return matcher;
    }

    abstract void updateUI(Manager manager, TestSession session);

    RecognizedOutput getRecognizedOutput() {
        return new FilteredOutput(new String[0]);
    }

    protected static int toMillis(String timeInSeconds) {
        Double elapsedTimeMillis = Double.parseDouble(timeInSeconds) * 1000;
        return elapsedTimeMillis.intValue();
    }
}
