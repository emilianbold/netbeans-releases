/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.ArrayList;

/**
*
* @author Ian Formanek
*/
public class RADContainer extends RADComponent implements ComponentContainer {
    private ArrayList subComponents;

    public RADComponent[] getSubBeans () {
        RADComponent[] components = new RADComponent [subComponents.size ()];
        subComponents.toArray (components);
        return components;
    }

    public void initSubComponents (RADComponent[] initComponents) {
        subComponents = new ArrayList (initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            subComponents.add (initComponents[i]);
        }
    }

    public void reorderSubComponents (int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = subComponents.remove (from);
            if (from < to) {
                subComponents.add (to - 1, value);
            } else {
                subComponents.add (to, value);
            }
        }
        getFormManager ().fireComponentsReordered (this);
    }

    public void add (RADComponent comp) {
        subComponents.add (comp);
        ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
    }

    public void remove (RADComponent comp) {
        int index = subComponents.indexOf (comp);
        if (index != -1) {
            subComponents.remove (index);
        }
        ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
    }

    public int getIndexOf (RADComponent comp) {
        return subComponents.indexOf (comp);
    }


    /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
    * It is expected that the returned code is either "" (in which case the form is the container) or is a name of variable
    * or method call ending with "." (e.g. "container.getContentPane ().").
    * This implementation returns "", as there is no sense to add visual components to non-visual containers
    * @return the prefix code for generating code to add subcomponents to this container
    */
    public String getContainerGenName () {
        return ""; // NOI18N
    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Pavel Buzek     I18N
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/15/99  Ian Formanek    getContainerGenName 
 *       usage clarified
 *  8    Gandalf   1.7         7/5/99   Ian Formanek    implemented additions to
 *       ComponentsContainer
 *  7    Gandalf   1.6         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
