/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.editors;

import org.openide.util.NbBundle;

public class IsolationLevelEditorRT extends BooleanEditor {
    
    public String[] choices = { 
            "read-uncommitted",  //NOI18N
            "read-committed",    //NOI18N
            "repeatable-read",   //NOI18N
            "serializable",       //NOI18N
    };  
    
    public IsolationLevelEditorRT() {
	curr_Sel = null;
    }
    
    public String[] getTags () {
	return choices;
    }
    
    public boolean supportsEditingTaggedValues () {
        return false;
    }    
}


