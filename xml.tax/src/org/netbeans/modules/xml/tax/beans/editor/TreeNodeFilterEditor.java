/*
 * TreeNodeFilterEditor.java -- synopsis.
 *
 *
 * SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
 *
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.netbeans.tax.traversal.TreeNodeFilter;

/**
 * 
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNodeFilterEditor extends PropertyEditorSupport {
    
    //
    // PropertyEditor
    //

    /**
     */
    public void setAsText (String text) throws IllegalArgumentException {
    }

    /**
     */
    public String getAsText () {
        return Util.getString ("LBL_nodeFilter");
    }

    /**
     */
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
     */
    public Component getCustomEditor () {
        return new TreeNodeFilterCustomEditor ((TreeNodeFilter)getValue());
    }

}
