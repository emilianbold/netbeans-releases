/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.builder;

import com.sun.tools.javac.model.JavacElements;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.*;
import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Context;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.ArrayType;
import javax.lang.model.util.Types;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import org.netbeans.modules.java.source.engine.RootTree;

/**
 * Factory for creating new com.sun.source.tree instances.
 */
public class TreeFactory {
    Name.Table names;
    ClassReader classReader;
    com.sun.tools.javac.tree.TreeMaker make;
    ASTService model;
    Elements elements;
    Types types;
    
    private static final Context.Key<TreeFactory> contextKey = new Context.Key<TreeFactory>();

    public static synchronized TreeFactory instance(Context context) {
	TreeFactory instance = context.get(contextKey);
	if (instance == null) {
	    instance = new TreeFactory(context);
        }
	return instance;
    }

    protected TreeFactory(Context context) {
        context.put(contextKey, this);
        model = ASTService.instance(context);
        names = Name.Table.instance(context);
        classReader = ClassReader.instance(context);
        make = com.sun.tools.javac.tree.TreeMaker.instance(context);
        elements = JavacElements.instance(context);
        types = JavacTypes.instance(context);
        make.at(NOPOS); // TODO: is this really neeeded?
        make.toplevel = null;
    }
    
    public AnnotationTree Annotation(Tree type, List<? extends ExpressionTree> arguments) {
        ListBuffer<JCExpression> lb = new ListBuffer<JCExpression>();
        for (ExpressionTree t : arguments)
            lb.append((JCExpression)t);
        return make.Annotation((JCTree)type, lb.toList());
    }

    public ArrayAccessTree ArrayAccess(ExpressionTree array, ExpressionTree index) {
        return make.Indexed((JCExpression)array, (JCExpression)index);
    }
    
    public ArrayTypeTree ArrayType(Tree type) {
        return make.TypeArray((JCExpression)type);
    }
    
    public AssertTree Assert(ExpressionTree condition, ExpressionTree detail) {
        return make.Assert((JCExpression)condition, (JCExpression)detail);
    }
    
    public AssignmentTree Assignment(ExpressionTree variable, ExpressionTree expression) {
        return make.Assign((JCExpression)variable, (JCExpression)expression);
    }
    
    public BinaryTree Binary(Kind operator, ExpressionTree left, ExpressionTree right) {
        final int op;
        switch (operator) {
            case MULTIPLY: op = JCTree.MUL; break;
            case DIVIDE: op = JCTree.DIV; break;
            case REMAINDER: op = JCTree.MOD; break;
            case PLUS: op = JCTree.PLUS; break;
            case MINUS: op = JCTree.MINUS; break;
            case LEFT_SHIFT: op = JCTree.SL; break;
            case RIGHT_SHIFT: op = JCTree.SR; break;
            case UNSIGNED_RIGHT_SHIFT: op = JCTree.USR; break;
            case LESS_THAN: op = JCTree.LT; break;
            case GREATER_THAN: op = JCTree.GT; break;
            case LESS_THAN_EQUAL: op = JCTree.LE; break;
            case GREATER_THAN_EQUAL: op = JCTree.GE; break;
            case EQUAL_TO: op = JCTree.EQ; break;
            case NOT_EQUAL_TO: op = JCTree.NE; break;
            case AND: op = JCTree.BITAND; break;
            case XOR: op = JCTree.BITXOR; break;
            case OR: op = JCTree.BITOR; break;
            case CONDITIONAL_AND: op = JCTree.AND; break;
            case CONDITIONAL_OR: op = JCTree.OR; break;
            default:
                throw new IllegalArgumentException("Illegal binary operator: " + operator);
        }
        return make.Binary(op, (JCExpression)left, (JCExpression)right);
    }
    
    public BlockTree Block(List<? extends StatementTree> statements, boolean isStatic) {
        ListBuffer<JCStatement> lb = new ListBuffer<JCStatement>();
        for (StatementTree t : statements)
            lb.append((JCStatement)t);
        return make.Block(isStatic ? Flags.STATIC : 0L, lb.toList());
    }
    
    public BreakTree Break(CharSequence label) {
        Name n = label != null ? names.fromString(label) : null;
        return make.Break(n);
    }
    
    public CaseTree Case(ExpressionTree expression, List<? extends StatementTree> statements) {
        ListBuffer<JCStatement> lb = new ListBuffer<JCStatement>();
        for (StatementTree t : statements)
            lb.append((JCStatement)t);
        return make.Case((JCExpression)expression, lb.toList());
    }
    
    public CatchTree Catch(VariableTree parameter, BlockTree block) {
        return make.Catch((JCVariableDecl)parameter, (JCBlock)block);
    }
    
    public ClassTree Class(ModifiersTree modifiers, 
                     CharSequence simpleName,
                     List<? extends TypeParameterTree> typeParameters,
                     Tree extendsClause,
                     List<? extends Tree> implementsClauses,
                     List<? extends Tree> memberDecls) 
    {
        ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
        for (TypeParameterTree t : typeParameters)
            typarams.append((JCTypeParameter)t);
        ListBuffer<JCExpression> impls = new ListBuffer<JCExpression>();
        for (Tree t : implementsClauses)
            impls.append((JCExpression)t);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        for (Tree t : memberDecls)
            defs.append((JCTree)t);
        return make.ClassDef((JCModifiers)modifiers, 
                             names.fromString(simpleName),
                             typarams.toList(),
                             (JCTree)extendsClause,
                             impls.toList(),
                             defs.toList());
        
    }
    
    public ClassTree Interface(ModifiersTree modifiers, 
                     CharSequence simpleName,
                     List<? extends TypeParameterTree> typeParameters,
                     List<? extends Tree> extendsClauses,
                     List<? extends Tree> memberDecls) 
    {
        long flags = getBitFlags(modifiers.getFlags()) | Flags.INTERFACE;
        return Class(flags, (com.sun.tools.javac.util.List<JCAnnotation>) modifiers.getAnnotations(), simpleName, typeParameters, null, extendsClauses, memberDecls);
    }

    public ClassTree AnnotationType(ModifiersTree modifiers, 
             CharSequence simpleName,
             List<? extends Tree> memberDecls) {
        long flags = getBitFlags(modifiers.getFlags()) | Flags.ANNOTATION;
        return Class(flags, (com.sun.tools.javac.util.List<JCAnnotation>) modifiers.getAnnotations(), simpleName, Collections.<TypeParameterTree>emptyList(), null, Collections.<ExpressionTree>emptyList(), memberDecls);
    }
    
    public ClassTree Enum(ModifiersTree modifiers, 
             CharSequence simpleName,
             List<? extends Tree> implementsClauses,
             List<? extends Tree> memberDecls) {
        long flags = getBitFlags(modifiers.getFlags()) | Flags.ENUM;
        return Class(flags, (com.sun.tools.javac.util.List<JCAnnotation>) modifiers.getAnnotations(), simpleName, Collections.<TypeParameterTree>emptyList(), null, implementsClauses, memberDecls);
    }
    
    public CompilationUnitTree CompilationUnit(ExpressionTree packageDecl, 
                                               List<? extends ImportTree> importDecls, 
                                               List<? extends Tree> typeDecls, 
                                               JavaFileObject sourceFile) {

        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        if (importDecls != null)
            for (Tree t : importDecls)
                defs.append((JCTree)t);
        if (typeDecls != null) 
            for (Tree t : typeDecls)
                defs.append((JCTree)t);
        JCCompilationUnit unit = make.TopLevel(com.sun.tools.javac.util.List.<JCAnnotation>nil(), 
                                               (JCExpression)packageDecl, defs.toList());
        unit.sourcefile = sourceFile;
        return unit;
    }
    
    public CompoundAssignmentTree CompoundAssignment(Kind operator, 
                                                     ExpressionTree variable, 
                                                     ExpressionTree expression) {
        final int op;
        switch (operator) {
            case MULTIPLY_ASSIGNMENT: op = JCTree.MUL_ASG; break;
            case DIVIDE_ASSIGNMENT: op = JCTree.DIV_ASG; break;
            case REMAINDER_ASSIGNMENT: op = JCTree.MOD_ASG; break;
            case PLUS_ASSIGNMENT: op = JCTree.PLUS_ASG; break;
            case MINUS_ASSIGNMENT: op = JCTree.MINUS_ASG; break;
            case LEFT_SHIFT_ASSIGNMENT: op = JCTree.SL_ASG; break;
            case RIGHT_SHIFT_ASSIGNMENT: op = JCTree.SR_ASG; break;
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT: op = JCTree.USR_ASG; break;
            case AND_ASSIGNMENT: op = JCTree.BITAND_ASG; break;
            case XOR_ASSIGNMENT: op = JCTree.BITXOR_ASG; break;
            case OR_ASSIGNMENT: op = JCTree.BITOR_ASG; break;
            default:
                throw new IllegalArgumentException("Illegal binary operator: " + operator);
        }
        return make.Assignop(op, (JCExpression)variable, (JCExpression)expression);
    }
    
    public ConditionalExpressionTree ConditionalExpression(ExpressionTree condition,
                                                           ExpressionTree trueExpression,
                                                           ExpressionTree falseExpression) {
        return make.Conditional((JCExpression)condition,
                                (JCExpression)trueExpression,
                                (JCExpression)falseExpression);
    }
    
    public ContinueTree Continue(CharSequence label) {
        Name n = label != null ? names.fromString(label) : null;
        return make.Continue(n);
    }
    
    public DoWhileLoopTree DoWhileLoop(ExpressionTree condition, StatementTree statement) {
        return make.DoLoop((JCStatement)statement, (JCExpression)condition);
    }
    
    public EmptyStatementTree EmptyStatement() {
        return make.Skip();
    }
    
    public EnhancedForLoopTree EnhancedForLoop(VariableTree variable, 
                                               ExpressionTree expression,
                                               StatementTree statement) {
        return make.ForeachLoop((JCVariableDecl)variable,
                                (JCExpression)expression,
                                (JCStatement)statement);
    }
    
    public ErroneousTree Erroneous(List<? extends Tree> errorTrees) {
        ListBuffer<JCTree> errors = new ListBuffer<JCTree>();
        for (Tree t : errorTrees)
           errors.append((JCTree)t);
        return make.Erroneous(errors.toList());
    }
    
    public ExpressionStatementTree ExpressionStatement(ExpressionTree expression) {
        return make.Exec((JCExpression)expression);
    }
    
    public ForLoopTree ForLoop(List<? extends StatementTree> initializer, 
                               ExpressionTree condition,
                               List<? extends ExpressionStatementTree> update,
                               StatementTree statement) {
        ListBuffer<JCStatement> init = new ListBuffer<JCStatement>();
        for (StatementTree t : initializer)
            init.append((JCStatement)t);
        ListBuffer<JCExpressionStatement> step = new ListBuffer<JCExpressionStatement>();
        for (ExpressionStatementTree t : update)
            step.append((JCExpressionStatement)t);
        return make.ForLoop(init.toList(), (JCExpression)condition,
                            step.toList(), (JCStatement)statement);
    }
    
    public IdentifierTree Identifier(CharSequence name) {
        return make.Ident(names.fromString(name));
    }
    
    public IdentifierTree Identifier(Element element) {
        return make.Ident((Symbol)element);
    }
    
    public IfTree If(ExpressionTree condition, StatementTree thenStatement, StatementTree elseStatement) {
        return make.If((JCExpression)condition, (JCStatement)thenStatement, (JCStatement)elseStatement);
    }
    
    public ImportTree Import(Tree qualid, boolean importStatic) {
        return make.Import((JCTree)qualid, importStatic);
    }
    
    public InstanceOfTree InstanceOf(ExpressionTree expression, Tree type) {
        return make.TypeTest((JCExpression)expression, (JCTree)type);
    }
    
    public LabeledStatementTree LabeledStatement(CharSequence label, StatementTree statement) {
        return make.Labelled(names.fromString(label), (JCStatement)statement);
    }
    
    public LiteralTree Literal(Object value) {
        try {
            if (value instanceof Boolean)  // workaround for javac issue 6504896
                return make.Literal(TypeTags.BOOLEAN, value == Boolean.FALSE ? 0 : 1);
            if (value instanceof Character) // looks like world championship in workarounds here ;-)
                return make.Literal(TypeTags.CHAR, Integer.valueOf((Character) value));
            // workaround for making NULL_LITERAL kind.
            if (value == null) {
                return make.Literal(TypeTags.BOT, value);
            }
            return make.Literal(value);
        } catch (AssertionError e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public MemberSelectTree MemberSelect(ExpressionTree expression, CharSequence identifier) {
        return make.Select((JCExpression)expression, names.fromString(identifier));
    }
    
    public MemberSelectTree MemberSelect(ExpressionTree expression, Element element) {
        return (MemberSelectTree)make.Select((JCExpression)expression, (Symbol)element);
    }
    
    public MethodInvocationTree MethodInvocation(List<? extends ExpressionTree> typeArguments, 
                                                 ExpressionTree method, 
                                                 List<? extends ExpressionTree> arguments) {
        ListBuffer<JCExpression> typeargs = new ListBuffer<JCExpression>();
        for (ExpressionTree t : typeArguments)
            typeargs.append((JCExpression)t);
        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
        for (ExpressionTree t : arguments)
            args.append((JCExpression)t);
        return make.Apply(typeargs.toList(), (JCExpression)method, args.toList());
    }
    
    public MethodTree Method(ModifiersTree modifiers,
                             CharSequence name,
                             Tree returnType,
                             List<? extends TypeParameterTree> typeParameters,
                             List<? extends VariableTree> parameters,
                             List<? extends ExpressionTree> throwsList,
                             BlockTree body,
                             ExpressionTree defaultValue) {
        ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
        for (TypeParameterTree t : typeParameters)
            typarams.append((JCTypeParameter)t);
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        for (VariableTree t : parameters)
            params.append((JCVariableDecl)t);
        ListBuffer<JCExpression> throwz = new ListBuffer<JCExpression>();
        for (ExpressionTree t : throwsList)
            throwz.append((JCExpression)t);
        return make.MethodDef((JCModifiers)modifiers, names.fromString(name),
                              (JCExpression)returnType, typarams.toList(),
                              params.toList(), throwz.toList(),
                              (JCBlock)body, (JCExpression)defaultValue);
    }
    
    public MethodTree Method(ExecutableElement element, BlockTree body) {
        return make.MethodDef((Symbol.MethodSymbol)element, (JCBlock)body);
    }
    
    public ModifiersTree Modifiers(Set<Modifier> flagset, List<? extends AnnotationTree> annotations) {
        return Modifiers(modifiersToFlags(flagset), annotations);
    }
    
    public ModifiersTree Modifiers(long mods, List<? extends AnnotationTree> annotations) {
        ListBuffer<JCAnnotation> anns = new ListBuffer<JCAnnotation>();
        for (AnnotationTree t : annotations)
            anns.append((JCAnnotation)t);
        return make.Modifiers(mods, anns.toList());
    }
    
    public static long modifiersToFlags(Set<Modifier> flagset) {
        long flags = 0L;
        for (Modifier mod : flagset)
            switch (mod) {
                case PUBLIC: flags |= Flags.PUBLIC; break;
                case PROTECTED: flags |= Flags.PROTECTED; break;
                case PRIVATE: flags |= Flags.PRIVATE; break;
                case ABSTRACT: flags |= Flags.ABSTRACT; break;
                case STATIC: flags |= Flags.STATIC; break;
                case FINAL: flags |= Flags.FINAL; break;
                case TRANSIENT: flags |= Flags.TRANSIENT; break;
                case VOLATILE: flags |= Flags.VOLATILE; break;
                case SYNCHRONIZED: flags |= Flags.SYNCHRONIZED; break;
                case NATIVE: flags |= Flags.NATIVE; break;
                case STRICTFP: flags |= Flags.STRICTFP; break;
                default:
                    throw new AssertionError("unknown Modifier enum");
            }
        return flags;
    }
    
    public ModifiersTree Modifiers(Set<Modifier> flagset) {
        return Modifiers(flagset, com.sun.tools.javac.util.List.<AnnotationTree>nil());
    }
    
    public ModifiersTree Modifiers(ModifiersTree oldMods, List<? extends AnnotationTree> annotations) {
        ListBuffer<JCAnnotation> anns = new ListBuffer<JCAnnotation>();
        for (AnnotationTree t : annotations)
            anns.append((JCAnnotation)t);
        return make.Modifiers(((JCModifiers)oldMods).flags, anns.toList());
    }
    
    public NewArrayTree NewArray(Tree elemtype, 
                                 List<? extends ExpressionTree> dimensions,
                                 List<? extends ExpressionTree> initializers) {
        ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
        for (ExpressionTree t : dimensions)
            dims.append((JCExpression)t);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (initializers != null)
            for (ExpressionTree t : initializers)
                elems.append((JCExpression)t);
        return make.NewArray((JCExpression)elemtype, dims.toList(), elems.toList());
    }
    
    public NewClassTree NewClass(ExpressionTree enclosingExpression, 
                          List<? extends ExpressionTree> typeArguments,
                          ExpressionTree identifier,
                          List<? extends ExpressionTree> arguments,
                          ClassTree classBody) {
        ListBuffer<JCExpression> typeargs = new ListBuffer<JCExpression>();
        for (ExpressionTree t : typeArguments)
            typeargs.append((JCExpression)t);
        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
        for (ExpressionTree t : arguments)
            args.append((JCExpression)t);
        return make.NewClass((JCExpression)enclosingExpression, typeargs.toList(),
                             (JCExpression)identifier, args.toList(),
                             (JCClassDecl)classBody);
    }
    
    public RootTree Root(List<CompilationUnitTree> units) {
        return new RootTree(units);
    }
    
    public ParameterizedTypeTree ParameterizedType(Tree type,
                                                   List<? extends ExpressionTree> typeArguments) {
        ListBuffer<JCExpression> typeargs = new ListBuffer<JCExpression>();
        for (ExpressionTree t : typeArguments)
            typeargs.append((JCExpression)t);
        return make.TypeApply((JCExpression)type, typeargs.toList());        
    }
    
    public ParenthesizedTree Parenthesized(ExpressionTree expression) {
        return make.Parens((JCExpression)expression);
    }
    
    public PrimitiveTypeTree PrimitiveType(TypeKind typekind) {
        final int typetag;
        switch (typekind) {
            case BOOLEAN:
                typetag = TypeTags.BOOLEAN;
                break;
            case BYTE:
                typetag = TypeTags.BYTE;
                break;
            case SHORT:
                typetag = TypeTags.SHORT;
                break;
            case INT:
                typetag = TypeTags.INT;
                break;
            case LONG:
                typetag = TypeTags.LONG;
                break;
            case CHAR:
                typetag = TypeTags.CHAR;
                break;
            case FLOAT:
                typetag = TypeTags.FLOAT;
                break;
            case DOUBLE:
                typetag = TypeTags.DOUBLE;
                break;
            case VOID:
                typetag = TypeTags.VOID;
                break;
            default:
                throw new AssertionError("unknown primitive type " + typekind);
        }
        return make.TypeIdent(typetag);
    }
    
    public ExpressionTree QualIdentImpl(Element element) {
        return make.QualIdent((Symbol) element);
    }
    
    public ExpressionTree QualIdent(Element element) {
        Symbol s = (Symbol) element;
        QualIdentTree result = new QualIdentTree(make.QualIdent(s.owner), s.name, s);
        
        result.setPos(make.pos).setType(s.type);
        
        return result;
    }
    
    public ReturnTree Return(ExpressionTree expression) {
        return make.Return((JCExpression)expression);        
    }
    
    public SwitchTree Switch(ExpressionTree expression, List<? extends CaseTree> caseList) {
        ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
        for (CaseTree t : caseList)
            cases.append((JCCase)t);
        return make.Switch((JCExpression)expression, cases.toList());
    }
    
    public SynchronizedTree Synchronized(ExpressionTree expression, BlockTree block) {
        return make.Synchronized((JCExpression)expression, (JCBlock)block);
    }
    
    public ThrowTree Throw(ExpressionTree expression) {
        return make.Throw((JCExpression)expression);
    }
    
    public TryTree Try(BlockTree tryBlock, 
                       List<? extends CatchTree> catchList, 
                       BlockTree finallyBlock) {
        ListBuffer<JCCatch> catches = new ListBuffer<JCCatch>();
        for (CatchTree t : catchList)
            catches.append((JCCatch)t);
        return make.Try((JCBlock)tryBlock, catches.toList(), (JCBlock)finallyBlock);
    }
    
    public com.sun.tools.javac.util.List<JCExpression> Types(List<Type> ts) {
        ListBuffer<JCExpression> types = new ListBuffer<JCExpression>();
        for (Type t : ts)
            types.append((JCExpression) Type(t));
        return types.toList();
    }
    
    public ExpressionTree Type(TypeMirror type) {
        Type t = (Type) type;
        JCExpression tp;
        switch (type.getKind()) {
            case WILDCARD: {
                WildcardType a = ((WildcardType) type);
                tp = make.Wildcard(make.TypeBoundKind(a.kind), (JCExpression) Type(a.type));
                break;
            }
            case DECLARED:
                Type outer = t.getEnclosingType();
                JCExpression clazz = outer.tag == CLASS && t.tsym.owner.kind == TYP
                        ? make.Select((JCExpression) Type(outer), t.tsym)
                        : (JCExpression) QualIdent(t.tsym);
                tp = t.getTypeArguments().isEmpty()
                ? clazz
                        : make.TypeApply(clazz, Types(t.getTypeArguments()));
                break;
            case ARRAY:
                
                tp = make.TypeArray((JCExpression) Type(((ArrayType) type).getComponentType()));
                break;
            default:
        return make.Type((Type)type);
    }
    
        return tp;
    }
    
    public TypeCastTree TypeCast(Tree type, ExpressionTree expression) {
        return make.TypeCast((JCTree)type, (JCExpression)expression);
    }
    
    public TypeParameterTree TypeParameter(CharSequence name, List<? extends ExpressionTree> boundsList) {
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        for (Tree t : boundsList)
            bounds.append((JCExpression)t);
        return make.TypeParameter(names.fromString(name), bounds.toList());
    }
    
    public UnaryTree Unary(Kind operator, ExpressionTree arg) {
        final int op;
        switch (operator) {
            case POSTFIX_INCREMENT: op = JCTree.POSTINC; break;
            case POSTFIX_DECREMENT: op = JCTree.POSTDEC; break;
            case PREFIX_INCREMENT: op = JCTree.PREINC; break;
            case PREFIX_DECREMENT: op = JCTree.PREDEC; break;
            case UNARY_PLUS: op = JCTree.POS; break;
            case UNARY_MINUS: op = JCTree.NEG; break;
            case BITWISE_COMPLEMENT: op = JCTree.COMPL; break;
            case LOGICAL_COMPLEMENT: op = JCTree.NOT; break;
            default:
                throw new IllegalArgumentException("Illegal unary operator: " + operator);
        }
        return make.Unary(op, (JCExpression)arg);
    }
    
    public VariableTree Variable(ModifiersTree modifiers,
                                 CharSequence name,
                                 Tree type,
                                 ExpressionTree initializer) {
        return make.VarDef((JCModifiers)modifiers, names.fromString(name), 
                           (JCExpression)type, (JCExpression)initializer);
    }
    
    public VariableTree Variable(VariableElement variable, ExpressionTree initializer) {
        return make.VarDef((Symbol.VarSymbol)variable, (JCExpression)initializer);
    }
    
    public WhileLoopTree WhileLoop(ExpressionTree condition, StatementTree statement) {
        return make.WhileLoop((JCExpression)condition, (JCStatement)statement);
    }
    
    public WildcardTree Wildcard(Kind kind, Tree type) {
        final BoundKind boundKind;
        switch (kind) {
            case UNBOUNDED_WILDCARD:
                boundKind = BoundKind.UNBOUND;
                break;
            case EXTENDS_WILDCARD:
                boundKind = BoundKind.EXTENDS;
                break;
            case SUPER_WILDCARD:
                boundKind = BoundKind.SUPER;
                break;
            default:
                throw new IllegalArgumentException("Unknown wildcard bound " + kind);
        }
        TypeBoundKind tbk = make.TypeBoundKind(boundKind);
        return make.Wildcard(tbk, (JCTree)type);
    }
    
    ////////////////////////////////////// makers modification suggested by Tom
    
    // AnnotationTree
    public AnnotationTree addAnnotationAttrValue(AnnotationTree annotation, ExpressionTree attrValue) {
        return modifyAnnotationAttrValue(annotation, -1, attrValue, Operation.ADD);
    }
    
    public AnnotationTree insertAnnotationAttrValue(AnnotationTree annotation, int index, ExpressionTree attrValue) {
        return modifyAnnotationAttrValue(annotation, index, attrValue, Operation.ADD);
    }
    
    public AnnotationTree removeAnnotationAttrValue(AnnotationTree annotation, ExpressionTree attrValue) {
        return modifyAnnotationAttrValue(annotation, -1, attrValue, Operation.REMOVE);
    }
    
    public AnnotationTree removeAnnotationAttrValue(AnnotationTree annotation, int index) {
        return modifyAnnotationAttrValue(annotation, index, null, Operation.REMOVE);
    }

    private AnnotationTree modifyAnnotationAttrValue(AnnotationTree annotation, int index, ExpressionTree attrValue, Operation op) {
        AnnotationTree copy = Annotation(
                annotation.getAnnotationType(),
                c(annotation.getArguments(), index, attrValue, op)
        );
        return copy;
    }
    
    // BlockTree
    public BlockTree addBlockStatement(BlockTree block, StatementTree statement) {
        return modifyBlockStatement(block, -1, statement, Operation.ADD);
    }
    
    public BlockTree insertBlockStatement(BlockTree block, int index, StatementTree statement) {
        return modifyBlockStatement(block, index, statement, Operation.ADD);
    }
    
    public BlockTree removeBlockStatement(BlockTree block, StatementTree statement) {
        return modifyBlockStatement(block, -1, statement, Operation.REMOVE);
    }
    
    public BlockTree removeBlockStatement(BlockTree block, int index) {
        return modifyBlockStatement(block, index, null, Operation.REMOVE);
    }
    
    private BlockTree modifyBlockStatement(BlockTree block, int index, StatementTree statement, Operation op) {
        BlockTree copy = Block(
            c(block.getStatements(), index, statement, op),
            block.isStatic()
        );
        return copy;
    }
    
    // CaseTree
    public CaseTree addCaseStatement(CaseTree kejs, StatementTree statement) {
        return modifyCaseStatement(kejs, -1, statement, Operation.ADD);
    }

    public CaseTree insertCaseStatement(CaseTree kejs, int index, StatementTree statement) {
        return modifyCaseStatement(kejs, index, statement, Operation.ADD);
    }
    
    public CaseTree removeCaseStatement(CaseTree kejs, StatementTree statement) {
        return modifyCaseStatement(kejs, -1, statement, Operation.REMOVE);
    }

    public CaseTree removeCaseStatement(CaseTree kejs, int index) {
        return modifyCaseStatement(kejs, index, null, Operation.REMOVE);
    }
    
    private CaseTree modifyCaseStatement(CaseTree kejs, int index, StatementTree statement, Operation op) {
        CaseTree copy = Case(
                kejs.getExpression(),
                c(kejs.getStatements(), index, statement, op)
        );
        return copy;
    }

    // ClassTree
    public ClassTree addClassMember(ClassTree clazz, Tree member) {
        return modifyClassMember(clazz, -1, member, Operation.ADD);
    }
    
    public ClassTree insertClassMember(ClassTree clazz, int index, Tree member) {
        return modifyClassMember(clazz, index, member, Operation.ADD);
    }
    
    public ClassTree removeClassMember(ClassTree clazz, Tree member) {
        return modifyClassMember(clazz, -1, member, Operation.REMOVE);
    }
    
    public ClassTree removeClassMember(ClassTree clazz, int index) {
        return modifyClassMember(clazz, index, null, Operation.REMOVE);
    }
    
    private ClassTree modifyClassMember(ClassTree clazz, int index, Tree member, Operation op) {
        ClassTree copy = Class(
            clazz.getModifiers(),
            clazz.getSimpleName(),
            clazz.getTypeParameters(),
            clazz.getExtendsClause(),
            (List<ExpressionTree>) clazz.getImplementsClause(),
            c(clazz.getMembers(), index, member, op)
        );
        return copy;
    }
    
    public ClassTree addClassTypeParameter(ClassTree clazz, TypeParameterTree typeParameter) {
        return modifyClassTypeParameter(clazz, -1, typeParameter, Operation.ADD);
    }
    
    public ClassTree insertClassTypeParameter(ClassTree clazz, int index, TypeParameterTree typeParameter) {
        return modifyClassTypeParameter(clazz, index, typeParameter, Operation.ADD);
    }

    public ClassTree removeClassTypeParameter(ClassTree clazz, TypeParameterTree typeParameter) {
        return modifyClassTypeParameter(clazz, -1, typeParameter, Operation.REMOVE);
    }
    
    public ClassTree removeClassTypeParameter(ClassTree clazz, int index) {
        return modifyClassTypeParameter(clazz, index, null, Operation.REMOVE);
    }

    private ClassTree modifyClassTypeParameter(ClassTree clazz, int index, TypeParameterTree typeParameter, Operation op) {
        ClassTree copy = Class(
            clazz.getModifiers(),
            clazz.getSimpleName(),
            c(clazz.getTypeParameters(), index, typeParameter, op),
            clazz.getExtendsClause(),
            (List<ExpressionTree>) clazz.getImplementsClause(),
            clazz.getMembers()
        );
        return copy;
    }
    
    public ClassTree addClassImplementsClause(ClassTree clazz, Tree implementsClause) {
        return modifyClassImplementsClause(clazz, -1, implementsClause, Operation.ADD);
    }

    public ClassTree insertClassImplementsClause(ClassTree clazz, int index, Tree implementsClause) {
        return modifyClassImplementsClause(clazz, index, implementsClause, Operation.ADD);
    }
    
    public ClassTree removeClassImplementsClause(ClassTree clazz, Tree implementsClause) {
        return modifyClassImplementsClause(clazz, -1, implementsClause, Operation.REMOVE);
    }

    public ClassTree removeClassImplementsClause(ClassTree clazz, int index) {
        return modifyClassImplementsClause(clazz, index, null, Operation.REMOVE);
    }
    
    private ClassTree modifyClassImplementsClause(ClassTree clazz, int index, Tree implementsClause, Operation op) {
        ClassTree copy = Class(
            clazz.getModifiers(),
            clazz.getSimpleName(),
            clazz.getTypeParameters(),
            clazz.getExtendsClause(),
            c((List<ExpressionTree>) clazz.getImplementsClause(), index, implementsClause, op), // todo: cast!
            clazz.getMembers()
        );
        return copy;
    }
    
    // CompilationUnit
    public CompilationUnitTree addCompUnitTypeDecl(CompilationUnitTree compilationUnit, Tree typeDeclaration) {
        return modifyCompUnitTypeDecl(compilationUnit, -1, typeDeclaration, Operation.ADD);
    }
    
    public CompilationUnitTree insertCompUnitTypeDecl(CompilationUnitTree compilationUnit, int index, Tree typeDeclaration) {
        return modifyCompUnitTypeDecl(compilationUnit, index, typeDeclaration, Operation.ADD);
    }
    
    public CompilationUnitTree removeCompUnitTypeDecl(CompilationUnitTree compilationUnit, Tree typeDeclaration) {
        return modifyCompUnitTypeDecl(compilationUnit, -1, typeDeclaration, Operation.REMOVE);
    }
    
    public CompilationUnitTree removeCompUnitTypeDecl(CompilationUnitTree compilationUnit, int index) {
        return modifyCompUnitTypeDecl(compilationUnit, index, null, Operation.REMOVE);
    }
    
    private CompilationUnitTree modifyCompUnitTypeDecl(CompilationUnitTree compilationUnit, int index, Tree typeDeclaration, Operation op) {
        CompilationUnitTree copy = CompilationUnit(
            compilationUnit.getPackageName(),
            compilationUnit.getImports(),
            c(compilationUnit.getTypeDecls(), index, typeDeclaration, op),
            compilationUnit.getSourceFile()
        );
        return copy;
    }
    
    // CompilationUnit
    public CompilationUnitTree addCompUnitImport(CompilationUnitTree compilationUnit, ImportTree importt) {
        return modifyCompUnitImport(compilationUnit, -1, importt, Operation.ADD);
    }
    
    public CompilationUnitTree insertCompUnitImport(CompilationUnitTree compilationUnit, int index, ImportTree importt) {
        return modifyCompUnitImport(compilationUnit, index, importt, Operation.ADD);
    }
    
    public CompilationUnitTree removeCompUnitImport(CompilationUnitTree compilationUnit, ImportTree importt) {
        return modifyCompUnitImport(compilationUnit, -1, importt, Operation.REMOVE);
    }
    
    public CompilationUnitTree removeCompUnitImport(CompilationUnitTree compilationUnit, int index) {
        return modifyCompUnitImport(compilationUnit, index, null, Operation.REMOVE);
    }
    
    private CompilationUnitTree modifyCompUnitImport(CompilationUnitTree compilationUnit, int index, ImportTree importt, Operation op) {
        CompilationUnitTree copy = CompilationUnit(
            compilationUnit.getPackageName(),
            c(compilationUnit.getImports(), index, importt, op),
            compilationUnit.getTypeDecls(),
            compilationUnit.getSourceFile()
        );
        return copy;
    }
    
    /** ErroneousTree */
    
    // ForLoop
    public ForLoopTree addForLoopInitializer(ForLoopTree forLoop, StatementTree statement) {
        return modifyForLoopInitializer(forLoop, -1, statement, Operation.ADD);
    }
    
    public ForLoopTree insertForLoopInitializer(ForLoopTree forLoop, int index, StatementTree statement) {
        return modifyForLoopInitializer(forLoop, index, statement, Operation.ADD);
    }

    public ForLoopTree removeForLoopInitializer(ForLoopTree forLoop, StatementTree statement) {
        return modifyForLoopInitializer(forLoop, -1, statement, Operation.REMOVE);
    }
    
    public ForLoopTree removeForLoopInitializer(ForLoopTree forLoop, int index) {
        return modifyForLoopInitializer(forLoop, index, null, Operation.REMOVE);
    }
    
    private ForLoopTree modifyForLoopInitializer(ForLoopTree forLoop, int index, StatementTree statement, Operation op) {
        ForLoopTree copy = ForLoop(
            c(forLoop.getInitializer(), index, statement, op),
            forLoop.getCondition(),
            forLoop.getUpdate(),
            forLoop.getStatement()
        );
        return copy;
    }
    
    public ForLoopTree addForLoopUpdate(ForLoopTree forLoop, ExpressionStatementTree update) {
        return modifyForLoopUpdate(forLoop, -1, update, Operation.ADD);
    }
    
    public ForLoopTree insertForLoopUpdate(ForLoopTree forLoop, int index, ExpressionStatementTree update) {
        return modifyForLoopUpdate(forLoop, index, update, Operation.ADD);
    }
    
    public ForLoopTree removeForLoopUpdate(ForLoopTree forLoop, ExpressionStatementTree update) {
        return modifyForLoopUpdate(forLoop, -1, update, Operation.REMOVE);
    }
    
    public ForLoopTree removeForLoopUpdate(ForLoopTree forLoop, int index) {
        return modifyForLoopUpdate(forLoop, index, null, Operation.REMOVE);
    }

    private ForLoopTree modifyForLoopUpdate(ForLoopTree forLoop, int index, ExpressionStatementTree update, Operation op) {
        ForLoopTree copy = ForLoop(
            forLoop.getInitializer(),
            forLoop.getCondition(),
            c(forLoop.getUpdate(), index, update, op),
            forLoop.getStatement()
        );
        return copy;
    }
    
    // MethodInvocation
    public MethodInvocationTree addMethodInvocationArgument(MethodInvocationTree methodInvocation, ExpressionTree argument, ExpressionTree typeArgument) {
        return modifyMethodInvocationArgument(methodInvocation, -1, argument, typeArgument, Operation.ADD);
    }
    
    public MethodInvocationTree insertMethodInvocationArgument(MethodInvocationTree methodInvocation, int index, ExpressionTree argument, ExpressionTree typeArgument) {
        return modifyMethodInvocationArgument(methodInvocation, index, argument, typeArgument, Operation.ADD);
    }

    public MethodInvocationTree removeMethodInvocationArgument(MethodInvocationTree methodInvocation, ExpressionTree argument, ExpressionTree typeArgument) {
        return modifyMethodInvocationArgument(methodInvocation, -1, argument, typeArgument, Operation.REMOVE);
    }
    
    public MethodInvocationTree removeMethodInvocationArgument(MethodInvocationTree methodInvocation, int index) {
        return modifyMethodInvocationArgument(methodInvocation, index, null, null, Operation.REMOVE);
    }
    
    private MethodInvocationTree modifyMethodInvocationArgument(MethodInvocationTree methodInvocation, int index, ExpressionTree argument, ExpressionTree typeArgument, Operation op) {
        MethodInvocationTree copy = MethodInvocation(
            c((List<? extends ExpressionTree>) methodInvocation.getTypeArguments(), index, typeArgument, op),
            methodInvocation.getMethodSelect(),
            c(methodInvocation.getArguments(), index, argument, op)
        );
        return copy;
    }
    
    // Method
    public MethodTree addMethodTypeParameter(MethodTree method, TypeParameterTree typeParameter) {
        return modifyMethodTypeParameter(method, -1, typeParameter, Operation.ADD);
    }

    public MethodTree insertMethodTypeParameter(MethodTree method, int index, TypeParameterTree typeParameter) {
        return modifyMethodTypeParameter(method, index, typeParameter, Operation.ADD);
    }

    public MethodTree removeMethodTypeParameter(MethodTree method, TypeParameterTree typeParameter) {
        return modifyMethodTypeParameter(method, -1, typeParameter, Operation.REMOVE);
    }

    public MethodTree removeMethodTypeParameter(MethodTree method, int index) {
        return modifyMethodTypeParameter(method, index, null, Operation.REMOVE);
    }
    
    private MethodTree modifyMethodTypeParameter(MethodTree method, int index, TypeParameterTree typeParameter, Operation op) {
        MethodTree copy = Method(
                method.getModifiers(),
                method.getName(),
                method.getReturnType(),
                c(method.getTypeParameters(), index, typeParameter, op),
                method.getParameters(),
                method.getThrows(),
                method.getBody(),
                (ExpressionTree) method.getDefaultValue()
        );
        return copy;
    }
    
    public MethodTree addMethodParameter(MethodTree method, VariableTree parameter) {
        return modifyMethodParameter(method, -1, parameter, Operation.ADD);
    }

    public MethodTree insertMethodParameter(MethodTree method, int index, VariableTree parameter) {
        return modifyMethodParameter(method, index, parameter, Operation.ADD);
    }
    
    public MethodTree removeMethodParameter(MethodTree method, VariableTree parameter) {
        return modifyMethodParameter(method, -1, parameter, Operation.REMOVE);
    }

    public MethodTree removeMethodParameter(MethodTree method, int index) {
        return modifyMethodParameter(method, index, null, Operation.REMOVE);
    }
    
    private MethodTree modifyMethodParameter(MethodTree method, int index, VariableTree parameter, Operation op) {
        MethodTree copy = Method(
                method.getModifiers(),
                method.getName(),
                method.getReturnType(),
                method.getTypeParameters(),
                c(method.getParameters(), index, parameter, op),
                method.getThrows(),
                method.getBody(),
                (ExpressionTree) method.getDefaultValue()
        );
        return copy;
    }
    
    public MethodTree addMethodThrows(MethodTree method, ExpressionTree throwz) {
        return modifyMethodThrows(method, -1, throwz, Operation.ADD);
    }
    
    public MethodTree insertMethodThrows(MethodTree method, int index, ExpressionTree throwz) {
        return modifyMethodThrows(method, index, throwz, Operation.ADD);
    }
    
    public MethodTree removeMethodThrows(MethodTree method, ExpressionTree throwz) {
        return modifyMethodThrows(method, -1, throwz, Operation.REMOVE);
    }
    
    public MethodTree removeMethodThrows(MethodTree method, int index) {
        return modifyMethodThrows(method, index, null, Operation.REMOVE);
    }
    
    private MethodTree modifyMethodThrows(MethodTree method, int index, ExpressionTree throwz, Operation op) {
        MethodTree copy = Method(
                method.getModifiers(),
                method.getName(),
                method.getReturnType(),
                method.getTypeParameters(),
                method.getParameters(),
                c(method.getThrows(), index, throwz, op),
                method.getBody(),
                (ExpressionTree) method.getDefaultValue()
        );
        return copy;
    }
    
    // Modifiers
    public ModifiersTree addModifiersAnnotation(ModifiersTree modifiers, AnnotationTree annotation) {
        return modifyModifiersAnnotation(modifiers, -1, annotation, Operation.ADD);
    }

    public ModifiersTree insertModifiersAnnotation(ModifiersTree modifiers, int index, AnnotationTree annotation) {
        return modifyModifiersAnnotation(modifiers, index, annotation, Operation.ADD);
    }
    
    public ModifiersTree removeModifiersAnnotation(ModifiersTree modifiers, AnnotationTree annotation) {
        return modifyModifiersAnnotation(modifiers, -1, annotation, Operation.REMOVE);
    }

    public ModifiersTree removeModifiersAnnotation(ModifiersTree modifiers, int index) {
        return modifyModifiersAnnotation(modifiers, index, null, Operation.REMOVE);
    }
    
    private ModifiersTree modifyModifiersAnnotation(ModifiersTree modifiers, int index, AnnotationTree annotation, Operation op) {
        ModifiersTree copy = Modifiers(
            modifiers.getFlags(),
            c(modifiers.getAnnotations(), index, annotation, op)
        );
        return copy;
    }
    
    // NewArray
    public NewArrayTree addNewArrayDimension(NewArrayTree newArray, ExpressionTree dimension) {
        return modifyNewArrayDimension(newArray, -1, dimension, Operation.ADD);
    }

    public NewArrayTree insertNewArrayDimension(NewArrayTree newArray, int index, ExpressionTree dimension) {
        return modifyNewArrayDimension(newArray, index, dimension, Operation.ADD);
    }
    
    public NewArrayTree removeNewArrayDimension(NewArrayTree newArray, ExpressionTree dimension) {
        return modifyNewArrayDimension(newArray, -1, dimension, Operation.REMOVE);
    }

    public NewArrayTree removeNewArrayDimension(NewArrayTree newArray, int index) {
        return modifyNewArrayDimension(newArray, index, null, Operation.REMOVE);
    }
    
    private NewArrayTree modifyNewArrayDimension(NewArrayTree newArray, int index, ExpressionTree dimension, Operation op) {
        NewArrayTree copy = NewArray(
            newArray.getType(),
            c(newArray.getDimensions(), index, dimension, op),
            newArray.getInitializers()
        );
        return copy;
    }
    
    public NewArrayTree addNewArrayInitializer(NewArrayTree newArray, ExpressionTree initializer) {
        return modifyNewArrayInitializer(newArray, -1, initializer, Operation.ADD);
    }

    public NewArrayTree insertNewArrayInitializer(NewArrayTree newArray, int index, ExpressionTree initializer) {
        return modifyNewArrayInitializer(newArray, index, initializer, Operation.ADD);
    }
    
    public NewArrayTree removeNewArrayInitializer(NewArrayTree newArray, ExpressionTree initializer) {
        return modifyNewArrayInitializer(newArray, -1, initializer, Operation.REMOVE);
    }

    public NewArrayTree removeNewArrayInitializer(NewArrayTree newArray, int index) {
        return modifyNewArrayInitializer(newArray, index, null, Operation.REMOVE);
    }
    
    private NewArrayTree modifyNewArrayInitializer(NewArrayTree newArray, int index, ExpressionTree initializer, Operation op) {
        NewArrayTree copy = NewArray(
            newArray.getType(),
            newArray.getDimensions(),
            c(newArray.getInitializers(), index, initializer, op)
        );
        return copy;
    }
    
    // NewClass
    public NewClassTree addNewClassArgument(NewClassTree newClass, ExpressionTree typeArgument, ExpressionTree argument) {
        return modifyNewClassArgument(newClass, -1, typeArgument, argument, Operation.ADD);
    }

    public NewClassTree insertNewClassArgument(NewClassTree newClass, int index, ExpressionTree typeArgument, ExpressionTree argument) {
        return modifyNewClassArgument(newClass, index, typeArgument, argument, Operation.ADD);
    }

    public NewClassTree removeNewClassArgument(NewClassTree newClass, ExpressionTree typeArgument, ExpressionTree argument) {
        return modifyNewClassArgument(newClass, -1, typeArgument, argument, Operation.REMOVE);
    }

    public NewClassTree removeNewClassArgument(NewClassTree newClass, int index) {
        return modifyNewClassArgument(newClass, index, null, null, Operation.REMOVE);
    }
    
    private NewClassTree modifyNewClassArgument(NewClassTree newClass, int index, ExpressionTree typeArgument, ExpressionTree argument, Operation op) {
        NewClassTree copy = NewClass(
            newClass.getEnclosingExpression(),
            c((List<ExpressionTree>) newClass.getTypeArguments(), index, typeArgument, op),
            newClass.getIdentifier(),
            c(newClass.getArguments(), index, argument, op),
            newClass.getClassBody()
        );
        return copy;
    }

    // ParameterizedType
    public ParameterizedTypeTree addParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, ExpressionTree argument) {
        return modifyParameterizedTypeTypeArgument(parameterizedType, -1, argument, Operation.ADD);
    }

    public ParameterizedTypeTree insertParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, int index, ExpressionTree argument) {
        return modifyParameterizedTypeTypeArgument(parameterizedType, index, argument, Operation.ADD);
    }
    
    public ParameterizedTypeTree removeParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, ExpressionTree argument) {
        return modifyParameterizedTypeTypeArgument(parameterizedType, -1, argument, Operation.REMOVE);
    }

    public ParameterizedTypeTree removeParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, int index) {
        return modifyParameterizedTypeTypeArgument(parameterizedType, index, null, Operation.REMOVE);
    }
    
    private ParameterizedTypeTree modifyParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, int index, ExpressionTree argument, Operation op) {
        ParameterizedTypeTree copy = ParameterizedType(
            parameterizedType.getType(),
            c((List<ExpressionTree>) parameterizedType.getTypeArguments(), index, argument, op)
        );
        return copy;
    }

    // Switch
    public SwitchTree addSwitchCase(SwitchTree swic, CaseTree kejs) {
        return modifySwitchCase(swic, -1, kejs, Operation.ADD);
    }

    public SwitchTree insertSwitchCase(SwitchTree swic, int index, CaseTree kejs) {
        return modifySwitchCase(swic, index, kejs, Operation.ADD);
    }

    public SwitchTree removeSwitchCase(SwitchTree swic, CaseTree kejs) {
        return modifySwitchCase(swic, -1, kejs, Operation.REMOVE);
    }

    public SwitchTree removeSwitchCase(SwitchTree swic, int index) {
        return modifySwitchCase(swic, index, null, Operation.REMOVE);
    }

    private SwitchTree modifySwitchCase(SwitchTree swic, int index, CaseTree kejs, Operation op) {
        SwitchTree copy = Switch(
            swic.getExpression(),
            c(swic.getCases(), index, kejs, op)
        );
        return copy;
    }
    
    // Try
    public TryTree addTryCatch(TryTree traj, CatchTree kec) {
        return modifyTryCatch(traj, -1, kec, Operation.ADD);
    }
    
    public TryTree insertTryCatch(TryTree traj, int index, CatchTree kec) {
        return modifyTryCatch(traj, index, kec, Operation.ADD);
    }
    
    public TryTree removeTryCatch(TryTree traj, CatchTree kec) {
        return modifyTryCatch(traj, -1, kec, Operation.REMOVE);
    }
    
    public TryTree removeTryCatch(TryTree traj, int index) {
        return modifyTryCatch(traj, index, null, Operation.REMOVE);
    }

    private TryTree modifyTryCatch(TryTree traj, int index, CatchTree kec, Operation op) {
        TryTree copy = Try(
            traj.getBlock(),
            c(traj.getCatches(), index, kec, op),
            traj.getFinallyBlock()
        );
        return copy;
    }
            
    public TypeParameterTree addTypeParameterBound(TypeParameterTree typeParameter, ExpressionTree bound) {
        return modifyTypeParameterBound(typeParameter, -1, bound, Operation.ADD);
    }

    public TypeParameterTree insertTypeParameterBound(TypeParameterTree typeParameter, int index, ExpressionTree bound) {
        return modifyTypeParameterBound(typeParameter, index, bound, Operation.ADD);
    }
    
    public TypeParameterTree removeTypeParameterBound(TypeParameterTree typeParameter, ExpressionTree bound) {
        return modifyTypeParameterBound(typeParameter, -1, bound, Operation.REMOVE);
    }

    public TypeParameterTree removeTypeParameterBound(TypeParameterTree typeParameter, int index) {
        return modifyTypeParameterBound(typeParameter, index, null, Operation.REMOVE);
    }
            
    private TypeParameterTree modifyTypeParameterBound(TypeParameterTree typeParameter, int index, ExpressionTree bound, Operation op) {
        TypeParameterTree copy = TypeParameter(
            typeParameter.getName(),
            c((List<ExpressionTree>) typeParameter.getBounds(), index, bound, op)
        );
        return copy;
    }
    
    private <E extends Tree> List<E> c(List<? extends E> originalList, int index, E item, Operation operation) {
        List<E> copy = new ArrayList<E>(originalList);
        switch (operation) {
            case ADD:
                if (index > -1) {
                    copy.add(index, item);
                } else {
                    copy.add(item);
                }
                break;
            case REMOVE:
                if (index > -1) {
                    copy.remove(index);
                } else {
                    copy.remove(item);
                }
                break;
        }
        return copy;
    }
    
    /**
     * Represents operation on list
     */
    private static enum Operation {
        /** list's add operation */
        ADD,
        
        /** list's remove operation */
        REMOVE
    }
    
    public <N extends Tree> N setLabel(final N node, final CharSequence aLabel) 
            throws IllegalArgumentException
    {
        // todo (#pf): Shouldn't here be check that names are not the same?
        // i.e. node label == aLabel? -- every case branch has to check itself
        // This will improve performance, no change was done by API user.
        Tree.Kind kind = node.getKind();
        
        switch (kind) {
            case BREAK: {
                BreakTree t = (BreakTree) node;
                N clone = (N) Break(
                        aLabel
                        );
                return clone;
            }
            case CLASS: {
                ClassTree t = (ClassTree) node;
                // copy all the members, for constructor change their name
                // too!
                List<? extends Tree> members = t.getMembers();
                List<Tree> membersCopy = new ArrayList<Tree>();
                for (Tree member : members) {
                    if (member.getKind() == Kind.METHOD) {
                        MethodTree m = (MethodTree) member;
                        // there should be some better mechanism to detect
                        // that it is constructor, there are tree.sym.isConstr()
                        // at level of javac node.
                        if ("<init>".contentEquals(m.getName())) { // NOI18N
                            // ensure we will not do anything with syntetic
                            // constructor -- todo (#pf): one of strange
                            // hacks.
                            if (model.getPos(t) != model.getPos(m)) {
                                MethodTree a = setLabel(m, aLabel);
                                model.setPos(a, model.getPos(m));
                                membersCopy.add(a);
                            } else {
                                membersCopy.add(member);
                            }
                            continue;
                        }
                    }
                    membersCopy.add(member);
                }
                // and continue the same way as other cases
                N clone = (N) Class(
                        t.getModifiers(),
                        aLabel,
                        t.getTypeParameters(),
                        t.getExtendsClause(),
                        (List<ExpressionTree>) t.getImplementsClause(),
                        membersCopy);
                return clone;
            }
            case CONTINUE: {
                ContinueTree t = (ContinueTree) node;
                N clone = (N) Continue(aLabel);
                return clone;
            }
            case IDENTIFIER: {
                IdentifierTree t = (IdentifierTree) node;
                N clone = (N) Identifier(
                        aLabel
                        );
                return clone;
            }
            case LABELED_STATEMENT: {
                LabeledStatementTree t = (LabeledStatementTree) node;
                N clone = (N) LabeledStatement(
                        aLabel,
                        t.getStatement()
                        );
                return clone;
            }
            case MEMBER_SELECT: {
                MemberSelectTree t = (MemberSelectTree) node;
                N clone = (N) MemberSelect(
                        (ExpressionTree) t.getExpression(),
                        aLabel
                        );
                return clone;
            }
            case METHOD: {
                MethodTree t = (MethodTree) node;
                N clone = (N) Method(
                        t.getModifiers(),
                        aLabel,
                        t.getReturnType(),
                        t.getTypeParameters(),
                        (List) t.getParameters(),
                        t.getThrows(),
                        t.getBody(),
                        (ExpressionTree) t.getDefaultValue()
                );
                return clone;
            }
            case TYPE_PARAMETER: {
                TypeParameterTree t = (TypeParameterTree) node;
                N clone = (N) TypeParameter(
                        aLabel,
                        (List<ExpressionTree>) t.getBounds()
                        );
                return clone;
            }
            case VARIABLE: {
                VariableTree t = (VariableTree) node;
                N clone = (N) Variable(
                        (ModifiersTree) t.getModifiers(),
                        aLabel,
                        (Tree) t.getType(),
                        (ExpressionTree) t.getInitializer()
                        );
                model.setPos(clone, model.getPos(t));
                return clone;
            }
        }
        // provided incorrect node's kind, no case branch was used
        throw new IllegalArgumentException("Invalid node's kind. Supported" +
                " kinds are BREAK, CLASS, CONTINUE, IDENTIFIER, LABELED_STATEMENT," + 
                " MEMBER_SELECT, METHOD, TYPE_PARAMETER, VARIABLE");
    }
    
    private List<TypeMirror> typesFromTrees(List<? extends Tree> trees) {
        List<TypeMirror> types = new ArrayList<TypeMirror>();
        for (Tree t : trees)
            types.add(model.getType(t));
        return types;
    }
    
    private ClassTree Class(long modifiers, 
                     com.sun.tools.javac.util.List<JCAnnotation> annotations,
                     CharSequence simpleName,
                     List<? extends TypeParameterTree> typeParameters,
                     Tree extendsClause,
                     List<? extends Tree> implementsClauses,
                     List<? extends Tree> memberDecls) {
        ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
        for (TypeParameterTree t : typeParameters)
            typarams.append((JCTypeParameter)t);
        ListBuffer<JCExpression> impls = new ListBuffer<JCExpression>();
        for (Tree t : implementsClauses)
            impls.append((JCExpression)t);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        for (Tree t : memberDecls)
            defs.append((JCTree)t);
        return make.ClassDef(make.Modifiers(modifiers, annotations),
                             names.fromString(simpleName),
                             typarams.toList(),
                             (JCTree)extendsClause,
                             impls.toList(),
                             defs.toList());
        
    }
    
    private long getBitFlags(Set<Modifier> modifiers) {
        int flags  = 0;
        for (Modifier modifier : modifiers) {
            switch (modifier) {
                case PUBLIC:       flags |= PUBLIC; break;
                case PROTECTED:    flags |= PROTECTED; break;
                case PRIVATE:      flags |= PRIVATE; break;   
                case ABSTRACT:     flags |= ABSTRACT; break;  
                case STATIC:       flags |= STATIC; break;    
                case FINAL:        flags |= FINAL; break;     
                case TRANSIENT:    flags |= TRANSIENT; break; 
                case VOLATILE:     flags |= VOLATILE; break;  
                case SYNCHRONIZED: flags |= SYNCHRONIZED; break;
                case NATIVE:       flags |= NATIVE; break;
                case STRICTFP:     flags |= STRICTFP; break;
                default:
                    break;
            }
        }
        return flags;
    }
}
