package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext;

import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbExt;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.schema2beans.*;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.ErrorManager;
import java.nio.channels.FileLock;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import javax.xml.parsers.*;


public class WSEjbExtDataObject extends WSMultiViewDataObject {
    
    public WSEjbExtDataObject(FileObject pf, WSEjbExtDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);    
    }
    
    public DDXmi getDD() throws java.io.IOException {
        if (ddBaseBean==null) {
            ddBaseBean = new WSEjbExt(FileUtil.toFile(getPrimaryFile()),false);
        }
        return (WSEjbExt)ddBaseBean;
    }
    public WSEjbExt getEjbExt() throws java.io.IOException{
        return (WSEjbExt)getDD();
    }
    
    protected DDXmi createDDXmiFromDataCache() {
        return new WSEjbExt(getInputStream(), false);
    }
    
    
    protected Node createNodeDelegate() {
        return new WSEjbExtDataNode(this);
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc()  {
        designView = new DesignView(this);
        return new DesignMultiViewDesc[]{designView};
    }
    
    
    
    public WSEjbExtToolBarMVElement getEETB() {
        return ((DesignView)designView).getEETB();
    }
    
    protected class DesignView extends WSDesignView {
        private WSEjbExtToolBarMVElement eetb;
        private static final long serialVersionUID=7209502130942350230L;
        DesignView(WSEjbExtDataObject dObj) {
            super(dObj);
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            WSEjbExtDataObject dObj = (WSEjbExtDataObject)getDataObject();
            eetb=new WSEjbExtToolBarMVElement(dObj);
            return eetb;
        }
        
        public String preferredID() {
            return "ejbext_multiview_design";
        }
        public WSEjbExtToolBarMVElement getEETB() {
            return eetb;
            
        }
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/websphere6/dd/resources/ws4.gif"); //NOI18N
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
