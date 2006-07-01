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

/**
 * Implementation of this class represents one category (like "Ant"
 * or "Form Editor") in Miscellaneous Panel of Options Dialog. It should
 * be registerred in layers:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;folder name="Advanced"&gt;
 *         &lt;file name="FooAdvancedPanel.instance"&gt;
 *             &lt;attr name="instanceClass" stringvalue="org.foo.FooAdvancedPanel"/&gt;
 *         &lt;/file&gt;
 *     &lt;/folder&gt;
 * &lt;/folder&gt;</pre>
 * 
 * No explicit sorting recognized (may be sorted e.g. by display name).
 *
 * @see OptionsCategory
 * @see OptionsPanelController 
 * @author Jan Jancura
 */
public abstract class AdvancedOption {
    
    /**
     * Returns name of category used in Advanced Panel of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getDisplayName ();
    
    /**
     * Returns tooltip to be used on category name.
     *
     * @return tooltip for this category
     */
    public abstract String getTooltip ();
    
    /**
     * Returns {@link OptionsPanelController} for this category. PanelController 
     * creates visual component to be used inside of Advanced Panel.
     *
     * @return new instance of {@link OptionsPanelController} for this advanced options 
     *         category
     */
    public abstract OptionsPanelController create ();

}
