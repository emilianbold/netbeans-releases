/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            long time = System.currentTimeMillis();
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
                    log("cvs command failed on module '" + m + "'.  Retrying...");
                }
            }
            long timedelta = (System.currentTimeMillis() - time) / 1000;
            log("Processed  module: " + m + " (" +  timedelta + "s)");
        }
    }
}
