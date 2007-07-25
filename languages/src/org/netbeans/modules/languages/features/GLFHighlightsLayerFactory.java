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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Jan Jancura
 */
public class GLFHighlightsLayerFactory implements HighlightsLayerFactory {

    public HighlightsLayer[] createLayers (Context context) {
        return new HighlightsLayer[] {
            HighlightsLayer.create (
                "GLF Semantic Coloring", 
                ZOrder.SYNTAX_RACK.forPosition (10), 
                false, 
                new SemanticHighlightsLayer (context.getDocument ())
            ),
            HighlightsLayer.create (
                "GLF Languages Coloring", 
                ZOrder.SYNTAX_RACK.forPosition (11), 
                false, 
                new LanguagesHighlightsLayer (context.getDocument ())
            ),
            HighlightsLayer.create (
                "GLF Token Highlighting", 
                ZOrder.SHOW_OFF_RACK.forPosition (0), 
                false, 
                new TokenHighlightsLayer (context.getDocument ())
            )
        };
    }
}
