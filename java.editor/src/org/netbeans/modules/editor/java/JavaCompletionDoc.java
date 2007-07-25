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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.net.URL;
import javax.lang.model.element.Element;
import javax.swing.Action;
import com.sun.javadoc.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.spi.editor.completion.CompletionDocumentation;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionDoc implements CompletionDocumentation {

    private ElementJavadoc elementJavadoc;
    
    public JavaCompletionDoc(ElementJavadoc elementJavadoc) {
        this.elementJavadoc = elementJavadoc;
    }

    public JavaCompletionDoc resolveLink(String link) {
        ElementJavadoc doc = elementJavadoc.resolveLink(link);
        return doc != null ? new JavaCompletionDoc(doc) : null;
    }

    public URL getURL() {
        return elementJavadoc.getURL();
    }

    public String getText() {
        return elementJavadoc.getText();
    }

    public Action getGotoSourceAction() {
        return elementJavadoc.getGotoSourceAction();
    }

    public static final JavaCompletionDoc create(CompilationController controller, Element element) {
        return new JavaCompletionDoc( ElementJavadoc.create(controller, element) );
    }
    
}
