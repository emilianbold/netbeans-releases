/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.test.xml.schema.core.lib.util;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.test.xml.schema.core.lib.SchemaMultiView;

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
            if(theOneToClose != null) {
                new TopComponentOperator(theOneToClose).close();
                break;
            } else {
                pause(200);
            }
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
        System.out.println("Paused for " + milliseconds);
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
            Component[] comps = ((Container)component).getComponents();
            for (int i = 0; i < comps.length; i++) {
                recurseComponent(level+1, comps[i]);
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
        return qualifiedName.substring(qualifiedName.lastIndexOf(":")+1);
    }
    
    public static String getFullTestName(String strName) {
        return strName.replaceAll(" |:|,|#", "_");
    }
}
