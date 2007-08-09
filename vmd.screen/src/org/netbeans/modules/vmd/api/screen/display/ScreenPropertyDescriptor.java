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
 *
 */

package org.netbeans.modules.vmd.api.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class ScreenPropertyDescriptor {

    private DesignComponent relatedComponent;
    private JComponent relatedView;
    private Shape selectionShape;
    private ScreenPropertyEditor editor;

    public ScreenPropertyDescriptor (DesignComponent relatedComponent, JComponent relatedView, ScreenPropertyEditor editor) {
        this (relatedComponent, relatedView, new Rectangle (relatedView.getSize ()), editor);
    }

    public ScreenPropertyDescriptor (DesignComponent relatedComponent, JComponent relatedView, Shape selectionShape, ScreenPropertyEditor editor) {
        assert relatedComponent != null  &&  relatedView != null  &&  selectionShape != null  &&  editor != null;
        this.relatedComponent = relatedComponent;
        this.relatedView = relatedView;
        this.selectionShape = selectionShape;
        this.editor = editor;
    }

    public DesignComponent getRelatedComponent () {
        return relatedComponent;
    }

    public JComponent getRelatedView () {
        return relatedView;
    }

    public Shape getSelectionShape () {
        return selectionShape;
    }

    public ScreenPropertyEditor getEditor () {
        return editor;
    }

}
