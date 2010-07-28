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

package org.netbeans.modules.html.parser;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.HtmlAttributes;
import nu.validator.htmlparser.impl.StateSnapshot;
import nu.validator.htmlparser.impl.TreeBuilder;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeFactory;
import org.xml.sax.ErrorHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author marekfukala
 */
public class ReinstatingTreeBuilder extends TreeBuilder<AstNode>{

    private StateSnapshot<AstNode> snapshot;
    private Stack<AstNode> pushedNodes = new Stack<AstNode>();

    String errorOrFatalError; //debugging purposes

    public ReinstatingTreeBuilder(StateSnapshot<AstNode> treeBuilderSnapshot) {
        this.snapshot = treeBuilderSnapshot;

        setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception) throws SAXException {
                //no-op
            }

            public void error(SAXParseException exception) throws SAXException {
                errorOrFatalError = exception.getLocalizedMessage();
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                errorOrFatalError = exception.getLocalizedMessage();
            }
        });
    }

    
    public boolean canFollow(AstNode node, ElementName element) {
        try {
            //reset state
            loadSnapshot(snapshot);
            pushedNodes.clear();
            errorOrFatalError = null;

            //simulate the element occurence in the loaded context
            startTag(element, HtmlAttributes.EMPTY_ATTRIBUTES, false);

            //check if the treebuilder swallowed the element or not
            if(errorOrFatalError != null) {
                return false;
            }

            for(AstNode pushed : pushedNodes) {
                if(pushed.name().equals(element.name)) {
                    return true;
                }
            }

            errorOrFatalError = "No element " + element + " was pushed to the nodes stack.";
            
        } catch (SAXException ex) {
            Logger.getLogger(ReinstatingTreeBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    protected void elementPushed(String string, String string1, AstNode t) throws SAXException {
        System.out.println("+" + string1);
        super.elementPushed(string, string1, t);
        pushedNodes.push(t);
    }

    @Override
    protected AstNode createElement(String string, String string1, HtmlAttributes ha) throws SAXException {
        return AstNodeFactory.shared().createOpenTag(string1, -1, -1, false);
    }

    @Override
    protected AstNode createHtmlElementSetAsRoot(HtmlAttributes ha) throws SAXException {
        return AstNodeFactory.shared().createRootNode();
    }

    @Override
    protected void detachFromParent(AstNode t) throws SAXException {
        t.detachFromParent();
    }

    @Override
    protected boolean hasChildren(AstNode t) throws SAXException {
        return t.children().isEmpty();
    }

    @Override
    protected void appendElement(AstNode t, AstNode t1) throws SAXException {
        //no-op
    }

    @Override
    protected void appendChildrenToNewParent(AstNode t, AstNode t1) throws SAXException {
        //no-op
    }

    @Override
    protected void insertFosterParentedChild(AstNode t, AstNode t1, AstNode t2) throws SAXException {
        //no-op
    }

    @Override
    protected void insertFosterParentedCharacters(char[] chars, int i, int i1, AstNode t, AstNode t1) throws SAXException {
        //no-op
    }

    @Override
    protected void appendCharacters(AstNode t, char[] chars, int i, int i1) throws SAXException {
        //no-op
    }

    @Override
    protected void appendComment(AstNode t, char[] chars, int i, int i1) throws SAXException {
        //no-op
    }

    @Override
    protected void appendCommentToDocument(char[] chars, int i, int i1) throws SAXException {
        //no-op
    }

    @Override
    protected void addAttributesToElement(AstNode t, HtmlAttributes ha) throws SAXException {
        //no-op
    }

}
