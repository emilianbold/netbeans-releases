/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.java.editor.overridden;

import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.Icon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ElementDescription {
    
    private ClasspathInfo originalCPInfo;
    
    private ElementHandle<Element> handle;
    private ElementHandle<TypeElement> outtermostElement;
    private Collection<Modifier> modifiers;
    private String displayName;
    
    public ElementDescription(CompilationInfo info, Element element) {
        this.originalCPInfo = info.getClasspathInfo();
        this.handle = ElementHandle.create(element);
        this.outtermostElement = ElementHandle.create(SourceUtils.getOutermostEnclosingTypeElement(element));
        this.modifiers = element.getModifiers();
        this.displayName = element.accept(new ElementNameVisitor(), true);
    }

    public FileObject getSourceFile() {
        FileObject file = SourceUtils.getFile(outtermostElement, originalCPInfo);
        if (file != null)
            return SourceUtils.getFile(outtermostElement, ClasspathInfo.create(file));
        else
            return null;
    }

    public ElementHandle<Element> getHandle() {
        return handle;
    }

    public Icon getIcon() {
        return ElementIcons.getElementIcon(handle.getKind(), modifiers);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Collection<Modifier> getModifiers() {
        return modifiers;
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor6<String,Boolean> {
        
	@Override
        public String visitPackage(PackageElement e, Boolean p) {
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }

	@Override
        public String visitType(TypeElement e, Boolean p) {
            if (   e.getQualifiedName() == null
                || e.getQualifiedName().length() == 0
                || e.getSimpleName() == null
                || e.getSimpleName().length() == 0) {
                return NbBundle.getMessage(ElementDescription.class, "NAME_AnonynmousInner") + e.getEnclosingElement().accept(this, true);
            }
            
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }
        
        @Override
        public String  visitExecutable(ExecutableElement e, Boolean p) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(e.getEnclosingElement().accept(this, p));
            sb.append("."); //NOI18N
            sb.append(e.getSimpleName());
            sb.append("("); //NOI18N
            
            boolean addComma = false;
            
            for (VariableElement ve : e.getParameters()) {
                if (addComma)
                    sb.append(", "); //NOI18N
                
                addComma = true;
                
                sb.append(ve.accept(this, p));
            }
            
            sb.append(")"); //NOI18N
            
            return sb.toString();
        }
        
        @Override
        public String visitVariable(VariableElement ve, Boolean p) {
            return Utilities.getTypeName(ve.asType(), false) + " " + ve.getSimpleName(); //NOI18N
        }
        
        @Override
        public String visitTypeParameter(TypeParameterElement tpe, Boolean p) {
            return tpe.getSimpleName().toString();
        }
    }

}
