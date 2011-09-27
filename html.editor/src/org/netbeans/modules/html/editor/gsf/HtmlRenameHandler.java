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
package org.netbeans.modules.html.editor.gsf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
class HtmlRenameHandler implements InstantRenamer {

    public HtmlRenameHandler() {
    }

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (astCaretOffset == -1) {
            return false;
        }

        HtmlParserResult result = (HtmlParserResult) info;
        AstNode node = result.findLeafTag(astCaretOffset, true, true);

        return node != null
                && (node.type() == AstNode.NodeType.OPEN_TAG || node.type() == AstNode.NodeType.ENDTAG);
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (astCaretOffset == -1) {
            return Collections.emptySet();
        }

        HtmlParserResult result = (HtmlParserResult) info;
        AstNode node = result.findLeafTag(astCaretOffset, true, true);

        if (node != null) {
            AstNode pair = node.getMatchingTag();

            if (pair != null) {
                Set<OffsetRange> set = new HashSet<OffsetRange>();
                AstNode open, close;
                switch (node.type()) {
                    case OPEN_TAG:
                        open = node;
                        close = pair;
                        break;
                    case ENDTAG:
                        open = pair;
                        close = node;
                        break;
                    default:
                        return Collections.emptySet();
                }

                Snapshot s = info.getSnapshot();
                
                set.add(new OffsetRange(s.getOriginalOffset(open.startOffset() + 1), 
                        s.getOriginalOffset(open.startOffset() + 1 + open.name().length()))); //1 == "<".len
                
                set.add(new OffsetRange(s.getOriginalOffset(close.startOffset() + 2), 
                        s.getOriginalOffset(close.startOffset() + 2 + close.name().length()))); //2 == "</".len

                return set;
            }
        }

        return Collections.emptySet();
    }
}
