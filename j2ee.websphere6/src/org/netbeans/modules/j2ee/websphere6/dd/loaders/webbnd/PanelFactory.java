package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;


import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.CommonRef;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSReferenceBindingsPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSWebBndAttributesPanel;
import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * @author dlipin
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private WSWebBndDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, WSWebBndDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionView pv=(SectionView)editor.getContentView();
        if (key instanceof WSWebBnd){            
            return new WSWebBndAttributesPanel(pv, dObj, (WSWebBnd)key);
        } else if (key instanceof CommonRef){
            return new WSReferenceBindingsPanel(pv,dObj,(CommonRef)key);
        } 
        return null;
    }
}
