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

/**
 *
 * @author  Vladimir Zboril
 * @author  Libor Kramolis
 * @version 0.2
 */
public class StandaloneEditor extends NullChoicePropertyEditor {
 
    /** */
    private static String[] items;


    //
    // init
    //

    /** Creates new StandalonaEditor */
    public StandaloneEditor () {
        super (getItems());        
    }
    

    //
    // itself
    //
    
    /**
     */
    public static String[] getItems () {
        if ( items == null ) {
            items = new String[] { DEFAULT_NULL, "yes", "no" }; // NOI18N
        }
        return items;
    }

}
