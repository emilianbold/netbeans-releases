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

package org.netbeans.modules.java.freeform.jdkselection;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;

/**
 * Suppresses some new Ant messages introduced by jdk.xml that are undesirable.
 * @author Jesse Glick
 */
public class Logger extends AntLogger {

    /** Public for lookup */
    public Logger() {}

    @Override
    public void messageLogged(AntEvent event) {
        //System.err.println("GOT: " + event);
        if (!event.isConsumed()) {
            String msg = event.getMessage();
            if (isOurs(msg)) {
                //System.err.println("task=" + event.getTaskName());
                event.consume();
                event.getSession().deliverMessageLogged(event, msg, AntEvent.LOG_VERBOSE);
                return;
            }
        }
    }

    private static boolean isOurs(String msg) {
        String prefix = "Trying to override old definition of task "; // NOI18N
        if (msg.startsWith(prefix)) {
            String task = msg.substring(prefix.length());
            if (task.equals("javac") || // NOI18N
                    task.equals("java") || // NOI18N
                    task.equals("junit") || // NOI18N
                    task.equals("javadoc") || // NOI18N
                    task.equals("nbjpdastart") || // NOI18N
                    task.equals("http://java.netbeans.org/freeform/jdk.xml:property")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private static final String[] TASKS = {
        "macrodef", // NOI18N
        "presetdef", // NOI18N
        "propertyfile", // NOI18N
    };

    @Override
    public String[] interestedInTasks(AntSession session) {
        return TASKS;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    @Override
    public boolean interestedInAllScripts(AntSession session) {
        // XXX for some reason messages come in from nbjdk.xml, not jdk.xml...?
        // Also for ide-file-targets.xml etc.
        return true;
    }

    @Override
    public int[] interestedInLogLevels(AntSession session) {
        return new int[] {
            AntEvent.LOG_INFO,
            AntEvent.LOG_WARN,
        };
    }
    
}
