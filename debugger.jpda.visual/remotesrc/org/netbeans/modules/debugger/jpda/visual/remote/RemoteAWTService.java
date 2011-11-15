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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
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
    
    static boolean removeLoggingListener(Component c, Class listenerClass, Object listener) {
        return RemoteAWTServiceListener.remove(c, listenerClass, listener);
    }
    
    static void pushEventData(Component c, String[] data, String[] stack) {
        synchronized (eventData) {
            List ld = (List) eventData.get(c);
            if (ld == null) {
                ld = new ArrayList();
                eventData.put(c, ld);
            }
            ld.add(data);
            ld.add(stack);
        }
    }
    
    static void calledWithEventsData(Component c, String[] data) {
        // A breakpoint is submitted on this method.
        // When breakpoint is hit, data can be retrieved
    }
    
    static Snapshot[] getGUISnapshots() {
        List snapshots = new ArrayList();
        Window[] windows = Window.getWindows();
        for (int wi = 0; wi < windows.length; wi++) {
            Window w = windows[wi];
            if (!w.isVisible()) {
                continue;
            }
            Dimension d = w.getSize();
            BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.createGraphics();
            w.paint(g);
            Raster raster = bi.getData();
            Object data = raster.getDataElements(0, 0, d.width, d.height, null);
            int[] dataArr;
            if (data instanceof int[]) {
                dataArr = (int[]) data;
            } else {
                continue;
            }
            String title = null;
            if (w instanceof Frame) {
                title = ((Frame) w).getTitle();
            } else if (w instanceof Dialog) {
                title = ((Dialog) w).getTitle();
            }
            snapshots.add(new Snapshot(w, title, d.width, d.height, dataArr));
        }
        Snapshot[] snapshotArr = (Snapshot[]) snapshots.toArray(new Snapshot[] {});
        lastGUISnapshots = snapshotArr;
        return snapshotArr;
    }
    
    // This static field is used to prevent the result from GC until it's read by debugger.
    // Debugger should clear this field explicitly after it reads the result.
    private static Snapshot[] lastGUISnapshots;
    
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
    
    private static class Snapshot {
        
        private Window w;
        private String title;
        private int width;
        private int height;
        private int[] dataArr;
        private String allIntDataString;
        private String allNamesString;
        private Component[] allComponentsArray;
        private ComponentInfo component;
        private final Rectangle rectangle = new Rectangle();
        private static final char STRING_DELIMITER = (char) 3;   // ETX (end of text)
        
        Snapshot(Window w, String title, int width, int height, int[] dataArr) {
            this.w = w;
            this.title = title;
            this.width = width;
            this.height = height;
            this.dataArr = dataArr;
            component = retrieveComponentInfo(w, Integer.MIN_VALUE, Integer.MIN_VALUE);
            int componentCount = component.getComponentsCount();
            int[] allIntDataArray = createAllIntDataArray(componentCount);
            allIntDataString = intArraytoString(allIntDataArray);
            allNamesString = createAllNamesString();
            allComponentsArray = createAllComponentsArray(componentCount);
        }
        
        private ComponentInfo retrieveComponentInfo(Component c, int shiftx, int shifty) {
            String name = c.getName();
            c.getBounds(rectangle);
            int x = rectangle.x;
            int y = rectangle.y;
            if (shiftx == Integer.MIN_VALUE && shifty == Integer.MIN_VALUE) {
                shiftx = shifty = 0; // Do not shift the window as such
                x = y = 0;
            } else {
                shiftx += x;
                shifty += y;
            }
            ComponentInfo ci = new ComponentInfo(c, name, x, y,
                                                 rectangle.width, rectangle.height,
                                                 shiftx, shifty,
                                                 c.isVisible());
            if (c instanceof Container) {
                Component[] subComponents = ((Container) c).getComponents();
                int n = subComponents.length;
                if (n > 0) {
                    ComponentInfo[] cis = new ComponentInfo[n];
                    for (int i = 0; i < cis.length; i++) {
                        cis[i] = retrieveComponentInfo(subComponents[i], shiftx, shifty);
                    }
                    ci.setSubcomponents(cis);
                }
            }
            return ci;
        }
        
        private int[] createAllIntDataArray(int componentCount) {
            int n1 = dataArr.length;
            int n = 3 + n1 + componentCount * ComponentInfo.INT_DATA_LENGTH;
            int[] array = new int[n];
            array[0] = width;
            array[1] = height;
            array[2] = n1;
            System.arraycopy(dataArr, 0, array, 3, n1);
            component.putIntData(array, n1 + 3);
            return array;
        }
        
        private static String intArraytoString(int[] a) {
            int n = a.length;
            if (n == 0)
                return "0[]";

            StringBuffer b = new StringBuffer();
            b.append(n);
            b.append('[');
            b.append(a[0]);
            for (int i = 1; i < n; i++) {
                b.append(",");
                b.append(a[i]);
            }
            b.append(']');
            return b.toString();
        }

        /** Delimit the strings with char 3 */
        private String createAllNamesString() {
            StringBuffer sb = new StringBuffer();
            if (title == null) {
                sb.append((char) 0);
            } else {
                sb.append(title);
            }
            sb.append(STRING_DELIMITER);
            component.putNamesTo(sb);
            return sb.toString();
        }
        
        private Component[] createAllComponentsArray(int componentCount) {
            Component[] components = new Component[componentCount];
            component.putComponentsTo(components, 0);
            return components;
        }
        
        private static class ComponentInfo {
            
            private final static int INT_DATA_LENGTH = 8;
            private final static ComponentInfo[] NO_SUBCOMPONENTS = new ComponentInfo[] {};
            
            private Component c;
            private String name;
            private int x;
            private int y;
            private int width;
            private int height;
            private int shiftx;
            private int shifty;
            private boolean visible;
            private ComponentInfo[] subComponents = NO_SUBCOMPONENTS;
            
            ComponentInfo(Component c, String name, int x, int y,
                          int width, int height, int shiftx, int shifty,
                          boolean visible) {
                this.c = c;
                this.name = name;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.shiftx = shiftx;
                this.shifty = shifty;
                this.visible = visible;
            }
            
            void setSubcomponents(ComponentInfo[] subComponents) {
                this.subComponents = subComponents;
            }
            
            int countComponentIntData() {
                return getComponentsCount() * INT_DATA_LENGTH;
            }
            
            int getComponentsCount() {
                int n = 1;
                for (int i = 0; i < subComponents.length; i++) {
                    n += subComponents[i].getComponentsCount();
                }
                return n;
            }
            
            int putIntData(int[] array, int pos) {
                array[pos++] = x;
                array[pos++] = y;
                array[pos++] = width;
                array[pos++] = height;
                array[pos++] = shiftx;
                array[pos++] = shifty;
                array[pos++] = visible ? 1 : 0;
                array[pos++] = subComponents.length;
                for (int i = 0; i < subComponents.length; i++) {
                    pos = subComponents[i].putIntData(array, pos);
                }
                return pos;
            }
            
            int putComponentsTo(Component[] array, int pos) {
                array[pos++] = c;
                for (int i = 0; i < subComponents.length; i++) {
                    pos = subComponents[i].putComponentsTo(array, pos);
                }
                return pos;
            }
            
            void putNamesTo(StringBuffer sb) {
                if (name == null) {
                    sb.append((char) 0);
                } else {
                    sb.append(name);
                }
                sb.append(STRING_DELIMITER);
                for (int i = 0; i < subComponents.length; i++) {
                    subComponents[i].putNamesTo(sb);
                }
            }
        }
    }
    
}
