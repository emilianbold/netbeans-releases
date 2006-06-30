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
