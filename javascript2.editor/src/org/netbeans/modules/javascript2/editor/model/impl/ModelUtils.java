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
import org.netbeans.modules.javascript2.editor.lexer.CommonTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {
      
    public static JsObjectImpl getJsObject (ModelBuilder builder, List<Identifier> fqName) {
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
                result = new JsObjectImpl(tmpObject, name, name.getOffsetRange());
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
        if (jsObject.getOffsetRange(null).containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT 
                        || kind == JsElement.Kind.FUNCTION || kind == JsElement.Kind.METHOD || kind == JsElement.Kind.CONSTRUCTOR)
                tmpObject = findJsObject(property, offset);
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
        if (dScope.getOffsetRange(null).containsInclusive(offset)) {
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
        if (lEnd < lStart) {
            // TODO this is a workaround for bug in nashorn, when sometime the start and end are not crorrect
            int length = lStart - lEnd;
            lEnd = lStart + length;
        }
        return new OffsetRange(lStart, lEnd);
    }
    
    
    private static final Collection<CommonTokenId> CTX_DELIMITERS = Arrays.asList(
            CommonTokenId.BRACKET_LEFT_CURLY, CommonTokenId.BRACKET_RIGHT_CURLY,
            CommonTokenId.OPERATOR_SEMICOLON);
    
    private enum State {
        INIT
    }
    
    private static String getSemiType(TokenSequence<CommonTokenId> ts, int offset) {
        
        String result = "UNKNOWN";
        ts.move(offset);
        if (!ts.moveNext()) {
           return result;
        }
    
        State state = State.INIT;
        while (ts.movePrevious()) {
            Token<CommonTokenId> token = ts.token();
            if (!CTX_DELIMITERS.contains(token.id())){
                switch (state) {
                    case INIT:
                        if (token.id() == CommonTokenId.IDENTIFIER)
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
            if (objectName != null && object.getOffsetRange(null).getEnd() == objectName.getOffsetRange().getEnd()) {
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
        } else if (type.getType().startsWith("@new:")) {
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
        } else if (type.getType().startsWith("@call:")) {
            String functionName = type.getType().substring(6);
            JsObject globalObject = ModelUtils.getGlobalObject(object);
            JsObject function = globalObject.getProperty(functionName);
            if (function != null && function instanceof JsFunction) {
                result.addAll(((JsFunction)function).getReturnTypes());
            }
        } else if(type.getType().startsWith("@anonym:")){
            int start = Integer.parseInt(type.getType().substring(8));
            JsObject globalObject = ModelUtils.getGlobalObject(object);
            for(JsObject children : globalObject.getProperties().values()) {
                if(children.getOffset() == start && children.getName().startsWith("Anonym$")) {
                    result.add(new TypeUsageImpl(ModelUtils.createFQN(children), children.getOffset(), true));
                    break;
                }
                
            }
        } else {
            result.add(type);
        }
        return result;
    }

    private static class SemiTypeResolverVisitor extends PathNodeVisitor {
        
        private final Set<TypeUsage> result = new HashSet<TypeUsage>();
        
        public SemiTypeResolverVisitor() {
        }
        
        public Collection<TypeUsage> getSemiTypes() {
            return result;
        }

        @Override
        public Node visit(AccessNode aNode, boolean onset) {
            if (onset) {
                if (aNode.getBase() instanceof IdentNode) {
                    IdentNode iNode = (IdentNode)aNode.getBase();
                    if (iNode.getName().equals("this")) {
                        List<? extends Node> path = getPath();
                        if (!(path.size() > 0 && path.get(path.size() - 1) instanceof CallNode)) {
                            result.add(new TypeUsageImpl("@this." + aNode.getProperty().getName(), iNode.getStart(), false));                //NOI18N
                            // plus five due to this.
                        }
                        return null;
                    }
                }
            }
            return super.visit(aNode, onset);
        }

//        @Override
//        public Node visit(BinaryNode binaryNode, boolean onset) {
//            if (onset) {
//                if (!binaryNode.isAssignment()) {
//                    if(binaryNode.rhs() instanceof LiteralNode) {
//                        LiteralNode lNode = (LiteralNode)binaryNode.rhs();
//                        Object value = lNode.getObject();
//                        if (value instanceof String) {
//                            result.add(new TypeUsageImpl(Type.STRING, lNode.getStart(), true));
//                            return null;
//                        }
//                    }
//                    if (binaryNode.lhs() instanceof LiteralNode) {
//                        LiteralNode lNode = (LiteralNode)binaryNode.rhs();
//                        Object value = lNode.getObject();
//                        if (value instanceof String) {
//                            result.add(new TypeUsageImpl(Type.STRING, lNode.getStart(), true));
//                            return null;
//                        }
//                    }
//                }
//            }
//            return super.visit(binaryNode, onset);
//        }
        
        @Override
        public Node visit(CallNode callNode, boolean onset) {
            if (onset) {
                if (callNode.getFunction() instanceof ReferenceNode) {
                    FunctionNode function = (FunctionNode)((ReferenceNode)callNode.getFunction()).getReference();
                    String name = function.getIdent().getName();
                    result.add(new TypeUsageImpl("@call:" + name, function.getStart(), false));
                }
            }
            return super.visit(callNode, onset);
        }

        
        
        @Override
        public Node visit(IdentNode iNode, boolean onset) {
            if (onset) {
                if (getPath().isEmpty()) {
                    if (iNode.getName().equals("this")) {   //NOI18N
                        result.add(new TypeUsageImpl("@this", iNode.getStart(), false));                //NOI18N
                    } else {
                        result.add(new TypeUsageImpl(iNode.getName(), iNode.getStart(), false));
                    }
                }
                return null;
            }
            return super.visit(iNode, onset);
        }

        
        @Override
        public Node visit(LiteralNode lNode, boolean onset) {
            if (onset) {
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
                    result.add(new TypeUsageImpl("Array", lNode.getStart(), true));
                }
                return null;
            }
            return super.visit(lNode, onset);
        }

        @Override
        public Node visit(ObjectNode objectNode, boolean onset) {
            if (onset) {
                result.add(new TypeUsageImpl("@anonym:" + objectNode.getStart(), objectNode.getStart(), false));
                return null;
            }
            return super.visit(objectNode, onset);
        }

        
        @Override
        public Node visit(UnaryNode uNode, boolean onset) {
            if (onset) {
                if (com.oracle.nashorn.parser.Token.descType(uNode.getToken()) == TokenType.NEW) {
                    if (uNode.rhs() instanceof CallNode
                        && ((CallNode)uNode.rhs()).getFunction() instanceof IdentNode) {
                            IdentNode iNode = ((IdentNode)((CallNode)uNode.rhs()).getFunction());
                            result.add(new TypeUsageImpl("@new:" + iNode.getName(), iNode.getStart(), false));
                            return null;
                    }
                }
            }
            return super.visit(uNode, onset);
        }
        
    }
}
