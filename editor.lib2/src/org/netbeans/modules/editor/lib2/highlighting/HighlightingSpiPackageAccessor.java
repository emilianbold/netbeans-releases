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

import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author vita
 */
public abstract class HighlightingSpiPackageAccessor {
    
    private static HighlightingSpiPackageAccessor ACCESSOR = null;
    
    public static synchronized void register(HighlightingSpiPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!";
        ACCESSOR = accessor;
    }
    
    public static synchronized HighlightingSpiPackageAccessor get() {
        // Trying to wake up HighlightsLayer ...
        try {
            Class clazz = Class.forName(HighlightsLayer.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        assert ACCESSOR != null : "There is no package accessor available!";
        return ACCESSOR;
    }
    
    /** Creates a new instance of HighlightingSpiPackageAccessor */
    protected HighlightingSpiPackageAccessor() {
    }
    
    public abstract HighlightsLayerFactory.Context createFactoryContext(Document document, JTextComponent component);
    
    public abstract List<? extends HighlightsLayer> sort(Collection<? extends HighlightsLayer> layers) throws TopologicalSortException;
    
    public abstract HighlightsLayerAccessor getHighlightsLayerAccessor(HighlightsLayer layer);
}
