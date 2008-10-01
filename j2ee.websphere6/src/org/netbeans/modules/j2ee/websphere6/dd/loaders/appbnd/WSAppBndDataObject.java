/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
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
import org.openide.util.ImageUtilities;

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
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/websphere6/dd/resources/ws1.gif"); //NOI18N
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
