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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.loader;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.multiview.MultiViewSupport;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;

public final class JaxWsDataObject extends MultiDataObject {
    
    private transient JaxWsJavaEditorSupport jes;    
    private transient MultiViewSupport mvc;
    private transient Service service;
    
    public JaxWsDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                createEditorSupport().saveAs( folder, fileName );
            }
        });
    }

    private void lazyInitialize() {
        if(service==null) {
            service = findService();
        }
    }

    private Service findService() {
        FileObject fo = getPrimaryFile();
        Project p = FileOwnerQuery.getOwner(fo);
        if(p==null) return null;
        JaxWsModel model = p.getLookup().lookup(JaxWsModel.class);
        if(model==null) return null;
        ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        String implClass = classPath.getResourceName(fo, '.', false);
        if(implClass==null) return null;
        return model.findServiceByImplementationClass(implClass);
    }

    public @Override Node createNodeDelegate() {
        lazyInitialize();
        return new JaxWsDataNode(this);
    }

    public @Override <T extends Cookie> T getCookie(Class<T> type) {
        if (type.isAssignableFrom(MultiViewSupport.class)) {
            Cookie cake = createMultiViewCookie ();
            if(cake!=null) return type.cast(cake);
        }
        if (type.isAssignableFrom(JaxWsJavaEditorSupport.class)) {
            return type.cast(createEditorSupport ());
        }
        return super.getCookie(type);
    }
    
    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
    }
    
    private synchronized JaxWsJavaEditorSupport createEditorSupport() {
        if (jes == null) {
            jes = new JaxWsJavaEditorSupport (this);
        }
        return jes;
    }            
    
    private synchronized MultiViewSupport createMultiViewCookie() {
        lazyInitialize();
        if (mvc == null) {
            createEditorSupport();
            if(getPrimaryFile().getAttribute("jax-ws-service-provider")==null)
                mvc = new MultiViewSupport(service, this);
        }
        return mvc;
    }            
    
    static class JaxWsDataNode extends DataNode {
        public JaxWsDataNode(DataObject dobj) {
            super(dobj, Children.LEAF);
            setIconBaseWithExtension("org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.gif");
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            //preferred action - open in source mode
            if (type.isAssignableFrom(OpenCookie.class)) {
                return type.cast(((JaxWsDataObject)getDataObject()).createEditorSupport());
            }
            return super.getCookie(type);
        }

        @Override
        public boolean canCopy() {
            return false;
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public boolean canCut() {
            return false;
        }
    }

    public static final class JaxWsJavaEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
        
        private static final class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private final class SaveSupport implements SaveCookie {
                public void save() throws java.io.IOException {
                    ((JaxWsJavaEditorSupport)findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }
            
            public Environment(JaxWsDataObject obj) {
                super(obj);
            }
            
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }
            
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }
            
            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((JaxWsDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }
            
            
            public void addSaveCookie() {
                JaxWsDataObject javaData = (JaxWsDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null)
                        this.saveCookie = new SaveSupport();
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                JaxWsDataObject javaData = (JaxWsDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }
        
        public JaxWsJavaEditorSupport(JaxWsDataObject dataObject) {
            super(dataObject, new Environment(dataObject));
            setMIMEType("text/x-java"); // NOI18N
        }
        
        @Override 
        protected boolean notifyModified() {
            if (!super.notifyModified())
                return false;
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        @Override 
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment)this.env).removeSaveCookie();
        }

        @Override
        protected Pane createPane() {
            MultiViewSupport mvs = ((JaxWsDataObject) getDataObject()).getCookie(MultiViewSupport.class);
            if (mvs == null) return super.createPane();
            return (Pane) mvs.createMultiView();
        }
    
        @Override 
        public boolean close(boolean ask) {
            return super.close(ask);
        }
    }
    
}
