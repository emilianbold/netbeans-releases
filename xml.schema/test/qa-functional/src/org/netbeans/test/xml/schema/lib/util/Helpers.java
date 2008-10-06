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
package org.netbeans.test.xml.schema.lib.util;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

/**
 *
 * @author ca@netbeans.org
 */
public class Helpers {

    public static final String WAIT_COMPONENT_TIMEOUT = "ComponentOperator.WaitComponentTimeout";
    public static final int NO_EVENT_TIMEOUT = 500;
    public static EventTool et = new EventTool();

    public static void closeTopComponentIfOpened(String strName) {
        for (int i = 0; i < 5; i++) {
            JComponent theOneToClose = TopComponentOperator.findTopComponent(strName, 0);
            if (theOneToClose != null) {
                new TopComponentOperator(theOneToClose).close();
                break;
            } else {
                pause(200);
            }
        }
    }

    public static void closeUMLWarningIfOpened() {
        for (int i = 0; i < 5; i++) {
            JDialog theOneToClose = JDialogOperator.findJDialog("Platform Warning", true, true);
            if (theOneToClose != null) {
                new JDialogOperator(theOneToClose).close();
                break;
            } else {
                pause(200);
            }
        }
    }

    public static void closeMimeWarningIfOpened() {

        JDialog theOneToClose = JDialogOperator.findJDialog("Warning", true, true);
        if (theOneToClose != null) {
            new JDialogOperator(theOneToClose).close();
        } else {
            pause(200);
        }
    }

    public static void recurseColumns(int column, Node node, SchemaMultiView opView, final int maxColumnIndex) {
        node.select();

        System.out.println("path: " + node.getPath());
    /*
    if (column < maxColumnIndex) {
    JListOperator opList = opView.getColumnListOperator(column + 1);
    Node nextColumnNode = opTree.getRootNode();
    if (nextColumnNode != null) {
    recurseColumns(column + 1, nextColumnNode, opView, maxColumnIndex);
    }
    }
    
    for (int i = 0; i < node.getChildren().length; i++) {
    Node childNode = new Node(node, i);
    recurseColumns(column, childNode, opView, maxColumnIndex);
    }
     */
    }

    public static void pause(int milliseconds) {
        //System.out.println("Paused for " + milliseconds);
        try {
            Thread.currentThread().sleep(milliseconds);
        } catch (Exception e) {
        }
    }

    public static void recurseComponent(int level, Component component) {

        JemmyProperties.getCurrentOutput().print("*");
        System.out.print("*");

        for (int j = 0; j < level; j++) {
            JemmyProperties.getCurrentOutput().print("|");
            System.out.print("|");
        }

        System.out.println("Name: " + component.getName() + ", Class: " + component.getClass());
        JemmyProperties.getCurrentOutput().printLine("Name: " + component.getName() + ", Class: " + component.getClass());

//        System.out.println("Component: " + component);

        if (component instanceof Container) {
            Component[] comps = ((Container) component).getComponents();
            for (int i = 0; i < comps.length; i++) {
                recurseComponent(level + 1, comps[i]);
            }
        }
    }

    public static JComponentOperator getComponent(ContainerOperator opContainer, final String strComponentClass) {
        return new JComponentOperator(opContainer,
                new ComponentChooser() {

                    public boolean checkComponent(java.awt.Component comp) {
                        return comp.getClass().toString().equals("class " + strComponentClass);
                    }

                    public String getDescription() {
                        return strComponentClass;
                    }
                });
    }

    public static void waitNoEvent() {
        waitNoEvent(NO_EVENT_TIMEOUT);
    }

    public static void waitNoEvent(int milliseconds) {
        et.waitNoEvent(milliseconds);
    }

    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final int index) {
        return getComponentOperator(opContainer, strComponentClass, index, 2000);
    }

    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final int index, final int timeout) {

        Timeouts times = JemmyProperties.getCurrentTimeouts();
        long to = times.setTimeout(Helpers.WAIT_COMPONENT_TIMEOUT, timeout);

        JComponentOperator opJComponent = null;

        try {
            opJComponent = new JComponentOperator(opContainer,
                    new ComponentChooser() {

                        public boolean checkComponent(java.awt.Component comp) {
                            return comp.getClass().toString().equals("class " + strComponentClass);
                        }

                        public String getDescription() {
                            return strComponentClass;
                        }
                    }, index);
        } catch (TimeoutExpiredException e) {
        } finally {
            times.setTimeout(Helpers.WAIT_COMPONENT_TIMEOUT, to);
        }

        return opJComponent;
    }

    public static void writeJemmyLog(String str) {
        JemmyProperties.getCurrentOutput().printLine(str);
    }

    public static String getUnqualifiedName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(":") + 1);
    }

    public static String getFullTestName(String strName) {
        return strName.replaceAll(" |:|,|#", "_");
    }
}
