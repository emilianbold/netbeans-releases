/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.appbnd;


import org.netbeans.modules.j2ee.websphere6.dd.beans.AuthorizationsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSAppBndAttributesPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSAuthorizationsPanel;
import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * @author dlipin
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private WSAppBndDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, WSAppBndDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionView pv=(SectionView)editor.getContentView();
        SectionInnerPanel createdPanel=null;
        if (key instanceof WSAppBnd){
            createdPanel=new WSAppBndAttributesPanel(pv,dObj,(WSAppBnd)key);
        } else if(key instanceof AuthorizationsType) {
            createdPanel=new WSAuthorizationsPanel(pv,dObj,(AuthorizationsType)key);
        }
        return createdPanel;
    }
}
