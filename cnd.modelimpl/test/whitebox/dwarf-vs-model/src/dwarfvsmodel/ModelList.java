/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package dwarfvsmodel;

import java.io.PrintStream;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.api.model.util.*;

/**
 * A list of high-level model declarations - variables and functions.
 * @author Vladimir Kvashin
 */
public class ModelList {
    
    private CsmFile file;
    private Map<String, List<CsmDeclaration>> map = new HashMap<String, List<CsmDeclaration>>();
    private List<CsmDeclaration> list = new ArrayList<CsmDeclaration>();
    private List<CsmDeclaration> STUB = Collections.emptyList();
    
    public ModelList(CsmFile file) {
	this.file = file;
	add(file.getDeclarations());
    }
    
    public ModelList(CsmClass cls) {
	this.file = cls.getContainingFile();
	add(cls.getMembers());
    }

    private void add(Collection currDeclarations) {
	for(CsmDeclaration decl : (Collection<CsmDeclaration>)currDeclarations ) {
	    CsmDeclaration.Kind kind = decl.getKind();
	    if( kind == CsmDeclaration.Kind.CLASS ||  kind == CsmDeclaration.Kind.UNION ||  kind == CsmDeclaration.Kind.STRUCT) {
		add((CsmClass) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.ENUM ) {
		add((CsmEnum) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.ENUMERATOR ) {
		add((CsmEnumerator) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.VARIABLE || kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
		add((CsmVariable) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.FUNCTION || kind ==  CsmDeclaration.Kind.FUNCTION_DEFINITION) {
		add((CsmFunction) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.TEMPLATE_SPECIALIZATION ) {
		// TODO: implement as soon as template specializations are implemented
	    }
	    else if( kind == CsmDeclaration.Kind.TYPEDEF ) {
		add((CsmTypedef) decl);
	    }
	    else if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
		add((CsmNamespaceDefinition) decl);
	    }
	    // The following does not make sense here:
	    // ASM
	    // TEMPLATE_DECLARATION
	    // NAMESPACE_ALIAS
	    // USING_DIRECTIVE
	    // USING_DECLARATION
	    // CLASS_FORWARD_DECLARATION
	}
    }
    
    public Iterable<CsmDeclaration> getDeclarations(String qualifiedName) {
	List<CsmDeclaration> result = map.get(qualifiedName);
	if( result == null && ! qualifiedName.startsWith("::") ) { // NOI18N
	    result = map.get("::" + qualifiedName); // NOI18N
	}
	return (result == null) ? STUB : result;
    }
    
    public Iterable<CsmDeclaration> getDeclarations() {
	return list;
    }
    
    /** the same as getDeclarations, but creates a list */
    private List<CsmDeclaration> getList(String qualifiedName) {
	qualifiedName = qualifiedName.replaceAll(" ", ""); // NOI18N
	List<CsmDeclaration> result = map.get(qualifiedName);
	if( result == null ) {
	    result = new ArrayList<CsmDeclaration>();
	    map.put(qualifiedName, result);
	}
	return result;
    }

    private void add(CsmClass cls) {
	add(cls.getMembers());
    }
    
    private void add(CsmEnum enm) {
	add(enm.getEnumerators());
    }
    
    private void add(CsmNamespaceDefinition ns) {
	add(ns.getDeclarations());
    }
    
    private void add(CsmEnumerator enumerator) {
	put(enumerator);
    }
    
    private void add(CsmVariable var) {
	put(var);
    }

    private void add(CsmFunction funct) {
	put(funct);
    }

    private void add(CsmTypedef td) {
	put(td);
    }

    private void put(CsmFunction funct) {
	List<CsmDeclaration> overloads = getList(funct.getQualifiedName().toString());
	if( overloads.size() > 0 ) {
	    if( CsmKindUtilities.isFunctionDefinition(funct) ) {
		// if there is a declaration of this function in the list,
		// replace it with this definition
		CsmFunction decl = ((CsmFunctionDefinition) funct).getDeclaration();
		if( decl != funct && Collections.replaceAll(overloads, decl, funct) ) {
		    Collections.replaceAll(this.list, decl, funct);
		}
		else {
		    overloads.add(funct);
		    this.list.add(funct);
		}
	    }
	    else { // this is just a declaration
		CsmFunctionDefinition def = funct.getDefinition();
		if( ! overloads.contains(def) ) {
		    overloads.add(funct);
		    this.list.add(funct);
		}
	    }
	}
	else {
	    overloads.add(funct);
	    this.list.add(funct);
	}
    }
    
    private void put(CsmDeclaration decl) {
	String qName = ComparisonUtils.getQualifiedName(decl);
	List<CsmDeclaration> overloads = getList(qName);
	overloads.add(decl);
	this.list.add(decl);
    }
    
    public void dump(PrintStream ps, boolean bodies) {
	ps.println("==== Model comparison list for " + file.getName() + "  " + file.getAbsolutePath()); // NOI18N
	List<CsmDeclaration> sorted = new ArrayList<CsmDeclaration>(list);
	Collections.sort(sorted, new ComparisonUtils.CsmDeclarationComparator());
	for( CsmDeclaration decl : sorted ) {
	    dump(ps, decl, bodies);
	} 
	ps.println("\n"); // NOI18N
    }

    private void dump(PrintStream ps, CsmDeclaration decl, boolean bodies) {
	ps.println(Tracer.toString(decl));
	if( bodies && CsmKindUtilities.isFunctionDefinition(decl) ) {
	    Node<CsmDeclaration> node = ModelTree.createModelNode((CsmFunctionDefinition) decl);
	    Tracer tr = new Tracer(ps);
	    tr.indent();
	    tr.traceModel(node);
	}
    }
    

    
}
