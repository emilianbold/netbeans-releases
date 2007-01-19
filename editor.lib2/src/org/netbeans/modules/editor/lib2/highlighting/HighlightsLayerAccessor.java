package org.netbeans.modules.editor.lib2.highlighting;

import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.ZOrder;


public interface HighlightsLayerAccessor {
    
    public String getLayerTypeId();
    public boolean isFixedSize();
    public ZOrder getZOrder();
    public HighlightsContainer getContainer();
    
} // End of HighlightsLayerAccessor