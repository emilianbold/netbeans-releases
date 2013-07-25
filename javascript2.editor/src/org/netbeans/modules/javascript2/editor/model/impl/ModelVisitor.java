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
package org.netbeans.modules.javascript2.editor.model.impl;


import jdk.nashorn.internal.ir.AccessNode;
import jdk.nashorn.internal.ir.BinaryNode;
import jdk.nashorn.internal.ir.CallNode;
import jdk.nashorn.internal.ir.CatchNode;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.IndexNode;
import jdk.nashorn.internal.ir.LiteralNode;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.ObjectNode;
import jdk.nashorn.internal.ir.PropertyNode;
import jdk.nashorn.internal.ir.ReferenceNode;
import jdk.nashorn.internal.ir.ReturnNode;
import jdk.nashorn.internal.ir.TernaryNode;
import jdk.nashorn.internal.ir.UnaryNode;
import jdk.nashorn.internal.ir.VarNode;
import jdk.nashorn.internal.parser.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import jdk.nashorn.internal.ir.Block;
import jdk.nashorn.internal.ir.ExecuteNode;
import jdk.nashorn.internal.ir.WithNode;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.doc.DocumentationUtils;
import org.netbeans.modules.javascript2.editor.doc.spi.DocIdentifier;
import org.netbeans.modules.javascript2.editor.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.doc.spi.JsModifier;
import static org.netbeans.modules.javascript2.editor.doc.spi.JsModifier.PRIVATE;
import static org.netbeans.modules.javascript2.editor.doc.spi.JsModifier.PUBLIC;
import static org.netbeans.modules.javascript2.editor.doc.spi.JsModifier.STATIC;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsArray;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.spi.model.FunctionArgument;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.ModelFactory;
import org.netbeans.modules.javascript2.editor.model.Occurrence;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.spi.model.FunctionInterceptor;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelVisitor extends PathNodeVisitor {

    private final ModelBuilder modelBuilder;
    /**
     * Keeps the name of the visited properties
     */
    private final List<List<FunctionNode>> functionStack;
    private final JsParserResult parserResult;

    // keeps objects that are created as arguments of a function call
    private final Stack<Collection<JsObjectImpl>> functionArgumentStack = new Stack<Collection<JsObjectImpl>>();
    private Map<FunctionInterceptor, Collection<FunctionCall>> functionCalls = null;
    
    private JsObjectImpl fromAN = null;

    public ModelVisitor(JsParserResult parserResult) {
        FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        this.modelBuilder = new ModelBuilder(JsFunctionImpl.createGlobal(
                fileObject, Integer.MAX_VALUE, parserResult.getSnapshot().getMimeType()));
        this.functionStack = new ArrayList<List<FunctionNode>>();
        this.parserResult = parserResult;
    }

    public JsObject getGlobalObject() {
        return modelBuilder.getGlobal();
    }

    @Override
    public Node enter(AccessNode accessNode) {
        BinaryNode node = getPath().get(getPath().size() - 1) instanceof BinaryNode
                ? (BinaryNode)getPath().get(getPath().size() - 1) : null;
        if (!(node != null && node.tokenType() == TokenType.ASSIGN)) {
            if (accessNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)accessNode.getBase()).getName())) { //NOI18N
                IdentNode iNode = (IdentNode)accessNode.getProperty();
                JsObject current = modelBuilder.getCurrentDeclarationFunction();
                JsObject property = current.getProperty(iNode.getName());
                if (property == null && current.getParent() != null && (current.getParent().getJSKind() == JsElement.Kind.CONSTRUCTOR
                        || current.getParent().getJSKind() == JsElement.Kind.OBJECT)) {
                    current = current.getParent();
                    if (ModelUtils.PROTOTYPE.equals(current.getName())) {
                        current = current.getParent();
                    }
                    property = current.getProperty(iNode.getName());
                }
                if (property == null && current.getParent() == null) {
                    // probably we are in global space and there is used this
                    property = modelBuilder.getGlobal().getProperty(iNode.getName());
                }
                if (property != null) {
                    ((JsObjectImpl)property).addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                }
            }
        }
        return super.enter(accessNode);
    }

    @Override
    public Node leave(AccessNode accessNode) {
        if (accessNode.getBase() instanceof IdentNode) {
            IdentNode base = (IdentNode)accessNode.getBase();
            if (!"this".equals(base.getName())) {
                Identifier name = ModelElementFactory.create(parserResult, (IdentNode)accessNode.getBase());
                if (name != null) {
                    List<Identifier> fqname = new ArrayList<Identifier>();
                    fqname.add(name); 
                    Collection<? extends JsObject> variables = ModelUtils.getVariables(modelBuilder.getCurrentDeclarationFunction());
                    fromAN = null;
                    for(JsObject variable : variables) {
                        if (variable.getName().equals(name.getName()) && (variable.getModifiers().contains(Modifier.PRIVATE) || variable instanceof ParameterObject)) {
                            fromAN = (JsObjectImpl)variable;
                            break;
                        }
                    }
                    if (fromAN == null) {
                        JsObject global = modelBuilder.getGlobal();
                        fromAN = (JsObjectImpl)global.getProperty(name.getName());
                        if (fromAN == null) {
                            fromAN = new JsObjectImpl(global, name, name.getOffsetRange(), false, global.getMimeType(), global.getSourceLabel());
                            global.addProperty(name.getName(), fromAN);
                        }
                    }
                    fromAN.addOccurrence(name.getOffsetRange());
                }
            } else {
                JsObject current = modelBuilder.getCurrentDeclarationFunction();
                fromAN = (JsObjectImpl)resolveThis(current);
            }
        }
        if (fromAN != null) {
            JsObjectImpl property = (JsObjectImpl)fromAN.getProperty(accessNode.getProperty().getName());
            int pathSize = getPath().size();
            Node lastVisited = getPath().get(pathSize - 2);
            boolean onLeftSite = false;
            if (lastVisited instanceof BinaryNode) {
                BinaryNode bNode = (BinaryNode)lastVisited;
                onLeftSite = bNode.tokenType() == TokenType.ASSIGN && bNode.lhs().equals(accessNode);       
            }
            if (property != null) {
                if(onLeftSite && !property.isDeclared()) {
                    property.setDeclared(true);
                }
                property.addOccurrence(new OffsetRange(accessNode.getProperty().getStart(), accessNode.getProperty().getFinish()));
            } else {
                Identifier name = ModelElementFactory.create(parserResult, (IdentNode)accessNode.getProperty());
                if (name != null) {
                    if (pathSize > 1 && getPath().get(pathSize - 2) instanceof CallNode) {
                        CallNode cNode = (CallNode)getPath().get(pathSize - 2);
                        if (!cNode.getArgs().contains(accessNode)) {
                            property = ModelElementFactory.createVirtualFunction(parserResult, fromAN, name, cNode.getArgs().size());
                            //property.addOccurrence(name.getOffsetRange());
                        } else {
                            property = new JsObjectImpl(fromAN, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
                            property.addOccurrence(name.getOffsetRange());
                        }
                    } else {
                        boolean setDocumentation = false;
                        if (isPriviliged(accessNode) && getPath().size() > 1 && getPreviousFromPath(2) instanceof ExecuteNode ) {
                            // google style declaration of properties:  this.buildingID;    
                            onLeftSite = true;
                            setDocumentation = true;
                        }
                        property = new JsObjectImpl(fromAN, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
                        property.addOccurrence(name.getOffsetRange());
                        if (setDocumentation) {
                            JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
                            if (docHolder != null) {    
                                property.setDocumentation(docHolder.getDocumentation(accessNode));
                                property.setDeprecated(docHolder.isDeprecated(accessNode));
                                List<Type> returnTypes = docHolder.getReturnType(accessNode);
                                if (!returnTypes.isEmpty()) {
                                    for (Type type : returnTypes) {
                                        property.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset(), true), accessNode.getFinish());
                                    }
                                }
                                setModifiersFromDoc(property, docHolder.getModifiers(accessNode));
                            }
                        }
                    }
                    fromAN.addProperty(name.getName(), property);
                }
            }
            if(property != null) {
                fromAN = property;
            }
        }
        if (!(getPath().get(getPath().size() - 1) instanceof AccessNode)) {
            fromAN = null;
        }
        return super.leave(accessNode);
    }

    @Override
    public Node enter(BinaryNode binaryNode) {
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (binaryNode.tokenType() == TokenType.ASSIGN
                && !(rhs instanceof ReferenceNode || rhs instanceof ObjectNode)
                && (lhs instanceof AccessNode || lhs instanceof IdentNode || lhs instanceof IndexNode)) {
            // TODO probably not only assign
            JsObjectImpl parent = modelBuilder.getCurrentDeclarationFunction();
            if (parent == null) {
                // should not happened
                return super.enter(binaryNode);
            }
            if (lhs instanceof AccessNode) {
                AccessNode aNode = (AccessNode)lhs;
                JsObjectImpl property = null;
                if (aNode.getBase() instanceof IdentNode && "this".equals(((IdentNode)aNode.getBase()).getName())) { //NOI18N
                    // a usage of field
                    String fieldName = aNode.getProperty().getName();
                    parent = (JsObjectImpl)resolveThis(parent);
                    property = (JsObjectImpl)parent.getProperty(fieldName);
                    if(property == null) {
                        Identifier identifier = ModelElementFactory.create(parserResult, (IdentNode)aNode.getProperty());
                        if (identifier != null) {
                            property = new JsObjectImpl(parent, identifier, identifier.getOffsetRange(), true, parserResult.getSnapshot().getMimeType(), null);
                            parent.addProperty(fieldName, property);
                            JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
                            if (docHolder != null) {
                                property.setDocumentation(docHolder.getDocumentation(aNode));
                                property.setDeprecated(docHolder.isDeprecated(aNode));
                            }
                        }
                    }
                } else {
                    // probably a property of an object
                    List<Identifier> fqName = getName(aNode, parserResult);
                    if (fqName != null) {
                        property = ModelUtils.getJsObject(modelBuilder, fqName, true);
                        if (property.getParent().getJSKind().isFunction() && !property.getModifiers().contains(Modifier.STATIC)) {
                            property.getModifiers().add(Modifier.STATIC);
                        }
                    }
                }
                if (property != null) {
                    String parameter = null;
                    JsFunction function = (JsFunction)modelBuilder.getCurrentDeclarationFunction();
                    if(binaryNode.rhs() instanceof IdentNode) {
                        IdentNode iNode = (IdentNode)rhs;
                        if(/*function.getProperty(rhs.getName()) == null &&*/ function.getParameter(iNode.getName()) != null) {
                            parameter = "@param;" + function.getFullyQualifiedName() + ":" + iNode.getName(); //NOI18N
                        }
                    }
                    Collection<TypeUsage> types; 
                    if (parameter == null) {
                        types =  ModelUtils.resolveSemiTypeOfExpression(parserResult, binaryNode.rhs());
                        Collection<TypeUsage> correctedTypes = new ArrayList<TypeUsage>(types.size());
                        for (TypeUsage type : types) {
                            String typeName = type.getType();
                            // we have to check, whether a variable comming from resolvedr is not a parameter of function where the binary node is
                            if (typeName.startsWith(SemiTypeResolverVisitor.ST_VAR)) {
                                String varName = typeName.substring(SemiTypeResolverVisitor.ST_VAR.length());
                                if (function.getParameter(varName) != null) {
                                    correctedTypes.add(new TypeUsageImpl("@param;" + function.getFullyQualifiedName() + ":" + varName, type.getOffset(), false));
                                } else {
                                    correctedTypes.add(type);
                                }
                            } else {
                                correctedTypes.add(type);
                            }
                        }
                        types = correctedTypes;
                    } else {
                        types = new ArrayList<TypeUsage>();
                        types.add(new TypeUsageImpl(parameter, binaryNode.rhs().getStart(), false));
                    }

                    for (TypeUsage type : types) {
                        // plus 5 due to the this.
                        property.addAssignment(type, binaryNode.getStart() + 5);
                    }
                }

            } else {
                IdentNode ident = null;
                if (lhs instanceof IndexNode) {
                    IndexNode iNode = (IndexNode)lhs;
                    if (iNode.getBase() instanceof IdentNode) {
                        ident = (IdentNode)iNode.getBase();
                    }
                } else if (lhs instanceof IdentNode) {
                    ident = (IdentNode)lhs;
                }
                
                if (ident != null) {
                    final Identifier name = ModelElementFactory.create(parserResult, ident);
                    if (name != null) {
                        final String newVarName = name.getName();
                        boolean hasParent = parent.getProperty(newVarName) != null ;
                        boolean hasGrandParent = parent.getJSKind() == JsElement.Kind.METHOD && parent.getParent().getProperty(newVarName) != null;
                        JsObject lObject = null;
                        if (!hasParent && !hasGrandParent && modelBuilder.getGlobal().getProperty(newVarName) == null) {
                            addOccurence(ident, true);
                        } else {
                            lObject = hasParent ? parent.getProperty(newVarName) : hasGrandParent ? parent.getParent().getProperty(newVarName) : null;
                            if (lObject != null) {
                                ((JsObjectImpl)lObject).addOccurrence(name.getOffsetRange());
                            } else {
                                addOccurence(ident, true);
                            }
                        }
                        JsObjectImpl jsObject = (JsObjectImpl)parent.getProperty(newVarName);
                        if (jsObject == null) {
                            // it's not a property of the parent -> try to find in different context
                            Model model = parserResult.getModel();
                            Collection<? extends JsObject> variables = model.getVariables(name.getOffsetRange().getStart());
                            for(JsObject variable : variables) {
                                if(variable.getName().equals(newVarName)) {
                                    jsObject = (JsObjectImpl)variable;
                                    break;
                                }
                            }
                            if (jsObject == null) {
                                // the object with the name wasn't find yet -> create in global scope
                                jsObject = new JsObjectImpl(model.getGlobalObject(), name,
                                        name.getOffsetRange(), false, parserResult.getSnapshot().getMimeType(), null);
                            }
                        }

                        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(parserResult, binaryNode.rhs());
                        if (lhs instanceof IndexNode && jsObject instanceof JsArrayImpl) {
                            ((JsArrayImpl)jsObject).addTypesInArray(types);
                        } else {
                            for (TypeUsage type : types) {
                                jsObject.addAssignment(type, binaryNode.lhs().getFinish());
                            }
                        }
                        if (!(lObject != null && jsObject.getName().equals(lObject.getName()))) {
                            addOccurence(ident, true);
                        }
                    }
                }
            }
            if (binaryNode.rhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.rhs(), false);
            }
        } else if(binaryNode.tokenType() != TokenType.ASSIGN
                || (binaryNode.tokenType() == TokenType.ASSIGN && binaryNode.lhs() instanceof IndexNode)) {
            if (binaryNode.lhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.lhs(), true);
            }
            if (binaryNode.rhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.rhs(), false);
            }
        }
        return super.enter(binaryNode);
    }

    @Override
    public Node enter(CallNode callNode) {
        functionArgumentStack.push(new ArrayList<JsObjectImpl>(3));
        if (callNode.getFunction() instanceof IdentNode) {
            IdentNode iNode = (IdentNode)callNode.getFunction();
            addOccurence(iNode, false, true, callNode.getArgs().size());
        }
        for (Node argument : callNode.getArgs()) {
            if (argument instanceof IdentNode) {
                addOccurence((IdentNode) argument, false);
            }
        }
        return super.enter(callNode);
    }

    @Override
    public Node leave(WithNode withNode) {
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(parserResult, withNode.getExpression());
        modelBuilder.getCurrentDeclarationScope().addWithTypes(
                new OffsetRange(withNode.getStart(), withNode.getFinish()), types);
        return super.leave(withNode);
    }

    @Override
    public Node leave(CallNode callNode) {
        Collection<JsObjectImpl> functionArguments = functionArgumentStack.pop();
        if(callNode.getFunction() instanceof AccessNode) {
                List<Identifier> funcName = getName((AccessNode)callNode.getFunction(), parserResult);
                if (funcName != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Identifier identifier : funcName) {
                        sb.append(identifier.getName());
                        sb.append(".");
                    }
                    if (functionCalls == null) {
                        functionCalls = new LinkedHashMap<FunctionInterceptor, Collection<FunctionCall>>();
                    }

                    String name = sb.substring(0, sb.length() - 1);
                    FunctionInterceptor interceptorToUse = null;
                    for (FunctionInterceptor interceptor : ModelExtender.getDefault().getFunctionInterceptors()) {
                        if (interceptor.getNamePattern().matcher(name).matches()) {
                            interceptorToUse = interceptor;
                            break;
                        }
                    }


                    if (interceptorToUse != null) {
                        Collection<FunctionArgument> funcArg = new ArrayList<FunctionArgument>();
                        for (int i = 0; i < callNode.getArgs().size(); i++) {
                            Node argument = callNode.getArgs().get(i);
                            createFunctionArgument(argument, i, functionArguments, funcArg);
                        }
                        Collection<FunctionCall> calls = functionCalls.get(interceptorToUse);
                        if (calls == null) {
                            calls = new ArrayList<FunctionCall>();
                            functionCalls.put(interceptorToUse, calls);
                        }
                        calls.add(new FunctionCall(name, modelBuilder.getCurrentDeclarationScope(), funcArg));

                    }
                }
            }
        return super.leave(callNode);
    }

    private void createFunctionArgument(Node argument, int position, Collection<JsObjectImpl> functionArguments,
            Collection<FunctionArgument> result) {

        if (argument instanceof LiteralNode) {
            LiteralNode ln = (LiteralNode)argument;
            if (ln.isString()) {
                result.add(FunctionArgumentAccessor.getDefault().createForString(
                        position, argument.getStart(), ln.getString()));
            }
        } else if (argument instanceof ObjectNode) {
            for (JsObjectImpl jsObject: functionArguments) {
                if (jsObject.getOffset() == argument.getStart()) {
                    result.add(FunctionArgumentAccessor.getDefault().createForAnonymousObject(position, jsObject.getOffset(), jsObject));
                    break;
                }
            }
        } else if (argument instanceof AccessNode) {
            List<String> strFqn = new ArrayList<String>();
            if(fillName((AccessNode) argument, strFqn)) {
                result.add(FunctionArgumentAccessor.getDefault().createForReference(
                        position, argument.getStart(), strFqn));
            } else {
                result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
            }
        } else if (argument instanceof IndexNode) {
            List<String> strFqn = new ArrayList<String>();
            if(fillName((IndexNode) argument, strFqn)) {
                result.add(FunctionArgumentAccessor.getDefault().createForReference(
                        position, argument.getStart(), strFqn));
            } else {
                result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
            }
        } else if (argument instanceof IdentNode) {
            IdentNode in = (IdentNode) argument;
            String inName = in.getName();
            result.add(FunctionArgumentAccessor.getDefault().createForReference(
                    position, argument.getStart(),
                    Collections.singletonList(inName)));
        } else if (argument instanceof UnaryNode) {
            // we are handling foo(new Something())
            UnaryNode un = (UnaryNode) argument;
            if (un.tokenType() == TokenType.NEW) {
                CallNode constructor = (CallNode) un.rhs();
                createFunctionArgument(constructor.getFunction(), position, functionArguments, result);
            }
        } else {
            result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
        }
    }

    @Override
    public Node enter(CatchNode catchNode) {
        Identifier exception = ModelElementFactory.create(parserResult, catchNode.getException());
        if (exception != null) {
            DeclarationScopeImpl inScope = modelBuilder.getCurrentDeclarationScope();
            CatchBlockImpl catchBlock  = new CatchBlockImpl(inScope, exception,
                    new OffsetRange(catchNode.getStart(), catchNode.getFinish()), parserResult.getSnapshot().getMimeType());
            inScope.addDeclaredScope(catchBlock);
            modelBuilder.setCurrentObject(catchBlock);
        }
        return super.enter(catchNode);
    }

    @Override
    public Node leave(CatchNode catchNode) {
        if (!JsEmbeddingProvider.containsGeneratedIdentifier(catchNode.getException().getName())) {
            modelBuilder.reset();
        }
        return super.leave(catchNode);
    }

    
    @Override
    public Node enter(IdentNode identNode) {
        Node previousVisited = getPath().get(getPath().size() - 1);
        if(!(previousVisited instanceof AccessNode
                || previousVisited instanceof VarNode
                || previousVisited instanceof BinaryNode
                || previousVisited instanceof PropertyNode
                || previousVisited instanceof CatchNode)) {
            //boolean declared = previousVisited instanceof CatchNode;
            addOccurence(identNode, false);
        }
        return super.enter(identNode);
    }

    @Override
    public Node leave(IndexNode indexNode) {
        if (indexNode.getIndex() instanceof LiteralNode) {
            Node base = indexNode.getBase();
            JsObjectImpl parent = null;
            if (base instanceof AccessNode) {
                parent = fromAN;
            } else if (base instanceof IdentNode) {
                IdentNode iNode = (IdentNode)base;
                if (!"this".equals(iNode.getName())) {
                    Identifier parentName = ModelElementFactory.create(parserResult, iNode);
                    if (parentName != null) {
                        List<Identifier> fqName = new ArrayList<Identifier>();
                        fqName.add(parentName);
                        parent = ModelUtils.getJsObject(modelBuilder, fqName, false);
                        parent.addOccurrence(parentName.getOffsetRange());
                    }
                } else {
                    JsObject current = modelBuilder.getCurrentDeclarationFunction();
                    fromAN = (JsObjectImpl)resolveThis(current);
                }
            }
            if (parent != null && indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode literal = (LiteralNode)indexNode.getIndex();
                if (literal.isString()) {
                    String index = literal.getPropertyName();
                    JsObjectImpl property = (JsObjectImpl)parent.getProperty(index);
                    if (property != null) {
                        property.addOccurrence(new OffsetRange(indexNode.getIndex().getStart(), indexNode.getIndex().getFinish()));
                    } else {
                        Identifier name = ModelElementFactory.create(parserResult, (LiteralNode)indexNode.getIndex());
                        property = new JsObjectImpl(parent, name, name.getOffsetRange(), parserResult.getSnapshot().getMimeType(), null);
                        parent.addProperty(name.getName(), property);
                    }
                }
            }
        }
        return super.leave(indexNode);
    }

    @Override
    public Node enter(FunctionNode functionNode) {
        addToPath(functionNode);
        List<FunctionNode> functions = new ArrayList<FunctionNode>(functionNode.getFunctions().size());
        // store all function nodes in cache
        for (FunctionNode fn : functionNode.getFunctions()) {
            functions.add(fn);
        }

        List<Identifier> name = null;
        boolean isPrivate = false;
        boolean isStatic = false;
        boolean isPrivilage = false;
        int pathSize = getPath().size();
        if (pathSize > 1 && getPath().get(pathSize - 2) instanceof ReferenceNode) {
            // is the function declared as variable or field
            //      var fn = function () {} or in object literal or this.fn = function () {}
            List<FunctionNode> siblings = functionStack.get(functionStack.size() - 1);
            siblings.remove(functionNode);

            if (pathSize > 3) {
                Node node = getPath().get(pathSize - 3);
                if (node instanceof PropertyNode) {
                    name = getName((PropertyNode)node);
                } else if (node instanceof BinaryNode) {
                    BinaryNode bNode = (BinaryNode)node;
                    if (bNode.lhs() instanceof AccessNode ) {
                        AccessNode aNode = (AccessNode)bNode.lhs();
                        if (aNode.getBase() instanceof IdentNode) {
                            IdentNode iNode = (IdentNode)aNode.getBase();
                            if ("this".equals(iNode.getName())) {
                                isPrivilage = true;
                            }
                        }
                    }
                    name = getName((BinaryNode)node, parserResult);
                } else if (node instanceof VarNode) {
                   name = getName((VarNode)node, parserResult);
                    // private method
                    // It can be only if it's in a function
                    isPrivate = functionStack.size() > 1;
                }
            }
        }

        if (name == null || name.isEmpty()) {
            // function is declared as
            //      function fn () {}
            name = new ArrayList<Identifier>(1);
            int start = functionNode.getIdent().getStart();
            int end = functionNode.getIdent().getFinish();
            if(end == 0) {
                end = parserResult.getSnapshot().getText().length();
            }
            name.add(new IdentifierImpl(functionNode.getIdent().getName(), new OffsetRange(start, end)));
            if (pathSize > 2 && getPath().get(pathSize - 2) instanceof FunctionNode) {
                isPrivate = true;
                //isStatic = true;
            }
        }
        functionStack.add(functions);

        JsFunctionImpl fncScope = (JsFunctionImpl)modelBuilder.getCurrentDeclarationFunction();
        JsObject parent = null;
        if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
            // create the function object
            DeclarationScopeImpl scope = modelBuilder.getCurrentDeclarationFunction();
            boolean isAnonymous = false;
            if (getPreviousFromPath(2) instanceof ReferenceNode) {
                Node node = getPreviousFromPath(3);
                if (node instanceof CallNode || node instanceof ExecuteNode) {
                    isAnonymous = true;
                } else if (node instanceof AccessNode && getPreviousFromPath(4) instanceof CallNode) {
                    String methodName = ((AccessNode)node).getProperty().getName();
                    if ("call".equals(methodName) || "apply".equals(methodName)) {  //NOI18N
                        isAnonymous = true;
                    }
                } 
            }
            if (canBeSingletonPattern()) {
                // follow the patter to create new objects via new anonymous function 
                // exp: this.pro = new function () { this.field = "";}
                parent = resolveThis(fncScope);
            }
            fncScope = ModelElementFactory.create(parserResult, functionNode, name, modelBuilder, isAnonymous, parent);
            if (fncScope != null) {
                Set<Modifier> modifiers = fncScope.getModifiers();
                if (isPrivate || isPrivilage) {
                    modifiers.remove(Modifier.PUBLIC);
                    if (isPrivate) {
                        modifiers.add(Modifier.PRIVATE);
                    } else {
                        modifiers.add(Modifier.PROTECTED);
                    }
                }
                if (isStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                scope.addDeclaredScope(fncScope);
                // push the current function in the model builder stack
                modelBuilder.setCurrentObject((JsObjectImpl)fncScope);
            }
        }
        if (fncScope != null) {
            JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
            // create variables that are declared in the function
            // They has to be created here for tracking occurrences
            if (canBeSingletonPattern()) {
                parent = resolveThis(fncScope);
            } else {
                parent = fncScope;
            }
            for (VarNode varNode : functionNode.getDeclarations()) {
                Identifier varName = new IdentifierImpl(varNode.getName().getName(), new OffsetRange(varNode.getName().getStart(), varNode.getName().getFinish()));
                OffsetRange range = varNode.getInit() instanceof ObjectNode ? new OffsetRange(varNode.getName().getStart(), ((ObjectNode)varNode.getInit()).getFinish()) 
                        : varName.getOffsetRange();
                JsObject variable = handleArrayCreation(varNode.getInit(), parent, varName);
                if (variable == null) {
                    JsObjectImpl newObject = new JsObjectImpl(parent, varName, range, parserResult.getSnapshot().getMimeType(), null);
                    newObject.setDeclared(true);
                    if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                        // here are the variables allways private
                        newObject.getModifiers().remove(Modifier.PUBLIC);
                        newObject.getModifiers().add(Modifier.PRIVATE);
                    }
                    variable = newObject;
                }

                variable.addOccurrence(varName.getOffsetRange());
                parent.addProperty(varName.getName(), variable);
                if (docHolder != null) {
                    ((JsObjectImpl)variable).setDocumentation(docHolder.getDocumentation(varNode));
                    ((JsObjectImpl)variable).setDeprecated(docHolder.isDeprecated(varNode));
                }

            }

            for (FunctionNode fn : functions) {
                if (fn.getIdent().getStart() < fn.getIdent().getFinish()) {
                    // go through all functions defined via reference
                    String functionName = fn.getIdent().getName();
                    if (!(functionName.startsWith("get ") || functionName.startsWith("set "))) {  //NOI18N
                        // don't visit setter and getters in object literal
                        fn.accept(this);
                    }
                }
            }

            // mark constructors 
            if (fncScope != null && functionNode.getKind() != FunctionNode.Kind.SCRIPT && docHolder.isClass(functionNode)) {
                // needs to be marked before going through the nodes
                fncScope.setJsKind(JsElement.Kind.CONSTRUCTOR);
            }

            // go through all function statements
            for (Node node : functionNode.getStatements()) {
                node.accept(this);
            }


            if (fncScope != null) {
                // check parameters and return types of the function.
                fncScope.setDeprecated(docHolder.isDeprecated(functionNode));
                List<Type> types = docHolder.getReturnType(functionNode);
                if (types != null && !types.isEmpty()) {
                    for(Type type : types) {
                        fncScope.addReturnType(new TypeUsageImpl(type.getType(), type.getOffset(), ModelUtils.isKnownGLobalType(type.getType())));
                    }
                }
                if (fncScope.areReturnTypesEmpty()) {
                    // the function doesn't have return statement -> returns undefined
                    fncScope.addReturnType(new TypeUsageImpl(Type.UNDEFINED, -1, false));
                }

                List<DocParameter> docParams = docHolder.getParameters(functionNode);
                for (DocParameter docParameter : docParams) {
                    DocIdentifier paramName = docParameter.getParamName();
                    if (paramName != null) {
                        String sParamName = paramName.getName();
                        if(sParamName != null && !sParamName.isEmpty()) {
                            JsObjectImpl param = (JsObjectImpl) fncScope.getParameter(sParamName);
                            if (param != null) {
                                for (Type type : docParameter.getParamTypes()) {
                                    param.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset(), true), param.getOffset());
                                }
                                // param occurence in the doc
                                addDocNameOccurence(param);
                            }
                        }
                    }
                }

                List<Type> extendTypes = docHolder.getExtends(functionNode);
                if (!extendTypes.isEmpty()) {
                    JsObject prototype = fncScope.getProperty(ModelUtils.PROTOTYPE);
                    if (prototype == null) {
                        prototype = new JsObjectImpl(fncScope, ModelUtils.PROTOTYPE, true, OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC), parserResult.getSnapshot().getMimeType(), null);
                        fncScope.addProperty(ModelUtils.PROTOTYPE, prototype);
                    }
                    for (Type type : extendTypes) {
                        prototype.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset(), true), type.getOffset());
                    }
                }

                setModifiersFromDoc(fncScope, docHolder.getModifiers(functionNode));
            }

            for (FunctionNode fn : functions) {
                // go through all functions defined as function fn () {...}
                if (fn.getIdent().getStart() >= fn.getIdent().getFinish()) {
                    fn.accept(this);
                }
            }
        }
        
        if (fncScope != null && functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
            // pop the current level from model builder stack
            modelBuilder.reset();
        }
        functionStack.remove(functionStack.size() - 1);
        removeFromPathTheLast();
        return null;
    }

    private JsArray handleArrayCreation(Node initNode, JsObject parent, Identifier name) {
        if (initNode instanceof UnaryNode) {
            UnaryNode uNode = (UnaryNode)initNode;
            if (uNode.tokenType() == TokenType.NEW && uNode.rhs() instanceof CallNode) {
                CallNode cNode = (CallNode)uNode.rhs();
                if (cNode.getFunction() instanceof IdentNode && "Array".equals(((IdentNode)cNode.getFunction()).getName())) {
                    List<TypeUsage> itemTypes = new ArrayList<TypeUsage>();
                    for (Node node : cNode.getArgs()) {
                        itemTypes.addAll(ModelUtils.resolveSemiTypeOfExpression(parserResult, node));
                    }
                    EnumSet<Modifier> modifiers = parent.getJSKind() != JsElement.Kind.FILE ? EnumSet.of(Modifier.PRIVATE) : EnumSet.of(Modifier.PUBLIC);
                    JsArrayImpl result = new JsArrayImpl(parent, name, name.getOffsetRange(), true, modifiers, parserResult.getSnapshot().getMimeType(), null);
                    result.addTypesInArray(itemTypes);
                    return result;
                }
            }
        }
        return null;
    }
    
    @Override
    public Node enter(LiteralNode lNode) {
        Node lastVisited = getPreviousFromPath(1);
        if (lNode instanceof LiteralNode.ArrayLiteralNode) {
            LiteralNode.ArrayLiteralNode aNode = (LiteralNode.ArrayLiteralNode)lNode;
            List<Identifier> fqName = null;
            int pathSize = getPath().size();
            boolean isDeclaredInParent = false;
            boolean isPrivate = false;
            boolean treatAsAnonymous = false;
            JsObject parent = null;
            
            if (lastVisited instanceof TernaryNode && pathSize > 1) {
                lastVisited = getPath().get(pathSize - 2);
            } 
            int pathIndex = 1;
            
            while(lastVisited instanceof BinaryNode 
                    && (pathSize > pathIndex)
                    && ((BinaryNode)lastVisited).tokenType() != TokenType.ASSIGN) {
                pathIndex++;
                lastVisited = getPath().get(pathSize - pathIndex);
            }
            if ( lastVisited instanceof VarNode) {
                fqName = getName((VarNode)lastVisited, parserResult);
                isDeclaredInParent = true;
                JsObject declarationScope = modelBuilder.getCurrentDeclarationFunction();
                parent = declarationScope;
                if (fqName.size() == 1 && !ModelUtils.isGlobal(declarationScope)) {
                    isPrivate = true;
                }
            } else if (lastVisited instanceof PropertyNode) {
                fqName = getName((PropertyNode) lastVisited);
                isDeclaredInParent = true;
            } else if (lastVisited instanceof BinaryNode) {
                BinaryNode binNode = (BinaryNode) lastVisited;
                if (binNode.lhs() instanceof IndexNode) {
                    Node index =  ((IndexNode)binNode.lhs()).getIndex();
                    if (!(index instanceof LiteralNode && ((LiteralNode)index).isString())) {
                        treatAsAnonymous = true;
                    }
                } 
                if (!treatAsAnonymous) {
                    if (getPath().size() > 1) {
                        lastVisited = getPath().get(getPath().size() - pathIndex - 1);
                    }
                    fqName = getName(binNode, parserResult);
                    if (binNode.lhs() instanceof IdentNode || (binNode.lhs() instanceof AccessNode
                            && ((AccessNode) binNode.lhs()).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binNode.lhs()).getBase()).getName().equals("this"))) {
                        isDeclaredInParent = true;
                        if (!(binNode.lhs() instanceof IdentNode)) {
                            parent = resolveThis(modelBuilder.getCurrentObject());
                        }
                    }
                }
            } else if (lastVisited instanceof CallNode || lastVisited instanceof LiteralNode.ArrayLiteralNode) {
                // probably an anonymous array as a parameter of a function call
                // or array in an array: var a = [['a', 10], ['b', 20]];
                treatAsAnonymous = true;
            }
            if (!isDeclaredInParent) {
                if (lastVisited instanceof FunctionNode) {
                    isDeclaredInParent = ((FunctionNode) lastVisited).getKind() == FunctionNode.Kind.SCRIPT;
                }
            }
            JsArrayImpl array;
            if (!treatAsAnonymous) {
                if (fqName == null || fqName.isEmpty()) {
                    fqName = new ArrayList<Identifier>(1);
                    fqName.add(new IdentifierImpl("UNKNOWN", //NOI18N
                            new OffsetRange(lNode.getStart(), lNode.getFinish())));
                }
                
                
                array = ModelElementFactory.create(parserResult, aNode, fqName, modelBuilder, isDeclaredInParent, parent);
                if (array != null && isPrivate) {
                    array.getModifiers().remove(Modifier.PUBLIC);
                    array.getModifiers().add(Modifier.PRIVATE);
                }
            } else {
                array = ModelElementFactory.createAnonymousObject(parserResult, aNode, modelBuilder);
            }
            if (array != null) {
                int aOffset = fqName == null ? lastVisited.getStart() : fqName.get(fqName.size() - 1).getOffsetRange().getEnd();
                array.addAssignment(ModelUtils.resolveSemiTypeOfExpression(parserResult, lNode), aOffset);
                for (Node item : aNode.getArray()) {
                    array.addTypesInArray(ModelUtils.resolveSemiTypeOfExpression(parserResult, item));
                }
            }
        } 
        return super.enter(lNode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node enter(ObjectNode objectNode) {
        Node previousVisited = getPath().get(getPath().size() - 1);
        if(previousVisited instanceof CallNode
                || previousVisited instanceof LiteralNode.ArrayLiteralNode) {
            // TODO there should be handled anonymous object that are going as parameter to a funciton
            //create anonymous object
            JsObjectImpl object = ModelElementFactory.createAnonymousObject(parserResult, objectNode,  modelBuilder);
            modelBuilder.setCurrentObject(object);
            object.setJsKind(JsElement.Kind.OBJECT_LITERAL);
            if (!functionArgumentStack.isEmpty()) {
                functionArgumentStack.peek().add(object);
            }
            return super.enter(objectNode);
        } else if (previousVisited instanceof ReturnNode
                 || (previousVisited instanceof BinaryNode && ((BinaryNode)previousVisited).tokenType() == TokenType.COMMARIGHT)) {
            JsObjectImpl objectScope = ModelElementFactory.createAnonymousObject(parserResult, objectNode, modelBuilder);
            modelBuilder.setCurrentObject(objectScope);
            objectScope.setJsKind(JsElement.Kind.OBJECT_LITERAL);
        } else {
            List<Identifier> fqName = null;
            int pathSize = getPath().size();
            boolean isDeclaredInParent = false;
            boolean isDeclaredThroughThis = false;
            boolean isPrivate = false;
            boolean treatAsAnonymous = false;
            
            Node lastVisited = getPath().get(pathSize - 1);
            VarNode varNode = null;
            
            if (lastVisited instanceof TernaryNode && pathSize > 1) {
                lastVisited = getPath().get(pathSize - 2);
            } 
            int pathIndex = 1;
            while(lastVisited instanceof BinaryNode 
                    && (pathSize > pathIndex)
                    && ((BinaryNode)lastVisited).tokenType() != TokenType.ASSIGN) {
                pathIndex++;
                lastVisited = getPath().get(pathSize - pathIndex);
            }
            if ( lastVisited instanceof VarNode) {
                fqName = getName((VarNode)lastVisited, parserResult);
                isDeclaredInParent = true;
                JsObject declarationScope = modelBuilder.getCurrentDeclarationFunction();
                varNode = (VarNode)lastVisited;
                if (fqName.size() == 1 && !ModelUtils.isGlobal(declarationScope)) {
                    isPrivate = true;
                }
            } else if (lastVisited instanceof PropertyNode) {
                fqName = getName((PropertyNode) lastVisited);
                isDeclaredInParent = true;
            } else if (lastVisited instanceof BinaryNode) {
                BinaryNode binNode = (BinaryNode) lastVisited;
                Node binLhs = binNode.lhs();
                if (binLhs instanceof IndexNode) {
                    Node index =  ((IndexNode)binLhs).getIndex();
                    if (!(index instanceof LiteralNode && ((LiteralNode)index).isString())) {
                        treatAsAnonymous = true;
                    }
                } 
                if (!treatAsAnonymous) {
                    if (getPath().size() > 1) {
                        lastVisited = getPath().get(getPath().size() - pathIndex - 1);
                        if (lastVisited instanceof VarNode) {
                            varNode = (VarNode) lastVisited;
                        }
                    }
                    fqName = getName(binNode, parserResult);
                    if (binLhs instanceof IdentNode || (binLhs instanceof AccessNode
                            && ((AccessNode) binLhs).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binLhs).getBase()).getName().equals("this"))) {
                        // if it's not declared throgh the var node, then the variable doesn't have to be declared here
                        isDeclaredInParent = (binLhs instanceof IdentNode &&  varNode != null);
                        if (binLhs instanceof AccessNode) {
                            isDeclaredInParent = true;
                            isDeclaredThroughThis = true;
                        }
                    }
                }
            }
            if (!isDeclaredInParent) {
                if (lastVisited instanceof FunctionNode) {
                    isDeclaredInParent = ((FunctionNode) lastVisited).getKind() == FunctionNode.Kind.SCRIPT;
                }
            }
            if (!treatAsAnonymous) {
                if (fqName == null || fqName.isEmpty()) {
                    fqName = new ArrayList<Identifier>(1);
                    fqName.add(new IdentifierImpl("UNKNOWN", //NOI18N
                            new OffsetRange(objectNode.getStart(), objectNode.getFinish())));
                }
                JsObjectImpl objectScope;
                if (varNode != null) {
                    objectScope = modelBuilder.getCurrentObject();
                } else {
                    Identifier name = fqName.get(fqName.size() - 1);
                    JsObject alreadyThere = null;
                    if (isDeclaredThroughThis) {
                        JsObject thisIs = resolveThis(modelBuilder.getCurrentObject());
                        alreadyThere = thisIs.getProperty(name.getName());
                    } else {
                        if (isDeclaredInParent) {
                            alreadyThere = ModelUtils.getJsObjectByName(modelBuilder.getCurrentDeclarationFunction(), name.getName());
                        } else {
                            alreadyThere = ModelUtils.getJsObject(modelBuilder, fqName, true);
                        }
                    }
                     
                    objectScope = (alreadyThere == null) 
                            ? ModelElementFactory.create(parserResult, objectNode, fqName, modelBuilder, isDeclaredInParent)
                            : (JsObjectImpl)alreadyThere;
                    if (alreadyThere != null) {
                        ((JsObjectImpl)alreadyThere).addOccurrence(name.getOffsetRange());
                    }
                }
                if (objectScope != null) {
                    objectScope.setJsKind(JsElement.Kind.OBJECT_LITERAL);
                    if (!objectScope.isDeclared()) {
                        // the objec literal is always declared
                        objectScope.setDeclared(true);
                    }
                    modelBuilder.setCurrentObject(objectScope);
                    if (isPrivate) {
                        objectScope.getModifiers().remove(Modifier.PUBLIC);
                        objectScope.getModifiers().add(Modifier.PRIVATE);
                    }
                }
            } else {
                JsObjectImpl objectScope = ModelElementFactory.createAnonymousObject(parserResult, objectNode, modelBuilder);
                modelBuilder.setCurrentObject(objectScope);
            }
            
        }

        return super.enter(objectNode);
    }

    @Override
    public Node leave(ObjectNode objectNode) {
        modelBuilder.reset();
        return super.leave(objectNode);
    }

    @Override
    public Node enter(PropertyNode propertyNode) {
        if ((propertyNode.getKey() instanceof IdentNode || propertyNode.getKey() instanceof LiteralNode)
                && !(propertyNode.getValue() instanceof ObjectNode)) {
            JsObjectImpl scope = modelBuilder.getCurrentObject();
            Identifier name = null;
            Node key = propertyNode.getKey();
            if (key instanceof IdentNode) {
                name = ModelElementFactory.create(parserResult, (IdentNode)key);
            } else if (key instanceof LiteralNode) {
                name = ModelElementFactory.create(parserResult, (LiteralNode)key);
            }

            if (name != null) {
                JsObjectImpl property = (JsObjectImpl)scope.getProperty(name.getName());
                if (property == null) {
                    property = ModelElementFactory.create(parserResult, propertyNode, name, modelBuilder, true);
                } else {
                    // The property can be already defined, via a usage before declaration (see testfiles/model/simpleObject.js - called property)
                    JsObjectImpl newProperty = ModelElementFactory.create(parserResult, propertyNode, name, modelBuilder, true);
                    if (newProperty != null) {
                        newProperty.addOccurrence(property.getDeclarationName().getOffsetRange());
                        for(Occurrence occurrence : property.getOccurrences()) {
                            newProperty.addOccurrence(occurrence.getOffsetRange());
                        }
                        property = newProperty;
                    }
                }

                if (property != null) {
                    if (propertyNode.getGetter() != null) {
                        FunctionNode getter = ((FunctionNode)((ReferenceNode)propertyNode.getGetter()).getReference());
                        property.addOccurrence(new OffsetRange(getter.getIdent().getStart(), getter.getIdent().getFinish()));
                    }

                    if (propertyNode.getSetter() != null) {
                        FunctionNode setter = ((FunctionNode)((ReferenceNode)propertyNode.getSetter()).getReference());
                        property.addOccurrence(new OffsetRange(setter.getIdent().getStart(), setter.getIdent().getFinish()));
                    }
                    scope.addProperty(name.getName(), property);
                    property.setDeclared(true);
                    Node value = propertyNode.getValue();
                    if(value instanceof CallNode) {
                        // TODO for now, don't continue. There shoudl be handled cases liek
                        // in the testFiles/model/property02.js file
                        //return null;
                    } else {
                        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(parserResult, value);
                        if (!types.isEmpty()) {
                            property.addAssignment(types, name.getOffsetRange().getStart());
                        }
                        if (value instanceof IdentNode) {
                            IdentNode iNode = (IdentNode)value;
                            JsFunction function = (JsFunction)ModelUtils.getDeclarationScope(property);
                            String iName = iNode.getName();
                            JsObjectImpl param = (JsObjectImpl)function.getParameter(iName);
                            if(param != null) {
                                param.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                            } else {
                                Collection<? extends JsObject> variables = ModelUtils.getVariables((DeclarationScope)function);
                                for (JsObject variable : variables) {
                                    if (iName.equals(variable.getName())) {
                                        ((JsObjectImpl)variable).addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.enter(propertyNode);
    }

    @Override
    public Node enter(ReferenceNode referenceNode) {
        FunctionNode reference = referenceNode.getReference();
        if (reference != null) {
            addToPath(referenceNode);
            reference.accept(this);
            removeFromPathTheLast();
            return null;
        }
        return super.enter(referenceNode);
    }

    @Override
    public Node enter(ReturnNode returnNode) {
        Node expression = returnNode.getExpression();
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(parserResult, expression);
        if (expression == null) {
            types.add(new TypeUsageImpl(Type.UNDEFINED, returnNode.getStart(), true));
        } else {
            if (expression instanceof IdentNode) {
                addOccurence((IdentNode)expression, false);
            }
            if(types.isEmpty()) {
               types.add(new TypeUsageImpl(Type.UNRESOLVED, returnNode.getStart(), true));
            }
        }
        JsFunctionImpl function = modelBuilder.getCurrentDeclarationFunction();
        function.addReturnType(types);
        return super.enter(returnNode);
    }

    @Override
    public Node enter(TernaryNode ternaryNode) {
        if (ternaryNode.lhs() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.lhs(), false);
        }
        if (ternaryNode.rhs() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.rhs(), false);
        }
        if (ternaryNode.third() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.third(), false);
        }
        return super.enter(ternaryNode);
    }

    @Override
    public Node enter(UnaryNode unaryNode) {
        if (unaryNode.rhs() instanceof IdentNode) {
            addOccurence((IdentNode) unaryNode.rhs(), false);
        }
        return super.enter(unaryNode);
    }

    @Override
    public Node enter(VarNode varNode) {
         if (!(varNode.getInit() instanceof ObjectNode || varNode.getInit() instanceof ReferenceNode
                 || varNode.getInit() instanceof LiteralNode.ArrayLiteralNode)) {
            JsObject parent = modelBuilder.getCurrentObject();
            parent = canBeSingletonPattern(1) ? resolveThis(parent) : parent;
            if (parent instanceof CatchBlockImpl) {
                parent = parent.getParent();
            } 
            JsObjectImpl variable = (JsObjectImpl)parent.getProperty(varNode.getName().getName());
            Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
            if (name != null) {
                if (variable == null) {
                    // variable si not defined, so it has to be from global scope
                    // or from a code structure like for cycle

                    variable = new JsObjectImpl(parent, name, name.getOffsetRange(),
                            true, parserResult.getSnapshot().getMimeType(), null);
                    if (parent.getJSKind() != JsElement.Kind.FILE) {
                        variable.getModifiers().remove(Modifier.PUBLIC);
                        variable.getModifiers().add(Modifier.PRIVATE);
                    }
                    parent.addProperty(name.getName(), variable);
                    variable.addOccurrence(name.getOffsetRange());
                } else if (!variable.isDeclared()){
                    // the variable was probably created as temporary before, now we
                    // need to replace it with the real one
                    JsObjectImpl newVariable = new JsObjectImpl(parent, name, name.getOffsetRange(),
                            true, parserResult.getSnapshot().getMimeType(), null);
                    newVariable.addOccurrence(name.getOffsetRange());
                    for(String propertyName: variable.getProperties().keySet()) {
                        JsObject property = variable.getProperty(propertyName);
                        if (property instanceof JsObjectImpl) {
                            ((JsObjectImpl)property).setParent(newVariable);
                        }
                        newVariable.addProperty(propertyName, property);
                    }
                    if (parent.getJSKind() != JsElement.Kind.FILE) {
                        newVariable.getModifiers().remove(Modifier.PUBLIC);
                        newVariable.getModifiers().add(Modifier.PRIVATE);
                    }
                    for(TypeUsage type : variable.getAssignments()) {
                        newVariable.addAssignment(type, type.getOffset());
                    }
                    for(Occurrence occurrence: variable.getOccurrences()){
                        newVariable.addOccurrence(occurrence.getOffsetRange());
                    }
                    parent.addProperty(name.getName(), newVariable);
                    variable = newVariable;
                }
                JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
                variable.setDeprecated(docHolder.isDeprecated(varNode));
                variable.setDocumentation(docHolder.getDocumentation(varNode));
                modelBuilder.setCurrentObject(variable);
                if (varNode.getInit() instanceof IdentNode) {
                    addOccurence((IdentNode)varNode.getInit(), false);
                }
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(parserResult, varNode.getInit());
                for (TypeUsage type : types) {
                    variable.addAssignment(type, varNode.getName().getFinish());
                }
                List<Type> returnTypes = docHolder.getReturnType(varNode);
                if (returnTypes != null && !returnTypes.isEmpty()) {
                    for (Type type : returnTypes) {
                        variable.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset(), true), varNode.getName().getFinish());
                    }
                }
            }
        } else if(varNode.getInit() instanceof ObjectNode) {
            JsObjectImpl function = modelBuilder.getCurrentDeclarationFunction();
            Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
            if (name != null) {
                JsObjectImpl variable = (JsObjectImpl)function.getProperty(name.getName());
                if (variable != null) {
                    variable.setDeclared(true);
                } else {
                    List<Identifier> fqName = getName(varNode, parserResult);
                    variable = ModelElementFactory.create(parserResult, (ObjectNode)varNode.getInit(), fqName, modelBuilder, true);
                }
                if (variable != null) {
                    variable.setJsKind(JsElement.Kind.OBJECT_LITERAL);
                    modelBuilder.setCurrentObject(variable);
                }
            }
        }
        return super.enter(varNode);
    }

    @Override
    public Node leave(VarNode varNode) {
        if (!(varNode.getInit() instanceof ReferenceNode || varNode.getInit() instanceof LiteralNode.ArrayLiteralNode)
                // XXX can we avoid creation of object ?
                && ModelElementFactory.create(parserResult, varNode.getName()) != null) {
            modelBuilder.reset();
        }
        return super.leave(varNode);
    }

//--------------------------------End of visit methods--------------------------------------

    public Map<FunctionInterceptor, Collection<FunctionCall>> getCallsForProcessing() {
        return functionCalls;
    }

    private boolean fillName(AccessNode node, List<String> result) {
        List<Identifier> fqn = getName(node, parserResult);
        if (fqn != null) {
            for (int i = fqn.size() - 1; i >= 0; i--) {
                result.add(0, fqn.get(i).getName());
            }
        }

        JsObject current = modelBuilder.getCurrentObject();
        while (current != null && current.getDeclarationName() != null) {
            if (current != modelBuilder.getGlobal()) {
                result.add(0, current.getDeclarationName().getName());
            }
            current = current.getParent();
        }
        return true;
    }

    private boolean fillName(IndexNode node, List<String> result) {
        Node index = node.getIndex();
        Node base = node.getBase();
        if (index instanceof LiteralNode && base instanceof AccessNode) {
            LiteralNode literal = (LiteralNode) index;
            if (literal.isString()) {
                result.add(0, literal.getString());
                List<Identifier> fqn = getName((AccessNode) base, parserResult);
                for (int i = fqn.size() - 1; i >= 0; i--) {
                    result.add(0, fqn.get(i).getName());
                }
                return true;
            }
        }
        return false;
    }

    private List<Identifier> getName(PropertyNode propertyNode) {
        List<Identifier> name = new ArrayList(1);
        if (propertyNode.getGetter() != null || propertyNode.getSetter() != null) {
            // check whether this is not defining getter or setter of a property.
            Node previousNode = getPreviousFromPath(1);
            if (previousNode instanceof FunctionNode) {
                FunctionNode fNode = (FunctionNode)previousNode;
                String fName = fNode.getIdent().getName();
                if (fName.startsWith("get ") || fName.startsWith("set ")) { //NOI18N
                    name.add(new IdentifierImpl(fName,
                        new OffsetRange(fNode.getIdent().getStart(), fNode.getIdent().getFinish())));
                    return name;
                }
            }
        }
        return getName(propertyNode, parserResult);
    }

    private static List<Identifier> getName(PropertyNode propertyNode, JsParserResult parserResult) {
        List<Identifier> name = new ArrayList(1);
        if (propertyNode.getKey() instanceof IdentNode) {
            IdentNode ident = (IdentNode) propertyNode.getKey();
            name.add(new IdentifierImpl(ident.getName(),
                    new OffsetRange(ident.getStart(), ident.getFinish())));
        } else if (propertyNode.getKey() instanceof LiteralNode){
            LiteralNode lNode = (LiteralNode)propertyNode.getKey();
            name.add(new IdentifierImpl(lNode.getString(),
                    new OffsetRange(lNode.getStart(), lNode.getFinish())));
        }
        return name;
    }

    private static List<Identifier> getName(VarNode varNode, JsParserResult parserResult) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(varNode.getName().getName(),
                new OffsetRange(varNode.getName().getStart(), varNode.getName().getFinish())));
        return name;
    }

    private static List<Identifier> getName(BinaryNode binaryNode, JsParserResult parserResult) {
        List<Identifier> name = new ArrayList();
        Node lhs = binaryNode.lhs();
        if (lhs instanceof AccessNode) {
            name = getName((AccessNode)lhs, parserResult);
        } else if (lhs instanceof IdentNode) {
            IdentNode ident = (IdentNode) lhs;
            name.add(new IdentifierImpl(ident.getName(),
                        new OffsetRange(ident.getStart(), ident.getFinish())));
        } else if (lhs instanceof IndexNode) {
            IndexNode indexNode = (IndexNode)lhs;
            if (indexNode.getBase() instanceof AccessNode) {
                List<Identifier> aName = getName((AccessNode)indexNode.getBase(), parserResult);
                if (aName != null) {
                    name.addAll(getName((AccessNode)indexNode.getBase(), parserResult));
                }
                else {
                    return null;
                }
            }
            if (indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode lNode = (LiteralNode)indexNode.getIndex();
                name.add(new IdentifierImpl(lNode.getPropertyName(), 
                        new OffsetRange(lNode.getStart(), lNode.getFinish())));
            }
        }
        return name;
    }

    private static List<Identifier> getName(AccessNode aNode, JsParserResult parserResult) {
        List<Identifier> name = new ArrayList();
        name.add(new IdentifierImpl(aNode.getProperty().getName(),
                new OffsetRange(aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        while (aNode.getBase() instanceof AccessNode) {
            aNode = (AccessNode) aNode.getBase();
            name.add(new IdentifierImpl(aNode.getProperty().getName(),
                    new OffsetRange(aNode.getProperty().getStart(), aNode.getProperty().getFinish())));
        }
        if (aNode.getBase() instanceof IdentNode) {
            if (name.size() > 0 && aNode.getBase() instanceof IdentNode) {
                IdentNode ident = (IdentNode) aNode.getBase();
                if (!"this".equals(ident.getName())) {
                    name.add(new IdentifierImpl(ident.getName(),
                            new OffsetRange(ident.getStart(), ident.getFinish())));
                }
            }
            Collections.reverse(name);
            return name;
        } else {
            return null;
        }
    }

    /**
     * Gets the node name if it has any (case of AccessNode, BinaryNode, VarNode, PropertyNode).
     *
     * @param node examined node for getting its name
     * @return name of the node if it supports it
     */
    public static List<Identifier> getNodeName(Node node, JsParserResult parserResult) {
        if (node instanceof AccessNode) {
            return getName((AccessNode) node, parserResult);
        } else if (node instanceof BinaryNode) {
            return getName((BinaryNode) node, parserResult);
        } else if (node instanceof VarNode) {
            return getName((VarNode) node, parserResult);
        } else if (node instanceof PropertyNode) {
            return getName((PropertyNode) node, parserResult);
        } else if (node instanceof FunctionNode) {
            if (((FunctionNode) node).getKind() == FunctionNode.Kind.SCRIPT) {
                return Collections.<Identifier>emptyList();
            }
            IdentNode ident = ((FunctionNode) node).getIdent();
            return Arrays.<Identifier>asList(new IdentifierImpl(
                    ident.getName(),
                    new OffsetRange(ident.getStart(), ident.getFinish())));
        } else {
            return Collections.<Identifier>emptyList();
        }
    }

//    private Variable findVarWithName(final Scope scope, final String name) {
//        Variable result = null;
//        Collection<Variable> variables = ScopeImpl.filter(scope.getElements(), new ScopeImpl.ElementFilter() {
//
//            @Override
//            public boolean isAccepted(ModelElement element) {
//                return element.getJSKind().equals(JsElement.Kind.VARIABLE)
//                        && element.getName().equals(name);
//            }
//        });
//
//        if (!variables.isEmpty()) {
//            result = variables.iterator().next();
//        } else {
//            if (!(scope instanceof FileScope)) {
//                result = findVarWithName((Scope)scope.getInElement(), name);
//            }
//        }
//
//        return result;
//    }
//
//    private Field findFieldWithName(FunctionScope function, final String name) {
//        Field result = null;
//        Collection<? extends Field> fields = function.getFields();
//        result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
//        if (result == null && function.getInElement() instanceof FunctionScope) {
//            FunctionScope parent = (FunctionScope)function.getInElement();
//            fields = parent.getFields();
//            result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
//        }
//        return result;
//    }

    private boolean isInPropertyNode() {
        boolean inFunction = false;
        for (int i = getPath().size() - 1; i > 0 ; i--) {
            final Node node = getPath().get(i);
            if(node instanceof FunctionNode) {
                if (!inFunction) {
                    inFunction = true;
                } else {
                    return false;
                }
            } else if (node instanceof PropertyNode) {
                return true;
            }
        }
        return false;
    }

    private void addOccurence(IdentNode iNode, boolean leftSite) {
        addOccurence(iNode, leftSite, false, 0);
    }

    private void addOccurence(IdentNode iNode, boolean leftSite, boolean isFunction, int countParam) {
        if ("this".equals(iNode.getName())) {
            // don't process this node.
            return;
        }
        DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
        JsObject property = null;
        JsObject parameter = null;
        while (scope != null && property == null && parameter == null) {
            JsFunction function = (JsFunction)scope;
            property = function.getProperty(iNode.getName());
            parameter = function.getParameter(iNode.getName());
            scope = scope.getParentScope();
        }
        if(parameter != null) {
            if (property == null) {
                property = parameter;
            } else {
                if(property.getJSKind() != JsElement.Kind.VARIABLE) {
                    property = parameter;
                }
            }
        }

        if (property != null) {

            // occurence in the doc
            addDocNameOccurence(((JsObjectImpl)property));
            addDocTypesOccurence(((JsObjectImpl)property));

            ((JsObjectImpl)property).addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
        } else {
            // it's a new global variable?
            IdentifierImpl name = ModelElementFactory.create(parserResult, iNode);
            if (name != null) {
                JsObjectImpl newObject;
                if (!isFunction) {
                    newObject = new JsObjectImpl(modelBuilder.getGlobal(), name, name.getOffsetRange(),
                            leftSite, parserResult.getSnapshot().getMimeType(), null);
                } else {
                    FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                    newObject = new JsFunctionImpl(fo, modelBuilder.getGlobal(), name, Collections.EMPTY_LIST,
                            parserResult.getSnapshot().getMimeType(), null);
                }
                newObject.addOccurrence(name.getOffsetRange());
                modelBuilder.getGlobal().addProperty(name.getName(), newObject);
            }
        }
    }
    
    private void addDocNameOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = parserResult.getDocumentationHolder();
        JsComment comment = holder.getCommentForOffset(jsObject.getOffset(), holder.getCommentBlocks());
        if (comment != null) {
            for (DocParameter docParameter : comment.getParameters()) {
                DocIdentifier paramName = docParameter.getParamName();
                String name = (docParameter.getParamName() == null) ? "" : docParameter.getParamName().getName(); //NOI18N
                if (name.equals(jsObject.getName())) {
                    jsObject.addOccurrence(DocumentationUtils.getOffsetRange(paramName));
                }
            }
        }
    }

    private void addDocTypesOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = parserResult.getDocumentationHolder();
        if (holder.getOccurencesMap().containsKey(jsObject.getName())) {
            for (OffsetRange offsetRange : holder.getOccurencesMap().get(jsObject.getName())) {
                ((JsObjectImpl)jsObject).addOccurrence(offsetRange);
            }
        }
    }

    private Node getPreviousFromPath(int back) {
        int size = getPath().size();
        if (size >= back) {
            return getPath().get(size - back);
        }
        return null;
    }

    /**
     * 
     * @param where the declaration context, where this is used
     * @return JsObject that should represent this. 
     */
    private JsObject resolveThis(JsObject where) {
        JsElement.Kind whereKind = where.getJSKind();
        if (canBeSingletonPattern()) {
            JsObject result = resolveThisInSingletonPattern(where);
            if (result != null) {
                return result;
            }
        }
        if (whereKind == JsElement.Kind.FILE) {
            // this is used in global context
            return where;
        }
        if (whereKind.isFunction() && where.getModifiers().contains(Modifier.PRIVATE)) {
            // the case where is defined private function in another function
            return where;
        }
        JsObject parent = where.getParent();
        if (parent == null) {
            return where;
        }
        JsElement.Kind parentKind = parent.getJSKind();
        if (parentKind == JsElement.Kind.FILE && !where.isAnonymous()) {
            // this is used in a function that is in the global context
            return where;
        }
        if (ModelUtils.PROTOTYPE.equals(parent.getName())) {
            // this is used in a function defined in prototype object
            return where.getParent().getParent();
        }
        if (whereKind == JsElement.Kind.CONSTRUCTOR) {
            return where;
        }
        if (whereKind.isFunction() && !where.getModifiers().contains(Modifier.PRIVATE) && !where.isAnonymous()) {
            // public or protected method
            return parent;
        }
        if (isInPropertyNode()) {
            // this is used in a method of an object -> this is the object
            return parent;
        }
        if (where.isAnonymous()) {
            JsObject result = resolveThisInSingletonPattern(where);
            if (result != null) {
                return result;
            }
        }
        return where;
    }
    
    private JsObject resolveThisInSingletonPattern(JsObject where) {
        int pathIndex = 1;
        Node lastNode = getPreviousFromPath(1);
        if (lastNode instanceof FunctionNode && !canBeSingletonPattern(pathIndex)) {
            pathIndex++;
        }
        while (pathIndex < getPath().size() && !(getPreviousFromPath(pathIndex) instanceof FunctionNode)) {
            pathIndex++;
        }
        // trying to find out that it corresponds with patter, where an object is defined via new function:
        // exp: this.pro = new function () { this.field = "";}
        if (canBeSingletonPattern(pathIndex)) {
            UnaryNode uNode = (UnaryNode) getPreviousFromPath(pathIndex + 3);
            if (uNode.tokenType() == TokenType.NEW) {

                String name = null;
                boolean simpleName = true;
                if (getPreviousFromPath(pathIndex + 4) instanceof BinaryNode) {
                    BinaryNode bNode = (BinaryNode) getPreviousFromPath(pathIndex + 4);
                    if (bNode.tokenType() == TokenType.ASSIGN) {
                        if (bNode.lhs() instanceof AccessNode) {
                            List<Identifier> identifier = getName((AccessNode) bNode.lhs(), parserResult);
                            if (identifier.size() == 1) {
                                name = identifier.get(0).getName();
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (Identifier part : identifier) {
                                    sb.append(part.getName()).append('.');
                                }
                                name = sb.toString().substring(0, sb.length() - 1);
                                simpleName = false;
                            }
                        } else if (bNode.lhs() instanceof IdentNode) {
                            name = ((IdentNode) bNode.lhs()).getName();
                        }
                    }
                } else if (getPreviousFromPath(pathIndex + 4) instanceof VarNode) {
                    VarNode vNode = (VarNode)getPreviousFromPath(pathIndex + 4);
                    name = vNode.getName().getName();
                }
                
                JsObject parent = where.getParent() == null ? where : where.getParent();
                if (name != null) {
                    if (simpleName) {
                        parent = where;
                        while (parent != null && parent.getProperty(name) == null) {
                            parent = parent.getParent();
                        }
                        if (parent != null && parent.getProperty(name) != null) {
                            return parent.getProperty(name);
                        }
                    } else {
                        JsObject property = ModelUtils.findJsObjectByName(ModelUtils.getGlobalObject(parent), name);
                        if (property != null) {
                            return property;
                        }
                    }

                }
            }
        }
        return null;
    }
    
    private boolean canBeSingletonPattern() {
        int pathIndex = 1;
        Node lastNode = getPreviousFromPath(1);
        if (lastNode instanceof FunctionNode && !canBeSingletonPattern(pathIndex)) {
            pathIndex++;
        } 
        while (pathIndex < getPath().size() && !(getPreviousFromPath(pathIndex) instanceof FunctionNode)) {
            pathIndex++;
        }
        return canBeSingletonPattern(pathIndex);
    }
    
    private boolean canBeSingletonPattern(int pathIndex) {
       return  (getPath().size() > pathIndex + 4 && getPreviousFromPath(pathIndex) instanceof FunctionNode
                    && getPreviousFromPath(pathIndex + 1) instanceof ReferenceNode
                    && getPreviousFromPath(pathIndex + 2) instanceof CallNode
                    && getPreviousFromPath(pathIndex + 3) instanceof UnaryNode
                    && (getPreviousFromPath(pathIndex + 4) instanceof BinaryNode
                        || getPreviousFromPath(pathIndex + 4) instanceof VarNode));
    }
    
    private boolean isPriviliged(AccessNode aNode) {
        Node node = aNode.getBase();
        while (node instanceof AccessNode) {
            node = ((AccessNode)node).getBase();
        }
        if (node instanceof IdentNode && "this".endsWith(((IdentNode)node).getName())) {
            return true;
        }
        return false;
    }
    
    private void setModifiersFromDoc(JsObject object, Set<JsModifier> modifiers) {
        if (modifiers != null && !modifiers.isEmpty()) {
            for (JsModifier jsModifier : modifiers) {
                switch (jsModifier) {
                    case PRIVATE:
                        object.getModifiers().remove(Modifier.PROTECTED);
                        object.getModifiers().remove(Modifier.PUBLIC);
                        object.getModifiers().add(Modifier.PRIVATE);
                        break;
                    case PUBLIC:
                        object.getModifiers().remove(Modifier.PROTECTED);
                        object.getModifiers().remove(Modifier.PRIVATE);
                        object.getModifiers().add(Modifier.PUBLIC);
                        break;
                    case STATIC:
                        object.getModifiers().add(Modifier.STATIC);
                        break;
                }
            }
        }
    }
    
    public static class FunctionCall {

        private final String name;

        private final DeclarationScope scope;

        private final Collection<FunctionArgument> arguments;

        public FunctionCall(String name, DeclarationScope scope,
                Collection<FunctionArgument> arguments) {
            this.name = name;
            this.scope = scope;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public DeclarationScope getScope() {
            return scope;
        }

        public Collection<FunctionArgument> getArguments() {
            return arguments;
        }
    }
}
