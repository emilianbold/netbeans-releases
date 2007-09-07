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

package org.netbeans.modules.cnd.highlight;

import javax.swing.text.Document;
import org.netbeans.modules.cnd.highlight.semantic.ifdef.*;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Sergey Grinev
 */
public class CppHighlightsLayerFactory implements HighlightsLayerFactory {
    
    private static InactiveCodeHighlighter getInactiveCodeHighlighter(Document doc) {
        InactiveCodeHighlighter ich = (InactiveCodeHighlighter)doc.getProperty(InactiveCodeHighlighter.class);
        if (ich == null)
        {
            doc.putProperty(InactiveCodeHighlighter.class, ich = new InactiveCodeHighlighter(doc));
        }
        return ich;
    }
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[] {
            HighlightsLayer.create(
                    InactiveCodeHighlighter.class.getName(), 
                    ZOrder.SYNTAX_RACK.forPosition(2000),
                    true,
                    getInactiveCodeHighlighter(context.getDocument()).getHighlightsBag())
            };
    }

}
