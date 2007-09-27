/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
