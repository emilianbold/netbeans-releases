package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd;

import java.io.IOException;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.netbeans.modules.xml.multiview.*;



public class WSEjbBndDataObject extends WSMultiViewDataObject {
    
    public WSEjbBndDataObject(FileObject pf, WSEjbBndDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);        
    }
    
    
    public DDXmi getDD() throws java.io.IOException {
        if (ddBaseBean==null) {
            ddBaseBean = new WSEjbBnd(FileUtil.toFile(getPrimaryFile()),false);
        }
        return (WSEjbBnd)ddBaseBean;
    }
    public WSEjbBnd getEjbBnd() throws java.io.IOException{
        return (WSEjbBnd)getDD();
    }
    
    protected DDXmi createDDXmiFromDataCache() {
        return new WSEjbBnd(getInputStream(), false);
    }
    
    
    protected Node createNodeDelegate() {
        return new WSEjbBndDataNode(this);
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc()  {
        designView = new DesignView(this);
        return new DesignMultiViewDesc[]{designView};
    }
    
    
    
    public WSEjbBndToolBarMVElement getEBTB() {
        return ((DesignView)designView).getEBTB();
    }
    
    protected class DesignView extends WSDesignView {
        private WSEjbBndToolBarMVElement ebtb;
        private static final long serialVersionUID=7209502130942350230L;
        DesignView(WSEjbBndDataObject dObj) {
            super(dObj);
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            WSEjbBndDataObject dObj = (WSEjbBndDataObject)getDataObject();
            ebtb=new WSEjbBndToolBarMVElement(dObj);
            return ebtb;
        }
        
        public String preferredID() {
            return "ejbext_multiview_design";
        }
        public WSEjbBndToolBarMVElement getEBTB() {
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
                element instanceof EjbRefBindingsType ||
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
