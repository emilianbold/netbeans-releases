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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;

import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.DDUtils;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.schema2beans.*;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.nio.channels.FileLock;
import org.openide.ErrorManager;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class WSWebBndDataObject extends WSMultiViewDataObject /*MultiDataObject*/
        /*implements  PropertyChangeListener */{
    
    
    public static final String WEBBND_DATA_MULTIVIEW_PREFIX="webbnd_data";
    public static final String WEBBND_MULTIVIEW_ATTR="attr";
    
    public WSWebBndDataObject(FileObject pf, WSWebBndDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }
    
    protected Node createNodeDelegate() {
        return new WSWebBndDataNode(this);
    }
    
    public WSWebBnd getWebBnd() throws java.io.IOException{
        return (WSWebBnd)getDD();
    }
    
    public DDXmi getDD() throws java.io.IOException {
        if (ddBaseBean==null) {
            ddBaseBean = new WSWebBnd(FileUtil.toFile(getPrimaryFile()),false);
        }
        return (WSWebBnd)ddBaseBean;
    }

    protected DDXmi createDDXmiFromDataCache() {
        return new WSWebBnd(getInputStream(), false);
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc()  {
        designView = new DesignView(this);
        return new DesignMultiViewDesc[]{designView};
    }
    
    
    
    public WSWebBndToolBarMVElement getWBTB() {
        return ((DesignView)designView).getWBTB();
    }
    
    protected class DesignView extends WSDesignView {
        private WSWebBndToolBarMVElement wbtb;
        private static final long serialVersionUID=7209572130942350230L;
        DesignView(WSWebBndDataObject dObj) {
            super(dObj);
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            WSWebBndDataObject dObj = (WSWebBndDataObject)getDataObject();
            wbtb=new WSWebBndToolBarMVElement(dObj);
            return wbtb;
        }
        
        public String preferredID() {
            return "webbnd_multiview_design";
        }
        public WSWebBndToolBarMVElement getWBTB() {
            return wbtb;            
        }
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/websphere6/dd/resources/ws5.gif"); //NOI18N
        }
    }
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View
     */
    
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof ResRefBindingsType ||
                element instanceof EjbRefBindingsType ||
                element instanceof ResEnvRefBindingsType) {
            openView(0);
            target=element;
        }
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
