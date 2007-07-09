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
package org.netbeans.modules.websvc.editor.hints.fixes;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit
 */
public class SetAnnotationArgument extends AddAnnotationArgument {

    /** Creates a new instance of SetAnnotationArgument */
    public SetAnnotationArgument(FileObject fileObject, Element element,
            AnnotationMirror annMirror, String argumentName, Object argumentValue) {
        super(fileObject, element, annMirror, argumentName, argumentValue);
    }

    @Override
    public String getText(){
        return NbBundle.getMessage(RemoveAnnotation.class, 
                "LBL_AddAnnotationAttribute_SetToValue",argumentName,argumentValue);
    }

}
