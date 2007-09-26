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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.options;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 * Implementation of this class represents one category (like "Fonts & Colors"
 * or "Editor") in Options Dialog. It should be registerred in layers:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;file name="FooOptionsPanel.instance"&gt;
 *         &lt;attr name="instanceClass" stringvalue="org.foo.FooOptionsPanel"/&gt;
 *     &lt;/file&gt;
 * &lt;/folder&gt;</pre>
 *
 * Use standard way how to sort items registered in layers:
 * 
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;attr name="GeneralPanel.instance/FooOptionsPanel.instance" boolvalue="true"/&gt;
 * </pre>
 *
 * @see AdvancedOption
 * @see OptionsPanelController 
 *
 * @author Jan Jancura
 */
public abstract class OptionsCategory {
    
    /**
     * Returns base name of 32x32 icon (gif, png) used in list on the left side of 
     * Options Dialog. See {@link AbstractNode#setIconBase} method for more info.
     *
     * @deprecated  This method will not be a part of NB50! Use
     *              {@link #getIcon} instead.
     * @return base name of 32x32 icon
     */
    public String getIconBase () {
        return null;
    }
    
    /**
     * Returns 32x32 icon used in list on the left side of 
     * Options Dialog.
     *
     * @return 32x32 icon
     */
    public Icon getIcon () {
        Image image = Utilities.loadImage (getIconBase () + ".png");
        if (image != null) return new ImageIcon (image);
        image = Utilities.loadImage (getIconBase () + ".gif");
        if (image == null) return null;
        return new ImageIcon (image);
    }
    
    /**
     * Returns name of category used in list on the left side of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getCategoryName ();
    
    /**
     * This text will be used in title component on the top of Options Dialog
     * when your panel will be selected.
     *
     * @return title of this panel
     */
    public abstract String getTitle ();
    
    /**
     * Returns new {@link OptionsPanelController} for this category. PanelController 
     * creates visual component to be used inside of the Options Dialog.
     * You should not do any time-consuming operations inside 
     * the constructor, because it blocks initialization of OptionsDialog. 
     * Initialization should be implemented in update method.
     *
     * @return new instance of PanelController for this options category
     */
    public abstract OptionsPanelController create ();
    
}
