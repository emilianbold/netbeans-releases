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


package org.netbeans.modules.bpel.core;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.transform.Source;
import org.netbeans.modules.bpel.core.annotations.impl.AnnotationManagerProvider;

import org.netbeans.modules.bpel.core.helper.impl.BusinessProcessHelperImpl;
import org.netbeans.modules.bpel.core.multiview.BpelMultiViewSupport;
import org.netbeans.modules.bpel.core.util.BadgedIconCache;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
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
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;

/**
 * @author ads
 */
public class BPELDataObject extends MultiDataObject {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ICON_BASE =
        "org/netbeans/modules/bpel/core/resources/bp_file.gif";         // NOI18N
    
    private static final String FILE_DESC = "LBL_FileNode_desc";        // NOI18N
    
    public BPELDataObject( final FileObject obj, final MultiFileLoader loader )
            throws DataObjectExistsException 
    {
        super(obj, loader);
        
        myEditorSupport = new BPELDataEditorSupport (this);
        
        CookieSet set = getCookieSet();
        set.add( getEditorSupport() );
        
        InputSource in = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(in));
        // add TransformableCookie
        Source source = DataObjectAdapters.source (this);
        set.add (new TransformableSupport (source));
        //set.add(new ValidateXMLSupport(in));
        set.add(new AnnotationManagerProvider(this));
    }
 
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BPELDataObject.class);
    }
    
    @Override
    public void setModified( boolean modified )
    {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
            if ( isLookupInit.get() ) {
                myServices.get().add(getSaveCookie());
            }
        }
        else {
            getCookieSet().remove(getSaveCookie());
            if ( isLookupInit.get() ) {
                myServices.get().remove( getSaveCookie());
            }
        }
    }

    public BPELDataEditorSupport getEditorSupport() {
        return myEditorSupport;
    }
    
    private SaveCookie getSaveCookie() {
        return new SaveCookie() {

            public void save() throws IOException {
                getEditorSupport().saveDocument();
            }

            @Override
            public int hashCode()
            {
                return getClass().hashCode();
            }

            @Override
            public boolean equals( Object other )
            {
                return other != null && getClass().equals(other.getClass());
            }
        };
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Lookup.Provider ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public final Lookup getLookup() {
        if (myLookup.get() == null) {
            
            Lookup lookup;
            List<Lookup> list = new LinkedList<Lookup>();

            list.add(Lookups.fixed( new Object[]{
                    super.getLookup(), 
                    this ,
                    // getEditorSupport() is needed for retrieving Editor Support as PrintProvider.
                    // This lookup will be put into Design Nodes, so they will have the same lookup. 
                    getEditorSupport(),
                    // Model is needed by all design. Design is used lookup for accessing to model.
////                    getEditorSupport().getBpelModel(),
                    // Helper is also needed by design. It used in property editors.
                    new BusinessProcessHelperImpl(this),
                    XmlFileEncodingQueryImpl.singleton()
                    // Add Validation Controller.
////                    new BPELValidationController(getEditorSupport().getBpelModel())
                    }));

            // add lazy initialization
            InstanceContent.Convertor<Class, Object> conv =
                    new InstanceContent.Convertor<Class, Object>() {
                private AtomicReference<BPELValidationController> valControllerRef = 
                        new AtomicReference<BPELValidationController>();
                
                public Object convert(Class obj) {
                    if (obj == BpelModel.class) {
                        return getEditorSupport().getBpelModel();
                    }
                    
                    if (obj == BPELValidationController.class) {
                        valControllerRef.compareAndSet(null, 
                                new BPELValidationController(getEditorSupport().getBpelModel()));
                        return valControllerRef.get();
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
            
            list.add(Lookups.fixed(
                    new Class[] {BpelModel.class, BPELValidationController.class}
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
            list.add(new AbstractLookup(myServices.get()));

            lookup = new ProxyLookup(list.toArray(new Lookup[list.size()]));

            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet( false, true );
        }
        return myLookup.get();
    }

    ////////////////////////////////////////////////////////////////////////////
    //            Node UI (see Decorating Subnodes) 
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * subclasses can override if necessary; FilterNode recommended instead
     */
    protected Node createNodeDelegate() {
        return new BPELNode( this, getEditorSupport());
    }
    
    private static class BPELNode extends DataNode {

        public BPELNode( BPELDataObject obj, BPELDataEditorSupport support ) {
            super( obj , Children.LEAF );
            myEditorSupport = support;  
            
            /* 
             * recomendation from javadoc for createNodeDelegate() that
             * getCookie(DataObject.class) for this class should return obj. 
             */ 
            getCookieSet().add( obj );
            
            setIconBaseWithExtension( ICON_BASE );
            setShortDescription(NbBundle.getMessage( getClass(), FILE_DESC ));
        }
        
        public Action getPreferredAction() {
            return new AbstractAction() {
                    private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                // Fix for #81066
                if ( myEditorSupport.getOpenedPanes()==null ||
                        myEditorSupport.getOpenedPanes().length==0 ) 
                {
                    myEditorSupport.open();
                    BpelMultiViewSupport support = 
                        BpelMultiViewSupport.getInstance();
                    support.requestViewOpen(myEditorSupport);
                }
                else {
                    myEditorSupport.open();
                }
            }
            };
        }
        
        protected boolean isWarning() {
            return false; // TODO - hook in to dataobject
        }

        protected boolean isError() {
            return false; // TODO - hook in to dataobject
        }

        /**
         * to pick up change in warn/error condition call fireIconChange()
         */
        public Image getIcon(int type) {
            if(!isWarning() && !isError())
                return super.getIcon(type);
            else if(isError()) {
                return BadgedIconCache.getErrorIcon(super.getIcon(type));
            }
            else { 
                return BadgedIconCache.getWarningIcon(super.getIcon(type));
            }
        }

        public Image getOpenedIcon(int type) {
            if(!isWarning() && !isError())
                return super.getOpenedIcon(type);
            else if(isError()) {
                return BadgedIconCache.getErrorIcon(super.getOpenedIcon(type));
            }
            else {
                return BadgedIconCache.getWarningIcon(super.getOpenedIcon(type));
            }
        }
    
        public HelpCtx getHelpCtx() {
            return new HelpCtx(BPELDataObject.class);
        }
        
        private BPELDataEditorSupport myEditorSupport;
    }

    private static class Empty {
        
    }
    
    private transient BPELDataEditorSupport myEditorSupport;
    private transient AtomicReference<Lookup> myLookup = 
        new AtomicReference<Lookup>();
    private transient AtomicReference<InstanceContent> myServices = 
        new AtomicReference<InstanceContent>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean( false );
}

