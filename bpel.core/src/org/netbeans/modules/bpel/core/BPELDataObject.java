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
package org.netbeans.modules.bpel.core;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.transform.Source;

import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.modules.bpel.core.annotations.impl.AnnotationManagerProvider;
import org.netbeans.modules.bpel.core.helper.impl.BusinessProcessHelperImpl;
import org.netbeans.modules.bpel.core.multiview.BpelMultiViewSupport;
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
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;

/**
 * @author ads
 */
public class BPELDataObject extends MultiDataObject {
    
    private static final long serialVersionUID = 1L;
    private static final String ICON_BASE = "org/netbeans/modules/bpel/core/resources/bp_file.gif"; // NOI18N
    private static final String FILE_DESC = "LBL_FileNode_desc"; // NOI18N
    
    public BPELDataObject(final FileObject obj, final MultiFileLoader loader) throws DataObjectExistsException {
        super(obj, loader);
        myEditorSupport = new BPELDataEditorSupport(this);
        
        CookieSet cookies = getCookieSet();
        cookies.add(getEditorSupport());
        
        InputSource is = DataObjectAdapters.inputSource(this);
        cookies.add(new CheckXMLSupport(is));

        Source source = DataObjectAdapters.source(this);
        cookies.add(new TransformableSupport(source));
        cookies.add(new AnnotationManagerProvider(this));

        cookies.assign(SearchProvider.class, new SearchProvider(this));
        cookies.assign(BusinessProcessHelperImpl.class, new BusinessProcessHelperImpl(this));
        cookies.assign(XmlFileEncodingQueryImpl.class, XmlFileEncodingQueryImpl.singleton());

    }
 
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BPELDataObject.class);
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
    public void setModified( boolean modified ) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
//            if ( isLookupInit.get() ) {
//                myServices.get().add(getSaveCookie());
//            }
        }
        else {
            getCookieSet().remove(getSaveCookie());
//            if ( isLookupInit.get() ) {
//                myServices.get().remove( getSaveCookie());
//            }
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
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object other) {
                return other != null && getClass().equals(other.getClass());
            }
        };
    }
    
    @Override
    public final Lookup getLookup() {
        if (myLookup.get() == null) {
            // add lazy initialization
            InstanceContent.Convertor<Class, Object> conv = new InstanceContent.Convertor<Class, Object>() {
                private AtomicReference<Controller> valControllerRef = new AtomicReference<Controller>();
                
                public Object convert(Class obj) {
                    if (obj == BpelModel.class) {
                        return getEditorSupport().getBpelModel();
                    }
                    if (obj == Controller.class) {
                        valControllerRef.compareAndSet(null, new Controller(getEditorSupport().getBpelModel()));
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
                    Lookups.fixed(new Class[] { BpelModel.class, Controller.class }, conv),
                    // Do not call super.getLookup(), it is deadlock-prone!
                    getCookieSet().getLookup()
            );

            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet( false, true );
        }
        return myLookup.get();
    }

    protected Node createNodeDelegate() {
        return new BPELNode( this, getEditorSupport());
    }
    
    private static class BPELNode extends DataNode {

        public BPELNode( BPELDataObject obj, BPELDataEditorSupport support ) {
            super( obj , Children.LEAF );
            myEditorSupport = support;  
            getCookieSet().add(obj);
            
            setIconBaseWithExtension( ICON_BASE );
            setShortDescription(NbBundle.getMessage( getClass(), FILE_DESC ));
        }
        
        public Action getPreferredAction() {
            return new AbstractAction() {
                    private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                // Fix for #81066
                if (myEditorSupport.getOpenedPanes() == null || myEditorSupport.getOpenedPanes().length == 0) {
                    myEditorSupport.open();
                    BpelMultiViewSupport support = BpelMultiViewSupport.getInstance();
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

    private transient BPELDataEditorSupport myEditorSupport;
    private transient AtomicReference<Lookup> myLookup = new AtomicReference<Lookup>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean( false );
}
