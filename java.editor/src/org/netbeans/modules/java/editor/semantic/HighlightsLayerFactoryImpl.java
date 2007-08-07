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
package org.netbeans.modules.java.editor.semantic;

import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.Context;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Jan Lahoda
 */
public class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {
    
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[] {
            HighlightsLayer.create(SemanticHighlighter.class.getName() + "-1", ZOrder.SYNTAX_RACK.forPosition(1000), false, LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, context.getDocument())),
            HighlightsLayer.create(SemanticHighlighter.class.getName() + "-2", ZOrder.SYNTAX_RACK.forPosition(1500), false, SemanticHighlighter.getImportHighlightsBag(context.getDocument())),
            //the mark occurrences layer should be "above" current row and "below" the search layers:
            HighlightsLayer.create(MarkOccurrencesHighlighter.class.getName(), ZOrder.CARET_RACK.forPosition(50), false, MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument())),
            //"above" mark occurrences, "below" search layers:
            HighlightsLayer.create(InstantRenamePerformer.class.getName(), ZOrder.CARET_RACK.forPosition(75), false, InstantRenamePerformer.getHighlightsBag(context.getDocument())),
        };
    }

}
