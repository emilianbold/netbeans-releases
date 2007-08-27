/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model.util;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

/**
 * Misc. static methods used for tracing of code model objects
 * @author vk155633
 */
public class CsmTracer {
    
    private static final String NULL_TEXT = "null";
    
    private final int step = 4;
    private StringBuilder indentBuffer = new StringBuilder();
    private boolean deep = true;
    private boolean testUniqueName = false;
    private PrintStream printStream;

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
    
    //TODO: remove as soon as regression tests are fixed
    
    public CsmTracer() {
	printStream = System.out;
    }
    
    public CsmTracer(boolean useStdErr) {
	printStream = useStdErr ? System.err : System.out;
    }
    
    public CsmTracer(PrintStream printStream) {
	this.printStream = printStream;
    }
    
    public void setDeep(boolean deep) {
	this.deep = deep;
    }
    
    public void setTestUniqueName(boolean value) {
	testUniqueName = value;
    }
    
    public void indent() {
	setupIndentBuffer(indentBuffer.length() + step);
    }
    
    public void unindent() {
	setupIndentBuffer(indentBuffer.length() - step);
    }
    
    private void setupIndentBuffer(int len) {
	if( len <= 0 ) {
	    indentBuffer.setLength(0);
	} else {
	    indentBuffer.setLength(len);
	    for( int i = 0; i < len; i++ ) {
		indentBuffer.setCharAt(i,  ' ');
	    }
	}
    }
    
    public void print(String s) {
	print(s, true);
    }
    
    protected PrintStream getStream() {
	return printStream;
    }
    
    public void print(String s, boolean newline) {
	PrintStream stream = getStream();
	if( newline ) {
	    stream.print('\n');
	    stream.print(indentBuffer.toString());
	}
	stream.print(s);
    }

    public static String toString(CsmObject obj) {
        String out;
        if (CsmKindUtilities.isMacro(obj)) {
            out = toString((CsmMacro)obj);
        } else if (CsmKindUtilities.isInclude(obj)) {
            out = toString((CsmInclude)obj);
        } else if (CsmKindUtilities.isNamespace(obj)) {
            out = toString((CsmNamespace)obj);
        } else if (CsmKindUtilities.isClassifier(obj)) {
            out = toString((CsmClassifier)obj);
        } else if (CsmKindUtilities.isFunction(obj)) {
            out = toString((CsmFunction)obj);
        } else if (CsmKindUtilities.isVariable(obj)) {
            out = toString((CsmVariable)obj);
        } else if (CsmKindUtilities.isDeclaration(obj)) {
            out = toString((CsmDeclaration)obj);
        } else if (CsmKindUtilities.isType(obj)) {
            out = "TYPE " + toString((CsmType)obj, true); // NOI18N
        } else if (CsmKindUtilities.isExpression(obj)) {
            out = toString((CsmExpression)obj, true);
        } else if (CsmKindUtilities.isStatement(obj)) {
            out = toString((CsmStatement)obj);
        } else if (CsmKindUtilities.isOffsetable(obj)) {
            out = getOffsetString(obj, true);
        } else if (CsmKindUtilities.isFile(obj)) {
            out = "FILE " + toString((CsmFile)obj); // NOI18N
        } else {
            out = (obj == null ? "" : "UNKNOWN CSM OBJECT ") + obj; // NOI18N
        }
        return out;
    }
    
    public static String toString(CsmNamespace nsp) {
	if( nsp == null ) {
	    return NULL_TEXT; // NOI18N
	}   
        return "NS " + nsp.getQualifiedName(); // NOI18N
    }
    
    public static String toString(CsmMacro macro) {
	if( macro == null ) {
	    return NULL_TEXT; // NOI18N
	}   
        return "MACROS " + macro; // NOI18N
    }
    
    public static String toString(CsmInclude incl) {
	if( incl == null ) {
	    return NULL_TEXT; // NOI18N
	}   
        return "INCLUDE " + incl; // NOI18N
    }
    
    public static String toString(CsmStatement stmt) {
	if( stmt == null ) {
	    return NULL_TEXT; // NOI18N
	}
	StringBuilder sb = new StringBuilder();
        sb.append("STMT ").append(stmt.getKind()).append(" "); // NOI18N
	sb.append("text='"); // NOI18N
	sb.append(stmt.getText());
	sb.append("'"); // NOI18N
	return sb.toString();
    }
    
    public static String toString(CsmExpression expr, boolean traceKind) {
	if( expr == null ) {
	    return NULL_TEXT; // NOI18N
	}
	StringBuilder sb = new StringBuilder();
        if (traceKind) {
            sb.append("EXPR ").append(expr.getKind()).append(" "); // NOI18N
        }
	sb.append("text='"); // NOI18N
	sb.append(expr.getText());
	sb.append("'"); // NOI18N
	return sb.toString();
    }
    
//    public boolean isDummyUnresolved(CsmClassifier decl) {
//        return decl == null || decl.getClass().getName().endsWith("Unresolved$UnresolvedClass");
//    }
    
    public static String toString(CsmInheritance inh) {
	StringBuilder sb = new StringBuilder();
	
	sb.append("CLASS="); // NOI18N
	CsmClassifier cls = inh.getCsmClassifier();
	//sb.append(isDummyUnresolved(cls) ? "<unresolved>" : cls.getQualifiedName());
	sb.append(cls == null ? NULL_TEXT : cls.getQualifiedName()); // NOI18N
	
	sb.append(" VISIBILITY==" + inh.getVisibility()); // NOI18N
	sb.append(" virtual==" + inh.isVirtual()); // NOI18N
	
	sb.append(" text='"); // NOI18N
	sb.append(inh.getText());
	sb.append("'"); // NOI18N
	return sb.toString();
    }
    
    public static String toString(CsmCondition condition) {
	if( condition == null ) {
	    return NULL_TEXT; // NOI18N
	}
	StringBuilder sb = new StringBuilder(condition.getKind().toString());
	sb.append(' ');
	if( condition.getKind() == CsmCondition.Kind.EXPRESSION  ) {
	    sb.append(toString(condition.getExpression(), false));
	} else { // condition.getKind() == CsmCondition.Kind.DECLARATION
	    CsmVariable var = condition.getDeclaration();
	    sb.append(toString(var, false));
	}
	return sb.toString();
    }

    public static String toString(CsmDeclaration decl) {
        return decl.getKind() + " " + toString(decl, true);
    }    

    private static String toString(CsmDeclaration decl, boolean traceFile) {
        if (decl == null) {
            return NULL_TEXT;
        }
        return decl.getQualifiedName() + getOffsetString(decl, traceFile);
    }
    
    public static String toString(CsmClassifier cls) {
        return cls.getKind() + " " + toString(cls, true);
    }    
        
    private static String toString(CsmClassifier cls, boolean traceFile) {
        if (cls == null) {
            return NULL_TEXT;
        }
        return cls.getQualifiedName() + getOffsetString(cls, traceFile);
    }
    
    private static String toString(CsmType type, boolean traceFile) {
	StringBuilder sb = new StringBuilder();
	if( type == null ) {
	    sb.append(NULL_TEXT); // NOI18N
	} else {
	    if( type.isConst() ) {
		sb.append("const "); // NOI18N
	    }
	    if( type.isPointer() ) {
		for( int i = 0; i < type.getPointerDepth(); i++ ) {
		    sb.append("*"); // NOI18N
		}
	    }
	    if( type.isReference() ) sb.append("&"); // NOI18N
	    CsmClassifier classifier = type.getClassifier();
	    if( classifier != null ) {
		sb.append(classifier.getQualifiedName());
//		if( classifier instanceof CsmOffsetable ) {
//		    CsmOffsetable offs = (CsmOffsetable) classifier;
//		    sb.append("(Declared in ");
//		    sb.append(offs.getContainingFile());
//		    sb.append(' ');
//		    sb.append(getOffsetString(offs));
//		    sb.append(')');
//		}
	    }
	    else {
		sb.append("<*no_classifier*>"); // NOI18N
	    }
	    
	    for( int i = 0; i < type.getArrayDepth(); i++ ) {
		sb.append("[]"); // NOI18N
	    }
	    sb.append(" TEXT=" + type.getText()); // NOI18N
	}
	if( type instanceof CsmOffsetable ) {
	    sb.append(' ');
	    sb.append(getOffsetString(type, traceFile));
	}
	return sb.toString();
    }
    
    public static String toString(CsmFile file) {
	if( file == null ) {
	    return NULL_TEXT; // NOI18N
	}
        File parent = new File(file.getAbsolutePath()).getParentFile();
        return (parent != null ? parent.getName() +"/" : "") + file.getName();
    }
    
    public static String toString(CsmVariable var) {
        return var.getKind() + " " + toString(var, true);
    }
    
    private static String toString(CsmVariable var, boolean traceFile) {
	if( var == null ) {
	    return NULL_TEXT; // NOI18N
	}
	StringBuilder sb = new StringBuilder(var.getName());
	sb.append(getOffsetString(var, traceFile));
	sb.append("  TYPE: " + toString(var.getType(), false)); // NOI18N
	sb.append("  INIT: " + toString(var.getInitialValue(), false)); // NOI18N
	sb.append("  " + getScopeString(var)); // NOI18N
	return sb.toString();
    }

    public static String toString(CsmFunction fun) {
        return fun.getKind() + " " + toString(fun, true);
    }
    
    private static String toString(CsmFunction fun, boolean signature) {
	if( fun == null ) {
	    return NULL_TEXT; // NOI18N
	} else {
	    return (signature ? fun.getSignature() : fun.getName()) + ' ' + getOffsetString(fun, signature);
	}
    }
    
    
    public void dumpModel(CsmFunction fun) {
	print("FUNCTION " + fun.getName() + getOffsetString(fun, false) + ' ' + getBriefClassName(fun) + // NOI18N
		' ' + getScopeString(fun)); // NOI18N
	if( fun instanceof CsmFunctionDefinition ) {
	    if( deep ) {
		//indent();
		dumpStatement(((CsmFunctionDefinition) fun).getBody());
		//unindent();
	    }
	}
	indent();
	print("DEFINITION: " + toString(fun.getDefinition(), false)); // NOI18N
	print("SIGNATURE " + fun.getSignature()); // NOI18N
	print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
        if (fun instanceof CsmFriendFunction) {
            print("REFERENCED FRIEND FUNCTION: " + toString(((CsmFriendFunction)fun).getReferencedFunction(), false));
        }
	dumpParameters(fun.getParameters());
	print("RETURNS " + toString(fun.getReturnType(), false)); // NOI18N
	unindent();
    }
    
    public void dumpModel(CsmFunctionDefinition fun) {
	CsmFunction decl = fun.getDeclaration();
	print("FUNCTION DEFINITION " + fun.getName() + ' ' + getOffsetString(fun, false) + // NOI18N
		' ' + getBriefClassName(fun) + ' ' + getScopeString(fun)); // NOI18N
	indent();
	print("SIGNATURE " + fun.getSignature()); // NOI18N
	print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
	print("DECLARATION: " + toString(decl, false)); // NOI18N
	dumpParameters(fun.getParameters());
	print("RETURNS " + toString(fun.getReturnType(), false)); // NOI18N
	if( deep ) {
	    dumpStatement((CsmStatement) fun.getBody());
	}
	unindent();
    }
    
    public static String getScopeString(CsmScopeElement el) {
	StringBuilder sb = new StringBuilder("SCOPE: ");
	int initLen = sb.length();
	CsmScope scope = el.getScope();
	if (scope == null) {
	    sb.append(NULL_TEXT);
	}
	else {
            if (CsmKindUtilities.isFile(scope)) {
                sb.append(((CsmFile) scope).getName());
	    } else {
                if (CsmKindUtilities.isNamedElement(scope)) {
                    sb.append(((CsmNamedElement) scope).getName());
                    sb.append(' ');
                }
		else {
                    if (CsmKindUtilities.isStatement(scope)) {
			CsmStatement stmt = (CsmStatement) scope;
                        sb.append("Stmt ");
                    }
                    if (CsmKindUtilities.isOffsetable(scope)) {
                        sb.append(getOffsetString(el, false));
                    }
		}
            }
	    if( sb.length() == initLen ) {
		sb.append("???");
	    }
	}
        return sb.toString();
    }
    
    public static String getOffsetString(CsmObject obj, boolean traceFile) {
	//return " [" + obj.getStartOffset() + '-' + obj.getEndOffset() + ']';
//        CsmOffsetable.Position start = obj.getStartPosition();
//        CsmOffsetable.Position end = obj.getEndPosition();
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return ""; // NOI18N
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
	return " [" + offs.getStartPosition() + '-' + offs.getEndPosition() + ']' + (traceFile ? " " + toString(offs.getContainingFile()) : ""); // NOI18N
    }
    
    public String getBriefClassName(Object o) {
	return getBriefClassName(o.getClass());
    }
    
    public String getBriefClassName(Class cls) {
	String name = cls.getName();
	int pos = name.lastIndexOf('.');
	if( pos > 0 ) {
	    name = name.substring(pos + 1);
	}
	return name;
    }
    
    public void dumpParameters(List/*<CsmParameter>*/ parameters) {
	print("PARAMETERS:"); // NOI18N
	if( parameters != null && parameters.size() > 0 ) {
	    indent();
	    for( Iterator iter = parameters.iterator(); iter.hasNext(); ) {
		print(toString((CsmParameter) iter.next(), false));
	    }
	    unindent();
	}
    }
    
    public void dumpStatement(CsmStatement stmt) {
	if( stmt == null ) {
	    print("STATEMENT is null"); // NOI18N
	    return;
	}
	print("STATEMENT " + stmt.getKind() + ' ' + getOffsetString(stmt, false) + ' ' + getScopeString(stmt)); // NOI18N
	indent();
	CsmStatement.Kind kind = stmt.getKind();
	if( kind == CsmStatement.Kind.COMPOUND ) {
	    dumpStatement((CsmCompoundStatement) stmt);
	} else if( kind == CsmStatement.Kind.IF ) {
	    dumpStatement((CsmIfStatement) stmt);
	} else if( kind == CsmStatement.Kind.TRY_CATCH ) {
	    dumpStatement((CsmTryCatchStatement) stmt);
	} else if( kind == CsmStatement.Kind.CATCH ) {
	    dumpStatement((CsmExceptionHandler) stmt);
	} else if( kind == CsmStatement.Kind.DECLARATION ) {
	    dumpStatement((CsmDeclarationStatement) stmt);
	} else if( kind == CsmStatement.Kind.WHILE || kind == CsmStatement.Kind.DO_WHILE ) {
	    dumpStatement((CsmLoopStatement) stmt);
	} else if( kind == CsmStatement.Kind.FOR ) {
	    dumpStatement((CsmForStatement) stmt);
	} else if( kind == CsmStatement.Kind.SWITCH ) {
	    dumpStatement((CsmSwitchStatement) stmt);
	} else if( kind == CsmStatement.Kind.CASE ) {
	    dumpStatement((CsmCaseStatement) stmt);
	} else if( kind == CsmStatement.Kind.BREAK ) {
	} else if( kind == CsmStatement.Kind.CONTINUE ) {
	} else if( kind == CsmStatement.Kind.DEFAULT ) {
	} else if( kind == CsmStatement.Kind.EXPRESSION ) {
	    print(" text: '" + stmt.getText() +'\'', false); // NOI18N
	} else if( kind == CsmStatement.Kind.GOTO ) {
	    print(" text: '" + stmt.getText() +'\'', false); // NOI18N
	} else if( kind == CsmStatement.Kind.LABEL ) {
	    print(" text: '" + stmt.getText() +'\'', false); // NOI18N
	} else if( kind == CsmStatement.Kind.RETURN ) {
	    print(" text: '" + stmt.getText() +'\'', false); // NOI18N
	} else  {
	    print("unexpected statement kind"); // NOI18N
	}
	unindent();
    }
    
    public void dumpStatement(CsmCompoundStatement stmt) {
	if( stmt != null ) {
	    for( Iterator iter = stmt.getStatements().iterator(); iter.hasNext(); ) {
		dumpStatement((CsmStatement) iter.next());
	    }
	}
    }
    
    public void dumpStatement(CsmTryCatchStatement stmt) {
	print("TRY:"); // NOI18N
	dumpStatement(stmt.getTryStatement());
	print("HANDLERS:"); // NOI18N
	for( Iterator iter = stmt.getHandlers().iterator(); iter.hasNext(); ) {
	    dumpStatement((CsmStatement) iter.next());
	}
    }
    
    public void dumpStatement(CsmExceptionHandler stmt) {
	print("PARAMETER: " + toString(stmt.getParameter(), false)); // NOI18N
	dumpStatement((CsmCompoundStatement) stmt);
    }
    
    public void dumpStatement(CsmIfStatement stmt) {
	print("CONDITION " + toString(stmt.getCondition())); // NOI18N
	print("THEN: "); // NOI18N
	indent();
	dumpStatement(stmt.getThen());
	unindent();
	print("ELSE: "); // NOI18N
	indent();
	dumpStatement(stmt.getElse());
	unindent();
    }
    
    public void dumpStatement(CsmDeclarationStatement stmt) {
	for( Iterator iter = stmt.getDeclarators().iterator(); iter.hasNext(); ) {
	    dumpModel((CsmDeclaration) iter.next());
	}
    }
    
    public void dumpStatement(CsmLoopStatement stmt) {
	print("CONDITION: " + toString(stmt.getCondition()) + " isPostCheck()=" + stmt.isPostCheck()); // NOI18N
	print("BODY:"); // NOI18N
	indent();
	dumpStatement(stmt.getBody());
	unindent();
    }
    
    public void dumpStatement(CsmForStatement stmt) {
	print("INIT:"); // NOI18N
	indent();
	dumpStatement(stmt.getInitStatement());
	unindent();
	print("ITERATION: " + toString(stmt.getIterationExpression(), false)); // NOI18N
	print("CONDITION: " + toString(stmt.getCondition())); // NOI18N
	print("BODY:"); // NOI18N
	indent();
	dumpStatement(stmt.getBody());
	unindent();
    }
    
    public void dumpStatement(CsmSwitchStatement stmt) {
	print("CONDITION: " + toString(stmt.getCondition())); // NOI18N
	print("BODY:"); // NOI18N
	indent();
	dumpStatement(stmt.getBody());
	unindent();
    }
    
    public void dumpStatement(CsmCaseStatement stmt) {
	print(" EXPRESSION: " + toString(stmt.getExpression(), false), false); // NOI18N
    }
    
    public void dumpNamespaceDefinitions(CsmNamespace nsp) {
	print("NAMESPACE DEFINITIONS for " + nsp.getName() + " (" + nsp.getQualifiedName() + ") "); // NOI18N
	indent();
	for( Iterator iter = nsp.getDefinitions().iterator(); iter.hasNext(); ) {
	    CsmNamespaceDefinition def = (CsmNamespaceDefinition) iter.next();
	    print(def.getContainingFile().getName() + ' ' + getOffsetString(def, false));
	}
	unindent();
    }
    
    public void dumpModel(CsmProject project) {
	CsmNamespace nsp = project.getGlobalNamespace();
	print("\n========== Dumping model of PROJECT " + project.getName(), true); // NOI18N
	dumpModel(nsp);
    }
    
    public void dumpModel(CsmNamespace nsp) {
	if( ! nsp.isGlobal() ) {
	    dumpNamespaceDefinitions(nsp);
	    print("NAMESPACE " + nsp.getName() + " (" + nsp.getQualifiedName() + ") "); // NOI18N
	    indent();
	}
	for( Iterator<CsmOffsetableDeclaration> iter = getSortedDeclarations(nsp); iter.hasNext(); ) {
	    dumpModel(iter.next());
	}
	for( Iterator<CsmNamespace> iter = getSortedNestedNamespaces(nsp); iter.hasNext(); ) {
	    dumpModel(iter.next());
	}
	if( ! nsp.isGlobal() ) {
	    unindent();
	}
    }
    
    private Iterator<CsmOffsetableDeclaration> getSortedDeclarations(CsmNamespace nsp) {
	SortedMap<String,CsmOffsetableDeclaration> map = new TreeMap<String,CsmOffsetableDeclaration>();
	for( CsmOffsetableDeclaration decl : nsp.getDeclarations() ) {
	    map.put(getSortKey(decl), decl);
	}
	return map.values().iterator();
    }
    
    private Iterator<CsmNamespace> getSortedNestedNamespaces(CsmNamespace nsp) {
	SortedMap<String,CsmNamespace> map = new TreeMap<String,CsmNamespace>();
	for( CsmNamespace decl : nsp.getNestedNamespaces() ) {
	    map.put(decl.getQualifiedName(), decl);
	}
	return map.values().iterator();
    }
    
    private static String getSortKey(CsmDeclaration declaration) {
	StringBuilder sb = new StringBuilder();
	if( declaration instanceof CsmOffsetable ) {
	    sb.append(((CsmOffsetable) declaration).getContainingFile().getAbsolutePath());
	    int start = ((CsmOffsetable) declaration).getStartOffset();
	    String s = Integer.toString(start);
	    int gap = 8 - s.length();
	    while( gap-- > 0 ) {
		sb.append('0');
	    }
	    sb.append(s);
	    sb.append(declaration.getName());
	} else {
	    // actually this never happens
	    // since of all declarations only CsmBuiltin isn't CsmOffsetable
	    // and CsmBuiltin is never added to any file
	    sb.append(declaration.getUniqueName());
	}
	return sb.toString();
    }
    
    public void dumpModel(CsmFile file) {
	dumpModel(file, "\n========== Dumping model of FILE " + file.getName()); // NOI18N
    }
    
    public void dumpModel(CsmFile file, String title) {
	print(title);
	List<CsmInclude> includes = file.getIncludes();
	print("Includes:"); // NOI18N
	if (includes.size() > 0) {
	    for( Iterator<CsmInclude> iter = includes.iterator(); iter.hasNext(); ) {
		CsmInclude o = iter.next();
		print(o.toString());
	    }
	} else {
	    indent();print("<no includes>");unindent(); // NOI18N
	}
	List macros = file.getMacros();
	print("Macros:"); // NOI18N
	if (macros.size() > 0) {
	    for( Iterator iter = macros.iterator(); iter.hasNext(); ) {
		CsmMacro o = (CsmMacro) iter.next();
		print(o.toString());
	    }
	} else {
	    indent();print("<no macros>");unindent(); // NOI18N
	}
	List/*CsmDeclaration*/ objects = file.getDeclarations();
	for( Iterator iter = objects.iterator(); iter.hasNext(); ) {
	    dumpModel((CsmDeclaration) iter.next());
	}
    }
    
    public void dumpModel(CsmVariable var) {
	print((var.isExtern() ? "EXTERN " : "") +  "VARIABLE " + toString(var, false)); // NOI18N
	CsmVariableDefinition def = var.getDefinition();
	if (def != null){
	    indent();
	    print("DEFINITION: " + toString(def, false)); // NOI18N
	    unindent();
	}
    }
    
    public void dumpModel(CsmVariableDefinition var) {
	CsmVariable decl = var.getDeclaration();
	print("VARIABLE DEFINITION " + toString(var, false)); // NOI18N
	indent();
	print("DECLARATION: " + toString(decl, false)); // NOI18N
	unindent();
    }
    
    
//    public void dumpModel(CsmField field) {
//	StringBuilder sb = new StringBuilder("FIELD "); // NOI18N
//	sb.append(field.getVisibility().toString());
//	if( field.isStatic() ) {
//	    sb.append(" STATIC "); // NOI18N
//	}
//	sb.append(" " + toString(field.getType(), false)); // NOI18N
//	sb.append(' ');
//	sb.append(field.getName());
//	sb.append(getOffsetString(field, false));
//	print(sb.toString());
//	CsmVariableDefinition def = field.getDefinition();
//	if (def != null){
//	    indent();
//	    print("DEFINITION: " + toString(def, false)); // NOI18N
//	    unindent();
//	}        
//    }
  
    public void dumpModel(CsmField field) {
	StringBuilder sb = new StringBuilder("FIELD "); // NOI18N
	sb.append(field.getVisibility().toString());
	if( field.isStatic() ) {
	    sb.append(" static"); // NOI18N
	}
	sb.append(" "); // NOI18N
	
	sb.append(toString(field, false));
	print(sb.toString());
	CsmVariableDefinition def = field.getDefinition();
	if (def != null){
	    indent();
	    print("DEFINITION: " + toString(def, false)); // NOI18N
	    unindent();
	}        
    }
    
    public void checkUniqueName(CsmDeclaration decl) {
	String uname = decl.getUniqueName();
	if( (decl instanceof CsmOffsetableDeclaration) &&  needsCheckUniqueName(decl) ) {
	    CsmProject project = ((CsmOffsetable) decl).getContainingFile().getProject();
	    CsmDeclaration found = project.findDeclaration(uname);
	    if( found == null ) {
		print("Unique name check failed: cant't find in project: " + uname); // NOI18N
	    } else if( found != decl ) {
		print("Unique name check failed: declaration found in project differs " + uname); // NOI18N
	    }
	}
	if( ! uname.startsWith(decl.getKind().toString()) ) {
	    print("Warning: unique name '" + uname + "' desn't start with " + decl.getKind().toString()); // NOI18N
	}
    }
    
    protected boolean needsCheckUniqueName(CsmDeclaration decl) {
	if( decl.getName().length() == 0 ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.USING_DECLARATION ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.USING_DIRECTIVE ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.ASM ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.BUILT_IN ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
	    return false;
	} else if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
	    return false;
	}
//        else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
//            return false;
//        }
	else if( decl.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
	    return false;
	} 
	else if( decl.getKind() == CsmDeclaration.Kind.VARIABLE ) {
	    if( CsmKindUtilities.isLocalVariable(decl) ) {
		return false;
	    } else if( CsmKindUtilities.isFileLocalVariable(decl) ) {
		return false;
	    }
	}
	return true;
    }
    
    public void dumpModel(CsmDeclaration decl) {
	if( testUniqueName && (decl instanceof CsmOffsetableDeclaration) ) {
	    checkUniqueName(decl);
	}
	if( CsmKindUtilities.isClass(decl) ) {
	    dumpModel((CsmClass) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.ENUM ) {
	    dumpModel((CsmEnum) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
	    dumpModel((CsmNamespaceDefinition) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION ) {
	    dumpModel((CsmFunction) decl);
	} else if( decl.getKind()== CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
	    dumpModel((CsmFunctionDefinition) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.VARIABLE ) {
	    dumpModel((CsmVariable) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
	    dumpModel((CsmVariableDefinition) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
	    dumpModel((CsmNamespaceAlias) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.USING_DECLARATION ) {
	    dumpModel((CsmUsingDeclaration) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.USING_DIRECTIVE ) {
	    dumpModel((CsmUsingDirective) decl);
	} else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
	    dumpModel((CsmTypedef) decl);
	} else {
	    String ofStr = getOffsetString(decl, false);
	    print("" + decl.getKind() + ' ' + decl.getName() + ofStr);
	}
    }
    
    
    public void dumpModel(CsmNamespaceAlias alias) {
	CsmNamespace referencedNamespace = alias.getReferencedNamespace();
	String refNsName = (referencedNamespace == null) ? NULL_TEXT : referencedNamespace.getQualifiedName(); // NOI18N
	print("ALIAS " + alias.getAlias() + ' ' + refNsName + ' ' + getOffsetString(alias, false) + // NOI18N
		' ' + getScopeString(alias)); // NOI18N
    }
    
    public void dumpModel(CsmUsingDeclaration ud) {
	CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) ud.getReferencedDeclaration();
	String qname = decl == null ? NULL_TEXT : decl.getQualifiedName(); // NOI18N
	print("USING DECL. " + ud.getName() + ' ' + getOffsetString(ud, false) + "; REF DECL: " + qname + // NOI18N
		' ' + getOffsetString(decl, false)  + ' ' + getScopeString(ud)); // NOI18N
    }
    
    public void dumpModel(CsmTypedef td) {
	print("TYPEDEF " + td.getName() + ' ' + getOffsetString(td, false) + " TYPE: " + toString(td.getType(), false)  + // NOI18N
		' ' + getScopeString(td)); // NOI18N
    }
    
    public void dumpModel(CsmUsingDirective ud) {
	CsmNamespace nsp = ud.getReferencedNamespace();
	print("USING NAMESPACE. " + ud.getName() + ' ' + getOffsetString(ud, false) + // NOI18N
		"; REF NS: " + (nsp == null ? NULL_TEXT : nsp.getQualifiedName())  + ' ' + getScopeString(ud)); // NOI18N
    }
    
    public void dumpModel(CsmClass cls) {
	String kw =
		(cls.getKind() == CsmDeclaration.Kind.CLASS) ? "CLASS" : // NOI18N
		    (cls.getKind() == CsmDeclaration.Kind.STRUCT) ? "STRUCT" : // NOI18N
			(cls.getKind() == CsmDeclaration.Kind.UNION) ? "UNION" : // NOI18N
			    "<unknown-CsmClass-kind>"; // NOI18N
	
	String tmplStr = cls.isTemplate() ? "<>" : ""; // NOI18N
	print(kw + ' ' + cls.getName() + tmplStr + " (" + cls.getQualifiedName() + " )" + // NOI18N
		getOffsetString(cls, false) + " lcurly=" + cls.getLeftBracketOffset() + ' ' + getScopeString(cls)); // NOI18N
	
	indent();
	print("BASE CLASSES:"); // NOI18N
	indent();
	for( Iterator iter = cls.getBaseClasses().iterator(); iter.hasNext(); ) {
	    CsmInheritance inh = (CsmInheritance) iter.next();
	    print(toString(inh));
	}
	unindent();
	print("MEMBERS:"); // NOI18N
	indent();
	List/*<CsmMember>*/ members = cls.getMembers();
	for( Iterator iter = members.iterator(); iter.hasNext(); ) {
	    CsmMember member = (CsmMember) iter.next();
	    if( CsmKindUtilities.isClass(member) ) {
		dumpModel((CsmClass) member);
	    } else if( member.getKind() == CsmDeclaration.Kind.ENUM ) {
		dumpModel((CsmEnum) member);
	    } else if( member.getKind() == CsmDeclaration.Kind.VARIABLE ) {
		dumpModel((CsmField) member);
	    } else if( member.getKind() == CsmDeclaration.Kind.FUNCTION ) {
		dumpModel((CsmFunction) member);
	    } else if ( member.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) { // inline function
		dumpModel((CsmFunctionDefinition) member);
	    } else {
		StringBuilder sb = new StringBuilder(member.getKind().toString());
		sb.append(' ');
		sb.append(member.getVisibility().toString());
		if( member.isStatic() ) {
		    sb.append(" static"); // NOI18N
		}
		sb.append(' ');
		sb.append(member.getName());
		sb.append(getOffsetString(member, false));
		sb.append(' ');
		sb.append(getBriefClassName(member));
		print(sb.toString());
	    }
	}
	unindent();
	List/*<CsmMember>*/ friends = cls.getFriends();
        if (!friends.isEmpty()) {
            print("FRIENDS:"); // NOI18N
            indent();
            for( Iterator iter = friends.iterator(); iter.hasNext(); ) {
                CsmFriend friend = (CsmFriend) iter.next();
                if( friend.getKind() == CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION ) {
                    CsmFriendClass frClass = (CsmFriendClass) friend;
                    StringBuilder sb = new StringBuilder(frClass.getKind().toString());
                    sb.append(' ');
                    sb.append(friend.getName());
                    sb.append(getOffsetString(friend, false));
                    sb.append(' ');
                    sb.append(getBriefClassName(friend));
                    print(sb.toString());
                    indent();
                    CsmClass refClass = frClass.getReferencedClass();
                    print("REFERENCED CLASS: " + refClass == null ? "*UNRESOLVED*" : refClass.getUniqueName());
                    unindent();
                } else if( friend.getKind() == CsmDeclaration.Kind.FUNCTION ) {
                    dumpModel((CsmFunction) friend);
                } else if ( friend.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) { // inline function
                    dumpModel((CsmFunctionDefinition) friend);
                } else {
                    assert false : "unexpected friend object " + friend;
                }
            }
            unindent(); 
        }
	unindent();
    }
    
    public void dumpModel(CsmEnum enumeration) {
	print("ENUM " + enumeration.getName() + getOffsetString(enumeration, false) + ' ' + getScopeString(enumeration)); // NOI18N
	indent();
	for( Iterator iter = enumeration.getEnumerators().iterator(); iter.hasNext(); ) {
	    CsmEnumerator enumerator = (CsmEnumerator) iter.next();
	    StringBuilder sb = new StringBuilder(enumerator.getName());
	    if( enumerator.getExplicitValue() != null ) {
		sb.append(' ');
		sb.append(enumerator.getExplicitValue().getText() + getOffsetString(enumerator, false));
	    }
	    print(sb.toString());
	}
	unindent();
    }
    
    public void dumpModel(CsmNamespaceDefinition nsp) {
	print("NAMESPACE DEFINITOIN " + nsp.getName() + getOffsetString(nsp, false) + ' ' + getScopeString(nsp)); // NOI18N
	indent();
	for( Iterator iter = nsp.getDeclarations().iterator(); iter.hasNext(); ) {
	    dumpModel((CsmDeclaration) iter.next());
	}
	unindent();
    }
    
    private Object modelChangeEventLock = new Object();
    
    public void dumpModelChangeEvent(CsmChangeEvent e) {
	synchronized( modelChangeEventLock ) {
	    print("Model Changed Event:"); // NOI18N
	    dumpFilesCollection(e.getNewFiles(), "New files"); // NOI18N
	    dumpFilesCollection(e.getRemovedFiles(), "Removed files"); // NOI18N
	    dumpFilesCollection(e.getChangedFiles(), "Changed files"); // NOI18N
	    dumpDeclarationsCollection(e.getNewDeclarations(), "New declarations"); // NOI18N
	    dumpDeclarationsCollection(e.getRemovedDeclarations(), "Removed declarations"); // NOI18N
	    dumpDeclarationsCollection(e.getChangedDeclarations().keySet(), "Changed declarations"); // NOI18N
	    dumpNamespacesCollection(e.getNewNamespaces(), "New namespaces"); // NOI18N
	    dumpNamespacesCollection(e.getRemovedNamespaces(), "Removed namespaces"); // NOI18N
	    print("");
	}
    }
    
    public void dumpFilesCollection(Collection/*<CsmFile>*/ files, String title) {
	if( ! files.isEmpty() ) {
	    print(title);
	    indent();
	    dumpFilesCollection(files);
	    unindent();
	}
    }
    
    public void dumpFilesCollection(Collection/*<CsmFile>*/ files) {
	if( ! files.isEmpty() ) {
	    for( Iterator iter = files.iterator(); iter.hasNext(); ) {
		CsmFile file = (CsmFile) iter.next();
		print(file == null ? NULL_TEXT : file.getAbsolutePath()); // NOI18N
	    }
	}
    }
    
    public void dumpDeclarationsCollection(Collection/*<CsmDeclaration>*/ declarations, String title) {
	if( ! declarations.isEmpty() ) {
	    print(title);
	    indent();
	    dumpDeclarationsCollection(declarations);
	    unindent();
	}
    }
    
    public void dumpDeclarationsCollection(Collection/*<CsmDeclaration>*/ declarations) {
	if( ! declarations.isEmpty() ) {
	    for( Iterator iter = declarations.iterator(); iter.hasNext(); ) {
		CsmDeclaration decl = (CsmDeclaration) iter.next();
		print(decl == null ? NULL_TEXT : (decl.getUniqueName() + " of kind: " + decl.getKind())); // NOI18N
	    }
	}
    }
    
    public void dumpNamespacesCollection(Collection/*<CsmNamespace>*/ namespaces, String title) {
	if( ! namespaces.isEmpty() ) {
	    print(title);
	    indent();
	    dumpNamespacesCollection(namespaces);
	    unindent();
	}
    }
    
    public void dumpNamespacesCollection(Collection/*<CsmNamespace>*/ namespaces) {
	if( ! namespaces.isEmpty() ) {
	    for( Iterator iter = namespaces.iterator(); iter.hasNext(); ) {
		CsmNamespace nsp = (CsmNamespace) iter.next();
		print(nsp == null ? NULL_TEXT : nsp.getQualifiedName()); // NOI18N
	    }
	}
    }
}
