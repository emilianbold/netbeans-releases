/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.editors;

import org.netbeans.modules.j2ee.sun.ide.editors.*;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

public class IsolationLevelEditor extends BooleanEditor {

    private boolean isRuntime = false;

    public String[] choices = {
            "read-uncommitted",  //NOI18N
            "read-committed",    //NOI18N
            "repeatable-read",   //NOI18N
            "serializable",       //NOI18N
            WizardConstants.__IsolationLevelDefault,     //NOI18N
    }; 
    
    public String[] choicesRuntime = { 
            "read-uncommitted",  //NOI18N
            "read-committed",    //NOI18N
            "repeatable-read",   //NOI18N
            "serializable",       //NOI18N
    };
    
    public IsolationLevelEditor() {
	curr_Sel = null;
    }
     
    public IsolationLevelEditor(boolean isRT) {
	curr_Sel = null;
        this.isRuntime = isRT;
    }
    
    public String[] getTags () {
        if(this.isRuntime)
            return choicesRuntime;
        else
            return choices;
    }
    
    public boolean supportsEditingTaggedValues () {
        return false;
    }    
}


