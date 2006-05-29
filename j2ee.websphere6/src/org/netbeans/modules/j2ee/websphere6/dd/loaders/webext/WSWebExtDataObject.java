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

import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
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



public class WSWebExtDataObject extends  /*XmlMultiViewDataObject*/ WSMultiViewDataObject {
    
    
    public WSWebExtDataObject(FileObject pf, WSWebExtDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);        
    }
    
    public DDXmi getDD() throws java.io.IOException {
        if (ddBaseBean==null) {
            ddBaseBean = new WSWebExt(FileUtil.toFile(getPrimaryFile()),false);
        }
        return (WSWebExt)ddBaseBean;
    }
    public WSWebExt getWebExt() throws java.io.IOException{
        return (WSWebExt)getDD();
    }
    
    protected DDXmi createDDXmiFromDataCache() {
        return new WSWebExt(getInputStream(), false);
    }
    
    
    protected Node createNodeDelegate() {
        return new WSWebExtDataNode(this);
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc()  {
        designView = new DesignView(this);
        return new DesignMultiViewDesc[]{designView};
    }
    
    
    
    public WSWebExtToolBarMVElement getWETB() {
        return ((DesignView)designView).getWETB();
    }
    
    protected class DesignView extends WSDesignView {
        private WSWebExtToolBarMVElement wetb;
        private static final long serialVersionUID=7209502130942350230L;
        DesignView(WSWebExtDataObject dObj) {
            super(dObj);
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            WSWebExtDataObject dObj = (WSWebExtDataObject)getDataObject();
            wetb=new WSWebExtToolBarMVElement(dObj);
            return wetb;
        }
        
        public String preferredID() {
            return "webext_multiview_design";
        }
        public WSWebExtToolBarMVElement getWETB() {
            return wetb;
            
        }
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/websphere6/dd/resources/ws6.gif"); //NOI18N
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
