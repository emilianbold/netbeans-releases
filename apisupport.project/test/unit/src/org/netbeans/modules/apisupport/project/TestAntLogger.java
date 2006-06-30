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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Lookup;

/**
 *  Implementation of simple AntLogger. To enable/disable use  setEnabled(boolean ) method.
 *
 * @author pzajac
 */
public class TestAntLogger extends AntLogger {
    boolean bEnabled;


    public void setEnabled(boolean  bEnabled) {
        this.bEnabled = true;
    }
    public void messageLogged(AntEvent event) {
        if (bEnabled) {
            System.out.println(event.getMessage());
        }
    }

    public boolean interestedInSession(AntSession session) {
        return bEnabled;
    }

    public boolean interestedInScript(File script, AntSession session) {
        return bEnabled;
    }

    public boolean interestedInAllScripts(AntSession session) {
        return bEnabled;
    }
    
   public void targetStarted(AntEvent event) {
        System.out.println("target started:" + event.getTargetName());
    }

    public String[] interestedInTasks(AntSession session) {
        return (bEnabled) ? ALL_TASKS : new String[0];
    }

    public String[] interestedInTargets(AntSession session) {
        return (bEnabled) ? ALL_TARGETS : new String[0];
    }

    public int[] interestedInLogLevels(AntSession session) {
        return (bEnabled) ?
                 new int[]{AntEvent.LOG_INFO,AntEvent.LOG_WARN,AntEvent.LOG_ERR}:
                 new int[0];
    }

    static TestAntLogger getDefault() {
        return  (TestAntLogger) Lookup.getDefault().lookupItem(
                new Lookup.Template(AntLogger.class,
                                   "org.netbeans.modules.apisupport.project.TestAntLogger",
                                   null)).getInstance();
    }

}
