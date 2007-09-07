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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.types.Path;

/**
 * Akin to (a simple subset of) &lt;subant> but failures in subtasks are collected
 * and optionally sent to a JUnit-format report rather than halting the build.
 */
public class SubAntJUnitReport extends Task {

    private Path buildPath;
    public void setBuildPath(Path buildPath) {
        this.buildPath = buildPath;
    }

    private String targetToRun;
    public void setTarget(String target) {
        this.targetToRun = target;
    }

    private boolean failOnError;
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    private File report;
    public void setReport(File report) {
        this.report = report;
    }

    public @Override void execute() throws BuildException {
        Map<String,String> pseudoTests = new HashMap<String,String>();
        for (String path : buildPath.list()) {
            log("Entering: " + path);
            File dir = new File(path);
            Ant ant = new Ant(this);
            ant.init();
            ant.setTarget(targetToRun);
            ant.setDir(dir);
            String msg = null;
            try {
                ant.execute();
            } catch (BuildException x) {
                if (failOnError) {
                    throw x;
                } else {
                    msg = x.getMessage().replaceFirst("(?s).*The following error occurred while executing this line:\r?\n", "");
                }
            } catch (Throwable x) {
                if (failOnError) {
                    throw new BuildException(x, getLocation());
                } else {
                    StringWriter sw = new StringWriter();
                    x.printStackTrace(new PrintWriter(sw));
                    msg = sw.toString();
                }
            }
            pseudoTests.put(path, msg);
            if (msg != null) {
                log("Failed to build " + path + ": " + msg, Project.MSG_WARN);
            } else {
                log("Exiting: " + path);
            }
        }
        // XXX would be nice to permit the 'classname' field to be customized in output...
        JUnitReportWriter.writeReport(this, report, pseudoTests);
    }

}
