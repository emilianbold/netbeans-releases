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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.freeform.jdkselection;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;

/**
 * Suppresses some new Ant messages introduced by jdk.xml that are undesirable.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class, position=90)
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
