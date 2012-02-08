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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.lib.properties.GrammarParser;
import org.netbeans.modules.css.lib.properties.GrammarResolver;

/**
 * Represents a resolved css declaration
 *
 * @author mfukala@netbeans.org
 */
public class ResolvedProperty {

    private final GroupGrammarElement groupGrammarElement;
    private final GrammarResolver grammarResolver;
    private static final Pattern FILTER_COMMENTS_PATTERN = Pattern.compile("/\\*.*?\\*/");//NOI18N
    private Node simpleParseTree, fullParseTree;
    private PropertyModel propertyModel;

    public static ResolvedProperty resolve(PropertyModel propertyModel, CharSequence propertyValue) {
        return new ResolvedProperty(propertyModel, propertyValue);
    }
    
    public ResolvedProperty(PropertyModel propertyModel, CharSequence value) {
        this(GrammarParser.parse(propertyModel.getGrammar(), propertyModel.getPropertyName()), filterComments(value));
        this.propertyModel = propertyModel;
    }
   
    //No need to be public - used just by tests!
    //tests only
    //
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public ResolvedProperty(GroupGrammarElement groupGrammarElement, String value) {
        this.groupGrammarElement = groupGrammarElement;
        this.grammarResolver = GrammarResolver.resolve(groupGrammarElement, value);
    }
    
    public ResolvedProperty(String grammar, String value) {
        this(GrammarParser.parse(grammar), value);
    }

    public List<Token> getTokens() {
        return grammarResolver.tokens();
    }
    
    public List<ResolvedToken> getResolvedTokens() {
        return grammarResolver.resolved();
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * Return an instance of {@link PropertyModel}.
     */
    public PropertyModel getPropertyModel() {
        return propertyModel;
    }
    
    /**
     * @return true if the property value fully corresponds to the property grammar.
     */
    public boolean isResolved() {
        return grammarResolver.success();
    }
    
    /**
     * @return list of unresolved property value tokens. It means these tokens cannot
     * be consumed by the property grammar.
     * 
     * Currently used just by the CssAnalyzer - error checking of the property values
     */
    public List<Token> getUnresolvedTokens() {
        return grammarResolver.left();
    }
    
    /**
     * @return set of alternatives which may follow the end of the property value.
     * The values are computed according to the grammar and the existing property value
     */
    public Set<ValueGrammarElement> getAlternatives() {
        return grammarResolver.getAlternatives();
    }

    /**
     * @return a parse tree for the property value. 
     * The parse tree contains only named nodes (references).
     * 
     * In most cases clients will use this method.
     */    
    public synchronized Node getParseTree() {
        if(simpleParseTree == null) {
            simpleParseTree = generateParseTree(false);
        }
        return simpleParseTree;
    }
    
    /**
     * @return a parse tree for the property value. 
     * The parse tree contains also the anonymous group nodes.
     * If false the parse tree contains only named nodes (references)
     * 
     * Possibly remove from the API later since {@link #getParseTree()} 
     * is the preferred way for clients wishing to work with the parse tree.
     * 
     * Mostly used for debugging and tests.
     */
    @Deprecated
    public synchronized Node getFullParseTree() {
        if(fullParseTree == null) {
            fullParseTree = generateParseTree(true);
        }
        return fullParseTree;
    }

    //--------- private -----------
    
    /**
     * @return a text with all css comments replaced by spaces
     */
    private static String filterComments(CharSequence text) {
        Matcher m = FILTER_COMMENTS_PATTERN.matcher(text);
        StringBuilder b = new StringBuilder(text);
        while (m.find()) {
            int from = m.start();
            int to = m.end();
            if (from != to) {
                char[] spaces = new char[to - from];
                Arrays.fill(spaces, ' ');
                String replacement = new String(spaces);
                b.replace(from, to, replacement);
            }
        }
        return b.toString();
    }

    /**
     * @param fullParseTree - if true then the parse tree contains also the anonymous
     * group nodes. If false the parse tree contains only named nodes (references)
     */
    private Node generateParseTree(final boolean fullParseTree) {
        Node.GroupNode root = new Node.GroupNode(groupGrammarElement) {

            @Override
            public String toString() {
                return fullParseTree ? super.toString() : group.getName();
            }
            
        };
        
        for(ResolvedToken token : getResolvedTokens()) {
            Node.GroupNode current = root; //each path starts with the root element
            List<GrammarElement> path = token.getGrammarElement().elementsPath();
            //create group nodes for the elements excluding the root node and the value node itself
            for(GrammarElement element : path.subList(1, path.size() - 1)) {
                GroupGrammarElement groupElement = (GroupGrammarElement)element;
                if(!fullParseTree) {
                    if(groupElement.getName() == null) {
                        //referred element == null so skip the anonymous element
                        continue; 
                    }
                }
                
                Node.GroupNode newGroupNode = new Node.GroupNode(groupElement) {

                    @Override
                    public String toString() {
                        return fullParseTree ? super.toString() : group.getName();
                    }
                    
                };
                Node.GroupNode child = current.addChild(newGroupNode); //either returns the given node or an existing one equal to it.
                current = child;
            }
            current.addChild(new Node.ResolvedTokenNode(token)); //add the leaf node for the resolved token itself
        }
        
        return root;
    }

}
