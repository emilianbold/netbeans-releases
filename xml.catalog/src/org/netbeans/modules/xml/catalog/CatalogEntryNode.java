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

import org.netbeans.modules.xml.catalog.lib.*;

/**
 * Node representing single catalog.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CatalogEntryNode extends BeanNode {

    /** Creates new CatalogNode */
    public CatalogEntryNode(String publicID, String systemID) throws IntrospectionException {        
        super(new CatalogEntry(publicID, systemID));
    }
    
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(ViewAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    public Node.Cookie getCookie(Class clazz) {
        
        if (ViewCookie.class.equals(clazz)) {
            
            try {         
                CatalogEntry cat = (CatalogEntry) getBean();
                
                URL url = new URL(cat.getSystemID());
                InputStream in = url.openStream();  //??? encoding, let kit takes care ??? what kit
//                FileObject fo = new StreamFileObject(in);
//                MultiDataObject.Entry entry = new FileEntry.Numb(fo);
                MyEnv env = new MyEnv(in);
                return new ViewCookieImpl(env, cat.getPublicID(), cat.getSystemID());
                
            } catch (MalformedURLException ex) {
                return null;
            } catch (IOException ex) {
                return null;
            }
            
        } else {
            return super.getCookie(clazz);
        }
    }

    
    /**
     * OpenSupport that is able to open an input stream.
     */
    private class ViewCookieImpl extends CloneableEditorSupport implements ViewCookie {
                    
        private String loc;  //name of Entity that will be opened
        private String uri;
        
        ViewCookieImpl(Env env, String id, String uri) {
            super(env);
            loc = id;
            this.uri = uri;
        }
                                
        protected String messageName() {
            return loc;
        }
        
        protected String messageSave() {
            return Util.getString ("MSG_ENTITY_SAVE", loc);  // NOI18N
        }
        
        protected java.lang.String messageToolTip() {
            return Util.getString ("MSG_ENTITY_TOOLTIP", uri); // NOI18N
        }

        protected java.lang.String messageOpening() {
            return Util.getString ("MSG_ENTITY_OPENING", loc); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return Util.getString ("MSG_ENTITY_OPENED", loc); // NOI18N
        }
                
    }    
    
    // ~~~~~~~~~~~~~~~~~ environment ~~~~~~~~~~~~~~~~~~~

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
