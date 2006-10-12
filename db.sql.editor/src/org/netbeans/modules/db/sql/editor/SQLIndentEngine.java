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
    
    static final long serialVersionUID = -2095935054411935707L;
    
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
