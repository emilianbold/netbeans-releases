/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.text.MessageFormat;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

class J2SEPlatformNode extends AbstractNode {

    private J2SEPlatformImpl platform;
    private String toolTip;
    private boolean broken;

    public J2SEPlatformNode (J2SEPlatformImpl platform, DataObject definition) {
        super (Children.LEAF, Lookups.fixed(new Object[] {platform, definition}));
        this.platform = platform;
        super.setIconBase ("org/netbeans/modules/java/j2seplatform/resources/platform");
    }

    public String getDisplayName () {
        return this.platform.getDisplayName();
    }

    public String getHtmlDisplayName() {
        if (isBroken()) {
            return "<font color=\"#A40000\">"+this.platform.getDisplayName()+"</font>";
        }
        else {
            return null;
        }
    }

    public String getName () {
        return this.getDisplayName();
    }

    public void setName (String name) {
        this.platform.setDisplayName (name);
    }

    public void setDisplayName(String name) {
        this.setName (name);
    }
    
    public synchronized String getShortDescription() {
        if (this.toolTip == null) {
            this.toolTip = MessageFormat.format (
            NbBundle.getMessage(J2SEPlatformNode.class,"TXT_J2SEPlatformToolTip"),
            new Object[] {
                this.platform.getSpecification().getVersion()
            });
        }
        return this.toolTip;
    }

    public boolean hasCustomizer () {
        return true;
    }

    public java.awt.Component getCustomizer () {
        if (isBroken()) {
            return new BrokenPlatformCustomizer (this.platform);
        }
        else {
            return new J2SEPlatformCustomizer (this.platform);
        }
    }

    private boolean isBroken () {
        return this.platform.getInstallFolders().size()==0;
    }

}
