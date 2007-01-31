/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.fortran;

import java.io.*;
import java.awt.Toolkit; // for beeping

import org.netbeans.editor.Formatter;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.cnd.MIMENames;

import org.openide.util.HelpCtx;

/**
* Fortran indentation engine that delegates to Fortran formatter
*/

// duped from editor/src/org/netbeans/modules/editor/java/JavaIndentEngine.java

public class FIndentEngine extends FormatterIndentEngine {

    //public static final String FORMAT_SPACE_AFTER_COMMA_PROP
    //    = "FormatSpaceAfterComma"; // NOI18N

    //public static final String FREE_FORMAT_PROP
    //    = "FreeFormat"; // NOI18N

    public FIndentEngine() {
        setAcceptedMimeTypes(new String[] { MIMENames.FORTRAN_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
	return (FFormatter)Formatter.getFormatter(FKit.class);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_indent_fortran"); // NOI18N
    }
}

