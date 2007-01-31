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

package dwarfvsmodel;

import java.io.PrintStream;
import java.util.Formatter;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.*;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfDeclaration;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;

/**
 * Prints trace info
 * @author vk155633
 */
public class Tracer {
    
    private final int step = 4;
    private StringBuffer indentBuffer = new StringBuffer();
    private PrintStream stream;
    
    public Tracer(PrintStream stream) {
	this.stream = stream;
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
    
    public void println(String s) {
	print(s);
    }
    
    public void print(String s) {
        stream.println(indentBuffer.toString() + s);
    }
    
    public void printf(String format, Object ... args) {
//	StringBuilder sb = new StringBuilder(indentBuffer.toString());
//	Formatter formatter = new Formatter(sb);
//	formatter.format(format, args);
	stream.printf(indentBuffer.toString() + format, args);
    }
    
//    private interface Node {
//	Iterable<Object> getLeaves();
//	Iterable<Iterable<Object>> getBranches();
//    }

    public void traceModel(Node<CsmDeclaration> node, String text) {
	print(text);
	traceModel(node);
    }

    public void traceModel(Node<CsmDeclaration> node) {
	print("NODE"); // NOI18N
	indent();
	for (CsmDeclaration decl : node.getDeclarations()) {
	    trace(decl);
	}
	for (Node<CsmDeclaration> subnode : node.getSubnodes()) {
	    traceModel(subnode);
	}
	unindent();
    }
    
    private void trace(CsmDeclaration decl) {
	//print(decl.getKind().toString() + ' ' + decl.getName() + ' ' + CsmTracer.getOffsetString((CsmOffsetable)decl)); // NOI18N
	print(toString(decl));
    }
    
    public static String toString(CsmDeclaration decl) {
	StringBuilder sb = new StringBuilder(decl.getQualifiedName());
	sb.append("    "); // NOI18N
	if( CsmKindUtilities.isFunction(decl) && ! CsmKindUtilities.isConstructor(decl) && ! CsmKindUtilities.isDestructor(decl) ) {
	    CsmFunction fun = (CsmFunction) decl;
	    sb.append(ComparisonUtils.getSignature(fun));
	    sb.append(" -> "); // NOI18N
	    sb.append(ComparisonUtils.getText(fun.getReturnType()));
	}
	else {
	    sb.append(decl.getName());
	}
	sb.append("  "); // NOI18N

	if( decl instanceof  CsmOffsetable ) {
	    sb.append(CsmTracer.getOffsetString((CsmOffsetable) decl));
	}
	if( CsmKindUtilities.isVariable(decl) ) {
	    sb.append("  "); // NOI18N
	    sb.append(ComparisonUtils.getText(((CsmVariable) decl).getType()));
	}
	return sb.toString();
    }    
    
    public void traceDwarf(Node<DwarfEntry> node, String text) {
	print(text);
	traceDwarf(node);
    }    
    
    public void traceDwarf(Node<DwarfEntry> node) {
	print("NODE"); // NOI18N
	indent();
	for (DwarfEntry decl : node.getDeclarations()) {
	    traceDwarf(decl);
	}
	for (Node<DwarfEntry> subnode : node.getSubnodes()) {
	    traceDwarf(subnode);
	}
	unindent();
    }
    
    public void traceDwarf(DwarfEntry entry) {
	DwarfDeclaration decl = entry.getDeclaration();
	print(decl == null ? "null" : decl.toString()); // NOI18N
    }

    public void traceRecursive(DwarfEntry entry) {
	String name = entry.getName();
	printf("\n---------- Tracing entry %s ----------\n", name); // NOI18N
	_traceRecursive(entry);
	printf("---------- End trace for %s ----------\n\n", name); // NOI18N
    }
    
    private void _traceRecursive(DwarfEntry entry) {
	traceDwarf(entry);
	indent();
	for( DwarfEntry child : entry.getChildren() ) {
	    _traceRecursive(child);
	}
	unindent();
    }
    
}
