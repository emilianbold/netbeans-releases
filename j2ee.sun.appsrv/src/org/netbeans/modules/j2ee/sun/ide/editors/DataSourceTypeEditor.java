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

import org.netbeans.modules.j2ee.sun.ide.editors.*;

public class DataSourceTypeEditor extends BooleanEditor {

    public String[] choices = { 
        "javax.sql.DataSource",    //NOI18N
        "javax.sql.XADataSource"   //NOI18N
    };
    
    public DataSourceTypeEditor() {
	curr_Sel = null;
    }
    
    public String[] getTags () {
	return choices;
    }
}


