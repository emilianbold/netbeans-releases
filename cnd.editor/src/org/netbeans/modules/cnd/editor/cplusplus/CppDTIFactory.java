/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistrations({
    // cnd source files
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.DOXYGEN_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_DOUBLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_SINGLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.PREPROC_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
})
public class CppDTIFactory implements DeletedTextInterceptor.Factory {

    @Override
    public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
        return new DeletedTextInterceptorImpl();
    }

    private static class DeletedTextInterceptorImpl implements DeletedTextInterceptor {
        private CppTypingCompletion.ExtraText rawStringExtraText;

        @Override
        public boolean beforeRemove(final Context context) throws BadLocationException {
            final CppTypingCompletion.ExtraText[] res = new CppTypingCompletion.ExtraText[]{null};
            BaseDocument doc = (BaseDocument) context.getDocument();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    try {
                        res[0] = CppTypingCompletion.checkRawStringRemove(context);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            rawStringExtraText = res[0];
            return false;
        }

        @Override
        public void remove(Context context) throws BadLocationException {
            if (rawStringExtraText != null) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                String extraText = rawStringExtraText.getExtraText();
                if (extraText != null) {
                    doc.remove(rawStringExtraText.getExtraTextPostion(), extraText.length());
                }
            } else {
                if (context.isBackwardDelete()) {
                    BracketCompletion.charBackspaced((BaseDocument) context.getDocument(), context.getOffset() - 1, context.getText().charAt(0));
                }
            }
        }

        @Override
        public void afterRemove(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }
    }
}
