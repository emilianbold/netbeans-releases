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
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.jsf.editor.JsfSupport;

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
                    JsfVariableContext context = new JsfVariableContext(
                            node.logicalStartOffset(),
                            node.logicalEndOffset(),
                            node.getAttribute(VARIABLE_NAME).unquotedValue(),
                            node.getAttribute(VALUE_NAME).unquotedValue());

                    contextsList.add(context);
                }
            }
        }
    }

    public SortedSet<JsfVariableContext> getContexts() {
        return contextsList;
    }

    /** returns most leaf context which contains offset */
    public JsfVariableContext getContext(int offset) {
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


}
