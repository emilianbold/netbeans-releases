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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements 
 * @author Vladimir Kvashin
 */
public class ClassImplSpecialization extends ClassImpl implements CsmTemplate {
    
    private String qualifiedNameSuffix = "";
    
    private ClassImplSpecialization(AST ast, CsmFile file) { 
	super(ast, file, null);
    }
    
    protected void init(NamespaceImpl namespace, CsmClass containingClass, AST ast) {
	AST qIdToken = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
	assert qIdToken != null;
	qualifiedNameSuffix = TemplateUtils.getSpecializationSuffix(qIdToken);
	super.init(namespace, containingClass, ast);
	// super.register(); // super.init() has already registered me
    }
    
    public static ClassImplSpecialization create(AST ast, NamespaceImpl namespace, CsmFile file) {
	ClassImplSpecialization impl = new ClassImplSpecialization(ast, file);
	impl.init(namespace, null, ast);
	return impl;
    }
    
    public boolean isTemplate() {
	return true;
    }
    
    
    public boolean isSpecialization() {
	return true;
    }

//    public String getTemplateSignature() {
//	return qualifiedNameSuffix;
//    }

    public List<CsmTemplateParameter> getTemplateParameters() {
	return Collections.EMPTY_LIST;
    }

  
// This does not work since the method is called from base class' constructor    
//    protected String getQualifiedNamePostfix() {
//	String qName = super.getQualifiedNamePostfix();
//	if( isSpecialization() ) {
//	    qName += qualifiedNameSuffix;
//	}
//	return qName;
//    }

    protected String getQualifiedNamePostfix() {
	return super.getQualifiedNamePostfix() + qualifiedNameSuffix;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
	output.writeUTF(qualifiedNameSuffix);
    }
    
    public ClassImplSpecialization(DataInput input) throws IOException {
	super(input);
	qualifiedNameSuffix = input.readUTF();
    }
    
    public String getDisplayName() {
	return getName() + qualifiedNameSuffix;
    }
    
}
