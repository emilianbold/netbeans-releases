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

import org.openide.util.NbBundle;

import org.netbeans.editor.Settings;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.editor.NbEditorDocument;

import org.openide.util.HelpCtx;

/**
 * Options for the Fortran editor kit
 */
public class FOptions extends org.netbeans.modules.editor.options.BaseOptions {
    static final long serialVersionUID = -4433293395349414796L;

    public static final String FORTRAN = "fortran"; // NOI18N

    public FOptions() {
        super (FKit.class, FORTRAN);

	Settings.setValue(FKit.class, NbEditorDocument.FORMATTER, 
			  Formatter.getFormatter(FKit.class));
    }
  
    /** @return localized string */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(FOptions.class).getString(s);
            return (res == null) ? super.getString(s) : res;
        }
        catch (Exception e) {
            return super.getString(s);
        }
    }
  
    /** Return the Fortran Indent Engine class */
    protected Class getDefaultIndentEngineClass() {
        return FIndentEngine.class;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_editor_fortran"); // NOI18N
    }

}
