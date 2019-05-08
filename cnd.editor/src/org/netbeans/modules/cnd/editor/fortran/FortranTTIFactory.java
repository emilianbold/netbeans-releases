/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.fortran;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.fortran.indent.FortranHotCharIndent;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistration(mimeType = MIMENames.FORTRAN_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
public class FortranTTIFactory implements TypedTextInterceptor.Factory {

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
        assert mimePath.getPath().equals(MIMENames.FORTRAN_MIME_TYPE);
        return new TypedTextInterceptorImpl();
    }

    private static class TypedTextInterceptorImpl implements TypedTextInterceptor {
        private int caretPosition;

        public TypedTextInterceptorImpl() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            caretPosition = -1;
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            if (!FortranBracketCompletion.INSTANCE.completionSettingEnabled()) {
                return;
            }
            char insertedChar = context.getText().charAt(0);
            switch(insertedChar) {
                case '(':
                        FortranBracketCompletion.INSTANCE.completeOpeningBracket(context);
                    break;
                case ')':
                    caretPosition = FortranBracketCompletion.INSTANCE.skipClosingBracket(context);
                    break;
            }
        }

        @Override
        public void afterInsert(final Context context) throws BadLocationException {
            final BaseDocument doc = (BaseDocument) context.getDocument();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    int offset = context.getOffset();
                    if (FortranHotCharIndent.INSTANCE.getKeywordBasedReformatBlock(doc, offset, context.getText())) {
                        Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                            indent.reindent(offset);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                            indent.unlock();
                        }
                    } else if (caretPosition != -1) {
                        context.getComponent().setCaretPosition(caretPosition);
                        caretPosition = -1;
                    } else {
                        try {
                            FortranBracketCompletion.INSTANCE.charInserted(doc,
                                    offset, context.getComponent().getCaret(), context.getText().charAt(0));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }

        @Override
        public void cancelled(Context context) {
        }
    }
}
