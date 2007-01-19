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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * The factory for editor default highlighting layers.
 * 
 * @author Vita Stejskal
 */
public class Factory implements HighlightsLayerFactory {

    /** A unique identifier of the block search layer type. */
    public static final String BLOCK_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/BLOCK_SEARCH"; //NOI18N
    
    /** A unique identifier of the incremental search layer type. */
    public static final String INC_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/INC_SEARCH"; //NOI18N
    
    /** Creates a new instance of Factory */
    public Factory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        layers.add(HighlightsLayer.create(
            CaretRowHighlighting.LAYER_TYPE_ID,
            ZOrder.CARET_RACK,
            true,
            new CaretRowHighlighting(context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            TextSelectionHighlighting.LAYER_TYPE_ID,
            ZOrder.SHOW_OFF_RACK.aboveLayers(CaretRowHighlighting.LAYER_TYPE_ID), 
            true, 
            new TextSelectionHighlighting(context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            BLOCK_SEARCH_LAYER, 
            ZOrder.SHOW_OFF_RACK.aboveLayers(CaretRowHighlighting.LAYER_TYPE_ID),
            true,
            new BlockHighlighting(BLOCK_SEARCH_LAYER, context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            TextSearchHighlighting.LAYER_TYPE_ID,
            ZOrder.SHOW_OFF_RACK.aboveLayers(BLOCK_SEARCH_LAYER),
            true,
            new TextSearchHighlighting(context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            INC_SEARCH_LAYER, 
            ZOrder.SHOW_OFF_RACK.aboveLayers(TextSearchHighlighting.LAYER_TYPE_ID).belowLayers(TextSelectionHighlighting.LAYER_TYPE_ID),
            true,
            new BlockHighlighting(INC_SEARCH_LAYER, context.getComponent()))
        );

        // If there is a lexer for the document create lexer-based syntax highlighting
        if (TokenHierarchy.get(context.getDocument()) != null) {
            layers.add(HighlightsLayer.create(
                SyntaxHighlighting.LAYER_TYPE_ID,
                ZOrder.SYNTAX_RACK,
                true,
                new SyntaxHighlighting(context.getDocument()))
            );
        }
        
        return layers.toArray(new HighlightsLayer [layers.size()]);
    }
    
}
