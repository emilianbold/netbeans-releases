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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.transform.Source;
import org.netbeans.modules.xslt.core.multiview.XsltDesignViewOpenAction;
import org.netbeans.modules.xslt.core.multiview.XsltMultiViewSupport;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
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
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XSLTDataObject extends MultiDataObject {
    
    private static final long serialVersionUID = 1L;
    
//    private transient final DataObjectCookieManager cookieManager;
    private static final String FILE_DESC = "LBL_FileNode_desc";      // NOI18N
    private transient AtomicReference<Lookup> myLookup =
            new AtomicReference<Lookup>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean( false );
    private XSLTDataEditorSupport myDataEditorSupport;
    private transient AtomicReference<InstanceContent> myServices =
            new AtomicReference<InstanceContent>();
    
    
    public XSLTDataObject( final FileObject pf, final MultiFileLoader loader )
    throws DataObjectExistsException {
        super(pf, loader);
        myDataEditorSupport = new XSLTDataEditorSupport(this);
        
        CookieSet cookies = getCookieSet();
        cookies.add( getEditorSupport() );
        
               // add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource (this);
        cookies.add(new CheckXMLSupport (is));
        cookies.add(new ValidateXSLSupport (is));
        
        Source source = DataObjectAdapters.source(this);
        cookies.add(new TransformableSupport(source));
//        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }
    
    public Lookup getLookup() {
        Lookup lookup;
        List<Lookup> list = new LinkedList<Lookup>();
        //TODO m
        if (myLookup.get() == null) {
            
            list.add(Lookups.fixed( new Object[]{
                    super.getLookup(), 
                    this}));            
            
            //
            // add lazy initialization elements
            InstanceContent.Convertor<Class, Object> conv =
                    new InstanceContent.Convertor<Class, Object>() {
// TODO a
//                private AtomicReference<XSLTValidationController> valControllerRef = 
//                        new AtomicReference<XSLTValidationController>();
                
                public Object convert(Class obj) {
                    if (obj == XslModel.class) {
                        return getEditorSupport().getXslModel();
                    }
                    //
                    if (obj == MapperContext.class) {
                        return getEditorSupport().getMapperContext();
                    }
                    //
                    if (obj == XSLTDataEditorSupport.class) {
                        return getEditorSupport();
                    }
                    //
//                    if (obj == XSLTValidationController.class) {
//                        valControllerRef.compareAndSet(null, 
//                                new XSLTValidationController(getEditorSupport().getXslModel()));
//                        return valControllerRef.get();
//                    }
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
            
            list.add(Lookups.fixed(
                    new Class[] {XslModel.class, 
                    MapperContext.class, 
                    XSLTDataEditorSupport.class
                    /*, XSLTValidationController.class*/}
                    , conv));
            //
            
            //
            // WARNING
            // CANNOT add Lookups.singleton(getNodeDelegate()) or will stack
            // overflow
            // WARNING
            //
            
            /*
             * Services are used for push/pop SaveCookie in lookup. This allow to work
             * "Save" action on diagram.
             */
            myServices.compareAndSet( null, new InstanceContent() );
            myServices.get().add( new Empty() );                      // FIX for #IZ78702
            list.add(Lookups.fixed(myServices.get()));
            
            lookup = new ProxyLookup(list.toArray(new Lookup[list.size()]));
            
            // Lookup is now available from this Lookup.Provider but only from this
            // same thread which has the lock on Lookup
            //
            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet( false, true );
        }
        return myLookup.get();
    }
    
    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
            if ( isLookupInit.get() ) {
                myServices.get().add(getSaveCookie());
            }
        } else {
            getCookieSet().remove(getSaveCookie());
            if ( isLookupInit.get() ) {
                myServices.get().remove( getSaveCookie());
            }
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
            return SystemAction.get(XsltDesignViewOpenAction.class);
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
    
    private static class Empty {
        
    }
    
    
}
