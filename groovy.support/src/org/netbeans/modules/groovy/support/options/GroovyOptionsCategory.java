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

package org.netbeans.modules.groovy.support.options;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class GroovyOptionsCategory extends OptionsCategory {

    @Override
    public Icon getIcon() {
        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/groovy/support/resources/groovy-options.png")); // NOI18N
    }

    public String getCategoryName() {
        return NbBundle.getMessage(GroovyOptionsCategory.class, "OptionsCategory_Name_Groovy");
    }

    public String getTitle() {
        return NbBundle.getMessage(GroovyOptionsCategory.class, "OptionsCategory_Title_Groovy");
    }

    public OptionsPanelController create() {
        return new GroovyPanelController();
    }
}