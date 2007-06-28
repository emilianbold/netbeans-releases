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
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Cvs;
import org.apache.tools.ant.types.*;

/**
 * This is wrapper around the ANT's CVS task.
 * It is able to split the CVS commands module by module
 * and in case of fail to retry failing part
 */
public class NBCvs extends Task {

    String command = null;
    int compressionlevel = 6;
    String cvsroot = null;
    File dest = null;
    String cvspackage = null;
    String tag = null;
    String date = null;
    boolean quiet = true;
    boolean failonerror = true;
    int reTry = 0;

    public void setCompressionlevel(int compressionlevel) {
        this.compressionlevel = compressionlevel;
    }

    public void setPackage(String cvspackage) {
        this.cvspackage = cvspackage;
    }

    public void setCvsroot(String cvsroot) {
        this.cvsroot = cvsroot;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setCommand(String c) {
        this.command = c;
    }

    public void setReTry(int n) {
        this.reTry = n;
    }

    public void execute() throws BuildException {
        if (cvspackage == null) {
            throw new BuildException("You have to specify the list of module - package attribute", getLocation());
        }
        Cvs cvsTask = (Cvs) getProject().createTask("cvs");
        if (command != null) {
            cvsTask.setCommand(command);
        }
        cvsTask.setCompressionLevel(compressionlevel);
        if (cvsroot != null) {
            cvsTask.setCvsRoot(cvsroot);
        }
        if (dest != null) {
            cvsTask.setDest(dest);
        }
        if (tag != null) {
            cvsTask.setTag(tag);
        }
        if (date != null) {
            cvsTask.setDate(date);
        }
        cvsTask.setQuiet(quiet);
        cvsTask.setFailOnError(failonerror);

        String[] modules = cvspackage.split(" ");
        for (String m : modules) {
            log("Processing module: " + m);
            int tries = 0;
            boolean ok = false;
            cvsTask.setPackage(m);
            while (!ok) {
                try {
                    cvsTask.init();
                    cvsTask.execute();
                    ok = true;
                } catch (BuildException be) {
                    if (tries == reTry)
                        throw be;
                    tries++;
                    log("cvs command failed on module '" + m + "' for " + tries + ". time");
                }
            }
        }
    }
}
