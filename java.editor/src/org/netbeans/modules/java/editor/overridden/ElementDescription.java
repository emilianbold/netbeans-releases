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
package org.netbeans.modules.java.editor.overridden;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.Icon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class ElementDescription {
    
    private FileObject sourceFile;
    private ElementHandle<Element> handle;
    private Icon icon;
    private String displayName;
    
    /** Creates a new instance of ElementDescription */
    public ElementDescription(CompilationInfo info, Element element) {
        FileObject file = SourceUtils.getFile(element, info.getClasspathInfo());
        if (file != null)
            this.sourceFile = SourceUtils.getFile(element, ClasspathInfo.create(file));
        this.handle = ElementHandle.create(element);
        this.icon = UiUtils.getDeclarationIcon(element);
        this.displayName = element.accept(new ElementNameVisitor(), true);
    }

    public FileObject getSourceFile() {
        return sourceFile;
    }

    public ElementHandle<Element> getHandle() {
        return handle;
    }

    public Icon getIcon() {
        return icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor6<String,Boolean> {
        
	@Override
        public String visitPackage(PackageElement e, Boolean p) {
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }

	@Override
        public String visitType(TypeElement e, Boolean p) {
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }
        
        @Override
        public String  visitExecutable(ExecutableElement e, Boolean p) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(e.getEnclosingElement().accept(this, p));
            sb.append(".");
            sb.append(e.getSimpleName());
            sb.append("(");
            
            boolean addComma = false;
            
            for (VariableElement ve : e.getParameters()) {
                if (addComma)
                    sb.append(", ");
                
                addComma = true;
                
                sb.append(ve.accept(this, p));
            }
            
            sb.append(")");
            
            return sb.toString();
        }
        
        @Override
        public String visitVariable(VariableElement ve, Boolean p) {
            return Utilities.getTypeName(ve.asType(), false) + " " + ve.getSimpleName();
        }
        
        @Override
        public String visitTypeParameter(TypeParameterElement tpe, Boolean p) {
            return tpe.getSimpleName().toString();
        }
    }

}
