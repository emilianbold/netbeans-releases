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
/*
 * PathDef.java
 *
 * Created on May 6, 2001, 4:06 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.util.LinkedList;
import java.util.Iterator;

/**
 *
 * @author  vs124454
 * @version
 */
public class PathDef extends Task {
    private LinkedList elements = new LinkedList();
    private String id;

    public Path createPath() {
        Path p = new Path(getProject());
        elements.add(p);
        return p;
    }

    public void setUseId(String id) {
        this.id = id;
    }
    
    public void execute () throws BuildException {
        if (null == id)
            throw new BuildException("Set attribute 'useid'.");

        Path path = new Path(getProject());
        Iterator i = elements.iterator();
        while(i.hasNext()) {
            Path p = (Path)i.next();
            path.append(p);
        }
        
        getProject().addReference(id, path);
    }
}
