/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import org.netbeans.modules.editor.options.AnnotationTypesFolder;


/**
 * "Warm-up" task for editor. Executed after IDE startup, it should
 * pre-initialize some suitable parts of the module to improve first time usage
 * experience - which might suffer from long response time due to class loading
 * and various initialization.
 * See {@link org.netbeans.core.AfterStartWarmUp} for details about how the task is run.
 *
 */

public class EditorWarmUpTask implements Runnable{
    
    public void run() {
        AnnotationTypesFolder.getAnnotationTypesFolder(); //#57414
    }

}
