/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.netbeans.modules.dbschema.DBElementProvider;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.dbschema.nodes.SchemaRootChildren;

public class DBschemaDataObject extends MultiDataObject {
  
    transient protected SchemaElement schemaElement;
    transient SchemaElementImpl schemaElementImpl;

    public DBschemaDataObject (FileObject pf, DBschemaDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init ();
    }
  
    private void init () {
        CookieSet cookies = getCookieSet ();
        
		cookies.add(new DBElementProvider());
        
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("valid")) //NOI18N
                    if (! isValid())
                        if (schemaElement == null) {
                            schemaElement = SchemaElementUtil.forName(getPrimaryFile());
                            if (schemaElement != null) {
                                SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                                try {
                                    SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
                                } catch (Exception exc) {
                                    if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                        exc.printStackTrace();
                                }
                                schemaElement = null;
                            }
                            return;
                        } else {
                            SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                            try {
                                SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
                            } catch (Exception exc) {
                                if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                    exc.printStackTrace();
                            }
                            schemaElement = null;
                            return;
                        }

                if (event.getPropertyName().equals("primaryFile")) //NOI18N
                    if (schemaElement == null)
                        return;
                    else {
                        SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                        try {
                            SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
                        } catch (Exception exc) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                exc.printStackTrace();
                        }
                        schemaElement = null;
                        getSchema();
                        return;
                    }
            }
        };

        addPropertyChangeListener(listener);
    }

    public Node.Cookie getCookie (Class c) {
        // Looks like a bug - why is it done this way? This inevitable leads to a ClassCastException
        if (SchemaElement.class.isAssignableFrom(c))
            return getCookie(DBElementProvider.class);
        return super.getCookie(c);
    }

    public SchemaElement getSchema() {
        if (schemaElement == null)
            setSchema(SchemaElementUtil.forName(getPrimaryFile()));

        return schemaElement;
    }
  
      public void setSchema(SchemaElement schema) {
        schemaElement = schema;
        Node n = getNodeDelegate();
        Children ch = n.getChildren();
        ((SchemaRootChildren) ch).setElement(schemaElement);
      }

    public SchemaElementImpl getSchemaElementImpl() {
        return schemaElementImpl;
    }

    public void setSchemaElementImpl(SchemaElementImpl schemaImpl) {
        schemaElementImpl = schemaImpl;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }
  
    protected Node createNodeDelegate () {
    	Node nodeDelegate = new DBschemaDataNode(this);
        
        return nodeDelegate;
    }
}
