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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext;


import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbExt;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbExtensionsType;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSEjbExtAttributesPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.EjbExtensionPanel;

import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * @author dlipin
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private WSEjbExtDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, WSEjbExtDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionView pv=(SectionView)editor.getContentView();
        SectionInnerPanel createdPanel=null;
        if (key instanceof WSEjbExt){            
            createdPanel=new WSEjbExtAttributesPanel(pv, dObj, (WSEjbExt)key);
        } else if (key instanceof EjbExtensionsType){
            createdPanel=new EjbExtensionPanel(pv,dObj,(EjbExtensionsType)key);
        } /*else if (key instanceof EjbRefBindingsType){
            return new WSEjbRefBindingsPanel(pv,dObj,(EjbRefBindingsType)key);
        } else if (key instanceof ResEnvRefBindingsType){
            return new WSResEnvRefBindingsPanel(pv,dObj,(ResEnvRefBindingsType)key);
        } */
        return createdPanel;
    }
}
