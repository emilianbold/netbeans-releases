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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 * SecurityFactory.java
 *
 * Factory for creating instances of SecurityRolesPanel, SecurityConstraintPanel
 * and LoginConfigPanel.
 *
 * @author ptliu
 */
public class SecurityFactory implements InnerPanelFactory {
    private ToolBarDesignEditor editor;
    private DDDataObject dObj;
    
    
    /**
     * Creates a new instance of SecurityFactory
     */
    public SecurityFactory(ToolBarDesignEditor editor, DDDataObject dObj) {
        this.editor = editor;
        this.dObj = dObj;
    }

    public SectionInnerPanel createInnerPanel(Object key) { 
        if (key.equals("security_roles")) {  //NOI18N
            return new SecurityRolesPanel((SectionView) editor.getContentView(), dObj);
        } else if (key instanceof SecurityConstraint) {
            return new SecurityConstraintPanel((SectionView) editor.getContentView(), dObj,
                    (SecurityConstraint) key);
        } else if (key.equals("login_config")) { //NOI18N
            return new LoginConfigPanel((SectionView) editor.getContentView(), dObj);
        }
        
        return null;
    } 
}
