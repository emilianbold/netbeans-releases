/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog;

import java.beans.*;
import java.net.*;
import java.io.*;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.util.actions.*;
import org.openide.util.*;
import org.openide.text.*;
import org.openide.*;

import org.netbeans.modules.xml.catalog.lib.*;

/**
 * Node representing single catalog entry. It can be viewed.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
final class CatalogEntryNode extends BeanNode {

    // cached ViewCookie instance
    private transient ViewCookie view;
    
    /** Creates new CatalogNode */
    public CatalogEntryNode(CatalogEntry entry) throws IntrospectionException {        
        super(entry);
    }
    
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(ViewAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    public SystemAction getDefaultAction() {
        return SystemAction.get(ViewAction.class);
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
                    ViewEnv env = new ViewEnv(url);
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

    
    /**
     * OpenSupport that is able to open an input stream.
     * Encoding, coloring, ..., let editor kit takes care
     */
    private class ViewCookieImpl extends CloneableEditorSupport implements ViewCookie {

        ViewCookieImpl(Env env) {
            super(env);
        }
                                
        protected String messageName() {
            return Util.THIS.getString("MSG_opened_entity", getPublicID());
        }
        
        protected String messageSave() {
            return Util.THIS.getString ("MSG_ENTITY_SAVE", getPublicID());  // NOI18N
        }
        
        protected java.lang.String messageToolTip() {
            return Util.THIS.getString ("MSG_ENTITY_TOOLTIP", getSystemID()); // NOI18N
        }

        protected java.lang.String messageOpening() {
            return Util.THIS.getString ("MSG_ENTITY_OPENING", getPublicID()); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return Util.THIS.getString ("MSG_ENTITY_OPENED", getPublicID()); // NOI18N
        }

        //#20646 associate the entry node with editor top component
        protected CloneableEditor createCloneableEditor() {
            CloneableEditor editor = super.createCloneableEditor();
            editor.setActivatedNodes(new Node[] {CatalogEntryNode.this});
            return editor;
        }

        /**
         * Do not write it down, it is runtime view. #20007
         */
        private Object writeReplace() {
            return null;
        }
                
    }    
    
    
    // ~~~~~~~~~~~~~~~~~ environment ~~~~~~~~~~~~~~~~~~~

    /**
     * text/xml stream environment.
     */
    private class ViewEnv extends URLEnvironment {

        /** Serial Version UID */
        private static final long serialVersionUID =-5031004511063404433L;
        
        ViewEnv (URL url) {
            super(url);
        }

        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return (ViewCookieImpl) CatalogEntryNode.this.getCookie(ViewCookieImpl.class);
        }
    }
    
}
