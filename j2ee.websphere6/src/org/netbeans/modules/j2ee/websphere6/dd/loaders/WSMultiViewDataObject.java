/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmi;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.*;


/**
 *
 * @author dlm198383
 */
public abstract class WSMultiViewDataObject extends XmlMultiViewDataObject{
    protected WSDesignView designView;
    protected ModelSynchronizer modelSynchronizer;
    protected boolean changedFromUI;
    protected DDXmi ddBaseBean;
    private static final long serialVersionUID = 76675745399723L;
    public static final String DD_MULTIVIEW_POSTFIX = "_multiview_design";
    public static final String MULTIVIEW_WEBBND = "webbnd";
    public static final String MULTIVIEW_WEBEXT = "webext";
    public static final String MULTIVIEW_APPBND = "appbnd";
    public static final String MULTIVIEW_APPEXT = "appext";
    public static final String MULTIVIEW_EJBBND = "ejbbnd";
    public static final String MULTIVIEW_EJBEXT = "ejbext";
    /**
     * Creates a new instance of WSMultiViewDataObject
     */
    public WSMultiViewDataObject(FileObject pf, MultiFileLoader loader)  throws DataObjectExistsException, IOException {
        super(pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        try {
            parseDocument();
        } catch (IOException ex) {
            System.out.println("ex="+ex);
        }
    }
    protected String getPrefixMark() {
        return null;
    }
    
    /**
     *
     * @throws IOException
     */
    protected java.io.InputStream getInputStream() {
        return getDataCache().createInputStream();
    }
    
    
    
    
    protected void parseDocument() throws IOException {
        if(ddBaseBean==null) {
            ddBaseBean=getDD();
        } else {
            try {
                SAXParseException error = DDUtils.parse(new InputSource(getDataCache().createReader()));
                setSaxError(error);
                
                DDXmi bb = createDDXmiFromDataCache();
                
                if (bb!=null) {
                    ddBaseBean.merge(bb, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
                }
            } catch (SAXException ex) {
                setSaxError(ex);
            }
        }
    }
    
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();
    
    public WSDesignView getDesignView() {
        return designView;
    }
    
    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }
    public boolean isChangedFromUI() {
        return changedFromUI;
    }
    
    public void setChangedFromUI(boolean changedFromUI) {
        this.changedFromUI=changedFromUI;
    }
    protected abstract class WSDesignView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 71111745399723L;
        protected WSDesignView(WSMultiViewDataObject dObj) {
            super(dObj, "Design");
        }
        public abstract MultiViewElement createElement();
        public abstract java.awt.Image getIcon();
        public abstract String preferredID() ;
        
    }
    public abstract DDXmi getDD() throws java.io.IOException;
    
    protected abstract DDXmi createDDXmiFromDataCache() ;
    
    protected class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        
        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 500);
        }
        
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }
        public void updateData(org.openide.filesystems.FileLock dataLock, boolean modify) {
            super.updateData(dataLock, modify);
            try {
                parseDocument();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        
        protected void updateDataFromModel(Object model, org.openide.filesystems.FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((DDXmi) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (Schema2BeansException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        protected Object getModel() {
            try {
                return getDD();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
                return null;
            }
        }
        
        protected void reloadModelFromData() {
            try {
                parseDocument();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        //protected void updateDataFromModel(Object object, org.openide.filesystems.FileLock fileLock, boolean b) {
        //}
    }
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return modelSynchronizer;
    }
    
    /** Enable to get active MultiViewElement object
     */
    
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }
    
    
}
