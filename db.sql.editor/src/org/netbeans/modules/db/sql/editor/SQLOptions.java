/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;

import java.util.MissingResourceException;
import org.openide.util.HelpCtx;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.NbBundle;

/**
* Options for the sql editor kit
*
* @author Jesse Beaumont based on code by Miloslav Metelka
*/
public class SQLOptions extends BaseOptions {
    
    public static String SQL = "sql"; // NOI18N
    
    private static final String HELP_ID = "editing.editor.sql"; // NOI18N
    
    //no sql specific options at this time
    static final String[] SQL_PROP_NAMES = new String[] {};

    public SQLOptions() {
        super(SQLEditorKit.class, SQL);
    }

    /**
     * Determines the class of the default indentation engine, in this case
     * SQLIndentEngine.class
     */
    protected Class getDefaultIndentEngineClass() {                             
	return SQLIndentEngine.class;                                          
    }

    /**
     * Gets the help ID
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    /**
     * Look up a resource bundle message, if it is not found locally defer to 
     * the super implementation
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(SQLOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }
}
