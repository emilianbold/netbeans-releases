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

package org.netbeans.modules.debugger.jpda.projects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerAnnotation extends Annotation {

    private Line        line;
    private String      type;


    DebuggerAnnotation (String type, Line line) {
        this.type = type;
        this.line = line;
        attach (line);
    }
    
    DebuggerAnnotation (String type, Line.Part linePart) {
        this.type = type;
        this.line = linePart.getLine();
        attach (linePart);
    }
    
    DebuggerAnnotation (String type, Highlight highlight, FileObject fo) {
        this.type = type;
        attach (new HighlightAnnotatable(highlight, fo));
    }
    
    public String getAnnotationType () {
        return type;
    }
    
    Line getLine () {
        return line;
    }
    
    public String getShortDescription () {
        if (type.endsWith("_broken")) {
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_BREAKPOINT_BROKEN"); // NOI18N
        }
        if (type == EditorContext.BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_BREAKPOINT"); // NOI18N
        else 
        if (type == EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
        else 
        if (type == EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CONDITIONAL_BREAKPOINT"); // NOI18N
        else
        if (type == EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT"); // NOI18N
        else
        if (type == EditorContext.CURRENT_LINE_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        else
        if (type == EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CALLSITE"); // NOI18N
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown breakpoint type '"+type+"'."));
        return null;
    }
    
    private static final class HighlightAnnotatable extends Annotatable {
        
        private static Map highlightsByFiles = new HashMap();
        
        private Highlight highlight;
        private FileObject fo;
        
        public HighlightAnnotatable(Highlight highlight, FileObject fo) {
            this.highlight = highlight;
            this.fo = fo;
        }
        
        public String getText() {
            return null;
        }

        protected void addAnnotation(Annotation anno) {
            Collection highlights;
            synchronized (highlightsByFiles) {
                highlights = (Collection) highlightsByFiles.get(fo);
                if (highlights == null) {
                    highlights = new HashSet();
                    highlightsByFiles.put(fo, highlights);
                }
                highlights.add(highlight);
            }
            Highlighter.getDefault().setHighlights(fo, getClass().getName(), highlights);
        }

        protected void removeAnnotation(Annotation anno) {
            Collection highlights;
            synchronized (highlightsByFiles) {
                highlights = (Collection) highlightsByFiles.get(fo);
                if (highlights == null) {
                    highlights = Collections.EMPTY_SET;
                } else {
                    highlights.remove(highlight);
                    if (highlights.isEmpty()) {
                        highlightsByFiles.remove(fo);
                    }
                }
            }
            Highlighter.getDefault().setHighlights(fo, getClass().getName(), highlights);
        }
        

    }
    
}
