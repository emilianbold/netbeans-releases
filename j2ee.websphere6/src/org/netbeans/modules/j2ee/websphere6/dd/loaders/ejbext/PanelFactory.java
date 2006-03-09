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
