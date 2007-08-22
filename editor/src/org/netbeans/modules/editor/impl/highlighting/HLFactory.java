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

package org.netbeans.modules.editor.impl.highlighting;

import java.util.ArrayList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Vita Stejskal
 */
public final class HLFactory implements HighlightsLayerFactory {
    
    /** Creates a new instance of HLFactory */
    public HLFactory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        final Document d = context.getDocument();
        final JTextComponent c = context.getComponent();
        final String mimeType = getMimeType(c, d);
        
        layers.add(HighlightsLayer.create(
            GuardedBlocksHighlighting.LAYER_TYPE_ID, 
            ZOrder.BOTTOM_RACK, 
            true,  // fixedSize
            new GuardedBlocksHighlighting(d, mimeType)
        ));
        
        layers.add(HighlightsLayer.create(
            ComposedTextHighlighting.LAYER_TYPE_ID, 
            ZOrder.TOP_RACK, 
            true,  // fixedSize
            new ComposedTextHighlighting(d, mimeType)
        ));
        
        if (TokenHierarchy.get(context.getDocument()) == null) {
            // There is no lexer yet, we will use this layer for backwards compatibility
            layers.add(HighlightsLayer.create(
                NonLexerSyntaxHighlighting.LAYER_TYPE_ID, 
                ZOrder.SYNTAX_RACK, 
                true,  // fixedSize
                new NonLexerSyntaxHighlighting(d, mimeType)
            ));
        }
        
        return layers.toArray(new HighlightsLayer[layers.size()]);
    }
    
    private static String getMimeType(JTextComponent c, Document d) {
        String mimeType = (String) d.getProperty("mimeType"); //NOI18N
        
        if (mimeType == null) {
            mimeType = c.getUI().getEditorKit(c).getContentType();
        }
        
        return mimeType == null ? "" : mimeType; //NOI18N
    }
}
