package org.netbeans.modules.ant.debugger;

import java.io.File;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.openide.text.Line;

/**
 *
 * @author  Honza
 */
public class Task {
    
    private TaskStructure taskStructure;
    private Object        line;
    private File          file;
    
    Task (
        TaskStructure   taskStructure,
        Object          line,
        File            file
    ) {
        this.taskStructure = taskStructure;
        this.line = line;
        this.file = file;
    }
    
    TaskStructure getTaskStructure () {
        return taskStructure;
    }
    
    Object getLine () {
        return line;
    }
    
    File getFile () {
        return file;
    }
}
