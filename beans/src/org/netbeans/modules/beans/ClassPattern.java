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

package org.netbeans.modules.beans;

import java.awt.Image;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TypeMirrorHandle;

/** Class representing a JavaBeans Property
 * @author Petr Hrebejk
 */
public class ClassPattern extends Pattern {

    private Image icon;
    
    // Special constructorfo root
    public ClassPattern( PatternAnalyser patternAnalyser ) {
        super( patternAnalyser, Pattern.Kind.CLASS, "root", null );
        this.icon = PATTERNS; // NOI18N
    }
    
    public ClassPattern( PatternAnalyser patternAnalyser,                         
                         TypeMirror type,
                         String name ) {

        super( patternAnalyser, Pattern.Kind.CLASS, name, TypeMirrorHandle.create(type) );
        this.icon = ((DeclaredType)type).asElement().getKind() == ElementKind.INTERFACE ? INTERFACE : CLASS;
    }
    
    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public String getHtmlDisplayName() {
        return null;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private Object Utilities;
    
}
