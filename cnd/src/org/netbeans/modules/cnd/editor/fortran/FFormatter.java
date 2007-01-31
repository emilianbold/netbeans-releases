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

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;

import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatWriter;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/** Fortran indentation services */
public class FFormatter extends ExtFormatter {

    public FFormatter(Class kitClass) {
        super(kitClass);
    }

    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof FSyntax);
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new FortranLayer());
    }
    
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
            boolean indentOnly) throws BadLocationException, IOException {
        return null;
    }

    public int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        return 0;
    }

    
    public void shiftLine(BaseDocument doc, int dotPos, boolean right) throws BadLocationException {
        StatusDisplayer.getDefault().setStatusText(
                NbBundle.getBundle(FFormatter.class).getString("MSG_NoFortranShifting")); // NOI18N
    }
    
    public class FortranLayer extends AbstractFormatLayer {

        public FortranLayer() {
            super("fortran-layer"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getBundle(FFormatter.class).getString("MSG_NoFortranReformatting")); // NOI18N
            return null;
        }

        public void format(FormatWriter fw) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getBundle(FFormatter.class).getString("MSG_NoFortranReformatting")); // NOI18N
        }
    } // end class FortranLayer
}
