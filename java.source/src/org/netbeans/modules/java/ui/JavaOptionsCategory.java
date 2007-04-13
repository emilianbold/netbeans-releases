/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Defines a new options category in the IDE's options dialog.
 */
public final class JavaOptionsCategory extends OptionsCategory {

    public Icon getIcon() {
	return new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/source/resources/icons/JavaOptions_32.png"));
    }
    
    public String getCategoryName() {
	return NbBundle.getMessage(JavaOptionsCategory.class, "OptionsCategory_Name");
    }
    
    public String getTitle() {
	return NbBundle.getMessage(JavaOptionsCategory.class, "OptionsCategory_Title");
    }
    
    public OptionsPanelController create() {
	return new JavaOptionsPanelController();
    }
}

