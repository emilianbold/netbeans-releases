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

package org.netbeans.test.xslt.lib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author ca@netbeans.org
 */

public class Helpers {
    public static final String WAIT_COMPONENT_TIMEOUT = "ComponentOperator.WaitComponentTimeout";
    public static final int NO_EVENT_TIMEOUT = 500;
    public static EventTool et = new EventTool();
    
    /** Creates a new instance of Helpers */
    public Helpers() {
    }
    
    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass) {
        return getComponentOperator(opContainer, strComponentClass, null, 0);
    }
    
    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final String strComponentName) {
        return getComponentOperator(opContainer, strComponentClass, strComponentName, 0);
    }
    
    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final int index) {
        return getComponentOperator(opContainer, strComponentClass, null, index);
    }
    
    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final String strComponentName, final int index) {
        return getComponentOperator(opContainer, strComponentClass, strComponentName, index, 2000);
    }
    
    public static JComponentOperator getComponentOperator(ContainerOperator opContainer, final String strComponentClass, final String strComponentName, final int index, final int timeout) {
        
        Timeouts times = JemmyProperties.getCurrentTimeouts();
        long to = times.setTimeout(Helpers.WAIT_COMPONENT_TIMEOUT, timeout);
        
        JComponentOperator opJComponent = null;
        
        try {
            opJComponent = new JComponentOperator(opContainer,
                    new ComponentChooser() {
                
                public boolean checkComponent(java.awt.Component comp) {
                    boolean result = true;
                    if (strComponentName != null) {
                        String name = comp.getName();
                        if (name != null) {
                            result = name.equals(strComponentName);
                        }
                    }
                    return result && comp.getClass().toString().equals("class " + strComponentClass);
                }
                
                public String getDescription() {
                    return strComponentClass + " with name " +  strComponentName;
                }
            }, index);
        } catch (TimeoutExpiredException e) {
        } finally {
            times.setTimeout(Helpers.WAIT_COMPONENT_TIMEOUT, to);
        }
        
        return opJComponent;
    }
    
    public static void waitNoEvent() {
        waitNoEvent(NO_EVENT_TIMEOUT);
    }
    
    public static void waitNoEvent(int milliseconds) {
        et.waitNoEvent(milliseconds);
    }
    
    public static void pause(int milliseconds) {
        System.out.println("Paused for " + milliseconds);
        try {
            Thread.currentThread().sleep(milliseconds);
        } catch (Exception e) {
        }
    }
    
    public static void writeJemmyLog(String str) {
        JemmyProperties.getCurrentOutput().printLine(str);
    }
    
    public static void recurseComponent(int level, Component component) {
        
        JemmyProperties.getCurrentOutput().print("*");
        
        for (int j = 0; j < level; j++) {
            JemmyProperties.getCurrentOutput().print("|");
        }
        
//        JemmyProperties.getCurrentOutput().printLine("Name: " + component.toString() + ", Class: " + component.getClass());
        JemmyProperties.getCurrentOutput().printLine("Class: " + component.getClass());
        
        if (component instanceof Container) {
            Component[] comps = ((Container)component).getComponents();
            for (int i = 0; i < comps.length; i++) {
                recurseComponent(level+1, comps[i]);
            }
        }
    }
    
    public static Point getContainerPoint(JComponentOperator opComponent, Point componentPoint, JComponentOperator opContainer) {
        Point pComponent = opComponent.getLocationOnScreen();
        Point pContainer = opContainer.getLocationOnScreen();
        
        return new Point(pComponent.x + componentPoint.x - pContainer.x, pComponent.y + componentPoint.y - pContainer.y);
    }
    
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
    
    public static boolean isCRC32Equal(String strText, long checkSum) {
        strText = strText.trim().replaceAll("[  [\t\f\r]]", "");
        
        CRC32 crc32 = new CRC32();
        crc32.update(strText.getBytes());
        long newCheckSum = crc32.getValue();
        
        writeJemmyLog("CRC32=" + newCheckSum);
        
        return newCheckSum == checkSum;
    }
    
    public static String getFullTestName(String strName) {
        return strName.replaceAll(" |:|,|#", "_");
    }
    
    public static JTextFieldOperator getTextFieldOpByLabel(JLabelOperator opAssociatedLabel) {
        JLabel component = (JLabel)opAssociatedLabel.getSource();
        
        if (component.getLabelFor() instanceof JPanel) {
            return new JTextFieldOperator(new ContainerOperator((Container)component.getLabelFor().getParent()));
        } else {
            return new JTextFieldOperator((JTextField)component.getLabelFor());
        }
    }
    
}
