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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.type;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.gotodeclaration.util.ContextUtil;
import org.netbeans.modules.cnd.gotodeclaration.util.EmptyIcon;

import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * 
 * Implementation of type descriptor for "Jump to Type" for C/C++
 * @author Vladimir Kvashin
 */
/* package-local */
class CppTypeDescriptor extends TypeDescriptor {

    CsmUID<CsmClassifier> uid;

    private String simpleName;
    private String typeName;
    private CsmDeclaration.Kind kind;
    private int modifiers;
    private boolean isArtificial = true;
    private String projectName = "";
    private String contextName;

    private static Icon projectIcon = new ImageIcon(Utilities.loadImage(CsmImageLoader.PROJECT));
    private static Icon emptyIcon = new EmptyIcon(16);
    
    private static String contextNameFormat = ' ' + NbBundle.getMessage(CppTypeDescriptor.class, "CONTEXT_NAME_FORMAT");
    private static String typeNameFormat = ' ' + NbBundle.getMessage(CppTypeDescriptor.class, "TYPE_NAME_FORMAT");
    
    @SuppressWarnings("unchecked")
    public CppTypeDescriptor(CsmClassifier classifier) {
	uid = classifier.getUID();
	kind = classifier.getKind();
	modifiers = CsmUtilities.getModifiers(classifier);
	if( CsmKindUtilities.isOffsetable(classifier) ) {
            CsmFile file = ((CsmOffsetable) classifier).getContainingFile();
	    if( file != null ) {
                CsmProject project = file.getProject();
		projectName = project.getName();
		isArtificial = project.isArtificial();
	    }
	}
	simpleName = classifier.getName();
	typeName = simpleName;
	
	contextName = ContextUtil.getContextName(classifier);
	CsmScope scope = classifier.getScope();
	if( CsmKindUtilities.isClass(scope) ) {
	    CsmClass cls = ((CsmClass) scope);
	    typeName = String.format(typeNameFormat, simpleName, ContextUtil.getClassFullName(cls));
	}	
	if( contextName != null && contextName.length() > 0 ) {
            contextName = String.format(contextNameFormat, contextName);
	}
    }
    
    private final CsmClassifier getClassifier() {
	return uid.getObject();
    }
    
    public String getSimpleName() {
	return simpleName;
    }

    public String getOuterName() {
	return " $outerName$ ";
    }

    public String getTypeName() {
        return typeName;
    }

    public String getContextName() {
        return contextName;
    }

    public Icon getIcon() {
        return CsmImageLoader.getIcon(kind, modifiers);
    }

    public String getProjectName() {
        return projectName;
    }

    public Icon getProjectIcon() {
	return isArtificial ? emptyIcon : projectIcon;
    }

    public FileObject getFileObject() {
	CsmClassifier cls = uid.getObject();
	if( CsmKindUtilities.isOffsetable(cls) ) {
	    CsmFile file = ((CsmOffsetable) cls).getContainingFile();
	    return CsmUtilities.getFileObject(file);
	}
	return null;
    }

    public int getOffset() {
        return 0;
    }

    public void open() {
        CsmUtilities.openSource(uid.getObject());
    }

}
