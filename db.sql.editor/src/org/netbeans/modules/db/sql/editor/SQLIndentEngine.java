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

import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.db.sql.editor.SQLFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.openide.util.NbBundle;

/**
 * Implements an indentation engine for SQL
 *
 * @author Jesse Beaumont
 * @author Andrei Badea
 */
public class SQLIndentEngine extends FormatterIndentEngine {
    
    /** 
     * Creates a new instance of SQLIndentEngine 
     */
    public SQLIndentEngine() {
        setAcceptedMimeTypes(new String[] { SQLEditorKit.MIME_TYPE });
    }

    /**
     * Creates a suitable formatter for handling SQL
     */
    protected ExtFormatter createFormatter() {
        return new SQLFormatter(SQLEditorKit.class);
    }
}
