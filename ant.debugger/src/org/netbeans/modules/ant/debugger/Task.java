/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
