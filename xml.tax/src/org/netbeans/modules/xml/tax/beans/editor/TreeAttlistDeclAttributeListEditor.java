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

/**
 * 
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclAttributeListEditor extends PropertyEditorSupport {

    //
    // init
    //

    /** Creates new TreeAttlistDeclAttributeListEditor */
    public TreeAttlistDeclAttributeListEditor () {
    }

    
    //
    // itself
    //

    /**
     */
    public void setAsText (String text) throws IllegalArgumentException {
      // can not be set as text
    }

    /**
     */
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
     */
    public Component getCustomEditor () {
        TreeAttlistDeclAttributeListCustomizer comp = new TreeAttlistDeclAttributeListCustomizer();
        comp.setObject (getValue());

        return comp;
    }

    /**
     */
    public boolean isPaintable () {
      return false;
    }

    /**
     */
    public String getAsText () {
        return Util.getString ("NAME_pe_attributes");
    }

}
