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

import org.netbeans.modules.javascript2.editor.model.impl.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.editor.spi.model.FunctionInterceptor;
import java.text.MessageFormat;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.ModelVisitor;
import org.netbeans.modules.javascript2.editor.model.impl.UsageBuilder;
import org.netbeans.modules.javascript2.editor.spi.model.ModelElementFactory;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public final class Model {

    private static final Logger LOGGER = Logger.getLogger(OccurrencesSupport.class.getName());

    private static final Comparator<Map.Entry<String, ? extends JsObject>> PROPERTIES_COMPARATOR = new Comparator<Map.Entry<String, ? extends JsObject>>() {

        @Override
        public int compare(Entry<String, ? extends JsObject> o1, Entry<String, ? extends JsObject> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

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

            ModelElementFactory elementFactory = ModelElementFactoryAccessor.getDefault().createModelElementFactory();
            long startCallingME = System.currentTimeMillis();
            Map<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> calls = visitor.getCallsForProcessing();
            if (calls != null && !calls.isEmpty()) {
                for (Map.Entry<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> entry : calls.entrySet()) {
                    Collection<ModelVisitor.FunctionCall> fncCalls = entry.getValue();
                    if (fncCalls != null && !fncCalls.isEmpty()) {
                        for (ModelVisitor.FunctionCall call : fncCalls) {
                            entry.getKey().intercept(call.getName(),
                                    visitor.getGlobalObject(), elementFactory, call.getArguments());
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

    public void dumpModel(Printer printer) {
        StringBuilder sb = new StringBuilder();
        dumpModel(printer, getGlobalObject(), sb, "", new HashSet<JsObject>()); // NOI18N
        String rest = sb.toString();
        if (!rest.isEmpty()) {
            printer.println(rest);
        }
    }

    private static void dumpModel(Printer printer, JsObject jsObject,
            StringBuilder sb, String ident, Set<JsObject> path) {

        sb.append(jsObject.getName());
        sb.append(" [");
        sb.append("ANONYMOUS: ");
        sb.append(jsObject.isAnonymous());
        sb.append(", DECLARED: ");
        sb.append(jsObject.isDeclared());
        if (jsObject.getDeclarationName() != null) {
            sb.append(" - ").append(jsObject.getDeclarationName().getName());
        }
        if (!jsObject.getModifiers().isEmpty()) {
            sb.append(", MODIFIERS: ");
            for (Modifier m : jsObject.getModifiers()) {
                sb.append(m.toString());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            
        }

        sb.append(", ");
        sb.append(jsObject.getJSKind());
        sb.append("]");

        path.add(jsObject);

        if (jsObject instanceof JsFunction) {
            JsFunction function = ((JsFunction) jsObject);
            if (!function.getReturnTypes().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# RETURN TYPES");

                for (TypeUsage type : function.getReturnTypes()) {
                    newLine(printer, sb, ident);

                    sb.append(type.getType());
                }
            }
            if (!function.getParameters().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# PARAMETERS");

                for (JsObject param : function.getParameters()) {
                    newLine(printer, sb, ident);

                    if (path.contains(param)) {
                        sb.append("CYCLE ").append(param.getFullyQualifiedName()); // NOI18N
                    } else {
                        dumpModel(printer, param, sb, ident + "        ", path);
                    }
                }
            }
        }

        int length = 0;
        for (String str : jsObject.getProperties().keySet()) {
            if (str.length() > length) {
                length = str.length();
            }
        }

        StringBuilder identBuilder = new StringBuilder(ident);
        identBuilder.append(' '); // NOI18N
        for (int i = 0; i < length; i++) {
            identBuilder.append(' '); // NOI18N
        }

        List<Map.Entry<String, ? extends JsObject>> entries =
                new ArrayList<Entry<String, ? extends JsObject>>(jsObject.getProperties().entrySet());
        if (!entries.isEmpty()) {
            newLine(printer, sb, ident);
            sb.append("# PROPERTIES");

            Collections.sort(entries, PROPERTIES_COMPARATOR);
            for (Map.Entry<String, ? extends JsObject> entry : entries) {
                newLine(printer, sb, ident);

                sb.append(entry.getKey());
                for (int i = entry.getKey().length(); i < length; i++) {
                    sb.append(' '); // NOI18N
                }
                sb.append(" : "); // NOI18N
                if (path.contains(entry.getValue())) {
                    sb.append("CYCLE ").append(entry.getValue().getFullyQualifiedName()); // NOI18N
                } else {
                    dumpModel(printer, entry.getValue(), sb, identBuilder.toString(), path);
                }
            }
        }
        path.remove(jsObject);
    }

    private static void newLine(Printer printer, StringBuilder sb, String ident) {
        printer.println(sb.toString());
        sb.setLength(0);
        sb.append(ident);
    }

    public static interface Printer {

        void println(String str);
    }
}
