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

import org.openide.cookies.InstanceCookie;
import java.lang.ClassNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import java.io.IOException;
import org.openide.nodes.Node;
import java.beans.IntrospectionException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Document;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import java.io.OutputStream;
import org.openide.filesystems.FileStateInvalidException;
import java.util.ArrayList;
import org.netbeans.editor.MultiKeyBinding;
import java.util.Iterator;


/** Folder for multi properties.
 *  Standard representation of this type is keyBinding folder, where we have to gather
 *  properties from more than one XML file. 
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
abstract class MultiPropertyFolder extends org.openide.loaders.FolderInstance{
    
    
    private Map files = new HashMap();
    protected  BaseOptions base;
    private DataFolder folder;
    private boolean globalFolder = false;
    
    /** Creates new nonGlobal MultiPropertyFolder */    
    public MultiPropertyFolder(DataFolder fld, BaseOptions option) {
        this(fld, option, false);
    }
    
    /** Creates new MultiPropertyFolder */
    public MultiPropertyFolder(DataFolder fld, BaseOptions option, boolean global) {
        super(fld);
        this.folder = fld;
        this.base = option;
        this.globalFolder = global;
        recreate();
        instanceFinished();
    }
    
    
    /** Creates a new instance of XML files.
     *  In this folder are stored global options such as global keybindings.
     */
    protected Object createInstance(InstanceCookie[] cookies)
    throws java.io.IOException, ClassNotFoundException {
        for (int i = 0; i < cookies.length; i++) {

            if (! (cookies[i].instanceCreate() instanceof MIMEProcessor) ) continue;
            
            MIMEProcessor mp = (MIMEProcessor) cookies[i].instanceCreate();
            
            MIMEOptionFile file = mp.createMIMEOptionFile(base, mp);
            file.loadSettings();
            files.put(cookies[i].instanceName(),file);
        }
        return null;
    }

    public String getFolderName(){
        return folder.getName();
    }
    
    abstract List getProperties();
    
    abstract void setProperties(List newProps);
    
    abstract Map getFolderPropertiesMap();
    
    /** Gets all files in this multiPropertyFolder */
    protected Map getFiles(){
        return files;
    }
    
    protected DataFolder getFolder(){
        return folder;
    }

    /** Removes file from local Map */
    private void removeFile(String name){
        files.remove(name);
    }
    
    protected void deleteFile(String delFile){
        final String fileToDelete = delFile;
        try{
            folder.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileObject delFO = folder.getPrimaryFile().getFileObject(fileToDelete, "xml");
                    if( delFO != null){
                        FileLock lock = delFO.lock();
                        try {
                            delFO.delete(lock);
                            removeFile(fileToDelete);
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
    
    protected void createEmptyXMLFiles(List fileName, String tagRoot, String publicID, String systemID){
        
        final List fn = fileName;
        
        final Document doc = XMLUtil.createDocument(tagRoot, null, publicID, systemID);
//        doc.normalize();
        
        try{
            folder.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    for (int i=0; i<fn.size(); i++){
                        
                        if( folder.getPrimaryFile().getFileObject((String)fn.get(i), "xml") != null) continue; //NOI18N
                        
                        // file doesn't exist, create it.
                        FileObject fo = folder.getPrimaryFile().createData((String)fn.get(i), "xml"); // NOI18N
                        FileLock lock = fo.lock();
                        try {
                            OutputStream os = fo.getOutputStream(lock);
                            try {
                                System.out.println("writing");
                                XMLUtil.write(doc, fo.getOutputStream(lock), null);
                                
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
        // wait for the finishing of creating of new files
        instanceFinished();
        
    }
    
    
}
