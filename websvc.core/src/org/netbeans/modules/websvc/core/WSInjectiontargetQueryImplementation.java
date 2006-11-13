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

package org.netbeans.modules.websvc.core;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek, Milan Kuchtiak
 */
public class WSInjectiontargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of WSInjectiontargetQueryImplementation */
    public WSInjectiontargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to WSInjectionTargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        FileObject fo = controller.getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        if (Util.isJavaEE5orHigher(project) && !(ElementKind.INTERFACE==typeElement.getKind())) {
            
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            boolean found = false;

            for (AnnotationMirror m : annotations) {
                Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                if (qualifiedName.contentEquals("javax.jws.WebService")) { //NOI18N
                    found = true;
                    break;
                }
                if (qualifiedName.contentEquals("javax.jws.WebServiceProvider")) { //NOI18N
                    found = true;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
