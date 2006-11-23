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

package org.netbeans.modules.dbschema;

import java.io.InputStream;
import java.io.ObjectInput;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.filesystems.FileObject;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.dbschema.migration.archiver.XMLInputStream;

import org.netbeans.modules.dbschema.util.NameUtil;
import org.openide.loaders.DataObjectNotFoundException;

public class SchemaElementUtil {

    private static FileObject schemaFO = null;

    /** Returns the SchemaElement object associated with the schema with 
     * the given string name and object.  The second argument is meant to 
     * help define the context for loading of the schema and can be a 
     * FileObject[] or FileObject.  Note that if if FileObject[] is used, 
     * the first match is returned if it's not already in the cache.  
     * It might be extended later to accept a Project as well.  
     * Any other non-null value for the second argument will result in an 
     * UnsupportedOperationException.
     * @param name the schema name
     * @param obj the schema context
     * @return the SchemaElement object for the given schema name
     */
    public static SchemaElement forName(String name, Object obj) {
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

                FileObject fo = null;
                if (schemaFO == null) {
                    if (obj instanceof FileObject) {
                        fo = findResource((FileObject)obj, name);
                    }
                    else if (obj instanceof FileObject[]) {
                        FileObject[] sourceRoots = (FileObject[])obj;

                        for (int i = 0; ((fo == null) && (i < sourceRoots.length)); i++) {
                            fo = findResource(sourceRoots[i], name);
                        }
                    } else if (obj != null) {
                        throw new UnsupportedOperationException(
                            "Cannot lookup schema " + name + 
                            " in context of type " + obj.getClass() + 
                            " expected FileObject, FileObject[], or null.");
                    }
                } else
                    fo = schemaFO;
                if (fo != null) {
                    try {
                        org.openide.loaders.DataObject dataObject = org.openide.loaders.DataObject.find(fo);

                        if (dataObject != null)
                            se = (SchemaElement)dataObject.getCookie(SchemaElement.class);
                    } 
                    catch (ClassCastException e) {
                        // really ugly, caused by faulty code in DBSchemaDataObject.getCookie(...)
                        // just find it by unarchiving (below)
                    }
                    catch (DataObjectNotFoundException e) {
                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
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

                        } catch (Exception e) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
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
    public static SchemaElement forName(FileObject fo) {
        schemaFO = fo;
        SchemaElement se = forName(fo.getName(), null);
        schemaFO = null;
        
        return se;
    }

    private static FileObject findResource(FileObject sourceRoot, String name) {
        ClassPath cp = ClassPath.getClassPath(sourceRoot, ClassPath.SOURCE);
        return cp.findResource(NameUtil.getSchemaResourceName(name));
    }
}
