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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild.extlibs;

import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Defines a property release.files.extra.
 * Value is comma-separated list of those project properties whose name begins with "release."
 * where the remainder of the property name describes an existing file path (relative to basedir).
 */
public class ReleaseFilesExtra extends Task {

    private String property;
    public void setProperty(String property) {
        this.property = property;
    }

    public @Override void execute() throws BuildException {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<?,?> entry : ((Map<?,?>) getProject().getProperties()).entrySet()) {
            String k = (String) entry.getKey();
            if (k.startsWith("release.") && getProject().resolveFile(k.substring(8)).isFile()) {
                if (b.length() > 0) {
                    b.append(',');
                }
                b.append((String) entry.getValue());
            }
        }
        getProject().setNewProperty(property, b.toString());
    }

}
