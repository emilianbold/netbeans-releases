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
