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

package org.netbeans.modules.j2ee.persistence.unit;

import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;

/**
 *
 * @author mkuchtiak
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private PUDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, PUDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof PersistenceUnit) {
            return new PersistenceUnitPanel((SectionView)editor.getContentView(), dObj, (PersistenceUnit)key);
        }
//        System.out.println("### PanelFactory returning null for: " + key);
        return null;
    }
}

