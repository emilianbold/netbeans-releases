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
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Annotation;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public class ParseErrorAnnotation extends Annotation implements PropertyChangeListener {

    private final Severity severity;
    private final LazyFixList fixes;
    private final String description;
    private final Position lineStart;
    private final AnnotationHolder holder;
    
    /** Creates a new instance of ParseErrorAnnotation */
    public ParseErrorAnnotation(Severity severity, LazyFixList fixes, String description, Position lineStart, AnnotationHolder holder) {
        this.severity = severity;
        this.fixes = fixes;
        this.description = description;
        this.lineStart = lineStart;
        this.holder = holder;
        
        if (fixes.probablyContainsFixes() && !fixes.isComputed()) {
            fixes.addPropertyChangeListener(WeakListeners.propertyChange(this, fixes));
        }
    }

    public String getAnnotationType() {
        boolean hasFixes = fixes.isComputed() && !fixes.getFixes().isEmpty();
        
        switch (severity) {
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
                throw new IllegalArgumentException(String.valueOf(severity));
        }
    }

    public String getShortDescription() {
        return description;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (fixes.isComputed()) {
            try {
                holder.detachAnnotation(this);
                holder.attachAnnotation(lineStart, this);
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
    public LazyFixList getFixes() {
        return fixes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLineNumber() {
        return holder.lineNumber(lineStart);
    }
    
    Severity getSeverity() {
        return severity;
    }
}
