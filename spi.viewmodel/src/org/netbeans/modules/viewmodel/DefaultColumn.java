/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.beans.PropertyEditor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author   Jan Jancura
 */
public class DefaultColumn extends PropertySupport.ReadWrite {
    
    
    DefaultColumn () {
        super (
            "default",
            String.class,
            "DN",
            "SDN"
        );
        setValue (
            "ComparableColumnTTV", 
            Boolean.TRUE
        );
        setValue (
            "TreeColumnTTV", 
            Boolean.TRUE
        );
    }

    public Object getValue () {
        return null;
    }
    
    public void setValue (Object obj) {
    }
//    public PropertyEditor getPropertyEditor () {
//        return propertyEditor;
//    }
}

