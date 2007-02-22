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
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import javax.swing.ImageIcon;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Dialog used to create User Defined WSDL and the operations
 *
 * @author Kiran
 *
 * @version $Revision$
 */
public interface IWSDLPanelConstants {

    String TNS = "tns"; //NOI18N
    String PROPERTY_ALIAS_FOLDER_NODE_NAME =
        NbBundle.getMessage(IWSDLPanelConstants.class, "PROPERTY_ALIAS_FOLDER_NODE_NAME");        
    String PROPERTY_FOLDER_NODE_NAME =
        NbBundle.getMessage(IWSDLPanelConstants.class, "PROPERTY_FOLDER_NODE_NAME");        
}
