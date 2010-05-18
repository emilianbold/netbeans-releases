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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xslt.core;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.xml.transform.Source;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.modules.xslt.core.context.MapperContext;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.openide.actions.OpenAction;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;

/**
 * @author Vitaly Bychkov
 */
public class XSLTDataObject extends MultiDataObject {
    
    private static final long serialVersionUID = 1L;
    private static final String FILE_DESC = "LBL_FileNode_desc";      // NOI18N
    private transient AtomicReference<Lookup> myLookup = new AtomicReference<Lookup>();
    private XSLTDataEditorSupport myDataEditorSupport;
    
    public XSLTDataObject(final FileObject obj, final MultiFileLoader loader) throws DataObjectExistsException {
        super(obj, loader);
        myDataEditorSupport = new XSLTDataEditorSupport(this);
        
        CookieSet cookies = getCookieSet();
        cookies.add(getEditorSupport());
        
        InputSource is = DataObjectAdapters.inputSource(this);
        cookies.add(new CheckXMLSupport(is));
        
        Source source = DataObjectAdapters.source(this);
        cookies.add(new TransformableSupport(source));
        cookies.assign(XmlFileEncodingQueryImpl.class, XmlFileEncodingQueryImpl.singleton());

    }
    
    @SuppressWarnings("unchecked")
    public Lookup getLookup() {
        if (myLookup.get() == null) {
            // add lazy initialization
            InstanceContent.Convertor<Class, Object> conv = new InstanceContent.Convertor<Class, Object>() {
                private AtomicReference<Controller> valControllerRef = new AtomicReference<Controller>();
                
                public Object convert(Class obj) {
                    if (obj == XslModel.class) {
                        return getEditorSupport().getXslModel();
                    }
                    if (obj == Controller.class) {
                        valControllerRef.compareAndSet(null, new Controller(getEditorSupport().getXslModel()));
                        return valControllerRef.get();
                    }
                    if (obj == MapperContext.class) {
                        return getEditorSupport().getMapperContext();
                    }
                    return null;
                }

                public Class type(Class obj) {
                    return obj;
                }

                public String id(Class obj) {
                    return obj.toString();
                }

                public String displayName(Class obj) {
                    return obj.getName();
                }
            };
            
            Lookup lookup = new ProxyLookup(
                // No need to add XSLTDataEditorSupport, since already added to the cookie set,
                // and thus already present in getCookieSet().getLookup().
                Lookups.fixed(new Class[] { XslModel.class, Controller.class, MapperContext.class }, conv),
                // Do not call super.getLookup(), it is deadlock-prone!
                getCookieSet().getLookup()
            );

            myLookup.compareAndSet(null, lookup);
        }
        return myLookup.get();
    }
    
    public void addSaveCookie(SaveCookie cookie){
        getCookieSet().add(cookie);
    }

    public void removeSaveCookie(){
        Node.Cookie cookie = getCookie(SaveCookie.class);
        if (cookie != null) {
            getCookieSet().remove(cookie);
        }
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new XSLTDataNode(this, getEditorSupport());
    }
    
    public XSLTDataEditorSupport getEditorSupport() {
        return myDataEditorSupport;
    }
    
    private SaveCookie getSaveCookie() {
        return new SaveCookie() {
            
            public void save() throws IOException {
                getEditorSupport().saveDocument();
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            
            @Override
            public boolean equals( Object other ) {
                return other != null && getClass().equals(other.getClass());
            }
        };
    }
    
    private static class XSLTDataNode extends DataNode {
        XSLTDataEditorSupport myEditorSupport;
        
        public XSLTDataNode(XSLTDataObject obj, XSLTDataEditorSupport support) {
            super(obj, Children.LEAF);
            setIconBaseWithExtension(XSLTDataLoaderBeanInfo.PATH_TO_IMAGE);
            myEditorSupport = support;
        }
        
        @Override
        public Action getPreferredAction() {
//139025            return SystemAction.get(XsltDesignViewOpenAction.class);
//            return super.getPreferredAction();//139025
            return SystemAction.get(OpenAction.class);
//            return new AbstractAction() {
//                    private static final long serialVersionUID = 1L;
//            public void actionPerformed(ActionEvent e) {
//                // Fix for #81066
//                if ( myEditorSupport.getOpenedPanes()==null ||
//                        myEditorSupport.getOpenedPanes().length==0 ) 
//                {
//                    myEditorSupport.open();
//                    XsltMultiViewSupport support = 
//                        XsltMultiViewSupport.getInstance();
//                    support.requestViewOpen(myEditorSupport);
//                } else {
//                    myEditorSupport.open();
//                }
//            }
//            };
        }
    }
}
