/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.el;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfVariablesModel {

    static boolean inTest = false;

    private static final String VARIABLE_NAME = "var";  //NOI18N
    private static final String VALUE_NAME = "value";  //NOI18N
    private static WeakReference<JsfVariablesModel> lastModelCache;

    public static JsfVariablesModel getModel(HtmlParserResult result) {
        //first try to find out if the cached model can be used for given result
        if (lastModelCache != null) {
            JsfVariablesModel cachedModel = lastModelCache.get();
            if (cachedModel != null && cachedModel.result == result) {
                return cachedModel;
            }
        }

        //create a new model and cache it
        JsfVariablesModel model = new JsfVariablesModel(result);
        lastModelCache = new WeakReference<JsfVariablesModel>(model);

        return model;

    }
    
    private HtmlParserResult result;
    private SortedSet<JsfVariableContext> contextsList;

    private JsfVariablesModel(HtmlParserResult result) {
        this.result = result;
        initModel();
    }

    private void initModel() {
        //1.get all facelets parse trees
        //2.for each of them scan for tags with var and value attrs
        //
        //TODO: possibly fix later - simple implementation:
        // instead of creating a tree of variables
        // contexts so the search by offset is fast, just create a list of
        // contexts and sort it by contexts startoffsets.
        // The access is slower however

        JsfSupport sup = JsfSupport.findFor(result.getSnapshot().getSource());
        Collection<String> faceletsLibsNamespaces = inTest ? null : sup.getFaceletsLibraries().keySet();
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();

        contextsList = new TreeSet<JsfVariableContext>();

        for (String namespace : declaredNamespaces) {
            if (inTest || faceletsLibsNamespaces.contains(namespace)) {
                //ok, seems to be a facelets library
                AstNode root = result.root(namespace);
                //find all nodes with var and value attributes
                List<AstNode> matches = AstNodeUtils.getChildrenRecursivelly(root, new AstNode.NodeFilter() {

                    public boolean accepts(AstNode node) {
                        return node.getAttribute(VALUE_NAME) != null &&
                                node.getAttribute(VARIABLE_NAME) != null;
                    }
                }, false);

                for (AstNode node : matches) {

                    //I need to get the original document context for the value attribute
                    //Since the virtual html source already contains the substituted text (@@@)
                    //instead of the expression language, the code needs to be taken from
                    //the original document
                    AstNode.Attribute valueAttr = node.getAttribute(VALUE_NAME);
                    int doc_from = result.getSnapshot().getOriginalOffset(valueAttr.valueOffset());
                    int doc_to = result.getSnapshot().getOriginalOffset(valueAttr.valueOffset() + valueAttr.value().length());

                    if(doc_from == -1 || doc_to == -1) {
                        continue; //the offsets cannot be mapped to the document
                    }

                    try {
                        String documentValueContent = result.getSnapshot().getSource().getDocument(false).getText(doc_from, doc_to - doc_from);
                        
                        JsfVariableContext context = new JsfVariableContext(
                                node.logicalStartOffset(),
                                node.logicalEndOffset(),
                                node.getAttribute(VARIABLE_NAME).unquotedValue(),
                                unquotedValue(documentValueContent));

                        contextsList.add(context);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
        }
    }

    private String unquotedValue(String value) {
        return isValueQuoted(value) ? value.substring(1, value.length() - 1) : value;
    }

    private boolean isValueQuoted(String value) {
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"') &&
                    (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    public SortedSet<JsfVariableContext> getContexts() {
        return contextsList;
    }

    /** returns most leaf context which contains offset */
    public JsfVariableContext getContainingContext(int offset) {
        JsfVariableContext match = null;
        for(JsfVariableContext c : getContexts()) {
            if(c.getFrom() <= offset && c.getTo() > offset) {
                //we found first context which contains the offset,
                //now find a top most context inside this one.
                match = c;
            }
            if(match != null && c.getTo() < offset) {
                break; //overlapped the last matching element == found the best match
            }
        }
        return match;
    }

     /** returns most leaf context which contains offset */
    public JsfVariableContext getPrecedingContext(int offset) {
        JsfVariableContext match = null;
        for(JsfVariableContext c : getContexts()) {
            if(c.getFrom() < offset) {
                match = c;
            } else {
                break;
            }
        }
        return match;
    }


    /** returns a list of context ancestors. The context's parent is first element in the array,
     * the root is the last one.
     */
    List<JsfVariableContext> getAncestors(JsfVariableContext context, boolean includeItself) {
        SortedSet<JsfVariableContext> head = getContexts().headSet(context);

        JsfVariableContext[] head_array = head.toArray(new JsfVariableContext[]{});
        //scan backward for all elements which contains the given context
        //they will be the ancestors in the direct order
        ArrayList<JsfVariableContext> ancestors = new ArrayList<JsfVariableContext>();
        for(int i = head_array.length - 1; i >= 0; i--) {
            JsfVariableContext c = head_array[i];
            if(c.getTo() > context.getTo()) {
                ancestors.add(c);
            }
        }

        if(includeItself) {
            ancestors.add(0, context);
        }

        return ancestors;
    }


    /** returns a list of all contexts precessding the given context.
     */
    List<JsfVariableContext> getPredecessors(JsfVariableContext context, boolean includeItself) {
        SortedSet<JsfVariableContext> head = getContexts().headSet(context);
        List<JsfVariableContext> pre = new ArrayList<JsfVariableContext>();
        for(JsfVariableContext c : head) {
            pre.add(0, c);
        }

        if(includeItself) {
            pre.add(0, context);
        }

        return pre;
    }

    String resolveVariable(JsfVariableContext context, boolean nestingAware) {

        Expression expr = Expression.parse(context.getVariableValue());
        String resolved = expr.getPostfix() != null ? expr.getPostfix() : "";
        
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(context, false) : getPredecessors(context, false);
        if(ancestors.isEmpty()) {
            //there are no ancestors which can be resolved
            return expr.getCleanExpression();
        }

        List<JsfVariableContext> matching = new ArrayList<JsfVariableContext>();
        //gather matching contexts (those which baseObject fits to ancestor's variable name)
        for(JsfVariableContext c : ancestors) {
            if(c.getVariableName().equals(expr.getBase())) {
                //value = ProductMB.all
                //var = prop
                expr = Expression.parse(c.getVariableValue());
                matching.add(c);
            }
        }

        if(matching.isEmpty()) {
            //nothing to match to
            return expr.getCleanExpression();
        }

        //now resolve the variable using path of the matching contexts
        for(Iterator<JsfVariableContext> itr = matching.iterator() ; itr.hasNext(); ) {
            JsfVariableContext c  = itr.next();
            expr = Expression.parse(c.getVariableValue());
            if(itr.hasNext()) {
                resolved = expr.getPostfix() + "." + resolved;
            } else {
                //last one
                resolved = expr.getCleanExpression() + "." + resolved;
            }
        }

        return resolved;
    }

    public String resolveExpression(String expression, int offset, boolean nestingAware) {
        Expression parsedExpression = Expression.parse(expression);
        JsfVariableContext leaf = nestingAware ? getContainingContext(offset) : getPrecedingContext(offset);
        if(leaf == null) {
            return null; //nothing to resolve
        }
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(leaf, true) : getPredecessors(leaf, true);

        JsfVariableContext match = null;
        //find a context which defines the given variableName
        for(JsfVariableContext c : ancestors) {
            if(c.getVariableName().equals(parsedExpression.getBase())) {
                match = c;
                break;
            }
        }

        if(match == null) {
            return null; //no context matches
        }

        return resolveVariable(match, nestingAware) + (parsedExpression.getPostfix() != null ? "." + parsedExpression.getPostfix() : "");

    }

    //order: the closest var is first
    public List<JsfVariableContext> getAllAvailableVariables(int offset, boolean nestingAware) {
        List<JsfVariableContext> vars = new ArrayList<JsfVariableContext>();
        JsfVariableContext leaf = nestingAware ? getContainingContext(offset) : getPrecedingContext(offset);
        if(leaf == null) {
            return vars;
        }
        List<JsfVariableContext> ancestors = nestingAware ? getAncestors(leaf, true) : getPredecessors(leaf, true);
         for(JsfVariableContext c : ancestors) {
             //store the resolved type
             c.setResolvedType(resolveVariable(c, nestingAware));
             
             vars.add(c);
        }
        return vars;
    }

    private static class Expression {
        
        private String base, postfix, expression;

        /** expression can contain the EL delimiters */
        public static Expression parse(String expression) {
            return new Expression(expression);
        }

        private Expression(String expression) {
            //first strip the EL delimiters
            //strip #{ or ${ && }
            if((expression.charAt(0) == '#' || expression.charAt(0) == '$') && expression.charAt(1) == '{') {
                expression = expression.substring(2);
            }
            if(expression.charAt(expression.length() - 1) == '}') {
                expression = expression.substring(0, expression.length() - 1);
            }

            this.expression = expression;
            
            int dotIndex = expression.indexOf('.');
            base = dotIndex == -1 ? expression : expression.substring(0, dotIndex); //prop
            postfix = dotIndex == -1 ? null : expression.substring(dotIndex + 1); // exclude the dot itself
        }

        /** returns the given expression w/o EL delimiters */
        public String getCleanExpression() {
            return expression;
        }

        public String getBase() {
            return base;
        }

        public String getPostfix() {
            return postfix;
        }

        @Override
        public String toString() {
            return super.toString() + " (base=" + base +", postfix=" + postfix;
        }


    }
}
