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
package org.netbeans.modules.xml.text.indent;

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;

import org.netbeans.modules.editor.FormatterIndentEngineBeanInfo;
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    /** */
    private BeanDescriptor beanDescriptor;


    //
    // init
    //

    /** */
    public XMLIndentEngineBeanInfo () {
    }


    //
    // FormatterIndentEngineBeanInfo
    //

    /**
     */
    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor (getBeanClass());
            beanDescriptor.setDisplayName (getString ("LAB_XMLIndentEngine"));
            beanDescriptor.setShortDescription (getString ("HINT_XMLIndentEngine"));
        }
        return beanDescriptor;
    }

    /**
     */
    protected Class getBeanClass () {
        return XMLIndentEngine.class;
    }

    /**
     */
    protected String[] createPropertyNames () {
        return NbEditorUtilities.mergeStringArrays
            (super.createPropertyNames(),
             new String[] {
             }
             );
    }

    /**
     */
    protected String getString (String key) {
        try {
            return Util.getString (key);
        } catch (MissingResourceException e) {
            return super.getString (key);
        }
    }

}
