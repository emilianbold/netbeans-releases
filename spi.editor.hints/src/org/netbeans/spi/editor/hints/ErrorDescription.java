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
package org.netbeans.spi.editor.hints;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author Jan Lahoda
 */
public final class ErrorDescription {

    private String        description;
    private Severity      severity;
    private LazyFixList   fixes;
    private PositionBounds span;
    private FileObject     file;
    
    ErrorDescription(FileObject file, String description, Severity severity, LazyFixList fixes, PositionBounds span) {
        this.description = description;
        this.severity    = severity;
        this.fixes       = fixes;
        this.span        = span;
        this.file        = file;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public LazyFixList getFixes() {
        return fixes;
    }
    
    public PositionBounds getRange() {
        return span;
    }
    
    public FileObject getFile() {
        return file;
    }
    
    public String toString() {
        try {
            return span.getBegin().getLine() + ":" + span.getBegin().getColumn() + "-" + span.getEnd().getLine() + ":" + span.getEnd().getColumn() + ":" + severity.getDisplayName() + ":" + description;
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException().initCause(ex);
        }
    }
    
}
