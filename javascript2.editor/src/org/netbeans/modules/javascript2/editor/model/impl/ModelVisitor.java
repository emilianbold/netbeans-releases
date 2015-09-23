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
import jdk.nashorn.internal.ir.ExecuteNode;
import jdk.nashorn.internal.ir.ForNode;
import jdk.nashorn.internal.ir.LabelNode;
import jdk.nashorn.internal.ir.WithNode;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
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
import org.netbeans.modules.javascript2.editor.model.JsWith;
import org.netbeans.modules.javascript2.editor.model.Model;
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
    private final OccurrenceBuilder occurrenceBuilder;
    /**
     * Keeps the name of the visited properties
     */
    private final List<List<FunctionNode>> functionStack;
    private final JsParserResult parserResult;

    // keeps objects that are created as arguments of a function call
    private final Stack<Collection<JsObjectImpl>> functionArgumentStack = new Stack<Collection<JsObjectImpl>>();
    private Map<FunctionInterceptor, Collection<FunctionCall>> functionCalls = null;
    private final String scriptName;
    
//    private JsObjectImpl fromAN = null;

    public ModelVisitor(JsParserResult parserResult, OccurrenceBuilder occurrenceBuilder) {
        FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        this.modelBuilder = new ModelBuilder(JsFunctionImpl.createGlobal(
                fileObject, Integer.MAX_VALUE, parserResult.getSnapshot().getMimeType()));
        this.occurrenceBuilder = occurrenceBuilder;
        this.functionStack = new ArrayList<List<FunctionNode>>();
        this.parserResult = parserResult; 
        this.scriptName = fileObject != null ? fileObject.getName().replace('.', '_') : "";
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
                    property = current.getProperty(iNode.getName());
                    if (property == null && ModelUtils.PROTOTYPE.equals(current.getName())) {
                        current = current.getParent();
                        property = current.getProperty(iNode.getName());
                    }
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
        createJsObject(accessNode, parserResult, modelBuilder);
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
            String fieldName = null;
            if (lhs instanceof AccessNode) {
                AccessNode aNode = (AccessNode)lhs;
                JsObjectImpl property = null;
                List<Identifier> fqName = getName(aNode, parserResult);
                if (fqName != null && "this".equals(fqName.get(0).getName())) { //NOI18N
                    // a usage of field
                    fieldName = aNode.getProperty().getName();
                    if (binaryNode.rhs() instanceof IdentNode) {
                        // resolve occurrence of the indent node sooner, then is created the field. 
                        addOccurrence((IdentNode)binaryNode.rhs(), fieldName);
                    }
                    property = (JsObjectImpl)createJsObject(aNode, parserResult, modelBuilder);
//                    parent = (JsObjectImpl)property.getParent();
//                    if(property == null) {
//                        Identifier identifier = ModelElementFactory.create(parserResult, (IdentNode)aNode.getProperty());
//                        if (identifier != null) {
//                            property = new JsObjectImpl(parent, identifier, identifier.getOffsetRange(), true, parserResult.getSnapshot().getMimeType(), null);
//                            parent.addProperty(fieldName, property);
//                            JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
//                            if (docHolder != null) {
//                                property.setDocumentation(docHolder.getDocumentation(aNode));
//                                property.setDeprecated(docHolder.isDeprecated(aNode));
//                            }
//                        }
//                    }
                } else {
                    // probably a property of an object
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
                        types =  ModelUtils.resolveSemiTypeOfExpression(modelBuilder, binaryNode.rhs());
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
                JsObject lObject = null;
                boolean indexNodeReferProperty = false;
                int assignmentOffset = lhs.getFinish();
                if (lhs instanceof IndexNode) {
                    IndexNode iNode = (IndexNode)lhs;
                    if (iNode.getBase() instanceof IdentNode) {
                        lObject = processLhs(ModelElementFactory.create(parserResult, (IdentNode)iNode.getBase()), parent, false);
                        assignmentOffset = iNode.getFinish();
                    }
                    if (lObject != null && iNode.getIndex() instanceof LiteralNode) {
                        LiteralNode lNode = (LiteralNode)iNode.getIndex();
                        if (lNode.isString()) {
                            Identifier newPropName = ModelElementFactory.create(parserResult, lNode);
                            if (newPropName != null) {
                                indexNodeReferProperty = true;
                                if (lObject.getProperty(lNode.getString()) == null) {
                                    JsObject newProperty = new JsObjectImpl(lObject, newPropName, newPropName.getOffsetRange(), true, parserResult.getSnapshot().getMimeType(), null);
                                    lObject.addProperty(newPropName.getName(), newProperty);
                                    assignmentOffset = lNode.getFinish();
                                }
                                lObject = processLhs(newPropName, lObject, true);
                            }
                        }
                    }
                } else if (lhs instanceof IdentNode) {
                    lObject = processLhs(ModelElementFactory.create(parserResult, (IdentNode)lhs), parent, true);
                }
                
                if (lObject != null) {
                    Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, binaryNode.rhs());
                    if (lhs instanceof IndexNode && lObject instanceof JsArrayImpl) {
                        ((JsArrayImpl)lObject).addTypesInArray(types);
                    } else {
                        boolean isIndexNode = lhs instanceof IndexNode;
                        if (!isIndexNode || (isIndexNode && indexNodeReferProperty)) {
                            for (TypeUsage type : types) {
                                lObject.addAssignment(type, assignmentOffset);
                            }
                        }
                    }
                }
            }
            if (fieldName == null && binaryNode.rhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.rhs(), false);
            }
        } else if(binaryNode.tokenType() != TokenType.ASSIGN
                || (binaryNode.tokenType() == TokenType.ASSIGN && binaryNode.lhs() instanceof IndexNode)) {
            if (binaryNode.lhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.lhs(), binaryNode.tokenType() == TokenType.ASSIGN);
            }
            if (binaryNode.rhs() instanceof IdentNode) {
                addOccurence((IdentNode)binaryNode.rhs(), false);
            }
        } else if(binaryNode.tokenType() == TokenType.ASSIGN && rhs instanceof ReferenceNode) {
            
        }
        return super.enter(binaryNode);
    }

    @Override
    public Node leave(BinaryNode binaryNode) {
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (lhs instanceof IdentNode && rhs instanceof BinaryNode) {
            Node rlhs = ((BinaryNode)rhs).lhs();
            if (rlhs instanceof IdentNode) {
                JsObject origFunction = modelBuilder.getCurrentDeclarationFunction().getProperty(((IdentNode)rlhs).getName());
                if (origFunction != null && origFunction.getJSKind().isFunction()) {
                    JsObject refFunction = modelBuilder.getCurrentDeclarationFunction().getProperty(((IdentNode)lhs).getName());
                    if (refFunction != null && !refFunction.getJSKind().isFunction()) {
                        JsFunctionReference newReference = new JsFunctionReference(refFunction.getParent(), refFunction.getDeclarationName(), (JsFunction)origFunction, true, origFunction.getModifiers());
                        refFunction.getParent().addProperty(newReference.getName(), newReference);
                    }
                }
            }
        }
        return super.leave(binaryNode); 
    }
    
    @Override
    public Node enter(CallNode callNode) {
        functionArgumentStack.push(new ArrayList<JsObjectImpl>(3));
        if (callNode.getFunction() instanceof IdentNode) {
            IdentNode iNode = (IdentNode)callNode.getFunction();
            addOccurence(iNode, false, true);
        }
        for (Node argument : callNode.getArgs()) {
            if (argument instanceof IdentNode) {
                addOccurence((IdentNode) argument, false);
            }
        }
        return super.enter(callNode);
    }

    @Override
    public Node leave(CallNode callNode) {
        Collection<JsObjectImpl> functionArguments = functionArgumentStack.pop();

        Node function = callNode.getFunction();
        if (function instanceof AccessNode || function instanceof IdentNode) {
            List<Identifier> funcName;
            if (function instanceof AccessNode) {
                funcName = getName((AccessNode) function, parserResult);
            } else {
                funcName = new ArrayList<Identifier>();
                funcName.add(new IdentifierImpl(((IdentNode) function).getName(), ((IdentNode) function).getStart()));
            }
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
                    List<FunctionInterceptor> interceptorsToUse = new ArrayList<FunctionInterceptor>();
                    for (FunctionInterceptor interceptor : ModelExtender.getDefault().getFunctionInterceptors()) {
                        if (interceptor.getNamePattern().matcher(name).matches()) {
                            interceptorsToUse.add(interceptor);
                        }
                    }


                    for (FunctionInterceptor interceptor : interceptorsToUse) {
                        Collection<FunctionArgument> funcArg = new ArrayList<FunctionArgument>();
                        for (int i = 0; i < callNode.getArgs().size(); i++) {
                            Node argument = callNode.getArgs().get(i);
                            createFunctionArgument(argument, i, functionArguments, funcArg);
                        }
                        Collection<FunctionCall> calls = functionCalls.get(interceptor);
                        if (calls == null) {
                            calls = new ArrayList<FunctionCall>();
                            functionCalls.put(interceptor, calls);
                        }
                        int callOffset = callNode.getFunction().getStart();
                        if (callNode.getFunction() instanceof AccessNode) {
                            AccessNode anode = (AccessNode)callNode.getFunction();
                            callOffset = anode.getProperty().getStart();
                        }
                        calls.add(new FunctionCall(name, modelBuilder.getCurrentDeclarationScope(), funcArg, callOffset));
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
            } else if (ln instanceof LiteralNode.ArrayLiteralNode) {
                for (JsObjectImpl jsObject: functionArguments) {
                    if (jsObject.getOffset() == argument.getStart()) {
                        result.add(FunctionArgumentAccessor.getDefault().createForArray(position, jsObject.getOffset(), jsObject));
                        break;
                    }
                }
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
        } else if (argument instanceof ReferenceNode) {
            ReferenceNode reference = (ReferenceNode) argument;
            result.add(FunctionArgumentAccessor.getDefault().createForReference(
                    position, argument.getStart(),
                    Collections.singletonList(reference.getReference().getName())));
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
                || previousVisited instanceof CatchNode
                || previousVisited instanceof LabelNode)) {
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
               parent = (JsObjectImpl)createJsObject((AccessNode)base, parserResult, modelBuilder);
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
                }/* else {
                    JsObject current = modelBuilder.getCurrentDeclarationFunction();
                    fromAN = (JsObjectImpl)resolveThis(current);
                }*/
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
                        if (name != null) {
                            property = new JsObjectImpl(parent, name, name.getOffsetRange(), parserResult.getSnapshot().getMimeType(), null);
                            parent.addProperty(name.getName(), property);
                        }
                    }
                }
            }
        }
        return super.leave(indexNode);
    }

    @Override
    public Node enter(ForNode forNode) {
        if (forNode.getInit() instanceof IdentNode) {
            JsObject parent = modelBuilder.getCurrentObject();
            while (parent instanceof JsWith) {
                parent = parent.getParent();
            }
            IdentNode name = (IdentNode)forNode.getInit();
            JsObjectImpl variable = (JsObjectImpl)parent.getProperty(name.getName());
            if (variable != null) {
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, forNode.getModify());
                for (TypeUsage type : types) {
                    if (type.getType().contains(SemiTypeResolverVisitor.ST_VAR)) {
                        int index = type.getType().lastIndexOf(SemiTypeResolverVisitor.ST_VAR);
                        String newType = type.getType().substring(0, index) + SemiTypeResolverVisitor.ST_ARR + type.getType().substring(index + SemiTypeResolverVisitor.ST_VAR.length());
                        type = new TypeUsageImpl(newType, type.getOffset(), false);
                    } else if (type.getType().contains(SemiTypeResolverVisitor.ST_PRO)) {
                        int index = type.getType().lastIndexOf(SemiTypeResolverVisitor.ST_PRO);
                        String newType = type.getType().substring(0, index) + SemiTypeResolverVisitor.ST_ARR + type.getType().substring(index + SemiTypeResolverVisitor.ST_PRO.length());
                        type = new TypeUsageImpl(newType, type.getOffset(), false);
                    }
                    variable.addAssignment(type, forNode.getModify().getStart());
                }
            }
        }
        return super.enter(forNode); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Node enter(FunctionNode functionNode) {
        addToPath(functionNode);
        List<FunctionNode> functions = new ArrayList<FunctionNode>(functionNode.getFunctions());

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
                boolean singletoneConstruction = false;
                if (node instanceof PropertyNode) {
                    name = getName((PropertyNode)node);
                } else if (node instanceof BinaryNode) {
                    boolean processAsBinary = true;
                    if (pathSize > 4) {
                        Node node4 = getPreviousFromPath(4);
                        if (node4 instanceof VarNode) {
                            name = getName((VarNode)node4, parserResult);
                            // private method
                            // It can be only if it's in a function
                            isPrivate = functionStack.size() > 1;
                            processAsBinary = false;
                        }
                    }
                    if (processAsBinary) {
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
                        if (bNode.isAssignment()) {
                            name = getName((BinaryNode)node, parserResult);
                        }
                    }
                } else if (node instanceof VarNode) {
                   name = getName((VarNode)node, parserResult);
                    // private method
                    // It can be only if it's in a function
                    isPrivate = functionStack.size() > 1;
                } else if (!functionNode.isAnonymous() && node instanceof CallNode) {
                    // try to handle case like: var MyLib = new function MyLib () {}
                    if (pathSize > 5) {
                        Node node4 = getPreviousFromPath(4);
                        Node node5 = getPreviousFromPath(5);
                        if (node4 instanceof UnaryNode && node5 instanceof VarNode) {
                            name = getName((VarNode)node5, parserResult);
                            isPrivate = functionStack.size() > 1;
                            singletoneConstruction = true;
                        }
                    }
                    
                }
                if (name != null && !name.isEmpty() && !functionNode.isAnonymous()) {
                    // we need to create just referenci to non anonymous function
                    // example MyObject.method = function method(){}
                    DeclarationScope currentScope = modelBuilder.getCurrentDeclarationScope();
                    JsObject originalFunction = null;
                    String functionName = functionNode.getIdent() != null ? functionNode.getIdent().getName() : functionNode.getName();
                    while (originalFunction == null && currentScope != null) {
                        originalFunction = ((JsObject)currentScope).getProperty(functionName);
                        currentScope = currentScope.getParentScope();
                    }
                    if (originalFunction != null && originalFunction instanceof JsFunction) {
                        JsObjectImpl jsObject = ModelUtils.getJsObject(modelBuilder, name, true);
                        if (ModelUtils.isDescendant(jsObject, originalFunction)) {
                            //XXX This is not right solution. The right solution would be to create new anonymous function
                            // and the recreate the object that has the same name as the function.
                            // See issue #246598
                            removeFromPathTheLast();
                            return null;
                        }
                        if (singletoneConstruction) {
                            jsObject.addAssignment(new TypeUsageImpl(originalFunction.getFullyQualifiedName(), -1, true), -1);
                            removeFromPathTheLast();
                            return null; 
                        } else {
                            JsFunctionReference jsFunctionReference = new JsFunctionReference(jsObject.getParent(), jsObject.getDeclarationName(), (JsFunction)originalFunction, true, jsObject.getModifiers());
                            jsObject.getParent().addProperty(jsObject.getName(), jsFunctionReference);
                            removeFromPathTheLast();
                            return null; 
                        }
                }
            }
        }
        }

        JsObject previousUsage = null;
        if (name == null || name.isEmpty()) {
            // function is declared as
            //      function fn () {}
            name = new ArrayList<Identifier>(1);
            int start = functionNode.getIdent().getStart();
            int end = functionNode.getIdent().getFinish();
            if(end == 0) {
                end = parserResult.getSnapshot().getText().length();
            }
            previousUsage = (modelBuilder.getCurrentDeclarationScope()).getProperty(functionNode.getIdent().getName());
            if ( previousUsage != null && previousUsage.isDeclared() && previousUsage instanceof JsFunction) {
                // the function is alredy there
                removeFromPathTheLast();
                return null;
            }
            String funcName = functionNode.isAnonymous() ? functionNode.getName() : functionNode.getIdent().getName();
//            String funcName = functionNode.getIdent().getName();            
            name.add(new IdentifierImpl(funcName, new OffsetRange(start, end)));
            if (pathSize > 2 && getPath().get(pathSize - 2) instanceof FunctionNode) {
                isPrivate = true;
                //isStatic = true;
            }
        }
        functionStack.add(functions);

        JsFunctionImpl fncScope = (JsFunctionImpl)modelBuilder.getCurrentDeclarationFunction();
        JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
        JsObject parent = null;
        if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
            // create the function object
            DeclarationScopeImpl scope = modelBuilder.getCurrentDeclarationFunction();
            boolean isAnonymous = false;
            if (getPreviousFromPath(2) instanceof ReferenceNode) {
                Node node = getPreviousFromPath(3);
                if (node instanceof CallNode || node instanceof ExecuteNode || node instanceof LiteralNode.ArrayLiteralNode) {
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
            if ("this".equals(name.get(0).getName())) {
                name.remove(0);
            }
            if (!name.isEmpty()) {
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
                if (previousUsage != null) {
                    // move all occurrences here
                    for (Occurrence occurrence : previousUsage.getOccurrences()) {
                        fncScope.addOccurrence(occurrence.getOffsetRange());
                    }
                    Collection<? extends JsObject> propertiesCopy = new ArrayList(previousUsage.getProperties().values());
                    for (JsObject property : propertiesCopy) {
                        ModelUtils.moveProperty(fncScope, property);
                    }
                }
            }
        } 
//        else {
            for(FunctionNode cFunction: functionNode.getFunctions()) {
                if (cFunction.isAnonymous()) {
                    cFunction.setName(scriptName + cFunction.getIdent().getName());
                }
            }
//        }
        if (fncScope != null) {
            // create variables that are declared in the function
            // They has to be created here for tracking occurrences
            if (canBeSingletonPattern()) {
                Node lastNode = getPreviousFromPath(1);
                if (lastNode instanceof FunctionNode && !canBeSingletonPattern(1)) {
                    parent = fncScope;
                } else { 
                    parent = resolveThis(fncScope);
                }
            } else {
                parent = fncScope;
            }
            if (parent == null) {
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
            
            if (docHolder != null) {
                // look for the type defined through comment like @typedef
                Map<Integer, ? extends JsComment> commentBlocks = docHolder.getCommentBlocks();
                for (JsComment comment : commentBlocks.values()) {
                    DocParameter definedType = comment.getDefinedType();
                    if (definedType != null) {
                    // XXX the param name now can contains names with dot.
                        // it would be better if the getParamName returns list of identifiers
                        String typeName = definedType.getParamName().getName();
                        List<Identifier> fqn = new ArrayList<Identifier>();
                        JsObject whereOccurrence = getGlobalObject();
                        if (typeName.indexOf('.') > -1) {
                            String[] parts = typeName.split("\\.");
                            int offset = definedType.getParamName().getOffsetRange().getStart();
                            int delta = 0;
                            for (int i = 0; i < parts.length; i++) {
                                fqn.add(new IdentifierImpl(parts[i], offset + delta));
                                if (whereOccurrence != null) {
                                    whereOccurrence = whereOccurrence.getProperty(parts[i]);
                                    if (whereOccurrence != null) {
                                        whereOccurrence.addOccurrence(new OffsetRange(offset + delta, offset + delta + parts[i].length()));
                                    }
                                }
                                delta = delta + parts[i].length() + 1;
                            }
                        } else {
                            fqn.add(definedType.getParamName());
                        }
                        JsObject object = ModelUtils.getJsObject(modelBuilder, fqn, true);
//                    JsObject object = new JsObjectImpl(getGlobalObject(), definedType.getParamName(), definedType.getParamName().getOffsetRange(), true, JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                        //getGlobalObject().addProperty(object.getName(), object);
                        int assignOffset = definedType.getParamName().getOffsetRange().getEnd();
                        List<Type> types = definedType.getParamTypes();

                        for (Type type : types) {
                            object.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset()), assignOffset);
                        }
                        List<Type> assignedTypes = comment.getTypes();
                        for (Type type : assignedTypes) {
                            object.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset()), assignOffset);
                        }
                        List<DocParameter> properties = comment.getProperties();
                        for (DocParameter docProperty : properties) {
                            JsObject jsProperty = new JsObjectImpl(object, docProperty.getParamName(), docProperty.getParamName().getOffsetRange(), true, JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                            object.addProperty(jsProperty.getName(), jsProperty);
                            types = docProperty.getParamTypes();
                            jsProperty.setDocumentation(Documentation.create(docProperty.getParamDescription()));
                            assignOffset = docProperty.getParamName().getOffsetRange().getEnd();
                            for (Type type : types) {
                                jsProperty.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset()), assignOffset);
                            }
                        }
                    }
                    Type callBack = comment.getCallBack();
                    if (callBack != null) {
                        List<Identifier> fqn = fqnFromType(callBack);
                        markOccurrences(fqn);
                        List<Identifier> parentFqn = new ArrayList<Identifier>();
                        for (int i = 0; i < fqn.size() - 1; i++) {
                            parentFqn.add(fqn.get(i));
                        }
                        JsObject parentObject = parentFqn.isEmpty() ? getGlobalObject() : ModelUtils.getJsObject(modelBuilder, parentFqn, true);
                        JsFunctionImpl callBackFunction = new JsFunctionImpl(
                                parentObject instanceof DeclarationScope ? (DeclarationScope)parentObject : ModelUtils.getDeclarationScope(parentObject),
                                parentObject, fqn.get(fqn.size() - 1), Collections.EMPTY_LIST, 
                                callBack.getOffset() > -1 ? new OffsetRange(callBack.getOffset(), callBack.getOffset() + callBack.getType().length()) : OffsetRange.NONE,
                                JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                        parentObject.addProperty(callBackFunction.getName(), callBackFunction);
                        callBackFunction.setDocumentation(Documentation.create(comment.getDocumentation()));
                        callBackFunction.setJsKind(JsElement.Kind.CALLBACK);
                        List<DocParameter> docParameters = comment.getParameters();
                        for (DocParameter docParameter: docParameters) {
                            ParameterObject parameter = new ParameterObject(callBackFunction, docParameter.getParamName(), JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                            for (Type type : docParameter.getParamTypes()) {
                                parameter.addAssignment(new TypeUsageImpl(type.getType(), type.getOffset(), true), parameter.getOffset());
                            }
                            addDocNameOccurence(parameter);
                            callBackFunction.addParameter(parameter);
                        }
                    }
                }
            }
            
            List<FunctionNode> copy = new ArrayList<FunctionNode>(functions);
            for (FunctionNode fn : copy) {
                if (fn.getIdent().getStart() < fn.getIdent().getFinish()) {
                    if (modelBuilder.getCurrentDeclarationFunction().getProperty(fn.getIdent().getName()) == null
                            && !(fn.getIdent().getName().startsWith("get ") || fn.getIdent().getName().startsWith("set "))) {
                        IdentifierImpl fakeObjectName = ModelElementFactory.create(parserResult, fn.getIdent());
                        if (fakeObjectName != null) {
                            JsObjectImpl newObject = new JsObjectImpl(fncScope, fakeObjectName, fakeObjectName.getOffsetRange(), parserResult.getSnapshot().getMimeType(), null);
                            fncScope.addProperty(newObject.getName(), newObject);
                        }
                    }
                }
            }
            for (FunctionNode fn : copy) {
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
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT && docHolder.isClass(functionNode)) {
                // needs to be marked before going through the nodes
                fncScope.setJsKind(JsElement.Kind.CONSTRUCTOR);
            }

            // go through all function statements
            for (Node node : functionNode.getStatements()) {
                node.accept(this);
            }

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
                Identifier paramName = docParameter.getParamName();
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

            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
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
            }

            setModifiersFromDoc(fncScope, docHolder.getModifiers(functionNode));

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

    private List<Identifier> fqnFromType (final Type type) {
        List<Identifier> fqn = new ArrayList<Identifier>();
        String typeName = type.getType();
        int offset = type.getOffset();
        if (typeName.indexOf('.') > -1) {
            String[] parts = typeName.split("\\.");
            int delta = 0;
            for (int i = 0; i < parts.length; i++) {
                fqn.add(new IdentifierImpl(parts[i], offset + delta));
                delta = delta + parts[i].length() + 1;
            }
        } else {
            fqn.add(new IdentifierImpl(typeName, offset));
        }
        return fqn;
    }
    
    private void markOccurrences (List<Identifier> fqn) {
        JsObject whereOccurrence = getGlobalObject();
        for (Identifier iden: fqn) {
            whereOccurrence = whereOccurrence.getProperty(iden.getName());
            if (whereOccurrence != null) {
                whereOccurrence.addOccurrence(iden.getOffsetRange());
            } else {
                break;
            }
        }
    }
    
    private JsArray handleArrayCreation(Node initNode, JsObject parent, Identifier name) {
        if (initNode instanceof UnaryNode && parent != null) {
            UnaryNode uNode = (UnaryNode)initNode;
            if (uNode.tokenType() == TokenType.NEW && uNode.rhs() instanceof CallNode) {
                CallNode cNode = (CallNode)uNode.rhs();
                if (cNode.getFunction() instanceof IdentNode && "Array".equals(((IdentNode)cNode.getFunction()).getName())) {
                    List<TypeUsage> itemTypes = new ArrayList<TypeUsage>();
                    for (Node node : cNode.getArgs()) {
                        itemTypes.addAll(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, node));
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
                    if ((binNode.lhs() instanceof IdentNode)
                            || (binNode.lhs() instanceof AccessNode
                            && ((AccessNode) binNode.lhs()).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binNode.lhs()).getBase()).getName().equals("this"))) { //NOI18N
                        if (lastVisited instanceof ExecuteNode && !fqName.get(0).getName().equals("this")) { //NOI18N
                            // try to catch the case: pool = [];
                            List<Identifier> objectName = fqName.size() > 1 ? fqName.subList(0, fqName.size() - 1) : fqName;
                            JsObject existingArray = ModelUtils.getJsObject(modelBuilder, objectName, false);
                            if (existingArray != null) {
                                existingArray.addOccurrence(fqName.get(fqName.size() - 1).getOffsetRange());
                                return super.enter(lNode);
                            }
                        } else {
                            isDeclaredInParent = true;
                            if (!(binNode.lhs() instanceof IdentNode)) {
                                parent = resolveThis(modelBuilder.getCurrentObject());
                            }
                        }
                    }
                }
            } else if (lastVisited instanceof CallNode || lastVisited instanceof LiteralNode.ArrayLiteralNode
                    || lastVisited instanceof ReturnNode || lastVisited instanceof AccessNode) {
                // probably an anonymous array as a parameter of a function call
                // or array in an array: var a = [['a', 10], ['b', 20]];
                // or [1,2,3].join();
                treatAsAnonymous = true;
            }
            if (!isDeclaredInParent) {
                if (lastVisited instanceof FunctionNode) {
                    isDeclaredInParent = ((FunctionNode) lastVisited).getKind() == FunctionNode.Kind.SCRIPT;
                }
            }
            JsArrayImpl array = null;
            if (!treatAsAnonymous) {
//                if (fqName == null || fqName.isEmpty()) {
//                    fqName = new ArrayList<Identifier>(1);
//                    fqName.add(new IdentifierImpl("UNKNOWN", //NOI18N
//                            new OffsetRange(lNode.getStart(), lNode.getFinish())));
//                }
                
                if (fqName != null) {
                    if ("this".equals(fqName.get(0).getName())) {
                        parent = resolveThis(modelBuilder.getCurrentObject());
                        fqName.remove(0);
                        JsObject tmpObject = parent;
                        while (tmpObject.getParent() != null) {
                            Identifier dName = tmpObject.getDeclarationName();
                            fqName.add(0, dName != null ? tmpObject.getDeclarationName() : new IdentifierImpl(tmpObject.getName(), OffsetRange.NONE));
                            tmpObject = tmpObject.getParent();
                        }
                    }
                    array = ModelElementFactory.create(parserResult, aNode, fqName, modelBuilder, isDeclaredInParent, parent);
                    if (array != null && isPrivate) {
                        array.getModifiers().remove(Modifier.PUBLIC);
                        array.getModifiers().add(Modifier.PRIVATE);
                    }
                }
            } else {
                array = ModelElementFactory.createAnonymousObject(parserResult, aNode, modelBuilder);
            }
            if (array != null) {
                int aOffset = fqName == null ? lastVisited.getStart() : fqName.get(fqName.size() - 1).getOffsetRange().getEnd();
                array.addAssignment(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, lNode), aOffset);
                for (Node item : aNode.getArray()) {
                    array.addTypesInArray(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, item));
                }
                if (!functionArgumentStack.isEmpty()) {
                    functionArgumentStack.peek().add(array);
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
            } else if (lastVisited instanceof ExecuteNode || lastVisited instanceof AccessNode) {
                treatAsAnonymous = true;
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
                            if (lastVisited instanceof PropertyNode) {
                                alreadyThere = modelBuilder.getCurrentObject().getProperty(name.getName());
                            } else {
                                alreadyThere = ModelUtils.getJsObjectByName(modelBuilder.getCurrentDeclarationFunction(), name.getName());
                            }
                        } else {
                            if (fqName.size() == 1) {
                                Collection<? extends JsObject> variables = ModelUtils.getVariables(modelBuilder.getCurrentDeclarationScope());
                                for (JsObject variable : variables) {
                                    if (variable.getName().equals(name.getName())) {
                                        alreadyThere = variable;
                                        break;
                                    }
                                }
                            }
                            if (alreadyThere == null) {
                                alreadyThere = ModelUtils.getJsObject(modelBuilder, fqName, true);
                            }
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
                    property.getParent().addProperty(name.getName(), property);
                    property.setDeclared(true);
                    Node value = propertyNode.getValue();
                    if(value instanceof CallNode) {
                        // TODO for now, don't continue. There shoudl be handled cases liek
                        // in the testFiles/model/property02.js file
                        //return null;
                    } else {
                        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, value);
                        if (!types.isEmpty()) {
                            property.addAssignment(types, name.getOffsetRange().getStart());
                        }
                        if (value instanceof IdentNode) {
                            IdentNode iNode = (IdentNode)value;
                            if (!iNode.getPropertyName().equals(name.getName())) {
                                addOccurence((IdentNode)value, false);
                            } else {
                                // handling case like property: property
                                if (modelBuilder.getCurrentObject().getParent() != null) {
                                    occurrenceBuilder.addOccurrence(name.getName(), new OffsetRange(iNode.getStart(), iNode.getFinish()), modelBuilder.getCurrentDeclarationScope(), modelBuilder.getCurrentObject().getParent(), modelBuilder.getCurrentWith(), false, false);
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
            Node lastNode = getPreviousFromPath(1);
            if (!((lastNode instanceof VarNode) && !reference.isAnonymous())) {
                if (lastNode instanceof BinaryNode && !reference.isAnonymous()) {
                    Node lhs = ((BinaryNode)lastNode).lhs();
                    List<Identifier> nodeName = getNodeName(lhs, parserResult);
                    if (nodeName != null && !nodeName.isEmpty()) {
                        JsObject jsObject = null;
                        if ("this".equals(nodeName.get(0).getName())) { //NOI18N
                            jsObject = resolveThis(modelBuilder.getCurrentObject());
                            for (int i = 1; jsObject != null && i < nodeName.size(); i++ ) {
                                jsObject = jsObject.getProperty(nodeName.get(i).getName());
                            }
                        } else {
                            jsObject = ModelUtils.getJsObject(modelBuilder, nodeName, true);
                        }
                        if (jsObject != null) {
                            Identifier name = nodeName.get(nodeName.size() - 1);
                            DeclarationScopeImpl ds = modelBuilder.getCurrentDeclarationScope();
                            String referenceName = reference.getIdent().getName();
                            JsObject originalFnc = ds.getProperty(referenceName);
                            while (originalFnc != null && !(originalFnc instanceof JsFunction)) {
                                if (ds.getParentScope() != null) {
                                    ds = (DeclarationScopeImpl)ds.getParentScope();
                                    originalFnc = ds.getProperty(referenceName);
                                } else {
                                    originalFnc = null;
                                }
                            }
                            if (originalFnc != null && originalFnc instanceof JsFunction) {
                                //property contains the definition of the function
                                JsObject newRef = new JsFunctionReference(jsObject.getParent(), name, (JsFunction)originalFnc, true, jsObject.getModifiers());
                                jsObject.getParent().addProperty(jsObject.getName(), newRef);
                                for (Occurrence occurence : jsObject.getOccurrences()) {
                                    newRef.addOccurrence(occurence.getOffsetRange());
                                }
                                if (originalFnc instanceof JsFunctionImpl) {
//                                    ((JsFunctionImpl)originalFnc).setAnonymous(true);
                                    JsObject parent = jsObject.getParent();
                                    if (ModelUtils.PROTOTYPE.equals(parent.getName())) {
                                        parent = parent.getParent();
                                    }
                                    if (parent != null) {
                                        Collection<JsObject> propertiesCopy = new ArrayList(originalFnc.getProperties().values());
                                        for (JsObject property : propertiesCopy) {
                                            if (!property.getModifiers().contains(Modifier.PRIVATE)) {
                                                ModelUtils.moveProperty(parent, property);
                                            }
                                        }
                                    }
                                }
                                
                            }
                            
                        }
                    }
                } else {
                    addToPath(referenceNode);
                    reference.accept(this);
                    removeFromPathTheLast();
                }
            } 
            return null;
        }
        return super.enter(referenceNode);
    }

    @Override
    public Node enter(ReturnNode returnNode) {
        Node expression = returnNode.getExpression();
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, expression);
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
        Node init = varNode.getInit();
        ReferenceNode rNode = null;
        if (init instanceof ReferenceNode) {
            rNode = (ReferenceNode)init;
        } else if (init instanceof BinaryNode) {
            // this should handle cases like 
            // var prom  = another.prom = function prom() {}
            BinaryNode bNode = (BinaryNode)init;
            while (bNode.rhs() instanceof BinaryNode ) {
                bNode = (BinaryNode)bNode.rhs();
            }
            if (bNode.rhs() instanceof ReferenceNode) {
                 rNode = (ReferenceNode) bNode.rhs();
            }
        }
         if (!(init instanceof ObjectNode || rNode != null
                 || init instanceof LiteralNode.ArrayLiteralNode)) {
            JsObject parent = modelBuilder.getCurrentObject();
            parent = canBeSingletonPattern(1) ? resolveThis(parent) : parent;
            if (parent instanceof CatchBlockImpl) {
                parent = parent.getParent();
            }
            while (parent instanceof JsWith) {
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
                if (init instanceof IdentNode) {
                    IdentNode iNode = (IdentNode)init;
                    if (!iNode.getName().equals(variable.getName())) {
                        addOccurrence((IdentNode)init, variable.getName());
                    } else {
                        // the name of variable is the same as already existing function or var or parameter
                        JsFunctionImpl currentFunction = modelBuilder.getCurrentDeclarationFunction();
                        if (currentFunction != null && currentFunction.getParameter(variable.getName()) != null) {
                            // it's a parameter
                            addOccurrence((IdentNode)init, variable.getName());
                        } else {
                            variable.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                        }
                    }
                    
                }
                modelBuilder.setCurrentObject(variable);
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, init);
                if (modelBuilder.getCurrentWith() != null) {
                    ((JsWithObjectImpl)modelBuilder.getCurrentWith()).addObjectWithAssignment(variable);
                }
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
        } else if(init instanceof ObjectNode) {
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
        } else if (rNode != null) {
            if (rNode.getReference() != null && rNode.getReference() instanceof FunctionNode) {
                FunctionNode fnode = (FunctionNode)rNode.getReference();
                if (!fnode.isAnonymous()) {
                    // we expect case like: var prom = function name () {}
                    JsObjectImpl function = modelBuilder.getCurrentDeclarationFunction();
                    JsObject origFunction = function.getProperty(fnode.getIdent().getName());
                    Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
                    if (name != null && origFunction != null && origFunction instanceof JsFunction) {
                        JsObjectImpl oldVariable = (JsObjectImpl)function.getProperty(name.getName());
                        JsObjectImpl variable = new JsFunctionReference(function, name, (JsFunction)origFunction, true, 
                                oldVariable != null ? oldVariable.getModifiers() : null );
                        function.addProperty(variable.getName(), variable);
                        if (oldVariable != null) {
                            for(Occurrence occurrence : oldVariable.getOccurrences()) {
                               variable.addOccurrence(occurrence.getOffsetRange());
                            }
                        }
                    }
                } else {
                    if (init instanceof BinaryNode) {
                        init.accept(this);
                        JsObjectImpl function = modelBuilder.getCurrentDeclarationFunction();
                        JsObject oldVariable = function.getProperty(varNode.getName().getName());
                        if ((oldVariable != null && !(oldVariable instanceof JsFunctionReference)) || oldVariable == null) {
                            Node lhs = ((BinaryNode)init).lhs();
                            if (lhs instanceof IdentNode)  {
                                JsObject previousRef = function.getProperty(((IdentNode)lhs).getName());
                                if (previousRef != null && (previousRef instanceof JsFunction || previousRef instanceof JsFunctionReference)) {
                                    Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
                                    JsFunction origFunction = previousRef instanceof JsFunctionReference ? ((JsFunctionReference)previousRef).getOriginal() : (JsFunction)previousRef;
                                    JsObjectImpl variable = new JsFunctionReference(function, name, (JsFunction)origFunction, true, 
                                            oldVariable != null ? oldVariable.getModifiers() : null );
                                    function.addProperty(variable.getName(), variable);
                                    if (oldVariable != null) {
                                        for(Occurrence occurrence : oldVariable.getOccurrences()) {
                                           variable.addOccurrence(occurrence.getOffsetRange());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.enter(varNode);
    }

    @Override
    public Node leave(VarNode varNode) {
        Node init = varNode.getInit();
        if (init instanceof BinaryNode) {
            // this should handle cases like 
            // var prom  = another.prom = function prom() {}
            BinaryNode bNode = (BinaryNode)init;
            while (bNode.rhs() instanceof BinaryNode ) {
                bNode = (BinaryNode)bNode.rhs();
            }
            if (bNode.rhs() instanceof ReferenceNode /*&& bNode.tokenType() == TokenType.ASSIGN*/) {
                 init = (ReferenceNode) bNode.rhs();
            }
        }
        if (!(init instanceof ReferenceNode || init instanceof LiteralNode.ArrayLiteralNode)
                // XXX can we avoid creation of object ?
                && ModelElementFactory.create(parserResult, varNode.getName()) != null) {
            JsDocumentationHolder docHolder = parserResult.getDocumentationHolder();
            List<DocParameter> properties = docHolder.getProperties(varNode);
            for (DocParameter docProperty : properties) {
                String propertyName = docProperty.getParamName().getName();
                String names[];
                int delta = 0;
                if (propertyName.indexOf('.') > 0) {
                    names = propertyName.split("\\.");
                } else {
                    names = new String[]{propertyName};
                }
                JsObject parent = modelBuilder.getCurrentObject();
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    JsObject property = parent.getProperty(name);
                    int startOffset = docProperty.getParamName().getOffsetRange().getStart() + delta;
                    int endOffset = startOffset + name.length();
                    OffsetRange offsetRange = new OffsetRange(startOffset, endOffset);
                    if (property == null) {
                        IdentifierImpl iden = new IdentifierImpl(name, offsetRange);
                        property = new JsObjectImpl(parent, iden, offsetRange, true, JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                        parent.addProperty(name, property);
                    }
                    property.addOccurrence(offsetRange);
                     if (i == names.length - 1) {
                        for (Type type : docProperty.getParamTypes()) {
                            property.addAssignment(new TypeUsageImpl(type.getType(), endOffset), endOffset);
                        }
                    }
                    delta = delta + name.length() + 1;
                    parent = property;
                    
                }
                

            }
            modelBuilder.reset();
        }
        return super.leave(varNode);
    }

    @Override
    public Node enter(WithNode withNode) {
        JsObjectImpl currentObject = modelBuilder.getCurrentObject();
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, withNode.getExpression());
        JsWithObjectImpl withObject = new JsWithObjectImpl(currentObject, modelBuilder.getUnigueNameForWithObject(), types, new OffsetRange(withNode.getStart(), withNode.getFinish()), 
                        new OffsetRange(withNode.getExpression().getStart(), withNode.getExpression().getFinish()), modelBuilder.getCurrentWith(), parserResult.getSnapshot().getMimeType(), null);
        currentObject.addProperty(withObject.getName(), withObject);
//        withNode.getExpression().accept(this); // expression should be visted when the with object is the current object.
        modelBuilder.setCurrentObject(withObject);
        withNode.getBody().accept(this);
        modelBuilder.reset();
        return null;
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
        Node base = aNode.getBase();
        while (base instanceof AccessNode || base instanceof CallNode || base instanceof IndexNode) {
            if (base instanceof CallNode) {
                CallNode cNode = (CallNode)base;
                base = cNode.getFunction();
            } else if (base instanceof IndexNode) {
                IndexNode iNode = (IndexNode) base;
                if (iNode.getIndex() instanceof LiteralNode) {
                    LiteralNode lNode = (LiteralNode)iNode.getIndex();
                    if (lNode.isString()) {
                        name.add(new IdentifierImpl(lNode.getPropertyName(), 
                                new OffsetRange(lNode.getStart(), lNode.getFinish())));
                    }
                } else {
                    return null;
                } 
                base = iNode.getBase();
            }
            if (base instanceof AccessNode) {
                AccessNode aaNode = (AccessNode)base;
                base = aaNode.getBase();
                name.add(new IdentifierImpl(aaNode.getProperty().getName(),
                        new OffsetRange(aaNode.getProperty().getStart(), aaNode.getProperty().getFinish())));
            }
        }
        if (base instanceof IdentNode) {
            if (name.size() > 0) {
                IdentNode ident = (IdentNode) base;
//                if (!"this".equals(ident.getName())) {
                    name.add(new IdentifierImpl(ident.getName(),
                            new OffsetRange(ident.getStart(), ident.getFinish())));
//                }
            }
            Collections.reverse(name);
            return name;
        } else {
            return null;
        }
    }
    
    private JsObject createJsObject(AccessNode accessNode, JsParserResult parserResult, ModelBuilder modelBuilder) {
        List<Identifier> fqn = getName(accessNode, parserResult);
        if (fqn == null) {
            return null;
        }
        JsObject object = null;

        Identifier name = fqn.get(0);
        if (!"this".equals(fqn.get(0).getName())) { 
            if (modelBuilder.getCurrentWith() == null) {
                DeclarationScopeImpl currentDS = modelBuilder.getCurrentDeclarationScope();
                Collection<? extends JsObject> variables = ModelUtils.getVariables(currentDS);
                for(JsObject variable : variables) {
                    if (variable.getName().equals(name.getName()) ) {
                        if (variable instanceof ParameterObject || variable.getModifiers().contains(Modifier.PRIVATE)) {
                            object = (JsObjectImpl)variable;
                            break;
                        }
                        DeclarationScope variableDS = ModelUtils.getDeclarationScope(variable);
                        if (!variableDS.equals(currentDS)) {
                            object = (JsObjectImpl)variable;
                            break;
                        } else if (currentDS.getProperty(name.getName()) != null) {
                            Node lastNode = getPreviousFromPath(2);
                            if (lastNode instanceof BinaryNode) {
                                BinaryNode bNode = (BinaryNode)lastNode;
                                if (bNode.lhs().equals(accessNode)) {
                                    object = (JsObjectImpl) variable;
                                }
                            }
                            break;
                        }
                    }
                }
                if (object == null) {
                    JsObject global = modelBuilder.getGlobal();
                    object = (JsObjectImpl)global.getProperty(name.getName());
                    if (object == null) {
                        object = new JsObjectImpl(global, name, name.getOffsetRange(), false, global.getMimeType(), global.getSourceLabel());
                        global.addProperty(name.getName(), object);
                    }
                } 
            } else {
                JsObject withObject = modelBuilder.getCurrentWith();
                object = (JsObjectImpl)withObject.getProperty(name.getName());
                if (object == null) {
                    object = new JsObjectImpl(withObject, name, name.getOffsetRange(), false, parserResult.getSnapshot().getMimeType(), null);
                    withObject.addProperty(name.getName(), object);
                }
            }       
            object.addOccurrence(name.getOffsetRange());
        } else {
            JsObject current = modelBuilder.getCurrentDeclarationFunction();
            object = (JsObjectImpl)resolveThis(current);
            if (object != null) {
                // find out, whether is not defined in prototype
                if (object.getProperty(fqn.get(1).getName()) == null) {
                    JsObject prototype = object.getProperty(ModelUtils.PROTOTYPE);
                    if (prototype != null && prototype.getProperty(fqn.get(1).getName()) != null) {
                        object = prototype;
                    }
                }
            }
            if (object != null && fqn.size() == 2) {
                // try to handle case
                // function MyF() {
                //      this.f1 = f1;
                //      function f1() {};
                // }
                // in such case the name after this has to be equal to the declared function. 
                // -> in the model, just change  the f1 from private to privilaged. 
                String lastName = fqn.get(1).getName();
                JsObjectImpl property = (JsObjectImpl)object.getProperty(lastName);
                if (property != null && lastName.equals(property.getName()) && (property.getModifiers().contains(Modifier.PRIVATE) && property.getModifiers().size() == 1)) {
                    property.getModifiers().remove(Modifier.PRIVATE);
                    property.getModifiers().add(Modifier.PROTECTED);
                }
            }
        }
        
        if (object != null) {
            JsObjectImpl property = null;
            for (int i = 1; i < fqn.size(); i++) {
                property = (JsObjectImpl)object.getProperty(fqn.get(i).getName());
                if (property != null) {
                    object = property;
                }
            }
            int pathSize = getPath().size();
            Node lastVisited =  pathSize > 1 ? getPath().get(pathSize - 2) : getPath().get(0);
            boolean onLeftSite = false;
            if (lastVisited instanceof BinaryNode) {
                BinaryNode bNode = (BinaryNode)lastVisited;
                onLeftSite = bNode.tokenType() == TokenType.ASSIGN && bNode.lhs().equals(accessNode);       
            }
            if (property != null) {
                OffsetRange range = new OffsetRange(accessNode.getProperty().getStart(), accessNode.getProperty().getFinish());
                if(onLeftSite && !property.isDeclared()) {
                    property.setDeclared(true);
                    property.setDeclarationName(new IdentifierImpl(property.getName(), range));
                }
                property.addOccurrence(range);
            } else {
                name = ModelElementFactory.create(parserResult, (IdentNode)accessNode.getProperty());
                if (name != null) {
                    if (pathSize > 1 && getPath().get(pathSize - 2) instanceof CallNode) {
                        CallNode cNode = (CallNode)getPath().get(pathSize - 2);
                        if (!cNode.getArgs().contains(accessNode)) {
                            property = ModelElementFactory.createVirtualFunction(parserResult, object, name, cNode.getArgs().size());
                            //property.addOccurrence(name.getOffsetRange());
                        } else {
                            property = new JsObjectImpl(object, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
                            property.addOccurrence(name.getOffsetRange());
                        }
                    } else {
                        boolean setDocumentation = false;
                        if (isPriviliged(accessNode) && getPath().size() > 1 && (getPreviousFromPath(2) instanceof ExecuteNode || getPreviousFromPath(1) instanceof ExecuteNode)) {
                            // google style declaration of properties:  this.buildingID;    
                            onLeftSite = true;
                            setDocumentation = true;
                        }
                        property = new JsObjectImpl(object, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
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
                    object.addProperty(name.getName(), property);
                    object = property;
                }
            }
        }
        return object;
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
        addOccurence(iNode, leftSite, false);
    }

    private void addOccurence(IdentNode iNode, boolean leftSite, boolean isFunction) {
        addOccurrence(iNode.getName(), new OffsetRange(iNode.getStart(), iNode.getFinish()), leftSite, isFunction);
    }
    
    private void addOccurrence(String name, OffsetRange range, boolean leftSite, boolean isFunction) {
        if ("this".equals(name)) {
            // don't process this node.
            return;
        }
        occurrenceBuilder.addOccurrence(name, range, modelBuilder.getCurrentDeclarationScope(), modelBuilder.getCurrentObject(), modelBuilder.getCurrentWith(), isFunction, leftSite);
//        DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
//        JsObject property = null;
//        JsObject parameter = null;
//        JsObject parent = modelBuilder.getCurrentObject();
//        if (!(parent instanceof JsWith || (parent.getParent() != null && parent.getParent() instanceof JsWith))) {
//            while (scope != null && property == null && parameter == null) {
//                JsFunction function = (JsFunction)scope;
//                property = function.getProperty(name);
//                parameter = function.getParameter(name);
//                scope = scope.getParentScope();
//            }
//            if(parameter != null) {
//                if (property == null) {
//                    property = parameter;
//                } else {
//                    if(property.getJSKind() != JsElement.Kind.VARIABLE) {
//                        property = parameter;
//                    }
//                }
//            }
//        } else {
//            if (!(parent instanceof JsWith) && (parent.getParent() != null && parent.getParent() instanceof JsWith)) {
//                parent = parent.getParent();
//            }
//            property = parent.getProperty(name);
//        }
//
//        if (property != null) {
//
//            // occurence in the doc
//            addDocNameOccurence(((JsObjectImpl)property));
//            addDocTypesOccurence(((JsObjectImpl)property));
//
//            ((JsObjectImpl)property).addOccurrence(range);
//        } else {
//            // it's a new global variable?
//            IdentifierImpl nameIden = ModelElementFactory.create(parserResult, name, range.getStart(), range.getEnd());
//            if (nameIden != null) {
//                JsObjectImpl newObject;
//                if (!(parent instanceof JsWith)) {
//                        parent = modelBuilder.getGlobal();
//                }
//                if (!isFunction) {
//                    newObject = new JsObjectImpl(parent, nameIden, nameIden.getOffsetRange(),
//                            leftSite, parserResult.getSnapshot().getMimeType(), null);
//                } else {
//                    FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
//                    newObject = new JsFunctionImpl(fo, parent, nameIden, Collections.EMPTY_LIST,
//                            parserResult.getSnapshot().getMimeType(), null);
//                }
//                newObject.addOccurrence(nameIden.getOffsetRange());
//                parent.addProperty(nameIden.getName(), newObject);
//            }
//        }
    }
    
    /**
     * Handles adding occurrences in expression like var xxx = xxx or this.xxx = xxx;
     * @param iNode
     * @param name 
     */
    private void addOccurrence(IdentNode iNode, String name) {
        String valueName = iNode.getName();
        if (!name.equals(valueName)) {
            addOccurence(iNode, false);
        } else {
            DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
            JsObject parameter = null;
            JsFunction function = (JsFunction)scope;
            parameter = function.getParameter(iNode.getName());
            if (parameter != null) {
                parameter.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
            } else {
                boolean found = false;
                JsObject jsProperty = ((JsObject)scope).getProperty(valueName);
                if (jsProperty != null && jsProperty.isDeclared()) {
                    found = true;
                    jsProperty.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                } else {
                    Collection<? extends JsObject> variables = ModelUtils.getVariables(scope.getParentScope());
                    for (JsObject jsObject : variables) {
                        if (valueName.equals(jsObject.getName())) {
                            jsObject.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    // new global var?
                    IdentifierImpl nameI = ModelElementFactory.create(parserResult, iNode);
                    if (nameI != null) {
                        JsObjectImpl newObject;
                        newObject = new JsObjectImpl(modelBuilder.getGlobal(), nameI, nameI.getOffsetRange(),
                                false, parserResult.getSnapshot().getMimeType(), null);
                        newObject.addOccurrence(nameI.getOffsetRange());
                        modelBuilder.getGlobal().addProperty(nameI.getName(), newObject);
                    }
                }
            }
        }
    }
    
    private void addDocNameOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = parserResult.getDocumentationHolder();
        JsComment comment = holder.getCommentForOffset(jsObject.getOffset(), holder.getCommentBlocks());
        if (comment != null) {
            for (DocParameter docParameter : comment.getParameters()) {
                Identifier paramName = docParameter.getParamName();
                String name = (docParameter.getParamName() == null) ? "" : docParameter.getParamName().getName(); //NOI18N
                if (name.equals(jsObject.getName())) {
                    jsObject.addOccurrence(paramName.getOffsetRange());
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

    private JsObject processLhs(Identifier name, JsObject parent, boolean lastOnLeft) {
        JsObject lObject = null;
        if (name != null) {
            if ("this".equals(name.getName())) {
                return null;
            }
            final String newVarName = name.getName();
            boolean hasParent = parent.getProperty(newVarName) != null ;
            boolean hasGrandParent = parent.getJSKind() == JsElement.Kind.METHOD && parent.getParent().getProperty(newVarName) != null;
            if (!hasParent && !hasGrandParent && modelBuilder.getGlobal().getProperty(newVarName) == null) {
                addOccurrence(name.getName(), name.getOffsetRange(), lastOnLeft, false);
            } else {
                lObject = hasParent ? parent.getProperty(newVarName) : hasGrandParent ? parent.getParent().getProperty(newVarName) : null;
                if (lObject != null) {
                    ((JsObjectImpl)lObject).addOccurrence(name.getOffsetRange());
                } else {
                    addOccurrence(name.getName(), name.getOffsetRange(), lastOnLeft, false);
                }
            }
            lObject = (JsObjectImpl)parent.getProperty(newVarName);
            if (lObject == null) {
                // it's not a property of the parent -> try to find in different context
                Model model = parserResult.getModel();
                Collection<? extends JsObject> variables = model.getVariables(name.getOffsetRange().getStart());
                for(JsObject variable : variables) {
                    if(variable.getName().equals(newVarName)) {
                        lObject = (JsObjectImpl)variable;
                        break;
                    }
                }
                if (lObject == null) {
                    // the object with the name wasn't find yet -> create in global scope
                    JsObject where = modelBuilder.getCurrentWith() == null ? model.getGlobalObject() : modelBuilder.getCurrentWith();
                    lObject = new JsObjectImpl( where, name,
                            name.getOffsetRange(), lastOnLeft, parserResult.getSnapshot().getMimeType(), null);
                    where.addProperty(name.getName(), lObject);
                }
            }
        }
        return lObject;
    }
    
    // TODO move this method to the ModelUtils
    /**
     * 
     * @param where the declaration context, where this is used
     * @return JsObject that should represent this. 
     */
    public JsObject resolveThis(JsObject where) {
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
            if (parent.getJSKind() == JsElement.Kind.OBJECT_LITERAL) {
                if (Character.isUpperCase(parent.getName().charAt(0))) {
                    return parent;
                }
            } else {
                if (parent.isDeclared() || modelBuilder.getCurrentWith() != null) {
                    return parent;
                } else {
                    return where;
                }
            }
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
                            if (identifier != null) {
                                if (!identifier.isEmpty() && "this".equals(identifier.get(0).getName())) {
                                    identifier.remove(0);
                                }
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
    
    private  boolean canBeSingletonPattern() {
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
                    && ((CallNode)getPreviousFromPath(pathIndex + 2)).getFunction().equals(getPreviousFromPath(pathIndex + 1))
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
                        // if the modifier from doc is PRIVATE, keep information about the privilaged or public method anyway.
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
        
        private final int callOffset;

        public FunctionCall(String name, DeclarationScope scope,
                Collection<FunctionArgument> arguments, int callOffset) {
            this.name = name;
            this.scope = scope;
            this.arguments = arguments;
            this.callOffset = callOffset;
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

        public int getCallOffset() {
            return callOffset;
        }
        
        
    }
}
