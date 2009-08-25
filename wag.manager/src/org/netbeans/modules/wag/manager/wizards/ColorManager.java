/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wag.manager.wizards;

import java.awt.Color;
import javax.swing.UIManager;

/**
 *
 * @author S. Aubrecht
 */
public class ColorManager {
    
    private static ColorManager theInstance;

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N

    private Color defaultBackground = UIManager.getColor("Tree.textBackground") == null  //NOI18N
            ? UIManager.getColor("white")  //NOI18N
            : UIManager.getColor("Tree.textBackground"); //NOI18N
    private Color defaultForeground = UIManager.getColor("black"); //NOI18N
    private Color disabledColor = Color.gray;
    private Color linkColor = Color.blue;
    private Color errorColor = new Color(153,0,0);
    private Color stableBuildColor = new Color(0,153,0);
    private Color unstableBuildColor = Color.yellow.darker().darker();

    private ColorManager() {
    }

    public static ColorManager getDefault() {
        if( null == theInstance )
            theInstance = new ColorManager();
        return theInstance;
    }

    public Color getDefaultBackground() {
        if( isAqua )
            return UIManager.getColor("NbExplorerView.background"); // NOI18N
        return defaultBackground;
    }

    public Color getDefaultForeground() {
        return defaultForeground;
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public Color getLinkColor() {
        return linkColor;
    }

    public Color getStableBuildColor() {
        return stableBuildColor;
    }

    public static ColorManager getTheInstance() {
        return theInstance;
    }

    public Color getUnstableBuildColor() {
        return unstableBuildColor;
    }
}
