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
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 * 
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class NullChoicePropertyEditor extends NullStringEditor implements EnhancedPropertyEditor {

    /** */
    private String[] tags;


    //
    // init
    //

    /** Creates new NullChoicePropertyEditor */
    public NullChoicePropertyEditor (String[] tags) {
        super();

        this.tags = tags;
    }


    //
    // PropertyEditor
    //    
    
    /**
     */
    public boolean supportsCustomEditor () {
        return false;
    }
    
    /**
     */
    public String[] getTags () {
        return tags;
    }
    

    //
    // EnhancedPropertyEditor
    //
    
    /**
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
     */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
     */
    public boolean supportsEditingTaggedValues () {
        return false;
    }

}
