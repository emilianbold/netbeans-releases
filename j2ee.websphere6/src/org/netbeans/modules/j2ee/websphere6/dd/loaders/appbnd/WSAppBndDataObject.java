package org.netbeans.modules.j2ee.websphere6.dd.loaders.appbnd;

import java.awt.Image;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;

public class WSAppBndDataObject extends WSMultiViewDataObject {
    
    public WSAppBndDataObject(FileObject pf, WSAppBndDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);        
    }
    
    protected Node createNodeDelegate() {
        return new WSAppBndDataNode(this);
    }
     public DDXmi getDD() throws java.io.IOException {
        if (ddBaseBean==null) {
            ddBaseBean = new WSAppBnd(FileUtil.toFile(getPrimaryFile()),false);
        }
        return (WSAppBnd)ddBaseBean;
    }
    public WSAppBnd getAppBnd() throws java.io.IOException{
        return (WSAppBnd)getDD();
    }
    
    protected DDXmi createDDXmiFromDataCache() {
        return new WSAppBnd(getInputStream(), false);
    }
    protected DesignMultiViewDesc[] getMultiViewDesc()  {
        designView = new DesignView(this);
        return new DesignMultiViewDesc[]{designView};
    }
    
    public WSAppBndToolBarMVElement getEBTB() {
        return ((DesignView)designView).getEBTB();
    }
    
    protected class DesignView extends WSDesignView {
        private WSAppBndToolBarMVElement ebtb;
        private static final long serialVersionUID=7209502130942350230L;
        DesignView(WSAppBndDataObject dObj) {
            super(dObj);
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            WSAppBndDataObject dObj = (WSAppBndDataObject)getDataObject();
            ebtb=new WSAppBndToolBarMVElement(dObj);
            return ebtb;
        }
        
        public String preferredID() {
            return "appbnd_multiview_design";
        }
        public WSAppBndToolBarMVElement getEBTB() {
            return ebtb;
            
        }
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/websphere6/dd/resources/ws3.gif"); //NOI18N
        }
    }
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View
     */
    
    public void showElement(Object element) {
        Object target=null;
        /*if (element instanceof ResRefBindingsType ||
                element instanceof AppRefBindingsType ||
                element instanceof ResEnvRefBindingsType) {
          */  openView(0);
            target=element;
        //}
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }
}
