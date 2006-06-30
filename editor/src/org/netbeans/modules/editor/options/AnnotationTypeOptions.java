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
    
    public boolean isUseHighlightColor() {
        return delegate.isUseHighlightColor();
    }

    public void setUseHighlightColor(boolean use) {
        delegate.setUseHighlightColor(use);
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

    public Color getWaveUnderlineColor() {
        return delegate.getWaveUnderlineColor();
    }

    public void setWaveUnderlineColor(Color col) {
        delegate.setWaveUnderlineColor(col);
    }

    public boolean isUseWaveUnderlineColor() {
        return delegate.isUseWaveUnderlineColor();
    }

    public void setUseWaveUnderlineColor(boolean use) {
        delegate.setUseWaveUnderlineColor(use);
    }

}
