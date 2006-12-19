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

package org.netbeans.api.options;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 */
public final class RegisteredCategory extends OptionsCategory {
    private static Icon icon;
    
    public Icon getIcon() {
        if (icon == null) {
            Image image = Utilities.loadImage("org/netbeans/modules/options/resources/generalOptions.png");
            icon = new ImageIcon(image);
        }
        return icon;
    }
    
    public String getCategoryName() {
        return  "CTL_General_Options";
    }
    
    public String getTitle() {
        return "CTL_General_Options_Title";
    }
    
    public String getDescription() {
        return "CTL_General_Options_Description";
    }
    
    public OptionsPanelController create() {
        return new OptionsPanelController() {
            public void update() {}
            
            public void applyChanges() {}
            
            public void cancel() {}
            
            public boolean isValid() {
                return true;
            }
            
            public boolean isChanged() {
                return false;
            }
            
            public HelpCtx getHelpCtx() {
                return null;
            }
            
            public JComponent getComponent(Lookup masterLookup) {
                return new JLabel();
            }
            
            public void addPropertyChangeListener(PropertyChangeListener l) {}
            
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
    }
}
