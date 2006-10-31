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

package org.netbeans.modules.j2ee.common.source;

import java.io.IOException;
import java.util.Iterator;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;
import org.openide.util.NotImplementedException;

/**
 *
 * @author Andrei Badea
 */
public class SourceUtils {

    private final CompilationController controller;
    protected final TypeElement mainTypeElement;

    public SourceUtils(CompilationController controller) throws IOException, IllegalStateException {
        this.controller = controller;
        mainTypeElement = findMainTypeElement();
        if (mainTypeElement == null) {
            throw new IllegalStateException("Cannot find the main type element"); // NOI18N
        }
    }

    public boolean hasMainMethod() {
        throw new NotImplementedException("Not implemented yet");
    }

    private TypeElement findMainTypeElement() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        FileObject fo = controller.getFileObject();
        final String mainElementName = fo.getName();

        // XXX maybe
        // return controller.getElements().getTypeElement(mainElementName);
        Iterator<? extends TypeElement> globalTypes = controller.getElementUtilities().getGlobalTypes(new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {
                return mainElementName.equals(e.getSimpleName().toString());
            }
        }).iterator();
        if (!globalTypes.hasNext()) {
            throw new IllegalStateException("Could not find the main type element in " + fo); // NOI18N
        }
        return globalTypes.next();
    }
}
