/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import org.netbeans.editor.AnnotationType;
import java.awt.Color;

/** Customizable bean which delegates to AnnotationType
 *
 * @author David Konecny
 * @since 07/2001
 */
public class AnnotationTypeOptions {

    private AnnotationType delegate;
        
    public AnnotationTypeOptions(AnnotationType delegate) {
        this.delegate = delegate;
    }
    
    public boolean isVisible() {
        return delegate.isVisible();
    }
    
    public boolean isWholeLine() {
        return delegate.isWholeLine();
    }
    
    public Color getHighlightColor() {
        return delegate.getHighlight();
    }

    public void setHighlightColor(Color col) {
        delegate.setHighlight(col);
    }
    
    public Color getForegroundColor() {
        return delegate.getForegroundColor();
    }

    public void setForegroundColor(Color col) {
        delegate.setForegroundColor(col);
    }

    public boolean isInheritForegroundColor() {
        return delegate.isInheritForegroundColor();
    }

    public void setInheritForegroundColor(boolean inherit) {
        delegate.setInheritForegroundColor(inherit);
    }
    
    public boolean isUseHighlightColor() {
        return delegate.isUseHighlightColor();
    }

    public void setUseHighlightColor(boolean use) {
        delegate.setUseHighlightColor(use);
    }
    
}
