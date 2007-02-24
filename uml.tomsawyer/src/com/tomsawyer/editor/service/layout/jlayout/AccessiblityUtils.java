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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.tomsawyer.editor.service.layout.jlayout;

import com.tomsawyer.editor.TSEResourceBundleWrapper;
import java.awt.Component;
import javax.accessibility.AccessibleContext;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Thuy
 */
public class AccessiblityUtils
{
    public static TSEResourceBundleWrapper TSBundle = TSEResourceBundleWrapper.getSystemLabelBundle();
    /** Creates a new instance of AccessiblityUtils */
    public AccessiblityUtils()
    {
    }
    
    /**
     * this mehod set the accessible name and descrption of a component.
     * for any java component, the accessible name is set, by default, to be the same as the component's label or text or name,
     * so there is no need to set the accessible name unless you want to set it to a different name. For customized components, you
     * have to explitcilty set the accessibe name.
     * @param comp the component that needs to have accessible name and description set.
     * @param accsNameKey the bundle key without the fix "ACSN_" to get the accessible name. Set this parameter to null,
     * if you don't want to set the accessibel name.
     * @param accsDescKey the bundle key without the fix "ACSD_" to get the accessible description. Set this parameter to null,
     * if you don't want to set the accessibel description.
     */
    public static void setAccessibleProperties(Component comp, String accsNameKey, String accsDescKey)
    {
        if (comp != null)
        {
            String labelStr = null;
            AccessibleContext accsContext = comp.getAccessibleContext();
            if (accsNameKey != null)
            {
                labelStr = TSBundle.getStringSafely("ACSN_"+accsNameKey);
                accsContext.setAccessibleName(labelStr);
            }
            if (accsDescKey != null)
            {
                labelStr = TSBundle.getStringSafely("ACSD_"+accsDescKey);
                accsContext.setAccessibleDescription(labelStr);
            }
        }
    }
    
    /**
     * this mehod get the mnemonic character given the bundle key without the prefix "MNE_"
     *
     * @param bundleKey the bundle key without the prefix "MNE_".  If bundleKey is null, '?' is returned.
     */
    public static char getMnemonic(String bundleKey)
    {
        char mnmChar = '?';
        if (bundleKey != null)
        {
            String mnm = TSBundle.getStringSafely("MNE_" + bundleKey);
            if (mnm != null && mnm.length() > 0)
            {
                mnmChar = mnm.charAt(0);
            }
        }
        return mnmChar;
    }
}
