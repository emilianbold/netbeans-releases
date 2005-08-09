/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import org.openide.filesystems.FileStateInvalidException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import java.util.List;
import org.openide.filesystems.FileSystem;
import org.w3c.dom.Document;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/** Folder for multi properties.
 *  Standard representation of this type is Popup folder, where we have to gather
 *  properties from more than one file. 
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class MultiPropertyFolder {
    protected  BaseOptions base;
    protected  DataFolder folder;
    /** Creates new MultiPropertyFolder */
    public MultiPropertyFolder(DataFolder fld, BaseOptions option){
        this.base = option;
        this.folder = fld;
    }
    
    /** Gets folder properties */
    List getProperties(){
        List newSettings = new ArrayList();
        DataObject dob[] = folder.getChildren();
        
        for (int i=0; i<dob.length; i++){
            newSettings.add(dob[i]);
        }
        
        return newSettings;
    }
    
    /** Set changed properties to XML files */
    void setProperties(List newProps){
        //[PENDING]
    }
    
    public String getName(){
        return folder.getName();
    }
    
    /** Gets DataFolder that represents this MultiPropertFolder */
    public DataFolder getDataFolder(){
        return folder;
    }
    
    /** Deletes file from multiPropertyFolder */
    protected void deleteFile(final String fileToDelete, final String ext){
        try{
            folder.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileObject delFO = folder.getPrimaryFile().getFileObject(fileToDelete, ext);
                    if( delFO != null){
                        FileLock lock = delFO.lock();
                        try {
                            delFO.delete(lock);
                        } finally {
                            lock.releaseLock();
                        }
                        
                    }
                }
            });
        }catch(FileStateInvalidException fsie){
            fsie.printStackTrace();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Creates the empty XML files with names provided in fileName and given root tag, public and system ID */
    protected void createEmptyXMLFiles(final List fileName, String tagRoot, String publicID, String systemID){
        
        final Document doc = XMLUtil.createDocument(tagRoot, null, publicID, systemID);
        
        try{
            folder.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    for (int i=0; i<fileName.size(); i++){
                        
                        if( folder.getPrimaryFile().getFileObject((String)fileName.get(i), "xml") != null) continue; //NOI18N
                        
                        // file doesn't exist, create it.
                        FileObject fo = folder.getPrimaryFile().createData((String)fileName.get(i), "xml"); // NOI18N
                        FileLock lock = fo.lock();
                        try {
                            OutputStream os = fo.getOutputStream(lock);
                            try {
                                XMLUtil.write(doc, fo.getOutputStream(lock), "UTF-8"); // NOI18N
                            } finally {
                                os.close();
                            }
                        } finally {
                            lock.releaseLock();
                        }
                    }
                }
            });
        }catch(FileStateInvalidException fsie){
            fsie.printStackTrace();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    
}
