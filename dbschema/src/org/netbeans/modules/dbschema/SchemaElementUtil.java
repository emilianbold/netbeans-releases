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

package org.netbeans.modules.dbschema;

import java.io.InputStream;
import java.io.ObjectInput;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.netbeans.modules.dbschema.migration.archiver.XMLInputStream;

import org.netbeans.modules.dbschema.util.NameUtil;

public class SchemaElementUtil {

    private static org.openide.filesystems.FileObject schemaFO = null;
    
    /** Returns the SchemaElement object associated with the schema with the given string name.
     * @param name the schema name
     * @return the SchemaElement object for the given schema name
     */
    public static SchemaElement forName(String name) {
        //System.out.println("@@@@LUDOforname="+name);
        SchemaElement se = SchemaElement.getLastSchema();
        
        if (se != null && se.getName().getFullName().equals(name) && schemaFO == null)
            return se;
        else
            synchronized (SchemaElement.schemaCache) {
                String tempURL = ""; //NOI18N
                if (schemaFO != null)
                    try {
                        tempURL = schemaFO.getURL().toString();
                    } catch (Exception exc) {
                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, exc);
                    }
                
                if (schemaFO == null)
                    se = (SchemaElement) SchemaElement.schemaCache.get(name);
                else
                    se = (SchemaElement) SchemaElement.schemaCache.get(name + "#" + tempURL); //NOI18N
                if (se != null)
                    return se;

                org.openide.filesystems.FileObject fo;
                if (schemaFO == null) {
                    org.openide.filesystems.Repository rep = org.openide.filesystems.Repository.getDefault();
                    fo = rep.findResource(NameUtil.getSchemaResourceName(name));
                } else
                    fo = schemaFO;
                    
                if (fo != null) {
                    try {
                        org.openide.loaders.DataObject dataObject = org.openide.loaders.DataObject.find(fo);

                        if (dataObject != null)
                            se = (SchemaElement)dataObject.getCookie(SchemaElement.class);
                    } catch (Exception e) {
                        // just find it by unarchiving (below)
                    }

                    if (se == null) {
                        try {
                            org.openide.awt.StatusDisplayer.getDefault().setStatusText(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("RetrievingSchema")); //NOI18N

                            InputStream s = fo.getInputStream();
                            ObjectInput i = new XMLInputStream(s); 
                            se = (SchemaElement) i.readObject(); 
                            if (!se.isCompatibleVersion()) {
                                String message = MessageFormat.format(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("PreviousVersion"), new String[] {name}); //NOI18N
                                org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(message, org.openide.NotifyDescriptor.ERROR_MESSAGE));
                            }
                            i.close(); 

                            se.setName(DBIdentifier.create(name));

                            if (schemaFO == null)
                                SchemaElement.addToCache(se);
                            else {
                                SchemaElement.schemaCache.put(name + "#" + tempURL, se); //NOI18N
                                SchemaElement.setLastSchema(se);
                            }

                            // MBO: now set the declaring schema in TableElement(transient field)
                            TableElement tables[] = se.getTables();
                            int size = (tables != null) ? tables.length : 0;
                            for (int j = 0; j < size; j++)
                                tables[j].setDeclaringSchema(se);

                            org.openide.awt.StatusDisplayer.getDefault().setStatusText(""); //NOI18N
                        } catch (Exception e) {
                            org.openide.awt.StatusDisplayer.getDefault().setStatusText(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("CannotRetrieve")); //NOI18N
                        }
                    }
                } else
                    org.openide.ErrorManager.getDefault().log(org.openide.ErrorManager.INFORMATIONAL, ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("SchemaNotFound")); //NOI18N

                return se;
            }
    }

    /** Returns the SchemaElement object associated with the schema with the given file object.
     * @param fo the file object
     * @return the SchemaElement object for the given file object
     */
    public static SchemaElement forName(org.openide.filesystems.FileObject fo) {
        schemaFO = fo;
        SchemaElement se = forName(schemaFO.getPackageName('/'));
        schemaFO = null;
        
        return se;
    }
}
