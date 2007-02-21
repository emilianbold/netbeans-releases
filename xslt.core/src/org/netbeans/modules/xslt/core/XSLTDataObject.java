/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
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
public class XSLTDataObject extends MultiDataObject implements Lookup.Provider {
    
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
        
//        Source source = DataObjectAdapters.source(this);
//        cookies.add(new TransformableSupport(source));
//        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }
    
    public Lookup getLookup() {
        MapperContext mapperContext = getEditorSupport().getMapperContext();
        
        Lookup lookup;
        List<Lookup> list = new LinkedList<Lookup>();
        //TODO m
        if (myLookup.get() == null) {
            if (mapperContext == null) {
                list.add(Lookups.fixed( new Object[]{this,
                getEditorSupport()
                
                
                }));
            } else {
                list.add(Lookups.fixed( new Object[]{this,
                getEditorSupport(),
                mapperContext
                        
                }));
            }
            
            list.add(Lookups.fixed( new Object[]{this ,
            // getEditorSupport() is needed for retrieving Editor Support as PrintProvider.
            // This lookup will be put into Design Nodes, so they will have the same lookup.
            getEditorSupport(),
            // Model is needed by all design. Design is used lookup for accessing to model.
            getEditorSupport().getXslModel()
// TODO a
            // Add Validation Controller.
//                    new XSLTValidationController(getEditorSupport().getXslModel())
            }));
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
            list.add(new AbstractLookup(myServices.get()));
            
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
    
    protected Node createNodeDelegate() {
        return new XSLTDataNode(this);
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
        
        public XSLTDataNode(XSLTDataObject obj) {
            super(obj, Children.LEAF);
            setIconBaseWithExtension(XSLTDataLoaderBeanInfo.PATH_TO_IMAGE);
        }
        
//        public Action getPreferredAction() {
//            return new AbstractAction() {
//                private static final long serialVersionUID = 1L;
//                public void actionPerformed(ActionEvent e) {
//                    // Fix for #81066
//                    if ( myEditorSupport.getOpenedPanes()==null ||
//                            myEditorSupport.getOpenedPanes().length==0 ) {
//                        myEditorSupport.open();
//                        XsltMultiViewSupport support = XsltMultiViewSupport.getInstance();
//                        support.requestViewOpen(myEditorSupport);
//                    } else {
//                        myEditorSupport.open();
//                    }
//                }
//            };
//        }
//
        
    }
    
    private static class Empty {
        
    }
    
    
}
