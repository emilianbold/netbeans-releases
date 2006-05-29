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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.webext;


import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.*;
import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * @author dlipin
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private WSWebExtDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, WSWebExtDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionView pv=(SectionView)editor.getContentView();
        if (key instanceof WSWebExt){            
            return new WSWebExtAttributesPanel(pv, dObj, (WSWebExt)key);
        } else if (key instanceof ExtendedServletsType){
            return new WSExtendedServletPanel(pv,dObj,(ExtendedServletsType)key);
        }/* else if (key instanceof EjbRefBindingsType){
            return new WSEjbRefBindingsPanel(pv,dObj,(EjbRefBindingsType)key);
        } else if (key instanceof ResEnvRefBindingsType){
            return new WSResEnvRefBindingsPanel(pv,dObj,(ResEnvRefBindingsType)key);
        } */
        return null;
    }
}
