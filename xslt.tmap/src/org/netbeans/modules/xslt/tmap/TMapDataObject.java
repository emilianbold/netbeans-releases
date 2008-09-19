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
package org.netbeans.modules.xslt.tmap;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.transform.Source;
import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.multiview.TMapMultiViewSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapDataObject extends MultiDataObject {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/xslt/tmap/resources/tmap.png";
    private static final String FILE_DESC = "LBL_FileNode_desc"; // NOI18N

    private transient TMapDataEditorSupport myDataEditorSupport;
    private transient AtomicReference<Lookup> myLookup = 
        new AtomicReference<Lookup>();
    
    public TMapDataObject(FileObject pf, TMapDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        myDataEditorSupport = new TMapDataEditorSupport(this);
        
        CookieSet cookies = getCookieSet();
        cookies.add( getEditorSupport() );
        
        // add xsl transform support
        Source source = DataObjectAdapters.source(this);
        cookies.add(new TransformableSupport(source));

        cookies.assign(XmlFileEncodingQueryImpl.class, XmlFileEncodingQueryImpl.singleton());
    }

    /**
     * the name and location of the transformation descriptor 
     * are fixed and shouldn't be changed
     */
    @Override
    public boolean isRenameAllowed() {
        return false;
    }

    /**
     * the name and location of the transformation descriptor 
     * are fixed and shouldn't be changed
     */
    @Override
    public boolean isDeleteAllowed() {
        return false;
    }

    /**
     * the name and location of the transformation descriptor 
     * are fixed and shouldn't be changed
     */
    @Override
    public boolean isMoveAllowed() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TMapDataObject.class);
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
    public void setModified( boolean modified )
    {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
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
    
    @Override
    protected Node createNodeDelegate() {
        return new TMapDataNode(this, getEditorSupport());
    }
    
    @Override
    public Lookup getLookup() {
        if (myLookup.get() == null) {
            // add lazy initialization
            InstanceContent.Convertor<Class, Object> conv = new InstanceContent.Convertor<Class, Object>() {
                private AtomicReference<Controller> valControllerRef = new AtomicReference<Controller>();

                public Object convert(Class obj) {
                    if (obj == TMapModel.class) {
                        return getEditorSupport().getTMapModel();
                    }
                    if (obj == Controller.class) {
                        valControllerRef.compareAndSet(null, new Controller(getEditorSupport().getTMapModel()));
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

            Lookup lookup = new ProxyLookup(
                    // No need to add TMapDataEditorSupport, since already added to the cookie set,
                    // and thus already present in getCookieSet().getLookup().
                    Lookups.fixed(new Class[] { TMapModel.class, Controller.class }, conv),
                    // Do not call super.getLookup(), it is deadlock-prone!
                    getCookieSet().getLookup()
            );

            myLookup.compareAndSet(null, lookup);
        }
        return myLookup.get();
    }
    
    public TMapDataEditorSupport getEditorSupport() {
        return myDataEditorSupport;
    }
    
    private static class TMapDataNode extends DataNode {

        private TMapDataEditorSupport myEditorSupport;

        public TMapDataNode(TMapDataObject obj, TMapDataEditorSupport support) {
            super(obj, Children.LEAF);
            myEditorSupport = support;
            
            getCookieSet().add(obj);
            setIconBaseWithExtension(IMAGE_ICON_BASE);
            setShortDescription(NbBundle.getMessage(TMapDataNode.class, FILE_DESC));
        }
        
        @Override
        public Action getPreferredAction() {
            return new AbstractAction() {
                    private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                // Fix for #81066
                if ( myEditorSupport.getOpenedPanes()==null ||
                        myEditorSupport.getOpenedPanes().length==0 ) 
                {
                    myEditorSupport.open();
                    TMapMultiViewSupport support = 
                        TMapMultiViewSupport.getInstance();
                    support.requestViewOpen(myEditorSupport);
                }
                else {
                    myEditorSupport.open();
                }
            }
            };
        }

        /**
         * the name and location of the transformation descriptor 
         * are fixed and shouldn't be changed
         */
        @Override
        public boolean canRename() {
            return false;
        }

        /**
         * the name and location of the transformation descriptor 
         * are fixed and shouldn't be changed
         */
        @Override
        public boolean canDestroy() {
            return super.canDestroy();
        }

        /**
         * the name and location of the transformation descriptor 
         * are fixed and shouldn't be changed
         */
        @Override
        public boolean canCut() {
            return super.canCut();
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
// TODO a        
////        public Image getIcon(int type) {
////            if(!isWarning() && !isError())
////                return super.getIcon(type);
////            else if(isError()) {
////                return BadgedIconCache.getErrorIcon(super.getIcon(type));
////            }
////            else { 
////                return BadgedIconCache.getWarningIcon(super.getIcon(type));
////            }
////        }
////
////        public Image getOpenedIcon(int type) {
////            if(!isWarning() && !isError())
////                return super.getOpenedIcon(type);
////            else if(isError()) {
////                return BadgedIconCache.getErrorIcon(super.getOpenedIcon(type));
////            }
////            else {
////                return BadgedIconCache.getWarningIcon(super.getOpenedIcon(type));
////            }
////        }
    
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(TMapDataObject.class);
        }
        
        //    /** Creates a property sheet. */
        //    protected Sheet createSheet() {
        //        Sheet s = super.createSheet();
        //        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        //        if (ss == null) {
        //            ss = Sheet.createPropertiesSet();
        //            s.put(ss);
        //        }
        //        // TODO add some relevant properties: ss.put(...)
        //        return s;
        //    }

    }
    
}
