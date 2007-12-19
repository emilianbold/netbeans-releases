/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.options;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.ResourceMarker;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Defines a new options category in the IDE's options dialog.
 *
 * @author avk
 */
public final class PhpOptionsCategory extends OptionsCategory {

    /**
     * php options category specified in
     * org.netbeans.modules.php.project.resources.layer.xml
     * as 'OptionsDialog/PhpOptions.instance'
     */
    //private static final String PHP_OPTIONS_CATEGORY = "PhpOptions";
    /**
     * php options are moved to misclleaneous category now.
     */
    private static final String PHP_OPTIONS_CATEGORY = "Advanced";
    private static String LBL_OPTIONS_CATEGORY_NAME = "OptionsCategory_Name";
    private static String LBL_OPTIONS_CATEGORY_TITLE = "OptionsCategory_Title";

    public String getCategoryName() {
        return NbBundle.getMessage(
                PhpOptionsCategory.class, LBL_OPTIONS_CATEGORY_NAME);
    }

    public String getTitle() {
        return NbBundle.getMessage(
                PhpOptionsCategory.class, LBL_OPTIONS_CATEGORY_TITLE);
    }

    public OptionsPanelController create() {
        return new PhpOptionsPanelController();
    }

    public Icon getIcon() {
        return new ImageIcon(Utilities.loadImage( 
                ResourceMarker.getLocation()+ResourceMarker.OPTIONS_ICON ));

    }

    public static void displayPhpOptions() {
        OptionsDisplayer.getDefault().open(PHP_OPTIONS_CATEGORY);
    }
}