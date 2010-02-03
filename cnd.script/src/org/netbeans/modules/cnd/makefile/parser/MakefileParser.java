/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.parser;

import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.netbeans.modules.cnd.makefile.model.MakefileAssignment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.makefile.lexer.MakefileTokenId;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;

/**
 * @author Alexey Vladykin
 */
public class MakefileParser extends Parser {

    private final AtomicBoolean cancelled;
    private MakefileParseResult result;

    public MakefileParser() {
        cancelled = new AtomicBoolean();
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        result = null;
        cancelled.set(false);
        result = parse(snapshot, cancelled);
    }

    @Override
    public MakefileParseResult getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // nothing to listen to => no reason to register listeners
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // nothing to listen to => no reason to register listeners
    }


    private static MakefileParseResult parse(Snapshot snapshot, AtomicBoolean cancelled) {
        TokenSequence<MakefileTokenId> tokenSequence =
                snapshot.getTokenHierarchy().tokenSequence(MakefileTokenId.language());

        if (tokenSequence == null) {
            return null;
        }

        FileObject fobj = snapshot.getSource().getFileObject();

        List<AbstractMakefileElement> makefileElements = new ArrayList<AbstractMakefileElement>();

        int startIndex = 0;
        while (tokenSequence.moveNext() && !cancelled.get()) {
            Token<MakefileTokenId> token = tokenSequence.token();

            switch (token.id()) {
                case EQUALS:
                case COLON_EQUALS:
                    tokenSequence.moveIndex(startIndex);
                    makefileElements.add(createAssignment(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case COLON:
                    tokenSequence.moveIndex(startIndex);
                    makefileElements.add(createRule(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case COMMENT:
                case NEW_LINE:
                    startIndex = tokenSequence.index() + 1;
            }
        }

        return cancelled.get()? null : new MakefileParseResult(snapshot, makefileElements);
    }

    private static MakefileAssignment createAssignment(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = -1;
        NAME_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            if (startOffset == -1) {
                startOffset = tokenSequence.offset();
            }
            switch (token.id()) {
                case EQUALS:
                case COLON_EQUALS:
                    break NAME_LOOP;
                default:
                    nameBuilder.append(token.text());
            }
        }

        StringBuilder valueBuilder = new StringBuilder();
        int endOffset = startOffset;
        VALUE_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case COMMENT:
                case NEW_LINE:
                    break VALUE_LOOP;
                default:
                    valueBuilder.append(token.text());
            }
            if (endOffset == -1) {
                endOffset = tokenSequence.offset() + token.length();
            }
        }

        return new MakefileAssignment(nameBuilder.toString().trim(), valueBuilder.toString(), fobj, startOffset, endOffset);
    }

    private static MakefileRule createRule(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = -1;

        List<String> targets = new ArrayList<String>();
        TARGETS_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            if (startOffset == -1) {
                startOffset = tokenSequence.offset();
            }
            switch (token.id()) {
                case COLON:
                    if (0 < nameBuilder.length()) {
                        targets.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break TARGETS_LOOP;
                case WHITESPACE:
                case ESCAPED_NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        targets.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break;
                default:
                    nameBuilder.append(token.text());
            }
        }

        int endOffset = startOffset;
        List<String> prereqs = new ArrayList<String>();
        PREREQS_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case WHITESPACE:
                case ESCAPED_NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        prereqs.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break;
                case COMMENT:
                case NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        prereqs.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break PREREQS_LOOP;
                default:
                    nameBuilder.append(token.text());
            }
            if (endOffset == -1) {
                endOffset = tokenSequence.offset() + token.length();
            }
        }

        return new MakefileRule(targets, prereqs, fobj, startOffset, endOffset);
    }
}
