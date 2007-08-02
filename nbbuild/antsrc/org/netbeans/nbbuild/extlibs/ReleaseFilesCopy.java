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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

/**
 * Looks for project properties whose name start with "release." for files to copy.
 * If the remainder of the property name matches a file (relative to basedir),
 * it is copied to the location in the cluster given by the value.
 * Example: release.external/something.jar=modules/ext/something.jar
 */
public class ReleaseFilesCopy extends Task {

    private File cluster;
    public void setCluster(File cluster) {
        this.cluster = cluster;
    }

    public @Override void execute() throws BuildException {
        for (Map.Entry<?,?> entry : ((Map<?,?>) getProject().getProperties()).entrySet()) {
            String k = (String) entry.getKey();
            if (k.startsWith("release.") && !k.matches("release\\.(files|files\\.extra|dir)")) {
                File from = getProject().resolveFile(k.substring(8));
                if (from.isFile()) {
                    File to = FileUtils.getFileUtils().resolveFile(cluster, (String) entry.getValue());
                    log("Copying " + from + " to " + to);
                    to.getParentFile().mkdirs();
                    try {
                        FileUtils.getFileUtils().copyFile(from, to);
                    } catch (IOException x) {
                        throw new BuildException("Could not copy " + from + ": " + x, x, getLocation());
                    }
                } else {
                    log("Could not find file " + from + " to copy", Project.MSG_WARN);
                }
            }
        }
    }

}
