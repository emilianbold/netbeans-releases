/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common;

import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;

import java.util.HashMap;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;

import org.openide.filesystems.FileObject;

public class Util {
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
         
   
    /*
     * Determines if the class specified in rhs inherits from lhs, traversing the entire heirarchy tree.
     * @param lhs ClassElement representing the potential superclass or interface
     * @param rhs Fully-qualified name of class being tested
     * @param ref FileObject of a file that is thought to belong to the same classpath structure as rhs.
     */
    public static boolean isAssignableFrom(ClassElement lhs, String rhs, FileObject ref) {
        String lhsName = lhs.getName().getFullName();
        if (lhsName.equals(rhs)) {
            return true;
        }
        ClassElement rhsCls = ClassElement.forName(rhs, ref);
        boolean lhsIsInterface = lhs.isInterface();
        HashMap visited = null;
        
        while (rhsCls != null) {
            if (visited == null) {
                visited = new HashMap();
            } else {
                if (visited.get(rhsCls) != null) {
                    return false;
                }
            }
            visited.put(rhsCls, rhsCls);
            
            if (lhsIsInterface) {
                Identifier[] interfaces = rhsCls.getInterfaces();
                for (int i = 0; i < interfaces.length; ++i) {
                    String interfaceName = interfaces[i].getFullName();
                    // Recursively go and check with this interface
                    // (dealing with superinterfaces).
                    if (isAssignableFrom(lhs, interfaceName, ref)){
                        return true;
                    }    
                }
            }
            Identifier superCls = rhsCls.getSuperclass();
            if (superCls != null) {
                if (lhsName.equals(superCls.getFullName())) {
                    return true;
                }
                rhsCls = ClassElement.forName(superCls.getFullName(), ref);
            } else {
                rhsCls = null;
            }
        }
        return false;
    }
    
}
