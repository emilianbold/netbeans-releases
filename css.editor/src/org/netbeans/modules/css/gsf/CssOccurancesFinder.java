/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.css.gsf;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author marek
 */
public class CssOccurancesFinder extends OccurrencesFinder {

    private int caretDocumentPosition;
    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> occurances = new HashMap<OffsetRange, ColoringAttributes>();

    @Override
    public void setCaretPosition(int position) {
        caretDocumentPosition = position;
        
        //TODO Add an optimalization which caches the occurances for some
        //document range - typically a token start-end.
        occurances.clear();
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurances;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }


    @Override
    public void run(Result result, SchedulerEvent event) {
        if(cancelled) {
            return ;
        }

        final Snapshot snapshot = result.getSnapshot();
        int astOffset = snapshot.getEmbeddedOffset(caretDocumentPosition);
        if(astOffset == -1) {
            return ;
        }

        SimpleNode root = ((CssParserResult)result).root();
        if(root == null) {
            //broken source
            return ;
        }
        final SimpleNode currentNode = SimpleNodeUtil.findDescendant(root, astOffset);
        if(currentNode == null) {
            return ; //the node may be null at the very end of the document
        }

        //process only some intersting nodes
        switch(currentNode.kind()) {
            case CssParserTreeConstants.JJTHASH:
            case CssParserTreeConstants.JJT_CLASS:
            case CssParserTreeConstants.JJTELEMENTNAME:
            case CssParserTreeConstants.JJTHEXCOLOR:
                break;
            default:
                return ;
        }

        SimpleNodeUtil.visitChildren(root, new NodeVisitor() {

            @Override
            public void visit(SimpleNode node) {
                if(currentNode.kind() == node.kind() && currentNode.image().equals(node.image())) {
                    //something to highlight
                    int docFrom = snapshot.getOriginalOffset(node.startOffset());

                    //virtual class or id handling - the class and id elements inside
                    //html tag's CLASS or ID attribute has the dot or hash prefix just virtual
                    //so if we want to highlight such occurances we need to increment the
                    //start offset by one
                    if(docFrom == -1 && (node.kind() == CssParserTreeConstants.JJT_CLASS || node.kind() == CssParserTreeConstants.JJTHASH )) {
                        docFrom = snapshot.getOriginalOffset(node.startOffset() + 1); //lets try +1 offset
                    }

                    int docTo = snapshot.getOriginalOffset(node.endOffset());

                    if(docFrom == -1 || docTo == -1) {
                        return ; //something is virtual
                    }

                    occurances.put(new OffsetRange(docFrom, docTo), ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        });

    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return null;
    }

}
