/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
class Task {

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
