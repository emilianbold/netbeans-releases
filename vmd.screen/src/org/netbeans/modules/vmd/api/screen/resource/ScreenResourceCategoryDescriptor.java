package org.netbeans.modules.vmd.api.screen.resource;

import java.awt.*;
import java.util.Collection;

/**
 * Descriptor for the category which this resource belongs to.
 * @author breh
 *
 */
public final class ScreenResourceCategoryDescriptor {

    private Image icon;
    private String title;
    private String tooltip;
    private ScreenResourceOrderingController[] ordering;
    private int order;
   
    public ScreenResourceCategoryDescriptor (String title, Image icon, String tooltip, int order,ScreenResourceOrderingController... ordering) {
        if (ordering == null)
            throw new NullPointerException("Null value ordering"); //NOI18N
        this.ordering = ordering;
        this.title = title;
        this.icon = icon;
        this.tooltip = tooltip;
        this.order = order;
    }
    
    /**
     * @return the icon
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the toolTip
     */
    public String getToolTip () {
        return tooltip;
    }
    
    public int getOrder() {
        return order;
    }
    
    public ScreenResourceOrderingController[] getOrderingControllers() {
        return ordering;
    }
    

}
