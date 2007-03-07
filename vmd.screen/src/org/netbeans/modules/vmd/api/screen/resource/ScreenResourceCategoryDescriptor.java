package org.netbeans.modules.vmd.api.screen.resource;

import java.awt.*;

/**
 * Descriptor for the category which this resource belongs to.
 * @author breh
 *
 */
public final class ScreenResourceCategoryDescriptor {

    private Image icon;
    private String title;
    private String tooltip;
    private int order;

    public ScreenResourceCategoryDescriptor (String title, Image icon, String tooltip, int order) {
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

    public int getOrder () {
        return order;
    }

}
