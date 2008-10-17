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

package org.netbeans.modules.editor.deprecated.pre65formatting;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public final class LegacyFormattersProvider implements MimeDataProvider {

    public LegacyFormattersProvider() {
        // no-op
    }

    // ------------------------------------------------------------------------
    // MimeDataProvider implementation
    // ------------------------------------------------------------------------

    public Lookup getLookup(MimePath mimePath) {
        if (mimePath.size() == 1) {
            IndentReformatTaskFactoriesProvider provider = IndentReformatTaskFactoriesProvider.get(mimePath);
            if (provider != null) {
                IndentTask.Factory legacyIndenter = provider.getIndentTaskFactory();
                ReformatTask.Factory legacyFormatter = provider.getReformatTaskFactory();

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("'" + mimePath.getPath() + "' uses legacyIndenter=" + legacyIndenter
                            + " and legacyFormatter=" + legacyFormatter); //NOI18N
                }

                return Lookups.fixed(legacyIndenter, legacyFormatter);
            }
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // Formatting context manipulation methods
    // ------------------------------------------------------------------------

    public static Document getFormattingContextDocument() {
        Stack<Reference<Document>> stack = FORMATTING_CONTEXT_DOCUMENT.get();
        return stack.isEmpty() ? null : stack.peek().get();
    }

    public static void pushFormattingContextDocument(Document doc) {
        FORMATTING_CONTEXT_DOCUMENT.get().push(new WeakReference<Document>(doc));
    }

    public static void popFormattingContextDocument(Document doc) {
        Stack<Reference<Document>> stack = FORMATTING_CONTEXT_DOCUMENT.get();
        assert !stack.empty() : "Calling popFormattingContextDocument without pushFormattingContextDocument"; //NOI18N

        Reference<Document> ref = stack.pop();
        Document docFromStack = ref.get();
        assert docFromStack == doc : "Popping " + doc + ", but the stack contains " + docFromStack;

        ref.clear();
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(LegacyFormattersProvider.class.getName());
    
    private static ThreadLocal<Stack<Reference<Document>>> FORMATTING_CONTEXT_DOCUMENT = new ThreadLocal<Stack<Reference<Document>>>() {
        @Override
        protected Stack<Reference<Document>> initialValue() {
            return new Stack<Reference<Document>>();
        }
    };

    private static final class IndentReformatTaskFactoriesProvider {

        public static IndentReformatTaskFactoriesProvider get(MimePath mimePath) {
            Reference<IndentReformatTaskFactoriesProvider> ref = cache.get(mimePath);
            IndentReformatTaskFactoriesProvider provider = ref == null ? null : ref.get();
            if (provider == null) {
                try {
                    Class kitClass = KitsTracker.getInstance().findKitClass(mimePath.getPath());
                    Method createFormatterMethod = kitClass.getDeclaredMethod("createFormatter"); //NOI18N
                    provider = new IndentReformatTaskFactoriesProvider(mimePath);
                    cache.put(mimePath, new WeakReference<IndentReformatTaskFactoriesProvider>(provider));
                } catch (Exception e) {
                    // ignore
                }
            }
            return provider;
        }

        public IndentTask.Factory getIndentTaskFactory() {
            if (indentTaskFactory == null) {
                indentTaskFactory = new IndentTask.Factory() {
                    public IndentTask createTask(Context context) {
                        Formatter formatter = getFormatter();
                        if (formatter != null && context.document() instanceof BaseDocument) {
                            return new Indenter(context, formatter);
                        } else {
                            return null;
                        }
                    }
                };
            }
            return indentTaskFactory;
        }

        public ReformatTask.Factory getReformatTaskFactory() {
            if (reformatTaskFactory == null) {
                reformatTaskFactory = new ReformatTask.Factory() {
                    public ReformatTask createTask(Context context) {
                        Formatter formatter = getFormatter();
                        if (formatter != null && context.document() instanceof BaseDocument) {
                            return new Reformatter(context, formatter);
                        } else {
                            return null;
                        }
                    }
                };
            }
            return reformatTaskFactory;
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private static final Map<MimePath, Reference<IndentReformatTaskFactoriesProvider>> cache = new WeakHashMap<MimePath, Reference<IndentReformatTaskFactoriesProvider>>();
        private static final String NO_FORMATTER = new String("NO_FORMATTER"); //NOI18N

        private final MimePath mimePath;

        private IndentTask.Factory indentTaskFactory;
        private ReformatTask.Factory reformatTaskFactory;
        private Object legacyFormatter;

        private IndentReformatTaskFactoriesProvider(MimePath mimePath) {
            this.mimePath = mimePath;
        }

        private Formatter getFormatter() {
            if (legacyFormatter == null) {
                EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
                if (kit != null) {
                    try {
                        Method createFormatterMethod = kit.getClass().getDeclaredMethod("createFormatter"); //NOI18N
                        legacyFormatter = createFormatterMethod.invoke(kit);
                    } catch (Exception e) {
                        legacyFormatter = e;
                    }
                } else {
                    legacyFormatter = NO_FORMATTER;
                }
            }
            return legacyFormatter instanceof Formatter ? (Formatter) legacyFormatter : null;
        }

    } // End of IndentReformatTaskFactoriesProvider class

    private static final class Indenter implements IndentTask {

        private final Context context;
        private final Formatter formatter;

        public Indenter(Context context, Formatter formatter) {
            this.context = context;
            this.formatter = formatter;
        }

        public void reindent() throws BadLocationException {
            Document doc = context.document();
            int startOffset = context.startOffset();
            int endOffset = context.endOffset();
            
            pushFormattingContextDocument(doc);
            try {
                // Original formatter does not have reindentation of multiple lines
                // so reformat start line and continue for each line.
                Element lineRootElem = lineRootElement(doc);
                Position endPos = doc.createPosition(endOffset);
                do {
                    startOffset = formatter.indentLine(doc, startOffset);
                    int startLineIndex = lineRootElem.getElementIndex(startOffset) + 1;
                    if (startLineIndex >= lineRootElem.getElementCount())
                        break;
                    Element lineElem = lineRootElem.getElement(startLineIndex);
                    startOffset = lineElem.getStartOffset(); // Move to next line
                } while (startOffset < endPos.getOffset());
            } finally {
                popFormattingContextDocument(doc);
            }
        }

        public ExtraLock indentLock() {
            return new ExtraLock() {
                public void lock() {
                    formatter.indentLock();
                }

                public void unlock() {
                    formatter.indentUnlock();
                }
            };
        }

        private static Element lineRootElement(Document doc) {
            return (doc instanceof StyledDocument)
                ? ((StyledDocument)doc).getParagraphElement(0).getParentElement()
                : doc.getDefaultRootElement();
        }
    } // End of Indenter class

    private static final class Reformatter implements ReformatTask {

        private final Context context;
        private final Formatter formatter;

        public Reformatter(Context context, Formatter formatter) {
            this.context = context;
            this.formatter = formatter;
        }

        public void reformat() throws BadLocationException {
            pushFormattingContextDocument(context.document());
            try {
                formatter.reformat((BaseDocument) context.document(), context.startOffset(), context.endOffset());
            } finally {
                popFormattingContextDocument(context.document());
            }
        }

        public ExtraLock reformatLock() {
            return new ExtraLock() {
                public void lock() {
                    formatter.reformatLock();
                }

                public void unlock() {
                    formatter.reformatUnlock();
                }
            };
        }
    } // End of Reformatter class

}
