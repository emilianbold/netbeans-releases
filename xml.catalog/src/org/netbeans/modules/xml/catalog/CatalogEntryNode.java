/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
import org.openide.loaders.*;
import org.openide.*;

import org.netbeans.modules.xml.catalog.lib.*;

/**
 * Node representing single catalog.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
final class CatalogEntryNode extends BeanNode {

    // following cached view is valid 
    // if cached URL is the same as one a new view is requested for
    private String cachedURL;
    private ViewCookie view;
    
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
    
    /**
     * Provide <code>ViewCookie</code>. Always provide same instance for
     * entry until its system ID changes.
     */
    public Node.Cookie getCookie(Class clazz) {
        
        if (ViewCookie.class.equals(clazz)) {
            
            try {
                String sys = getSystemID();
                if (sys == null) return null;
                
                // do not attach the cookie if can not open stream
                // ??? it may block for a while
                URL url = new URL(sys);
                InputStream in = url.openStream();
                
                if (view == null || sys.equals(cachedURL) == false) {
                    MyEnv env = new MyEnv(in);
                    view = new ViewCookieImpl(env);
                    cachedURL = sys;
                }
                return view;                
                
            } catch (MalformedURLException ex) {
                ErrorManager emgr = TopManager.getDefault().getErrorManager();
                emgr.notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            } catch (IOException ex) {
                ErrorManager emgr = TopManager.getDefault().getErrorManager();
                emgr.notify(ErrorManager.INFORMATIONAL, ex);                
                return null;
            }
            
        } else {
            return super.getCookie(clazz);
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
            return getPublicID();
        }
        
        protected String messageSave() {
            return Util.getString ("MSG_ENTITY_SAVE", getPublicID());  // NOI18N
        }
        
        protected java.lang.String messageToolTip() {
            return Util.getString ("MSG_ENTITY_TOOLTIP", getSystemID()); // NOI18N
        }

        protected java.lang.String messageOpening() {
            return Util.getString ("MSG_ENTITY_OPENING", getPublicID()); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return Util.getString ("MSG_ENTITY_OPENED", getPublicID()); // NOI18N
        }
                
    }    
    
    private String getPublicID() {
        return ((CatalogEntry)getBean()).getPublicID();
    }
    
    private String getSystemID() {
        return ((CatalogEntry)getBean()).getSystemID();
    }
    
    // ~~~~~~~~~~~~~~~~~ environment ~~~~~~~~~~~~~~~~~~~

    /**
     * text/xml stream environment.
     */
    private class MyEnv extends StreamEnvironment {

        /** Serial Version UID */
        private static final long serialVersionUID =-5031004511063404433L;
        
        MyEnv (InputStream in) {
            super(in);
        }

        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return (ViewCookieImpl) CatalogEntryNode.this.getCookie(ViewCookieImpl.class);
        }
    }
    
}
