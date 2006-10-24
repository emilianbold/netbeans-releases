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
package org.netbeans.modules.editor.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public class ParseErrorAnnotation extends Annotation implements PropertyChangeListener {

    private ErrorDescription desc;

    /** Creates a new instance of ParseErrorAnnotation */
    public ParseErrorAnnotation(ErrorDescription desc) {
        this.desc = desc;
        LazyFixList fixes = desc.getFixes();
        
        if (fixes.probablyContainsFixes() && !fixes.isComputed())
            fixes.addPropertyChangeListener(WeakListeners.propertyChange(this, fixes));
    }

    public String getAnnotationType() {
        LazyFixList fixes = desc.getFixes();
        boolean hasFixes = fixes.isComputed() && !fixes.getFixes().isEmpty();
        
        switch (desc.getSeverity()) {
            case ERROR:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_err_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_err";
                
            case WARNING:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_warn_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_warn";
            case VERIFIER:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_verifier_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_verifier";
            case HINT:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_hint_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_hint";
            case TODO:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_todo_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_todo";
            default:
                throw new IllegalArgumentException(String.valueOf(desc.getSeverity()));
        }
    }

    public String getShortDescription() {
        return desc.getDescription();
    }

    ErrorDescription getDescription() {
        return desc;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (desc.getFixes().isComputed()) {
            Annotatable ann = getAttachedAnnotatable();
            
            detach();
            attach(ann);
        }
    }
    
}
