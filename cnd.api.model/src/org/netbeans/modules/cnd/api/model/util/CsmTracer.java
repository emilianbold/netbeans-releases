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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model.util;

import java.io.PrintStream;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

/**
 * Misc. static methods used for tracing of code model objects
 * @author vk155633
 */
public class CsmTracer {

    private final int step = 4;
    private StringBuffer indentBuffer = new StringBuffer();
    private boolean deep = true;
    private boolean testUniqueName = false;
    private PrintStream printStream;

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
    
    public static String toString(CsmExpression expr) {
        if( expr == null ) {
            return "null"; // NOI18N
        }
        StringBuffer sb = new StringBuffer();
        sb.append("text='"); // NOI18N
        sb.append(expr.getText());
        sb.append("'"); // NOI18N
        return sb.toString();
    }
    
//    public boolean isDummyUnresolved(CsmClassifier decl) {
//        return decl == null || decl.getClass().getName().endsWith("Unresolved$UnresolvedClass");
//    }
    
    public static String toString(CsmInheritance inh) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("CLASS="); // NOI18N
        CsmClassifier cls = inh.getCsmClassifier();
        //sb.append(isDummyUnresolved(cls) ? "<unresolved>" : cls.getQualifiedName());
        sb.append(cls == null ? "null" : cls.getQualifiedName()); // NOI18N
        
        sb.append(" VISIBILITY==" + inh.getVisibility()); // NOI18N
        sb.append(" virtual==" + inh.isVirtual()); // NOI18N
        
        sb.append(" text='"); // NOI18N
        sb.append(inh.getText());
        sb.append("'"); // NOI18N
        return sb.toString();
    }
    
    public static String toString(CsmCondition condition) {
        if( condition == null ) {
            return "null"; // NOI18N
        }
        StringBuffer sb = new StringBuffer(condition.getKind().toString());
        sb.append(' ');
        if( condition.getKind() == CsmCondition.Kind.EXPRESSION  ) {
            sb.append(toString(condition.getExpression()));
        } else { // condition.getKind() == CsmCondition.Kind.DECLARATION
            CsmVariable var = condition.getDeclaration();
            sb.append(toString(var));
        }
        return sb.toString();
    }
    
    public static String toString(CsmType type) {
        StringBuffer sb = new StringBuffer();
        if( type == null ) {
            sb.append("null"); // NOI18N
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
            //sb.append(isDummyUnresolved(classifier) ? "<unresolved>": classifier.getName());
            sb.append(classifier != null ? classifier.getQualifiedName() : "<*no_classifier*>"); // NOI18N
            for( int i = 0; i < type.getArrayDepth(); i++ ) {
                sb.append("[]"); // NOI18N
            }
            sb.append(" TEXT=" + type.getText()); // NOI18N
        }
        if( type instanceof CsmOffsetable ) {
            sb.append(' ');
            sb.append(getOffsetString((CsmOffsetable) type));
        }
        return sb.toString();
    }
    
    public static String toString(CsmVariable var) {
        if( var == null ) {
            return "null"; // NOI18N
        }
        StringBuffer sb = new StringBuffer(var.getName());
        sb.append(getOffsetString(var));
        sb.append(" type: " + toString(var.getType())); // NOI18N
        sb.append(" init: " + toString(var.getInitialValue())); // NOI18N
        return sb.toString();
    }
    
    public static String toString(CsmFunction fun) {
        if( fun == null ) {
            return "null"; // NOI18N
        } else {
            return fun.getName() + ' ' + getOffsetString(fun);
        }
    }
    
    public void dumpModel(CsmFunction fun) {
        print("FUNCTION " + fun.getName() + getOffsetString(fun) + ' ' + getBriefClassName(fun)); // NOI18N
        if( fun instanceof CsmFunctionDefinition ) {
            if( deep ) {
                //indent();
                dumpStatement(((CsmFunctionDefinition) fun).getBody());
                //unindent();
            }
        }
        indent();
        print("DEFINITION: " + toString(fun.getDefinition())); // NOI18N
        print("SIGNATURE " + fun.getSignature()); // NOI18N
        print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
        dumpParameters(fun.getParameters());
        print("RETURNS " + toString(fun.getReturnType())); // NOI18N
        unindent();
    }
    
    public void dumpModel(CsmFunctionDefinition fun) {
        CsmFunction decl = fun.getDeclaration();
        print("FUNCTION DEFINITION " + fun.getName() + ' ' + getOffsetString(fun) + ' ' + getBriefClassName(fun)); // NOI18N
        indent();
        print("SIGNATURE " + fun.getSignature()); // NOI18N
        print("UNIQUE NAME " + fun.getUniqueName()); // NOI18N
        print("DECLARATION: " + toString(decl)); // NOI18N
        dumpParameters(fun.getParameters());
        print("RETURNS " + toString(fun.getReturnType())); // NOI18N
        if( deep ) {
            dumpStatement((CsmStatement) fun.getBody());
        }
        unindent();
    }
    
    public static String getOffsetString(CsmOffsetable obj) {
        //return " [" + obj.getStartOffset() + '-' + obj.getEndOffset() + ']';
//        CsmOffsetable.Position start = obj.getStartPosition();
//        CsmOffsetable.Position end = obj.getEndPosition();
        return " [" + obj.getStartPosition() + '-' + obj.getEndPosition() + ']'; // NOI18N
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
                print(toString((CsmParameter) iter.next()));
            }
            unindent();
        }
    }
    
    public void dumpStatement(CsmStatement stmt) {
        if( stmt == null ) {
            print("STATEMENT is null"); // NOI18N
            return;
        }
        print("STATEMENT " + stmt.getKind() + ' ' + getOffsetString(stmt)); // NOI18N
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
        print("PARAMETER: " + toString(stmt.getParameter())); // NOI18N
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
        print("ITERATION: " + toString(stmt.getIterationExpression())); // NOI18N
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
        print(" EXPRESSION: " + toString(stmt.getExpression()), false); // NOI18N
    }
    
    public void dumpNamespaceDefinitions(CsmNamespace nsp) {
        print("NAMESPACE DEFINITIONS for " + nsp.getName() + " (" + nsp.getQualifiedName() + ") "); // NOI18N
        indent();
        for( Iterator iter = nsp.getDefinitions().iterator(); iter.hasNext(); ) {
            CsmNamespaceDefinition def = (CsmNamespaceDefinition) iter.next();
            print(def.getContainingFile().getName() + ' ' + getOffsetString(def));
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
        for( Iterator iter = getSortedDeclarations(nsp); iter.hasNext(); ) {
            dumpModel((CsmDeclaration) iter.next());
        }
        for( Iterator iter = getSortedNestedNamespaces(nsp); iter.hasNext(); ) {
            dumpModel((CsmNamespace) iter.next());
        }
        if( ! nsp.isGlobal() ) {
            unindent();
        }
    }
    
    private Iterator getSortedDeclarations(CsmNamespace nsp) {
        SortedMap map = new TreeMap();
        for( Iterator iter = nsp.getDeclarations().iterator(); iter.hasNext(); ) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            map.put(getSortKey(decl), decl);
        }
        return map.values().iterator();
    }
    
    private Iterator getSortedNestedNamespaces(CsmNamespace nsp) {
        SortedMap map = new TreeMap();
        for( Iterator iter = nsp.getNestedNamespaces().iterator(); iter.hasNext(); ) {
            CsmNamespace decl = (CsmNamespace) iter.next();
            map.put(decl.getQualifiedName(), decl);
        }
        return map.values().iterator();
    }    
    
    private static String getSortKey(CsmDeclaration declaration) {
        StringBuffer sb = new StringBuffer();
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
        List includes = file.getIncludes();
        print("Includes:"); // NOI18N
        if (includes.size() > 0) {
            for( Iterator iter = includes.iterator(); iter.hasNext(); ) {
                CsmInclude o = (CsmInclude) iter.next();
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
        print("VARIABLE " + toString(var)); // NOI18N
        CsmVariableDefinition def = var.getDefinition();
        if (def != null){
            indent();
            print("DEFINITION: " + toString(def)); // NOI18N
            unindent();
        }
    }
    
    public void dumpModel(CsmVariableDefinition var) {
        CsmVariable decl = var.getDeclaration();
        print("VARIABLE DEFINITION " + toString(var)); // NOI18N
        indent();
        print("DECLARATION: " + toString(decl)); // NOI18N
        unindent();
    }

    
    public void dumpModel(CsmField field) {
        StringBuffer sb = new StringBuffer("FIELD "); // NOI18N
        sb.append(field.getVisibility().toString());
        if( field.isStatic() ) {
            sb.append(" static "); // NOI18N
        }
        sb.append(" " + toString(field.getType())); // NOI18N
        sb.append(' ');
        sb.append(field.getName());
        sb.append(getOffsetString(field));
        print(sb.toString());
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
            String ofStr = (decl instanceof CsmOffsetable) ? getOffsetString((CsmOffsetable) decl) : "";
            print("" + decl.getKind() + ' ' + decl.getName() + ofStr);
        }
    }
    
    
    public void dumpModel(CsmNamespaceAlias alias) {
        CsmNamespace referencedNamespace = alias.getReferencedNamespace();
        String refNsName = (referencedNamespace == null) ? "null" : referencedNamespace.getQualifiedName(); // NOI18N
        print("ALIAS " + alias.getAlias() + ' ' + refNsName + ' ' + getOffsetString(alias)); // NOI18N
    }
    
    public void dumpModel(CsmUsingDeclaration ud) {
        CsmDeclaration decl = ud.getReferencedDeclaration();
        String qname = decl == null ? "null" : decl.getQualifiedName(); // NOI18N
        print("USING DECL. " + qname + ' ' + getOffsetString(ud)); // NOI18N
    }
    
    public void dumpModel(CsmTypedef td) {
        print("TYPEDEF " + td.getName() + ' ' + getOffsetString(td) + " TYPE: " + toString(td.getType())); // NOI18N
    }
    
    public void dumpModel(CsmUsingDirective ud) {
        CsmNamespace nsp = ud.getReferencedNamespace();
        print("USING DECL. " + (nsp == null ? "null" : nsp.getQualifiedName()) + ' ' + getOffsetString(ud)); // NOI18N
    }
    
    public void dumpModel(CsmClass cls) {
        String kw =
                (cls.getKind() == CsmDeclaration.Kind.CLASS) ? "CLASS" : // NOI18N
                    (cls.getKind() == CsmDeclaration.Kind.STRUCT) ? "STRUCT" : // NOI18N
                        (cls.getKind() == CsmDeclaration.Kind.UNION) ? "UNION" : // NOI18N
                            "<unknown-CsmClass-kind>"; // NOI18N
        
        String tmplStr = cls.isTemplate() ? "<>" : ""; // NOI18N
        print(kw + ' ' + cls.getName() + tmplStr + " (" + cls.getQualifiedName() + " )" + // NOI18N
                getOffsetString(cls) + " lcurly=" + cls.getLeftBracketOffset()); // NOI18N
        
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
                StringBuffer sb = new StringBuffer(member.getKind().toString());
                sb.append(' ');
                sb.append(member.getVisibility().toString());
                if( member.isStatic() ) {
                    sb.append(" static"); // NOI18N
                }
                sb.append(' ');
                sb.append(member.getName());
                sb.append(getOffsetString(member));
                sb.append(' ');
                sb.append(getBriefClassName(member));
                print(sb.toString());
            }
        }
        unindent();
        unindent();
    }
    
    public void dumpModel(CsmEnum enumeration) {
        print("ENUM " + enumeration.getName() + getOffsetString(enumeration)); // NOI18N
        indent();
        for( Iterator iter = enumeration.getEnumerators().iterator(); iter.hasNext(); ) {
            CsmEnumerator enumerator = (CsmEnumerator) iter.next();
            StringBuffer sb = new StringBuffer(enumerator.getName());
            if( enumerator.getExplicitValue() != null ) {
                sb.append(' ');
                sb.append(enumerator.getExplicitValue().getText() + getOffsetString(enumerator));
            }
            print(sb.toString());
        }
        unindent();
    }
    
    public void dumpModel(CsmNamespaceDefinition nsp) {
        print("NAMESPACE DEFINITOIN " + nsp.getName() + getOffsetString(nsp)); // NOI18N
        indent();
        for( Iterator iter = nsp.getDeclarations().iterator(); iter.hasNext(); ) {
            dumpModel((CsmDeclaration) iter.next());
        }
        unindent();
    }
    
    public void dumpModelChangeEvent(CsmChangeEvent e) {
        print("Model Changed Event:"); // NOI18N
        dumpFilesCollection(e.getNewFiles(), "New files"); // NOI18N
        dumpFilesCollection(e.getRemovedFiles(), "Removed files"); // NOI18N
        dumpFilesCollection(e.getChangedFiles(), "Changed files"); // NOI18N
        dumpDeclarationsCollection(e.getNewDeclarations(), "New declarations"); // NOI18N
        dumpDeclarationsCollection(e.getRemovedDeclarations(), "Removed declarations"); // NOI18N
        dumpDeclarationsCollection(e.getChangedDeclarations(), "Changed declarations"); // NOI18N
        dumpNamespacesCollection(e.getNewNamespaces(), "New namespaces"); // NOI18N
        dumpNamespacesCollection(e.getRemovedNamespaces(), "Removed namespaces"); // NOI18N
        print("");
    }
    
    public void dumpFilesCollection(Collection/*<CsmFile>*/ files, String title) {
        print(title);
        indent();
        dumpFilesCollection(files);
        unindent();
    }
    
    public void dumpFilesCollection(Collection/*<CsmFile>*/ files) {
        for( Iterator iter = files.iterator(); iter.hasNext(); ) {
            CsmFile file = (CsmFile) iter.next();
            print(file == null ? "null" : file.getAbsolutePath()); // NOI18N
        }
    }
    
    public void dumpDeclarationsCollection(Collection/*<CsmDeclaration>*/ declarations, String title) {
        print(title);
        indent();
        dumpDeclarationsCollection(declarations);
        unindent();
    }
    
    public void dumpDeclarationsCollection(Collection/*<CsmDeclaration>*/ declarations) {
        for( Iterator iter = declarations.iterator(); iter.hasNext(); ) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            print(decl == null ? "null" : (decl.getUniqueName() + " of kind: " + decl.getKind())); // NOI18N
        }
    }
    
    public void dumpNamespacesCollection(Collection/*<CsmNamespace>*/ namespaces, String title) {
        print(title);
        indent();
        dumpNamespacesCollection(namespaces);
        unindent();
    }
    
    public void dumpNamespacesCollection(Collection/*<CsmNamespace>*/ namespaces) {
        for( Iterator iter = namespaces.iterator(); iter.hasNext(); ) {
            CsmNamespace nsp = (CsmNamespace) iter.next();
            print(nsp == null ? "null" : nsp.getQualifiedName()); // NOI18N
        }
    }
}
