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

/**
 * 
 * @author  Libor Kramolis
 * @version 0.1
 */
public class NullStringEditor extends PropertyEditorSupport implements ExPropertyEditor {
    /** */
    private static final boolean DEBUG = false;

    /** */
    protected static final String DEFAULT_NULL = Util.getString ("TEXT_DEFAULT");

    /** */
    private boolean editable;


    //
    // init
    //

    /** Creates new NullStringEditor */
    public NullStringEditor () {
        super();
        editable = true;
    }

    
    //
    // PropertyEditor
    //

    /**
     */
    public void setAsText (String text) throws IllegalArgumentException {
	if ( DEBUG ) {
	    Util.debug ("NullStringEditor::setAsText: text = " + text); // NOI18N
        }

	if ( DEFAULT_NULL.equals (text) ) {
	    setValue (null);
	} else if ( text.length() == 0 ) {
	    setValue (null);
	} else {
	    setValue (text);
	}
    }

    /**
     */
    public String getAsText () {
	Object value = super.getValue();

	if ( DEBUG ) {
	    Util.debug ("NullStringEditor::getAsText: value = " + value); // NOI18N
        }

	if ( value == null ) {
	    return DEFAULT_NULL;
	} else {
	    String text = value.toString();
	    if ( text.length() == 0) {
		return DEFAULT_NULL;
	    }
	    return text;
	}
    }

    /**
     */
    public Component getCustomEditor () {
        return new NullStringCustomEditor (this);
    }

    /**
     */
    public String getJavaInitializationString () {
        String s = (String) getValue ();
        return "\"" + toAscii (s) + "\""; // NOI18N
    }


    //
    // ExPropertyEditor
    //

    /**
     */
    public void attachEnv (PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();

        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;

            editable = prop.canWrite();
        }
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


    //
    // itself
    //

    /**
     */
    public boolean isEditable () {
        return editable;
    }

    /**
     */
    private static String toAscii (String str) {
        StringBuffer buf = new StringBuffer (str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append ("\\b"); break; // NOI18N
            case '\t': buf.append ("\\t"); break; // NOI18N
            case '\n': buf.append ("\\n"); break; // NOI18N
            case '\f': buf.append ("\\f"); break; // NOI18N
            case '\r': buf.append ("\\r"); break; // NOI18N
            case '\"': buf.append ("\\\""); break; // NOI18N
//  	    case '\'': buf.append ("\\'"); break; // NOI18N
            case '\\': buf.append ("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && c <= 0x007f)
                    buf.append (c);
                else {
                    buf.append ("\\u"); // NOI18N
                    String hex = Integer.toHexString (c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append ('0');
                    buf.append (hex);
                }
            }
        }
        return buf.toString();
    }

}
