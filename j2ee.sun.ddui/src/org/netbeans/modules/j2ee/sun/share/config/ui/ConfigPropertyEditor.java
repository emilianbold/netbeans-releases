/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigPropertyEditor.java
 *
 * Created on August 21, 2001, 2:51 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.beans.*;
import java.awt.Component;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.PropertySheet;

import org.netbeans.modules.j2ee.sun.share.config.ConfigBeanStorage;

/**
 *
 * @author  gfink
 * @version
 */
public class ConfigPropertyEditor extends PropertyEditorSupport {
    
    private PropertySheet sheet = new PropertySheet();
    final Object bean;
    final Class type;
    Object value;
    
    /** Creates new ConfigPropertyEditor */
    public ConfigPropertyEditor(Object bean, Class type) {
        this.bean = bean; this.type = type;
//        System.err.println("New editor " + this);
    }
    
    public void setValue(Object value) {
//       System.err.println("Setting on editor " + this + " value " + value);
        this.value = value;
        Node n = new ConfigBeanNode((ConfigBeanStorage) value);
        sheet.setNodes(new Node[] { n });
    }
    
    public synchronized Component getCustomEditor() {
        return sheet;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public String getAsText() {
        return null;
    }
    
}
