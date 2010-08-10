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
package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Go to declaration for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELHyperlinkProvider implements HyperlinkProvider {

    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset) {
        final AtomicBoolean ret = new AtomicBoolean(false);
        doc.render(new Runnable() {

            @Override
            public void run() {
                ret.set(getELIdentifierSpan(doc, offset) != null);
            }
        });

        return ret.get();
    }

    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset) {
        final AtomicReference<int[]> ret = new AtomicReference<int[]>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                ret.set(getELIdentifierSpan(doc, offset));
            }
        });
        return ret.get();
    }

    @Override
    public void performClickAction(final Document doc, final int offset) {
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            public void run() {
                performGoTo(doc, offset, cancel);
            }
        }, NbBundle.getMessage(ELHyperlinkProvider.class, "LBL_GoToDeclaration"), cancel, false);
    }
    
    
    private void performGoTo(final Document doc, final int offset, final AtomicBoolean cancel) {
        Source source = Source.create(doc);
        try {
            ParserManager.parse(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator elRi = WebUtils.getResultIterator(resultIterator, ELLanguage.MIME_TYPE);
                    if (cancel.get()) {
                        return;
                    }
                    ELParserResult parserResult = (ELParserResult) elRi.getParserResult();
                    ELElement elElement = parserResult.getElementAt(offset);
                    if (elElement == null) {
                        return;
                    }
                    Node node = elElement.findNodeAt(offset);
                    if (node == null) {
                        return;
                    }
                    Element resolvedElement = ELTypeUtilities.create(parserResult.getFileObject()).resolveElement(elElement, node);
                    if (resolvedElement != null) {
                        ElementOpen.open(ClasspathInfo.create(parserResult.getFileObject()), resolvedElement);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private int[] getELIdentifierSpan(Document doc, int offset) {
        TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(offset);
        if (!tokenSequence.moveNext()) {
            return null; //no token
        }

        //check expression language
        TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(ELTokenId.language());
        if (elTokenSequence == null) {
            return null;
        }

        elTokenSequence.move(offset);
        if (!elTokenSequence.moveNext()) {
            return null; //no token
        }

        if (elTokenSequence.token().id() == ELTokenId.IDENTIFIER) {
            return new int[]{elTokenSequence.offset(), elTokenSequence.offset() + elTokenSequence.token().length()};
        }

        return null;
    }
}
