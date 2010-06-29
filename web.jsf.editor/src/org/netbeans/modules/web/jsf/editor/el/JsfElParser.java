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
package org.netbeans.modules.web.jsf.editor.el;

import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.ELParser;
import com.sun.el.parser.Node;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class JsfElParser {

    private static final Logger LOGGER = Logger.getLogger(JsfElParser.class.getName());
    private final Document document;
    private final String snapshot;

    private JsfElParser(Document document, String snapshot) {
        this.document = document;
        this.snapshot = snapshot;
    }

    public static JsfElParser create(final Document document) {
        //clone the document's text
        final AtomicReference<BadLocationException> ble = new AtomicReference<BadLocationException>();
        final AtomicReference<String> snap = new AtomicReference<String>();
        document.render(new Runnable() {

            @Override
            public void run() {
                try {
                    snap.set(document.getText(0, document.getLength()));
                } catch (BadLocationException ex) {
                    ble.set(ex);
                }
            }
        });
        if (ble.get() != null) {
            throw new RuntimeException(ble.get());
        }

        String snapshot = snap.get();
        return new JsfElParser(document, snapshot);
    }
    
    /**
     * Parses the given expression and returns the root AST node for it.
     *
     * @param expr the expression to parse.
     * @return the root AST node
     * @throws ELException if the given expression is not valid EL.
     */
    public static Node parse(String expr) {
        return ELParser.parse(expr);
    }

    /**
     * @return
     */
    public ELParserResult parse() {

        FileObject fo = NbEditorUtilities.getFileObject(document);
        ELParserResult result = new ELParserResult(fo);

        String documentMimetype = NbEditorUtilities.getMimeType(document);
        Language lang = Language.find(documentMimetype);

        if (lang == null) {
            return result;
        }

        TokenHierarchy<?> th = TokenHierarchy.get(document);
        TokenSequence<?> topLevel = th.tokenSequence();

        if (topLevel == null) {
            return result;
        }

        topLevel.moveStart();

        while (topLevel.moveNext()) {

            TokenSequence<ELTokenId> elTokenSequence = topLevel.embedded(ELTokenId.language());

            if (elTokenSequence != null) {
                String expression = topLevel.token().text().toString();
                int startOffset = topLevel.offset();
                int endOffset = startOffset + expression.length();
                OffsetRange range = new OffsetRange(startOffset, endOffset);
                try {
                    Node node = parse(expression);
                    result.add(ELElement.valid(node, range, expression));
                } catch (ELException ex) {
                    result.add(ELElement.error(ex, range, expression));
                }
            }
        }

        return result;
    }

    /**
     * Parse the EL expression at the given offset.
     * 
     * @param offset
     * @return
     */
    public ELElement parse(int offset) {
        String documentMimetype = NbEditorUtilities.getMimeType(document);
        Language lang = Language.find(documentMimetype);

        if (lang == null) {
            return null;
        }
        //get the input attributes from the document and use then for the TokenHierarchy creation
        //XXX the input attributes should be got from the document during creation of the snapshot,
        //    moreover they are mutable, so some kind of clone should be created there instead.
        InputAttributes inputAttrs = (InputAttributes) document.getProperty(InputAttributes.class);
        TokenHierarchy<String> hi = TokenHierarchy.create(snapshot, false, lang, null, inputAttrs);

        FileObject fo = NbEditorUtilities.getFileObject(document);
        //find EL token sequence and its superordinate sequence
        TokenSequence<?> ts = hi.tokenSequence();
        TokenSequence<?> last = null;
        for (;;) {
            if (ts == null) {
                break;
            }
            if (ts.language() == ELTokenId.language()) {
                //found EL
                String expression = last.token().text().toString();
                int startOffset = last.offset();
                int endOffset = startOffset + expression.length();
                OffsetRange range = new OffsetRange(startOffset, endOffset);
                try {
                    Node result = parse(expression);
                    return ELElement.valid(result, range, expression);
                } catch (ELException ex) {
                    return ELElement.error(ex, range, expression);
                }
            } else {
                //not el, scan next embedded token sequence
                ts.move(offset);
                if (ts.moveNext() || ts.movePrevious()) {
                    last = ts;
                    ts = ts.embedded();
                } else {
                    //no token, cannot embed
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Gets the AST root of the given node.
     * 
     * @param node
     * @return
     */
    private static Node root(Node node) {
        Node parent = null;
        Node newParent;
        for (;;) {
            newParent = node.jjtGetParent();
            if (newParent == null || newParent.equals(parent)) {
                break;
            }
            parent = newParent;
        }
        return parent != null ? parent : node;
    }

    private static ExpressionBuilder expressionBuilderFor(String expression) {
        ExpressionBuilder result = new ExpressionBuilder(expression, new ELContext() {

            @Override
            public ELResolver getELResolver() {
                return null;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return null;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return null;
            }
        });

        return result;

    }
}
