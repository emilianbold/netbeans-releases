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
package org.netbeans.nbbuild;

import java.io.File;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * Fails if any path element is misssing. Atributes :<br>
 *  path - input paths for validation<br>
 * The task is used for validation runtime class in binary tests distribution.
 */
public class ValidatePath extends Task {
    
    private Path path;
    public void setPath(Path p) {
        if (path == null) {
            path = p;
        } else {
            path.append(p);
        }
    }
    public Path createPath () {
        if (path == null) {
            path = new Path(getProject());
        }
        return path.createPath();
    }
    public void setPathRef(Reference r) {
        createPath().setRefid(r);
    }
    
    public void execute() throws BuildException {
     String paths[] = path.list();
     for (int i = 0 ; i < paths.length ; i++) {
         if (!new File(paths[i]).exists()) {
             throw new BuildException("File " + paths[i] + " doesn't exists.");
         }
     }
    }
    
}
