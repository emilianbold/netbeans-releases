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

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.javascript2.editor.model.impl.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.editor.spi.model.FunctionInterceptor;
import java.text.MessageFormat;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.model.impl.AnonymousObject;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsWithObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.ModelVisitor;
import org.netbeans.modules.javascript2.editor.model.impl.ParameterObject;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
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

    private static final Comparator<TypeUsage> RETURN_TYPES_COMPARATOR = new Comparator<TypeUsage>() {

        @Override
        public int compare(TypeUsage o1, TypeUsage o2) {
            return o1.getType().compareTo(o2.getType());
        }
    };

    private static final Pattern OBJECT_PATTERN = Pattern.compile(
            "(FUNCTION|OBJECT) (\\S+) \\[ANONYMOUS: (true|false), DECLARED: (true|false)( - (\\S+))?" // NOI18N
            + "(, MODIFIERS: ((PUBLIC|STATIC|PROTECTED|PRIVATE|DEPRECATED|ABSTRACT)" // NOI18N
            + "(, (PUBLIC|STATIC|PROTECTED|PRIVATE|DEPRECATED|ABSTRACT))*))?" // NOI18N
            + ", (FUNCTION|METHOD|CONSTRUCTOR|OBJECT|PROPERTY|VARIABLE|FIELD|FILE|PARAMETER|ANONYMOUS_OBJECT|PROPERTY_GETTER|PROPERTY_SETTER|OBJECT_LITERAL|CATCH_BLOCK)\\]"); // NOI18N

    private static final Pattern RETURN_TYPE_PATTERN = Pattern.compile("(\\S+), RESOLVED: (true|false)");

    private static enum ParsingState {
        RETURN, PARAMETER, PROPERTY
    }

    private final JsParserResult parserResult;

    private final OccurrencesSupport occurrencesSupport;

    private final UsageBuilder usageBuilder;
    
    private ModelVisitor visitor;
    
    /**
     * contains with expression?
     */
    private boolean resolveWithObjects;

    Model(JsParserResult parserResult) {
        this.parserResult = parserResult;
        this.occurrencesSupport = new OccurrencesSupport(this);
        this.usageBuilder = new UsageBuilder();
        this.resolveWithObjects = false;
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
            resolveLocalTypes(visitor.getGlobalObject(), parserResult.getDocumentationHolder());

            ModelElementFactory elementFactory = ModelElementFactoryAccessor.getDefault().createModelElementFactory();
            long startCallingME = System.currentTimeMillis();
            Map<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> calls = visitor.getCallsForProcessing();
            if (calls != null && !calls.isEmpty()) {
                for (Map.Entry<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> entry : calls.entrySet()) {
                    Collection<ModelVisitor.FunctionCall> fncCalls = entry.getValue();
                    if (fncCalls != null && !fncCalls.isEmpty()) {
                        for (ModelVisitor.FunctionCall call : fncCalls) {
                            entry.getKey().intercept(call.getName(),
                                    visitor.getGlobalObject(), call.getScope(), elementFactory, call.getArguments());
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(MessageFormat.format("Building model took {0}ms. Resolving types took {1}ms. Extending model took {2}", new Object[]{(end - start), (startCallingME - startResolve), (end - startCallingME)}));
            }
        } else if (resolveWithObjects) {
            long start = System.currentTimeMillis();
            resolveWithObjects = false;
            JsIndex jsIndex = JsIndex.get(parserResult.getSnapshot().getSource().getFileObject());
            processWithObjectIn(visitor.getGlobalObject(), jsIndex);
            long end = System.currentTimeMillis();
            System.out.println("resolving with took: " + (end - start));
        }
        return visitor;
    }

    private void processWithObjectIn(JsObject where, JsIndex jsIndex) {
        if (where.getProperties().isEmpty()) {
            return;
        }
        List<JsObject> properties = new ArrayList(where.getProperties().values());
        for (JsObject property : properties) {
            if (property instanceof JsWith) {
                processWithObject((JsWith)property, jsIndex, null);
            } else {
                processWithObjectIn(property, jsIndex);
            }
        }
    }
    
    private void processWithObject(JsWith with, JsIndex jsIndex, List<String> outerExpression) {
        Collection<TypeUsage> withTypes = with.getTypes();
        withTypes.clear();
        Collection<TypeUsage> resolveTypeFromExpression = new ArrayList<TypeUsage>();
        int offset = ((JsWithObjectImpl)with).getExpressionRange().getEnd();
        List<String> ech = ModelUtils.resolveExpressionChain(parserResult.getSnapshot(), offset, false);
        List<String> originalExp = new ArrayList<String>(ech);
        JsObject fromType = null;
        if (outerExpression == null) {
            outerExpression = ech;
            resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, ech, offset));
            resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, parserResult);
            withTypes.addAll(resolveTypeFromExpression);
        } else {
            ech.addAll(outerExpression);
            boolean resolved = false;
            resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, ech, offset));
            resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, parserResult);
            for(TypeUsage type : resolveTypeFromExpression) {
                fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
                if (fromType != null) {
                    resolved = true;
                    outerExpression = ech;
                    withTypes.add(type);
                    break;
                }
            }
            if (!resolved) {
                resolveTypeFromExpression.clear();
                resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(this, jsIndex, originalExp, offset));
                resolveTypeFromExpression = ModelUtils.resolveTypes(resolveTypeFromExpression, parserResult);
                for (TypeUsage type : resolveTypeFromExpression) {
                    fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
                    if (fromType != null) {
                        resolved = true;
                        outerExpression = originalExp;
                        withTypes.add(type);
                        break;
                    }
                }
            }
        }
        
        
        for (JsWith innerWith : with.getInnerWiths()) {
            processWithObject(innerWith, jsIndex, outerExpression);
        }

        for (TypeUsage type : resolveTypeFromExpression) {
            fromType = ModelUtils.findJsObjectByName(visitor.getGlobalObject(), type.getType());
            if (fromType != null) {
                processWithExpressionOccurrences(fromType, ((JsWithObjectImpl)with).getExpressionRange(), originalExp);
                Collection<TypeUsage> assignments = ModelUtils.resolveTypes(fromType.getAssignments(), parserResult);
                for (TypeUsage assignment : assignments) {
                    Collection<IndexedElement> properties = jsIndex.getProperties(assignment.getType());
                    for (IndexedElement indexedElement : properties) {
                        JsObject jsWithProperty = with.getProperty(indexedElement.getName());
                        if (jsWithProperty != null) {
                            moveProperty(fromType, jsWithProperty);
                        }
                    }
                }
                
                for (JsObject fromTypeProperty : fromType.getProperties().values()) {
                    JsObject jsWithProperty = with.getProperty(fromTypeProperty.getName());
                    if (jsWithProperty != null) {
                        moveProperty(fromType, jsWithProperty);
                    }
                }
            } else {
                Collection<IndexedElement> properties = jsIndex.getProperties(type.getType());
                if (!properties.isEmpty()) {
                    StringBuilder fqn = new StringBuilder();
                    for (int i =outerExpression.size() - 1; i > -1; i--) {
                        fqn.append(outerExpression.get(--i));
                        fqn.append('.');
                    }
                    if (fqn.length() > 0) {
                        DeclarationScope ds = ModelUtils.getDeclarationScope(with);
                        JsObject fromExpression = ModelUtils.findJsObjectByName((JsObject)ds, fqn.toString());
                        if (fromExpression != null) {
                            for (IndexedElement indexedElement : properties) {
                                JsObject jsWithProperty = with.getProperty(indexedElement.getName());
                                    if (jsWithProperty != null) {
                                        moveProperty(fromExpression, jsWithProperty);
                                    }
                            }
                            processWithExpressionOccurrences(fromExpression, ((JsWithObjectImpl)with).getExpressionRange(), originalExp);
                        }
                    }
                        
                }
            }
        }
        
//        for (TypeUsage typeUsage : withTypes) {
//            for (TypeUsage rType : ModelUtils.resolveTypeFromSemiType(with, typeUsage)) {
//                String type = rType.getType();
//                if (type.startsWith("@exp;")) {
//                    type = type.substring(5);
//                }
//                if (type.contains("@pro;")) {
//                    type = type.replace("@pro;", ".");
//                }
//                JsObject fromType = ModelUtils.findJsObjectByName((JsObject)ModelUtils.getDeclarationScope(with), type);
//                if (fromType != null) {
//                    Collection<TypeUsage> assignments = ModelUtils.resolveTypes(fromType.getAssignments(), parserResult);
//                    for (TypeUsage assignment : assignments) {
//                        Collection<IndexedElement> properties = jsIndex.getProperties(assignment.getType());
//                        for (IndexedElement indexedElement : properties) {
//                            JsObject jsWithProperty = with.getProperty(indexedElement.getName());
//                            if (jsWithProperty != null) {
//                                moveProperty(fromType, jsWithProperty);
//                            }
//                        }
//                    }
//                    
//                    for (TypeUsage typeFE : ModelUtils.resolveTypes(withTypes, parserResult)) {
//                        Collection<IndexedElement> properties = jsIndex.getProperties(typeFE.getType());
//                        for (IndexedElement indexedElement : properties) {
//                            JsObject jsWithProperty = with.getProperty(indexedElement.getName());
//                            if (jsWithProperty != null) {
//                                moveProperty(fromType, jsWithProperty);
//                            }
//                        }
//                    }
//                    
//                    String typeName = rType.getType();
//                    if (!typeName.startsWith("@")) {
//                        typeName = "@exp;" + typeName;
//                    } else if (typeName.startsWith("@var")) {
//                        typeName = "@exp;" + typeName.substring(5);
//                    }
//                    List<String> exp = ModelUtils.expressionFromType(new TypeUsageImpl(typeName));
//                    Collection<TypeUsage> resolveTypeFromExpression2 = ModelUtils.resolveTypeFromExpression(this, jsIndex, exp, typeUsage.getOffset());
//
//                    for (TypeUsage typeFE : resolveTypeFromExpression2) {
//                        Collection<IndexedElement> properties = jsIndex.getProperties(typeFE.getType());
//                        for (IndexedElement indexedElement : properties) {
//                            JsObject jsWithProperty = with.getProperty(indexedElement.getName());
//                            if (jsWithProperty != null) {
//                                moveProperty(fromType, jsWithProperty);
//                            }
//                        }
//                    }
//                }
//
//            }
//            
//        }
            
        boolean hasOuter = with.getOuterWith() != null;
        Collection<? extends JsObject> variables = ModelUtils.getVariables(ModelUtils.getDeclarationScope(with));
        List<JsObject> withProperties = new ArrayList(with.getProperties().values());
        for (JsObject jsWithProperty : withProperties) {
            if (!(jsWithProperty instanceof JsWith)) {
                String name = jsWithProperty.getName();
                boolean moved = false;
                if (hasOuter) {
                    // move the property for one level up
                    moveProperty(with.getOuterWith(), jsWithProperty);
                    moved = true;
                } else {
                    for (JsObject variable : variables) {
                        if (variable.getName().equals(name)) {
                            moveProperty(variable.getParent(), jsWithProperty);
                            moved = true;
                            break;
                        }
                    }
                }
                if (!moved) {
                    // move the property to the global space
                    moveProperty(visitor.getGlobalObject(), jsWithProperty);
                }
            }
        }
    }
    
    private void processWithExpressionOccurrences(JsObject jsObject, OffsetRange expRange, List<String> expression) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot(), expRange.getEnd());
        if (ts == null) {
            return;
        }
        ts.move(expRange.getEnd());
        if(!ts.movePrevious()) {
            return;
        }
        Token<? extends JsTokenId> token = ts.token();
        JsObject parent = jsObject.getParent();
        for (int i = 0; i < expression.size() - 1; i++) {
            String name = expression.get(i++);
            while ((token.id() != JsTokenId.IDENTIFIER || !(token.id() == JsTokenId.IDENTIFIER && token.text().toString().equals(name))) && ts.offset() > expRange.getStart() && ts.movePrevious()) {
                token = ts.token();
            }
            if (token.id() == JsTokenId.IDENTIFIER && token.text().toString().equals(name)) {
                JsObject property = parent.getProperty(name);
                if (property != null) {
                    property.addOccurrence(new OffsetRange(ts.offset(), ts.offset() + name.length()));
                }
                parent = parent.getParent();
            }
        }
    }
    
    private void moveProperty (JsObject newParent, JsObject property) {
        JsObject newProperty = newParent.getProperty(property.getName());
        System.out.println("moving property: " + property.getName() + " to " + newParent.getName());
        if (property.getParent() != null) {
            property.getParent().getProperties().remove(property.getName());
        }
        if (newProperty == null) {
            ((JsObjectImpl)property).setParent(newParent);
            newParent.addProperty(property.getName(), property);
        } else {
            JsObjectImpl.moveOccurrenceOfProperties((JsObjectImpl) newProperty, property);
            for (Occurrence occurrence : property.getOccurrences()) {
                newProperty.addOccurrence(occurrence.getOffsetRange());
            }
            // the property needs to be resolved again to handle right occurrences
            resolveLocalTypes(newProperty, parserResult.getDocumentationHolder());
        }
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
                if (!object.isAnonymous()) {
                    result.add(object);
                }
            }
            for (JsObject object : ((JsFunction)scope).getParameters()) {
                result.add(object);
            }
            scope = scope.getParentScope();
        }
        return result;
    }

    private void resolveLocalTypes(JsObject object, JsDocumentationHolder docHolder) {
        if(object instanceof JsFunctionImpl) {
            ((JsFunctionImpl)object).resolveTypes(docHolder);
        } else {
            ((JsObjectImpl)object).resolveTypes(docHolder);
            if (object instanceof JsWith) {
                resolveWithObjects = true;
            }
        }
        ArrayList<JsObject> copy = new ArrayList(object.getProperties().values());
        for(JsObject property: copy) {
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

    public void writeModel(Printer printer) {
        writeObject(printer, getGlobalObject(), null);
    }

    public void writeModel(Printer printer, boolean resolve) {
        writeObject(printer, getGlobalObject(), resolve ? parserResult : null);
    }

    public void writeObject(Printer printer, JsObject object, boolean resolve) {
        writeObject(printer, object, resolve ? parserResult : null);
    }

    public static void writeObject(Printer printer, JsObject object, @NullAllowed JsParserResult parseResult) {
        StringBuilder sb = new StringBuilder();
        writeObject(printer, object, parseResult, sb, "", new HashSet<JsObject>()); // NOI18N
        String rest = sb.toString();
        if (!rest.isEmpty()) {
            printer.println(rest);
        }
    }

    public static Collection<JsObject> readModel(BufferedReader reader, JsObject parent,
            @NullAllowed String sourceLabel) throws IOException {
        String line = null;
        StringBuilder pushback = new StringBuilder();
        List<JsObject> ret = new ArrayList<JsObject>();
        while (pushback.length() > 0 || (line = reader.readLine()) != null) {
            if (pushback.length() > 0) {
                line = pushback.toString();
                pushback.setLength(0);
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            ret.add(readObject(parent, line, 0, reader, pushback, false, sourceLabel));
        }
        return ret;
    }

    private static JsObject readObject(JsObject parent, String firstLine, int indent,
            BufferedReader reader, StringBuilder pushback, boolean parameter, String sourceLabel) throws IOException {

        JsObject object = readObject(parent, firstLine, parameter, sourceLabel);

        ParsingState state = null;
        String line = null;
        StringBuilder innerPushback = new StringBuilder();

        while (innerPushback.length() > 0 || (line = reader.readLine()) != null) {
            if (innerPushback.length() > 0) {
                line = innerPushback.toString();
                innerPushback.setLength(0);
            }

            if (line.length() < indent || !line.substring(0, indent).trim().isEmpty()) {
                pushback.append(line);
                break;
            }

            if ("# RETURN TYPES".equals(line.trim())) { // NOI18N
                state = ParsingState.RETURN;
                continue;
            } else if ("# PARAMETERS".equals(line.trim())) { // NOI18N
                state = ParsingState.PARAMETER;
                continue;
            } else if ("# PROPERTIES".equals(line.trim())) { // NOI18N
                state = ParsingState.PROPERTY;
                continue;
            } else if ("# SEPARATOR".equals(line.trim())) { // NOI18N
                break;
            } else {
                if (state == null) {
                    pushback.append(line);
                    break;
                }
                switch (state) {
                    case RETURN:
                        Matcher matcher = RETURN_TYPE_PATTERN.matcher(line.trim());
                        if (!matcher.matches()) {
                            throw new IOException("Unexpected line: " + line);
                        }
                        ((JsFunctionImpl) object).addReturnType(
                                new TypeUsageImpl(matcher.group(1), -1, Boolean.parseBoolean(matcher.group(2))));
                        break;
                    case PARAMETER:
                        JsObject parameterObject = readObject(object, line.trim(),
                                indent + 8, reader, innerPushback, true, sourceLabel);
                        ((JsFunctionImpl) object).addParameter(parameterObject);
                        break;
                    case PROPERTY:
                        int index = line.indexOf(':');
                        assert index > 0 && index < line.length() : line;

                        String name = line.substring(0, index);
                        String value = line.substring(index + 1);

                        int newIndent = name.length();
                        name = name.trim();
                        JsObject property = readObject(object, value.trim(), newIndent,
                                    reader, innerPushback, false, sourceLabel);
                        object.addProperty(name, property);
                        break;
                    default:
                        throw new IOException("Unexpected line: " + line);
                }
            }
        }
        return object;
    }

    private static JsObject readObject(JsObject parent, String line, boolean parameter, String sourceLabel) throws IOException {
        Matcher m = OBJECT_PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IOException("Malformed line: " + line);
        }

        boolean function = "FUNCTION".equals(m.group(1)); // NOI18N
        String name = m.group(2);
        boolean anonymous = Boolean.valueOf(m.group(3));
        boolean declared = Boolean.valueOf(m.group(4));
        // Decalartion name is not used actually
        //String declarationName = m.group(6);
        String strModifiers = m.group(8);
        // Kind is not used actually
        JsElement.Kind kind = JsElement.Kind.valueOf(m.group(12));
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (modifiers != null) {
            String[] parts = strModifiers.split(", ");
            for (String part : parts) {
                modifiers.add(Modifier.valueOf(part));
            }
        }
        JsObjectImpl ret;
        if (parameter) {
            ret = new ParameterObject(parent, new IdentifierImpl(name, OffsetRange.NONE), null, sourceLabel);
        } else if (function) {
            JsFunctionImpl functionImpl = new JsFunctionImpl(null, parent,
                    new IdentifierImpl(name, OffsetRange.NONE), Collections.<Identifier>emptyList(), OffsetRange.NONE, null, sourceLabel);
            functionImpl.setAnonymous(anonymous);
            ret = functionImpl;
        } else {
            if (anonymous) {
                ret = new AnonymousObject(parent, name, OffsetRange.NONE, null, sourceLabel);
            } else {
                ret = new JsObjectImpl(parent, new IdentifierImpl(name, OffsetRange.NONE),
                    OffsetRange.NONE, null, sourceLabel);
            }
        }

        //System.out.println("===" + declarationName + ":" + ret.getDeclarationName() + " " + name);
        //assert declarationName == null || declarationName.equals(ret.getDeclarationName().getName());
        ret.setJsKind(kind);

        ret.setDeclared(declared);
        ret.getModifiers().clear();
        for (Modifier modifier : modifiers) {
            ret.addModifier(modifier);
        }
        ret.getProperties().clear();
        return ret;
    }

    private static void writeObject(Printer printer, JsObject jsObject, JsParserResult parseResult,
            StringBuilder sb, String ident, Set<JsObject> path) {

        if (jsObject instanceof JsFunction) {
            sb.append("FUNCTION "); // NOI18N
        } else {
            sb.append("OBJECT "); // NOI18N
        }
        sb.append(jsObject.getName());
        sb.append(" ["); // NOI18N
        sb.append("ANONYMOUS: "); // NOI18N
        sb.append(jsObject.isAnonymous());
        sb.append(", DECLARED: "); // NOI18N
        sb.append(jsObject.isDeclared());
        if (jsObject.getDeclarationName() != null) {
            sb.append(" - ").append(jsObject.getDeclarationName().getName());
        }
        if (!jsObject.getModifiers().isEmpty()) {
            sb.append(", MODIFIERS: "); // NOI18N
            for (Modifier m : jsObject.getModifiers()) {
                sb.append(m.toString());
                sb.append(", "); // NOI18N
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append(", "); // NOI18N
        sb.append(jsObject.getJSKind());
        sb.append("]"); // NOI18N

        path.add(jsObject);

        if (jsObject instanceof JsFunction) {
            JsFunction function = ((JsFunction) jsObject);
            if (!function.getReturnTypes().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# RETURN TYPES"); // NOI18N

                Collection<? extends TypeUsage> ret = function.getReturnTypes();
                if (parseResult != null) {
                    ret = ModelUtils.resolveTypes(ret, parseResult);
                }
                List<TypeUsage> returnTypes = new ArrayList<TypeUsage>(ret);
                Collections.sort(returnTypes, RETURN_TYPES_COMPARATOR);
                for (TypeUsage type : returnTypes) {
                    newLine(printer, sb, ident);

                    sb.append(type.getType());
                    sb.append(", RESOLVED: ");
                    sb.append(type.isResolved());
                }
            }
            if (!function.getParameters().isEmpty()) {
                newLine(printer, sb, ident);
                sb.append("# PARAMETERS"); // NOI18N


                for (JsObject param : function.getParameters()) {
                    newLine(printer, sb, ident);

                    if (path.contains(param)) {
                        sb.append("CYCLE ").append(param.getFullyQualifiedName()); // NOI18N
                    } else {
                        writeObject(printer, param, parseResult, sb, ident + "        ", path);
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
            sb.append("# PROPERTIES"); // NOI18N

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
                    writeObject(printer, entry.getValue(), parseResult, sb, identBuilder.toString(), path);
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
