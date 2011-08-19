/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * This class provides the main access to remote services.
 * 
 * @author Martin Entlicher
 */
public class RemoteAWTService {
    
    private static final String AWTAccessThreadName = "org.netbeans.modules.debugger.jpda.visual AWT Access Loop";   // NOI18N
    private static volatile boolean awtAccess = false;
    private static volatile boolean awtAccessLoop = false;
    
    private static final Map eventData = new HashMap();
    
    public RemoteAWTService() {
    }
    
    static void startAccessLoop() {
        if (!awtAccessLoop) {
            awtAccessLoop = true;
            Thread loop = new Thread(new AWTAccessLoop(), AWTAccessThreadName);
            loop.setDaemon(true);
            loop.setPriority(Thread.MIN_PRIORITY);
            loop.start();
        }
    }
    
    static void stopAccessLoop() {
        awtAccessLoop = false;
    }
    
    static void calledInAWT() {
        // A breakpoint is submitted on this method.
        // When awtAccess field is set to true, this breakpoint is hit in AWT thread
        // and methods can be executed via debugger.
    }
    
    static Object addLoggingListener(Component c, Class listener) {
        return RemoteAWTServiceListener.add(c, listener);
    }
    
    static void removeLoggingListener(Component c, Object listener) {
        RemoteAWTServiceListener.remove(c, listener);
    }
    
    static void pushEventData(Component c, String[] data) {
        synchronized (eventData) {
            List ld = (List) eventData.get(c);
            if (ld == null) {
                ld = new ArrayList();
                eventData.put(c, ld);
            }
            ld.add(data);
        }
    }
    
    static void calledWithEventsData(Component c, String[] data) {
        // A breakpoint is submitted on this method.
        // When breakpoint is hit, data can be retrieved
    }
    
    private static class AWTAccessLoop implements Runnable {
        
        public AWTAccessLoop() {}

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                calledInAWT();
                return ;
            }
            while (awtAccessLoop) {
                if (awtAccess) {
                    awtAccess = false;
                    SwingUtilities.invokeLater(this);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return ;
                }
                Map eventDataCopy = null;
                synchronized (eventData) {
                    if (eventData.size() > 0) {
                        eventDataCopy = new HashMap(eventData);
                        eventData.clear();
                    }
                }
                if (eventDataCopy != null) {
                    for (Iterator ic = eventDataCopy.keySet().iterator(); ic.hasNext(); ) {
                        Component c = (Component) ic.next();
                        List dataList = (List) eventDataCopy.get(c);
                        int totalLength = 0;
                        int l = dataList.size();
                        for (int i = 0; i < l; i++) {
                            totalLength += 1 + ((String[]) dataList.get(i)).length;
                        }
                        String[] allData = new String[totalLength];
                        int ii = 0;
                        for (int i = 0; i < l; i++) {
                            String[] data = (String[]) dataList.get(i);
                            allData[ii++] = Integer.toString(data.length);
                            for (int j = 0; j < data.length; j++) {
                                allData[ii++] = data[j];
                            }
                        }
                        calledWithEventsData(c, allData);
                    }
                }
            }
        }
    }
    
}
