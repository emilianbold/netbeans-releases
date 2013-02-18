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
package org.netbeans.modules.javascript2.editor.model;

import java.text.MessageFormat;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelExtender;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.ModelVisitor;
import org.netbeans.modules.javascript2.editor.model.impl.UsageBuilder;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public final class Model {

    private static final Logger LOGGER = Logger.getLogger(OccurrencesSupport.class.getName());

    private final JsParserResult parserResult;

    private final OccurrencesSupport occurrencesSupport;

    private final UsageBuilder usageBuilder;
    
    private ModelVisitor visitor;

    Model(JsParserResult parserResult) {
        this.parserResult = parserResult;
        this.occurrencesSupport = new OccurrencesSupport(this);
        this.usageBuilder = new UsageBuilder();
    }

    private synchronized ModelVisitor getModelVisitor() {
        if (visitor == null) {
            long start = System.currentTimeMillis();
            visitor = new ModelVisitor(parserResult);
            FunctionNode root = parserResult.getRoot();
            if (root != null) {
                root.accept(visitor);
            }
            long startResolve = System.currentTimeMillis();
            resolveLocalTypes(getGlobalObject(), parserResult.getDocumentationHolder());
            
            long startCallingME = System.currentTimeMillis();
            ModelExtender modelExt = ModelExtender.getDefault();
            List<MethodCallProcessor> methodCallProcessors = modelExt.getMethodCallProcessors();
            Map<String, Collection<Collection<JsFunctionArgument>>> calls = visitor.getFuncCallsFroProcessing();
            if (calls != null && !calls.isEmpty()) {
                Collection<MethodCallProcessor> processors = ModelExtender.getDefault().getMethodCallProcessors();
                for(MethodCallProcessor mcp: processors) {
                    Collection<Collection<JsFunctionArgument>> fncCalls = calls.get(mcp.getFullyQualifiedMethodName());
                    if (fncCalls != null && !fncCalls.isEmpty()) {
                        for(Collection<JsFunctionArgument> args : fncCalls) {
                            mcp.process(visitor.getGlobalObject(), args);
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(MessageFormat.format("Building model took {0}ms. Resolving types took {1}ms. Extending model took {2}", new Object[]{(end - start), (startCallingME - startResolve), (end - startCallingME)}));
            }
        }
        return visitor;
    }

    public JsObject getGlobalObject() {
        return getModelVisitor().getGlobalObject();
    }

    public OccurrencesSupport getOccurrencesSupport() {
        return occurrencesSupport;
    }

    public Collection<? extends JsObject> getVariables(int offset) {
        List<JsObject> result = new ArrayList<JsObject>();
        DeclarationScope scope = ModelUtils.getDeclarationScope(this, offset);
        while (scope != null) {
            for (JsObject object : ((JsObject)scope).getProperties().values()) {
                result.add(object);
            }
            for (JsObject object : ((JsFunction)scope).getParameters()) {
                result.add(object);
            }
            scope = scope.getInScope();
        }
        return result;
    }

    private void resolveLocalTypes(JsObject object, JsDocumentationHolder docHolder) {
         if(object instanceof JsFunctionImpl) {
            ((JsFunctionImpl)object).resolveTypes(docHolder);
        } else {
            ((JsObjectImpl)object).resolveTypes(docHolder);
        }
        for(JsObject property: object.getProperties().values()) {
            resolveLocalTypes(property, docHolder);
        }
    }

    /**
     * Gets the node name if it has any (case of AccessNode, BinaryNode, VarNode, PropertyNode).
     *
     * @param node examined node for getting its name
     * @return name of the node if it supports it
     */
    public List<Identifier> getNodeName(Node node) {
        return ModelVisitor.getNodeName(node, parserResult);
    }

}
