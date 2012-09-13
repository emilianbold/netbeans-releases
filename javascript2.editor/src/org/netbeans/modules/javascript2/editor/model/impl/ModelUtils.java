/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.*;
import com.oracle.nashorn.parser.TokenType;
import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.jquery.JQueryModel;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.JsElement.Kind;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {
      
    public static JsObjectImpl getJsObject (ModelBuilder builder, List<Identifier> fqName, boolean isLHS) {
        JsObject result = builder.getCurrentObject();
        JsObject tmpObject = null;
        String firstName = fqName.get(0).getName();
        
        while (tmpObject == null && result.getParent() != null) {
            if (result instanceof JsFunctionImpl) {
                tmpObject = ((JsFunctionImpl)result).getParameter(firstName);
            }
            if (tmpObject == null) {
                if (result.getProperty(firstName) != null) {
                    tmpObject = result;
                }
                result = result.getParent();
            } else {
                result = tmpObject;
            }
        }
        if (tmpObject == null) {
            tmpObject = builder.getGlobal();
        }
        for (int index = (tmpObject instanceof ParameterObject ? 1 : 0); index < fqName.size() ; index++) {
            Identifier name = fqName.get(index);
            result = tmpObject.getProperty(name.getName());
            if (result == null) {
                result = new JsObjectImpl(tmpObject, name, name.getOffsetRange(), (index < (fqName.size() - 1)) ? false : isLHS );
                tmpObject.addProperty(name.getName(), result);
            }
            tmpObject = result;
        }
        return (JsObjectImpl)result;        
    }

    public static boolean isGlobal(JsObject object) {
        return object.getJSKind() == JsElement.Kind.FILE;
    }

    public static JsObject findJsObject(Model model, int offset) {
        JsObject result = null;
        JsObject global = model.getGlobalObject();
        result = findJsObject(global, offset);
        if (result == null) {
            result = global;
        }
        return result;
    }
    
    public static JsObject findJsObject(JsObject object, int offset) {
        JsObjectImpl jsObject = (JsObjectImpl)object;
        JsObject result = null;
        JsObject tmpObject = null;
        if (jsObject.getOffsetRange().containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT || kind == JsElement.Kind.ANONYMOUS_OBJECT
                        || kind == JsElement.Kind.FUNCTION || kind == JsElement.Kind.METHOD || kind == JsElement.Kind.CONSTRUCTOR) {
                    tmpObject = findJsObject(property, offset);
                }
                if (tmpObject != null) {
                    result = tmpObject;
                    break;
                }
            }
        }
        return result;
    }
    
    public static JsObject findJsObjectByName(JsObject global, String fqName) {
        JsObject result = global;
        for (StringTokenizer stringTokenizer = new StringTokenizer(fqName, "."); stringTokenizer.hasMoreTokens() && result != null;) {
            String token = stringTokenizer.nextToken();
            result = result.getProperty(token);
            if (result == null) {
                break;
            }
        }
        return result;
    }
    
    public static JsObject findJsObjectByName(Model model, String fqName) {
        return findJsObjectByName(model.getGlobalObject(), fqName);
    }
    
    public static JsObject getGlobalObject(JsObject jsObject) {
        JsObject result = jsObject;
        while(result.getParent() != null) {
            result = result.getParent();
        }
        return result;
    }
    
    public static DeclarationScope getDeclarationScope(Model model, int offset) {
        DeclarationScope result = null;
        JsObject global = model.getGlobalObject();
        result = getDeclarationScope((DeclarationScope)global, offset);
        if (result == null) {
            result = (DeclarationScope)global;
        }
        return result;
    }
    
    private static DeclarationScope getDeclarationScope(DeclarationScope scope, int offset) {
        DeclarationScopeImpl dScope = (DeclarationScopeImpl)scope;
        DeclarationScope result = null;
        DeclarationScope function = null;
        if (dScope.getOffsetRange().containsInclusive(offset)) {
            result = dScope;
            for (DeclarationScope innerScope : dScope.getDeclarationsScope()) {
                function = getDeclarationScope(innerScope, offset);
                if (function != null) {
                    result = function;
                    break;
                }
            }
        }
        return result;
    }
    
    public static String createFQN(JsObject object) {
        StringBuilder result = new StringBuilder();
        result.append(object.getName());
        JsObject parent = object;
        if (object.getParent() == null) {
            return object.getName();
        }
        while((parent = parent.getParent()).getParent() != null) {
            result.insert(0, ".");
            result.insert(0, parent.getName());
        }
        return result.toString();
    }
    
    public static OffsetRange documentOffsetRange(JsParserResult result, int start, int end) {
        int lStart = LexUtilities.getLexerOffset(result, start);
        int lEnd = LexUtilities.getLexerOffset(result, end);
        if (lStart == -1 || lEnd == -1) {
            return OffsetRange.NONE;
        }
        if (lEnd < lStart) {
            // TODO this is a workaround for bug in nashorn, when sometime the start and end are not crorrect
            int length = lStart - lEnd;
            lEnd = lStart + length;
        }
        return new OffsetRange(lStart, lEnd);
    }
    
    
    private static final Collection<JsTokenId> CTX_DELIMITERS = Arrays.asList(
            JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY,
            JsTokenId.OPERATOR_SEMICOLON);
    
    private enum State {
        INIT
    }
    
    private static String getSemiType(TokenSequence<JsTokenId> ts, int offset) {
        
        String result = "UNKNOWN";
        ts.move(offset);
        if (!ts.moveNext()) {
           return result;
        }
    
        State state = State.INIT;
        while (ts.movePrevious()) {
            Token<JsTokenId> token = ts.token();
            if (!CTX_DELIMITERS.contains(token.id())){
                switch (state) {
                    case INIT:
                        if (token.id() == JsTokenId.IDENTIFIER)
                        break;
                }
            }
        }
        return result;
    }
    
    public static Collection<TypeUsage> resolveSemiTypeOfExpression(Node expression) {
        SemiTypeResolverVisitor visitor = new SemiTypeResolverVisitor();
        if (expression != null) {
            if (expression instanceof BinaryNode) {
                expression = ((BinaryNode)expression).lhs();
            }
            expression.accept(visitor);
            return visitor.getSemiTypes();
        }
        return new HashSet<TypeUsage>();
    }
    
    public static Collection<TypeUsage> resolveTypeFromSemiType(JsObject object, TypeUsage uType) {
        Set<TypeUsage> result = new HashSet<TypeUsage>();
        TypeUsageImpl type = (TypeUsageImpl)uType;
        if (type.isResolved()) {
            result.add(type);
        } else if (Type.UNDEFINED.equals(type.getType())) {
            if (object.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
                result.add(new TypeUsageImpl(ModelUtils.createFQN(object), type.getOffset(), true));
            } else {
                result.add(new TypeUsageImpl(Type.UNDEFINED, type.getOffset(), true));
            }
        } else if ("@this".equals(type.getType())) { //NOI18N
            JsObject parent = null;
            if (object.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
                parent = object;
            } else {
                parent = object.getParent();
            } 
            if (parent.getJSKind() == JsElement.Kind.FUNCTION || parent.getJSKind() == JsElement.Kind.METHOD) {
                if (parent.getParent().getJSKind() == JsElement.Kind.FILE) {
                    result.add(new TypeUsageImpl("@global", 0, true)); //NOI18N
                } else {
                    JsObject grandParent = parent.getParent();
                    if ( grandParent != null && grandParent.getJSKind() == JsElement.Kind.OBJECT_LITERAL) {
                        result.add(new TypeUsageImpl(ModelUtils.createFQN(grandParent), type.getOffset(), true));
                    } else {
                        result.add(new TypeUsageImpl(ModelUtils.createFQN(parent), type.getOffset(), true));
                    }
                }
            } else {
                result.add(new TypeUsageImpl(ModelUtils.createFQN(parent), type.getOffset(), true));
            }
        } else if (type.getType().startsWith("@this.")) {
            Identifier objectName = object.getDeclarationName();
            if (objectName != null && object.getOffsetRange().getEnd() == objectName.getOffsetRange().getEnd()) {
                // the assignment is during declaration
                String pName = type.getType().substring(type.getType().indexOf('.') + 1);
                JsObject property = object.getParent().getProperty(pName);
                if (property != null && property.getJSKind().isFunction()) {
                    JsFunctionImpl function = property instanceof JsFunctionImpl
                            ? (JsFunctionImpl) property
                            : ((JsFunctionReference)property).getOriginal();
                    object.getParent().addProperty(object.getName(), new JsFunctionReference(object.getParent(), object.getDeclarationName(), function, true));
                }
            }
        } else if (type.getType().startsWith("@new;")) {
            String function = type.getType().substring(5);
            JsObject possible = null;
            JsObject parent = object;
            while (possible == null && parent != null) {
                possible = parent.getProperty(function);
                parent = parent.getParent();
            }
            if (possible != null) {
//                if (possible instanceof JsFunction) {
//                    result.addAll(((JsFunction)possible).getReturnTypes());
//                } else {
                    result.add(new TypeUsageImpl(ModelUtils.createFQN(possible), possible.getOffset(), true));
//                }
            } else {
                result.add(type);
            }
        } else if (type.getType().startsWith("@call;")) {
            String functionName = type.getType().substring(6);
            JsObject globalObject = ModelUtils.getGlobalObject(object);
            JsObject function = globalObject.getProperty(functionName);
            if (function != null && function instanceof JsFunction) {
                result.addAll(((JsFunction)function).getReturnTypes());
            }
        } else if(type.getType().startsWith("@anonym;")){
            int start = Integer.parseInt(type.getType().substring(8));
//            JsObject globalObject = ModelUtils.getGlobalObject(object);
            JsObject byOffset = ModelUtils.findJsObject(object, start);
            if(byOffset != null && byOffset.isAnonymous()) {
                result.add(new TypeUsageImpl(ModelUtils.createFQN(byOffset), byOffset.getOffset(), true));
            }
//            for(JsObject children : globalObject.getProperties().values()) {
//                if(children.getOffset() == start && children.getName().startsWith("Anonym$")) {
//                    result.add(new TypeUsageImpl(ModelUtils.createFQN(children), children.getOffset(), true));
//                    break;
//                }
//                
//            }
        } else if(type.getType().startsWith("@var;")){
            String name = type.getType().substring(5);
            JsObject parent = object.getParent();
            if(parent != null && parent.getJSKind().isFunction()) {
                Collection<? extends JsObject> parameters = ((JsFunction)parent).getParameters();
                for (JsObject parameter : parameters) {
                    if(name.equals(parameter.getName())) {
                        Collection<? extends TypeUsage> assignments = parameter.getAssignmentForOffset(parameter.getOffset());
                        result.addAll(assignments);
                        break;
                    }
                }
            } else {
                result.add(new TypeUsageImpl(name, type.getOffset(), false));
            }
        } else {
            result.add(type);
        }
        return result;
    }
    
    public static Collection<TypeUsage> resolveTypeFromExpression (Model model, JsIndex jsIndex, List<String> exp, int offset) {
        List<JsObject> localObjects = new ArrayList<JsObject>();
        List<JsObject> lastResolvedObjects = new ArrayList<JsObject>();
        List<TypeUsage> lastResolvedTypes = new ArrayList<TypeUsage>();
        
        
            for (int i = exp.size() - 1; i > -1; i--) {
                String kind = exp.get(i);
                String name = exp.get(--i);
                if (i == (exp.size() - 2)) {
                    JsObject localObject = null;
                    // resolving the first part of expression
                    // find possible variables from local context, index contains only 
                    // public definition, we are interested in the private here as well
                    for (JsObject object : model.getVariables(offset)) {
                        if (object.getName().equals(name)) {
                            localObjects.add(object);
                            localObject = object;
                            break;
                        }
                    }

                    for (JsObject libGlobal : getLibrariesGlobalObjects()) {
                        for (JsObject object : libGlobal.getProperties().values()) {
                            if (object.getName().equals(name)) {
                                //localObjects.add(object);
                                lastResolvedTypes.add(new TypeUsageImpl(object.getName(), -1, true));
                                break;
                            }
                        }
                    }
                    if(localObject == null || (localObject.getJSKind() != JsElement.Kind.PARAMETER
                            && localObject.getJSKind() != JsElement.Kind.VARIABLE)) {
                        // Add global variables from index
                        Collection<IndexedElement> globalVars = jsIndex.getGlobalVar(name);
                        for (IndexedElement globalVar : globalVars) {
                            Collection<TypeUsage> assignments = globalVar.getAssignments();
                            if (assignments.isEmpty()) {
                                lastResolvedTypes.add(new TypeUsageImpl(name, -1, true));
                            } else {
                                lastResolvedTypes.addAll(assignments);
                            }
                        }
                    }
                    
                    if(!localObjects.isEmpty()){
                        for(JsObject lObject : localObjects) {
                            if(lObject.getAssignmentForOffset(offset).isEmpty()) {
                                boolean addAsType = lObject.getJSKind() == JsElement.Kind.OBJECT_LITERAL;
                                if (lObject instanceof JsObjectReference) {
                                    // translate reference objects to the original objects / type
                                    name = ((JsObjectReference)lObject).getOriginal().getDeclarationName().getName();
                                }
                                if(addAsType) {
                                    // here it doesn't have to be real type, it's possible that it's just an object name
                                    lastResolvedTypes.add(new TypeUsageImpl(name, -1, true));
                                }
                            }
                            if ("@mtd".equals(kind)) {  //NOI18N
                                if (lObject.getJSKind().isFunction()) {
                                    // if it's a method call, add all retuturn types
                                    lastResolvedTypes.addAll(((JsFunction) lObject).getReturnTypes());
                                }
                            } else {
                                // just property
                                Collection<? extends Type> lastTypeAssignment = lObject.getAssignmentForOffset(offset);
                                if (lastTypeAssignment.isEmpty()) {
                                    // no assignments for the local object, we need to process the object later.
                                    lastResolvedObjects.add(lObject);
                                } else {
                                    // go through the assignments and find the last object / type in the assignment chain
                                    // it solve assignements like a = b; b = c; c = d;. the result for a should be d.
                                    resolveAssignments(model, lObject, offset, lastResolvedObjects, lastResolvedTypes);
                                    break;
                                }
                            }
                            
                        }
                    } 
                    // now we should have collected possible local objects
                    // also objects from index, that fits the first part of the expression
                } else {
                    List<JsObject> newResolvedObjects = new ArrayList<JsObject>();
                    List<TypeUsage> newResolvedTypes = new ArrayList<TypeUsage>();
                    for (JsObject localObject : lastResolvedObjects) {
                        // go through the loca object and try find the method / property from the next expression part
                        JsObject property = ((JsObject) localObject).getProperty(name);
                        if (property != null) {
                            if ("@mtd".equals(kind)) {  //NOI18N
                                if (property.getJSKind().isFunction()) {
                                    //Collection<TypeUsage> resovledTypes = resolveTypeFromSemiType(model, property, ((JsFunction) property).getReturnTypes());
                                    Collection<? extends TypeUsage> resovledTypes = ((JsFunction) property).getReturnTypes();
                                    newResolvedTypes.addAll(resovledTypes);
                                }
                            } else {
                                Collection<? extends TypeUsage> lastTypeAssignment = property.getAssignmentForOffset(offset);
                                if (lastTypeAssignment.isEmpty()) {
                                    newResolvedObjects.add(property);
                                } else {
                                    newResolvedTypes.addAll(lastTypeAssignment);
                                }
                            }
                        }
                    }
                    
                    
                    
                    for (TypeUsage typeUsage : lastResolvedTypes) {
                        // for the type build the prototype chain. 
                        Collection<String> prototypeChain = new ArrayList<String>();
                        prototypeChain.add(typeUsage.getType());
                        prototypeChain.addAll(findPrototypeChain(typeUsage.getType(), jsIndex));
                        
                        Collection<? extends IndexResult> indexResults = null;        
                        for (String fqn : prototypeChain) {
                            // at first look at the properties of the object
                            indexResults = jsIndex.findFQN(fqn + "." + name); //NOI18N
                            if (indexResults.isEmpty()) {
                                // if the property was not found, try to look at the prototype of the object
                                indexResults = jsIndex.findFQN(fqn + ".prototype." + name); //NOI18N
                            }
                            if(!indexResults.isEmpty()) {
                                // if the property / method was already found, we don't need to continue. 
                                // in the runtime is also used the first one that is found in the prototype chain
                                break;
                            }
                        }
                        
                        for (IndexResult indexResult : indexResults) {
                            // go through the resul from index and add appropriate types to the new resolved
                            JsElement.Kind jsKind = IndexedElement.Flag.getJsKind(Integer.parseInt(indexResult.getValue(JsIndex.FIELD_FLAG)));
                            if ("@mtd".equals(kind) && jsKind.isFunction()) {
                                //Collection<TypeUsage> resolved = resolveTypeFromSemiType(model, ModelUtils.findJsObject(model, offset), IndexedElement.getReturnTypes(indexResult));
                                Collection<? extends TypeUsage> resolvedTypes = IndexedElement.getReturnTypes(indexResult);
                                newResolvedTypes.addAll(resolvedTypes);
                            } else {
                                newResolvedTypes.add(new TypeUsageImpl(typeUsage.getType() + "." + name));
                            }
                        }
                        // from libraries look for top level types
                        for (JsObject libGlobal : getLibrariesGlobalObjects()) {
                            for (JsObject object : libGlobal.getProperties().values()) {
                                if (object.getName().equals(typeUsage.getType())) {
                                    JsObject property = object.getProperty(name);
                                    if (property != null) {
                                        JsElement.Kind jsKind = property.getJSKind();
                                        if ("@mtd".equals(kind) && jsKind.isFunction()) {
                                            newResolvedTypes.addAll(((JsFunction) property).getReturnTypes());
                                        } else {
                                            newResolvedObjects.add(property);
                                        }
                                    }
                                    newResolvedObjects.add(object);
                                    break;
                                }
                            }
                        }
                    }

                    lastResolvedObjects = newResolvedObjects;
                    lastResolvedTypes = newResolvedTypes;
                }
            }
            
            HashMap<String, TypeUsage> resultTypes  = new HashMap<String, TypeUsage> ();
            for (TypeUsage typeUsage : lastResolvedTypes) {
                if(!resultTypes.containsKey(typeUsage.getType())) {
                    resultTypes.put(typeUsage.getType(), typeUsage);
                }
            }
            for (JsObject jsObject : lastResolvedObjects) {
//                if (jsObject.getJSKind() == JsElement.Kind.OBJECT_LITERAL) {
                    String fqn = ModelUtils.createFQN(jsObject);
                    if(!resultTypes.containsKey(fqn)) {
                        resultTypes.put(fqn, new TypeUsageImpl(fqn, offset));
                    }
//                }
             }
            return resultTypes.values();
    }

    private static void resolveAssignments(Model model, JsObject jsObject, int offset, List<JsObject> resolvedObjects, List<TypeUsage> resolvedTypes) {
        Collection<? extends Type> assignments = jsObject.getAssignmentForOffset(offset);
        for (Type typeName : assignments) {
            
            JsObject byOffset = findObjectForOffset(typeName.getType(), offset, model);
            if (byOffset != null) {
                if(!jsObject.getName().equals(byOffset.getName())) {
                    resolvedObjects.add(byOffset);
                    resolveAssignments(model, byOffset, offset, resolvedObjects, resolvedTypes);
                }
            } else {
                resolvedTypes.add((TypeUsage)typeName);
            }
        }
    }
    
    public static JsObject findObjectForOffset(String name, int offset, Model model) {
        for (JsObject object : model.getVariables(offset)) {
            if (object.getName().equals(name)) {
                return object;
            }
        }
        return null;
    }

    public static Collection<String> findPrototypeChain(String fqn, JsIndex jsIndex) {
        Collection<String> result = new ArrayList<String>();
        Collection<IndexedElement> properties = jsIndex.getProperties(fqn);
        for (IndexedElement property : properties) {
            if("prototype".equals(property.getName())) {  //NOI18N
                Collection<? extends IndexResult> indexResults = jsIndex.findFQN(property.getFQN());
                for (IndexResult indexResult : indexResults) {
                    Collection<TypeUsage> assignments = IndexedElement.getAssignments(indexResult);
                    for (TypeUsage typeUsage : assignments) {
                        result.add(typeUsage.getType());
                    }
                    for (TypeUsage typeUsage : assignments) {
                        result.addAll(findPrototypeChain(typeUsage.getType(), jsIndex));
                    }
                }
            }
        }
        return result;
    }
    
    private static Collection<JsObject> getLibrariesGlobalObjects() {
        Collection<JsObject> result = new ArrayList<JsObject>();
        JsObject libGlobal = JQueryModel.getGlobalObject();
        if (libGlobal != null) {
            result.add(libGlobal);
        }
        return result;
    }
    
    private static class SemiTypeResolverVisitor extends PathNodeVisitor {
        
        private final Set<TypeUsage> result = new HashSet<TypeUsage>();
        private StringBuilder sb = new StringBuilder();
        
        public SemiTypeResolverVisitor() {
        }
        
        public Collection<TypeUsage> getSemiTypes() {
            return result;
        }

        @Override
        public Node enter(AccessNode aNode) {
            if (aNode.getBase() instanceof IdentNode) {
                IdentNode iNode = (IdentNode)aNode.getBase();
                if (iNode.getName().equals("this")) {
                    List<? extends Node> path = getPath();
                    if (!(path.size() > 0 && path.get(path.size() - 1) instanceof CallNode)) {
                        sb.append("@this."); //NOI18N
                        sb.append(aNode.getProperty().getName());
                        result.add(new TypeUsageImpl(sb.toString(), iNode.getStart(), false));                //NOI18N
                        // plus five due to this.
                    }
                } else {
                    if (sb.length() > 5) {
                        sb.insert(6, aNode.getProperty().getName());
                    } else {
                        sb.append(aNode.getProperty().getName());
                    }
                    sb.insert(0, ((IdentNode)aNode.getBase()).getName());
                    sb.insert(0, "@exp;");
                    result.add(new TypeUsageImpl(sb.toString(), aNode.getStart()));
                }
                return null;
            } else {
                if(sb.length() > 5) {
                    sb.insert(6, aNode.getProperty().getName());
                } else {
                    sb.append(aNode.getProperty().getName());
                }
            }
            return super.enter(aNode);
        }

//        @Override
//        public Node enter(BinaryNode binaryNode) {
//            if (!binaryNode.isAssignment()) {
//                if(binaryNode.rhs() instanceof LiteralNode) {
//                    LiteralNode lNode = (LiteralNode)binaryNode.rhs();
//                    Object value = lNode.getObject();
//                    if (value instanceof String) {
//                        result.add(new TypeUsageImpl(Type.STRING, lNode.getStart(), true));
//                        return null;
//                    }
//                }
//                if (binaryNode.lhs() instanceof LiteralNode) {
//                    LiteralNode lNode = (LiteralNode)binaryNode.rhs();
//                    Object value = lNode.getObject();
//                    if (value instanceof String) {
//                        result.add(new TypeUsageImpl(Type.STRING, lNode.getStart(), true));
//                        return null;
//                    }
//                }
//            }
//            return super.enter(binaryNode);
//        }

        @Override
        public Node enter(CallNode callNode) {
            if (callNode.getFunction() instanceof ReferenceNode) {
                FunctionNode function = (FunctionNode)((ReferenceNode)callNode.getFunction()).getReference();
                String name = function.getIdent().getName();
                result.add(new TypeUsageImpl("@call;" + name, function.getStart(), false)); //NOI18N
            } else {
                if (sb.length() < 6) {
                    sb.append("@call;");    //NOI18N
                } else {
                    sb.insert(6, "@call;"); //NOI18N
                }
                // don't visit arguments, just name the name of function.
                callNode.getFunction().accept(this);
            }
            return null;
        }

        @Override
        public Node enter(IdentNode iNode) {
            if (getPath().isEmpty()) {
                if (iNode.getName().equals("this")) {   //NOI18N
                    result.add(new TypeUsageImpl("@this", iNode.getStart(), false));                //NOI18N
                } else {
                    result.add(new TypeUsageImpl("@var;" + iNode.getName(), iNode.getStart(), false));
                }
            }
            return null;
        }

        @Override
        public Node enter(LiteralNode lNode) {
            Object value = lNode.getObject();
            if (value instanceof Boolean) {
                result.add(new TypeUsageImpl(Type.BOOLEAN, lNode.getStart(), true));
            } else if (value instanceof String) {
                result.add(new TypeUsageImpl(Type.STRING, lNode.getStart(), true));
            } else if (value instanceof Integer
                    || value instanceof Float
                    || value instanceof Double) {
                result.add(new TypeUsageImpl(Type.NUMBER, lNode.getStart(), true));
            } else if (lNode instanceof LiteralNode.ArrayLiteralNode) {
                result.add(new TypeUsageImpl("Array", -1, true));
            }
            return null;
        }

        @Override
        public Node enter(ObjectNode objectNode) {
            result.add(new TypeUsageImpl("@anonym;" + objectNode.getStart(), objectNode.getStart(), false));
            return null;
        }

        @Override
        public Node enter(UnaryNode uNode) {
            if (com.oracle.nashorn.parser.Token.descType(uNode.getToken()) == TokenType.NEW) {
                if (uNode.rhs() instanceof CallNode
                    && ((CallNode)uNode.rhs()).getFunction() instanceof IdentNode) {
                        IdentNode iNode = ((IdentNode)((CallNode)uNode.rhs()).getFunction());
                        result.add(new TypeUsageImpl("@new;" + iNode.getName(), iNode.getStart(), false));
                        return null;
                }
            }
            return super.enter(uNode);
        }        
    }
}
