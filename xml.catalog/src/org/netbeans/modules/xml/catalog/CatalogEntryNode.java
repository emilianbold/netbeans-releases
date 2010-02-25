/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.catalog;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.xml.catalog.lib.URLEnvironment;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.catalog.spi.CatalogWriter;
import org.netbeans.modules.xml.catalog.user.UserXMLCatalog;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.EditAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ViewAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Env;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Node representing single catalog entry. It can be viewed.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
final class CatalogEntryNode extends BeanNode implements EditCookie, Node.Cookie {

    // cached ViewCookie instance
    private transient ViewCookie view;
    private boolean isCatalogWriter;
    private CatalogReader catalogReader;
    
    /** Creates new CatalogNode */
    public CatalogEntryNode(CatalogEntry entry) throws IntrospectionException {        
        super(entry);
        getCookieSet().add(this);
        catalogReader = entry.getCatalog();
        if (catalogReader instanceof CatalogWriter) {
            isCatalogWriter = true;
        }
    }
    
    public boolean isCatalogWriter() {
        return isCatalogWriter;
    }

    public javax.swing.Action getPreferredAction() {
        if (isCatalogWriter) 
            return SystemAction.get(EditAction.class);
        else 
            return SystemAction.get(ViewAction.class);
    }
    
    public void edit() {
        UserXMLCatalog catalog = (UserXMLCatalog)getCatalogReader();
        try {
            java.net.URI uri = new java.net.URI(getSystemID());
            File file = new File(uri);
            FileObject fo = FileUtil.toFileObject(file);
            boolean editPossible=false;
            if (fo!=null) {
                DataObject obj = DataObject.find(fo);
                EditCookie editCookie = (EditCookie)obj.getCookie(EditCookie.class);
                if (editCookie!=null) {
                    editPossible=true;
                    editCookie.edit();
                }
            }
            if (!editPossible)
                org.openide.DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(CatalogEntryNode.class, "MSG_CannotOpenURI",getSystemID()), //NOI18N
                            NotifyDescriptor.INFORMATION_MESSAGE));
        } catch (Throwable ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private CatalogReader getCatalogReader() {
        return catalogReader;
    }
    
    public Action[] getActions(boolean context) {
        if (isCatalogWriter)
            return new Action[] {
                SystemAction.get(EditAction.class),
                SystemAction.get(DeleteAction.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
        else
            return new Action[] {
                SystemAction.get(ViewAction.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
    }
    
    /**
     * Provide <code>ViewCookie</code>. Always provide same instance for
     * entry until its system ID changes.
     */
    public Node.Cookie getCookie(Class clazz) {        
        if (ViewCookie.class.equals(clazz)) {            
            try {
                String sys = getSystemID();
                if (sys == null) return null;
                                
                if (view == null) {                    
                    URL url = new URL(sys);                  
                    ViewEnv env = new ViewEnv(getPublicID(), sys);
                    view = new ViewCookieImpl(env);
                }
                return view;
            } catch (MalformedURLException ex) {
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            } catch (IOException ex) {
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.notify(ErrorManager.INFORMATIONAL, ex);                
                return null;
            }            
        } else {
            return super.getCookie(clazz);
        }
    }

    
    public HelpCtx getHelpCtx() {
        //return new HelpCtx(CatalogEntryNode.class);
        return HelpCtx.DEFAULT_HELP;
    }

    private String getPublicID() {
        return ((CatalogEntry)getBean()).getPublicID();
    }
    
    private String getSystemID() {
        return ((CatalogEntry)getBean()).getSystemID();
    }
    
    public String getShortDescription() {
        String displayName = getPublicID();
        if(displayName.startsWith("SCHEMA:")) //NOI18N
            displayName = displayName.substring("SCHEMA:".length()); //NOI18N
        return displayName;
    }

    public void destroy() throws IOException {
        super.destroy();
        if (isCatalogWriter) {
            CatalogWriter catalogWriter = (CatalogWriter)((CatalogEntry)getBean()).getCatalog();
            catalogWriter.registerCatalogEntry(getPublicID(),null);
        }
    }

    
    /**
     * OpenSupport that is able to open an input stream.
     * Encoding, coloring, ..., let editor kit takes care
     */
    private class ViewCookieImpl extends CloneableEditorSupport implements ViewCookie {

        ViewCookieImpl(Env env) {
            super(env);
        }
                                
        protected String messageName() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_opened_entity", getPublicID());  // NOI18N
        }
        
        protected String messageSave() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_SAVE", getPublicID());  // NOI18N
        }
        
        protected java.lang.String messageToolTip() {
            //return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_TOOLTIP", getSystemID()); // NOI18N
            //return "hello there";
            String publicID = getPublicID();
            if(publicID.startsWith("SCHEMA:")) //NOI18N
                publicID = publicID.substring("SCHEMA:".length()); //NOI18N
            return publicID;
        }

        protected java.lang.String messageOpening() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_OPENING", getPublicID()); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_OPENED", getPublicID()); // NOI18N
        }

        //#20646 associate the entry node with editor top component
        protected CloneableEditor createCloneableEditor() {
            CloneableEditor editor = new CloneableEditor(this) {
                public @Override int getPersistenceType() {
                    return TopComponent.PERSISTENCE_NEVER;
                }
            };
            editor.setActivatedNodes(new Node[] {CatalogEntryNode.this});
            return editor;
        }

    }    
    
    
    // ~~~~~~~~~~~~~~~~~ environment ~~~~~~~~~~~~~~~~~~~

    /**
     * text/xml stream environment.
     */
    private class ViewEnv extends URLEnvironment {

        /** Serial Version UID */
        private static final long serialVersionUID =-5031004511063404433L;
        
        ViewEnv (String publicId, String systemId) {
            super(publicId, systemId);
        }

        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return (ViewCookieImpl) CatalogEntryNode.this.getCookie(ViewCookieImpl.class);
        }
    }
    
}
