/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Apr 27, 2004
 * Time: 8:06:47 PM
 * To change this template use Options | File Templates.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;

class J2SEPlatformNode extends AbstractNode {

    private J2SEPlatformImpl platform;

    public J2SEPlatformNode (J2SEPlatformImpl platform, DataObject definition) {
        super (Children.LEAF, Lookups.fixed(new Object[] {platform, definition}));
        this.platform = platform;
        super.setIconBase ("org/netbeans/modules/java/j2seplatform/resources/platform");
    }

    public String getDisplayName () {
        return this.platform.getDisplayName();
    }

    public String getName () {
        return this.getDisplayName();
    }

    public boolean hasCustomizer () {
        return true;
    }

    public java.awt.Component getCustomizer () {
        return new J2SEPlatformCustomizer (this.platform);
    }

}
