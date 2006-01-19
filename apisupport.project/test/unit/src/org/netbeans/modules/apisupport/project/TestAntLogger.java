/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
