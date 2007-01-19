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

import java.util.List;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;

/**
 *
 * @author vita
 */
public interface HighlightsLayerFilter {

    public static final HighlightsLayerFilter IDENTITY = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            return layers;
        }
    };
    
    public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers);
    
}
