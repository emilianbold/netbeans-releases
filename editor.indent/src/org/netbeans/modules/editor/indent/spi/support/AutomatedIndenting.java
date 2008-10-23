/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent.spi.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author vita
 * @since 1.11
 */
public final class AutomatedIndenting {

    /**
     * 
     * @param linePatterns
     * @return
     */
    public static TypedTextInterceptor createHotCharsIndenter(Pattern... linePatterns) {
        return new RegExBasedIndenter(linePatterns);
    }

    /**
     * This is a version of {@link #createHotCharsIndenter(java.util.regex.Pattern[])} method suitable
     * for XML layers registration.
     * 
     * @param fileAttributes The map of <code>FileObject</code> attributes. This method
     *   will recognize any attributes, which name starts with <code>regex</code> and will
     *   try to interpret their value as a regular expression.
     * 
     * @return The result of {@link #createHotCharsIndenter(java.util.regex.Pattern[])} called with
     *   the list of regex patterns recovered from the <code>fileAttributes</code>.
     */
    public static TypedTextInterceptor.Factory createHotCharsIndenter(Map<Object, Object> fileAttributes) {
        final ArrayList<Pattern> linePatterns = new ArrayList<Pattern>();

        for(Object key : fileAttributes.keySet()) {
            if (key.toString().startsWith("regex")) { //NOI18N
                Object value = fileAttributes.get(key);
                try {
                    Pattern pattern = Pattern.compile(value.toString());
                    linePatterns.add(pattern);
                } catch (PatternSyntaxException pse) {
                    LOG.log(Level.WARNING, null, pse);
                }
            }
        }

        return new TypedTextInterceptor.Factory() {
            public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
                return createHotCharsIndenter(linePatterns.toArray(new Pattern [linePatterns.size()]));
            }
        };
    }

    // ------------------------------------------------------------------------
    // private
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AutomatedIndenting.class.getName());
    
    private static final class RegExBasedIndenter implements TypedTextInterceptor {

        private final Pattern [] linePatterns;

        public RegExBasedIndenter(Pattern... linePatterns) {
            this.linePatterns = linePatterns;
        }

        public boolean beforeInsertion(Context context) {
            // no-op
            return false;
        }

        public void textTyped(MutableContext context) {
            // no-op
        }

        public void afterInsertion(Context context) {
            int textLen = context.getText().length();
            if (textLen > 0) {
                CharSequence lineText;
                final int lineStartOffset;
                final int lineEndOffset;

                try {
                    Element lineElement = DocumentUtilities.getParagraphElement(context.getDocument(), context.getOffset());
                    lineText = DocumentUtilities.getText(context.getDocument(),
                        lineElement.getStartOffset(),
                        context.getOffset() - lineElement.getStartOffset() + textLen);
                    lineStartOffset = lineElement.getStartOffset();
                    lineEndOffset = Math.max(lineStartOffset, lineElement.getEndOffset() - 1); // without EOL
                } catch (Exception e) {
                    LOG.log(Level.INFO, null, e);
                    return;
                }

                for(Pattern p : linePatterns) {
                    if (p.matcher(lineText).matches()) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("The line '" + lineText + "' matches '" + p.pattern() //NOI18N
                                + "' -> calling Indent.reindent(" + lineStartOffset + ", " + lineEndOffset + ")"); //NOI18N
                        }

                        final Indent indenter = Indent.get(context.getDocument());
                        indenter.lock();
                        try {
                            runAtomicAsUser(context.getDocument(), new Runnable() {
                                public void run() {
                                    try {
                                        indenter.reindent(lineStartOffset, lineEndOffset);
                                    } catch (BadLocationException ble) {
                                        LOG.log(Level.INFO, null, ble);
                                    }
                                }
                            });
                        } finally {
                            indenter.unlock();
                        }
                        break;
                    }
                }
            }
        }

        public void cancelled(Context context) {
            // no-op
        }

        private static void runAtomicAsUser(Document doc, Runnable run) {
            try {
                Method runAtomicAsUserMethod = doc.getClass().getMethod("runAtomicAsUser", Runnable.class); //NOI18N
                runAtomicAsUserMethod.invoke(doc, run);
            } catch (Exception e) {
                LOG.log(Level.INFO, null, e);
            }
        }
    } // End of RegExBasedIndenter class
}
