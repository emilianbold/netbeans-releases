/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.ui.options;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class CndOptionsCategory extends OptionsCategory {
    
    public OptionsPanelController create() {
        return new CndOptionsPanelController();
    }

    public String getDisplayName() {
        return getString("CndOptions"); // NOI18N
    }

    public String getTooltip() {
        return getString("CndOptionsCategory_Tooltip"); // NOI18N
    }

    public String getCategoryName() {
        return getString("CndOptionsCategory_Name"); // NOI18N
    }

    public String getTitle() {
        return getString("CndOptions_Title"); // NOI18N
    }  

    public Icon getIcon() {
        String path = "org/netbeans/modules/cnd/ui/options/cnd_32.png"; // NOI18N
        Image image = Utilities.loadImage(path);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(CndOptionsCategory.class, key);
    }
}
