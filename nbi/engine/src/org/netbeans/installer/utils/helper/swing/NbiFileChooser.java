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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.installer.utils.helper.swing;

import javax.swing.JFileChooser;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class NbiFileChooser extends JFileChooser {
    
    public NbiFileChooser() {
        super();
        String titleProp = System.getProperty(FILECHOOSER_TITLE_PROPERTY);
        setDialogTitle((titleProp==null) ? DEFAULT_FILECHOOSER_TITLE : titleProp);
        
        String approveButtonProp = System.getProperty(FILECHOOSER_APPROVE_BUTTON_TEXT_PROPERTY);        
        String approveButtonText = (approveButtonProp==null) ? 
            DEFAULT_FILECHOOSER_APPROVE_BUTTON_TEXT : 
            approveButtonProp;
        
        if ((approveButtonText != null) && !approveButtonText.equals("")) {            
            setApproveButtonText(StringUtils.stripMnemonic(approveButtonText));
            setApproveButtonToolTipText(StringUtils.stripMnemonic(approveButtonText));            
            if (!SystemUtils.isMacOS()) {
                setApproveButtonMnemonic(StringUtils.fetchMnemonic(approveButtonText));
            }
        }
    }
    public static final String DEFAULT_FILECHOOSER_TITLE =
            ResourceUtils.getString(NbiFileChooser.class,
            "NFC.filechooser.title"); // NOI18N    
    public static final String DEFAULT_FILECHOOSER_APPROVE_BUTTON_TEXT =
            ResourceUtils.getString(NbiFileChooser.class,
            "NFC.filechooser.approve.button.text"); // NOI18N
    
    public static final String FILECHOOSER_TITLE_PROPERTY =
            "filechooser.title";
    public static final String FILECHOOSER_APPROVE_BUTTON_TEXT_PROPERTY =
            "filechooser.approve.button";
}
