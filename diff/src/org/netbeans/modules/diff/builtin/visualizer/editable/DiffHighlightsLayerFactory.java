/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * Provides highliting in Diff panels.
 * 
 * @author Maros Sandor
 */
public class DiffHighlightsLayerFactory implements HighlightsLayerFactory {

    static final String HIGHLITING_LAYER_ID = "org.netbeans.modules.diff.builtin.visualizer.editable.DiffContentPanel"; // NOI18N
    
    public HighlightsLayer[] createLayers(Context context) {
        DiffContentPanel master = (DiffContentPanel) context.getComponent().getClientProperty(HIGHLITING_LAYER_ID);
        if (master == null) return null;
        
        HighlightsLayer [] layers = new HighlightsLayer[1];
        layers[0] = HighlightsLayer.create(HIGHLITING_LAYER_ID, ZOrder.TOP_RACK, true, master.getHighlightsContainer());
        return layers;
    }
}
