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

import org.netbeans.tax.TreeUtilities;

/**
 *
 * @author  Vladimir Zboril
 * @author  Libor Kramolis
 * @version 0.2
 */
public class EncodingEditor extends NullChoicePropertyEditor {
    
    /** */
    private static String[] items;


    //
    // init
    //

    /** Creates new EncodingEditor */
    public EncodingEditor () {
        super (getItems());
    }


    //
    // EnhancedPropertyEditor
    //
    
    /**
     */
    public boolean supportsEditingTaggedValues () {
        return true;
    }


    //
    // itself
    //
    
    /**
     */
    public static String[] getItems () {
        if ( items == null ) {
            String[] engs = (String[]) TreeUtilities.getSupportedEncodings().toArray (new String[0]);
            items = new String[engs.length + 1];
            
            items[0] = DEFAULT_NULL;
            for (int i = 0; i < engs.length; i++) {
                items[i + 1] = engs[i];
            }
        }
        return items;
    }

}
