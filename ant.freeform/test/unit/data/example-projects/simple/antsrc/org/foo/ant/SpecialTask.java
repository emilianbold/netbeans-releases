package org.foo.ant;
import org.apache.tools.ant.Task;
public class SpecialTask extends Task {
    public void execute() {
        log("special task run");
    }
}
