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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openide.TopManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderInstance;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.openide.loaders.DataObjectNotFoundException;
import java.lang.ClassNotFoundException;
import org.netbeans.editor.BaseKit;
import org.openide.loaders.DataObjectExistsException;
import java.lang.reflect.Field;


/** MIME Options Folder representation.
 *  Folder maintains MIME specific settings.
 *  The folder contains XML settings files like fontscolors.xml,
 *  abbreviations.xml, macros.xml, properties.xml ...
 *  The folder also contains subFolders like KeyBindings multi property folder.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */

public class MIMEOptionFolder{
    
    private String mime;
    private Map files = new HashMap(5);
    private BaseOptions base;
    private DataFolder folder;
    private Map mpFolderMap = new Hashtable();
    private Map subFolders = new Hashtable();
    
    /** Creates new MIMEOptionFolder */
    public MIMEOptionFolder(DataFolder f, BaseOptions bean){
        folder = f;
        base = bean;
        mime=BaseOptions.BASE.equals(base.getTypeName())? "text/base" : BaseKit.getKit(base.getKitClass()).getContentType(); //NOI18N
        if (mime == null) mime = base.getTypeName();
        loadAllFiles();
    }
    
    /** Creates instance of all founded and recognized XML files */
    protected Object createInstance(InstanceCookie[] cookies)
    throws java.io.IOException, ClassNotFoundException {
        for (int i = 0; i < cookies.length; i++) {
            if ( !(MIMEProcessor.class.isAssignableFrom(cookies[i].instanceClass() ))){
                continue;
            }
            
            MIMEProcessor mp = (MIMEProcessor) cookies[i].instanceCreate();
            if (!files.containsKey(mp.instanceClass())){
                synchronized(this){
                    files.put(
                    mp.instanceClass(),
                    mp.createMIMEOptionFile(base, mp)
                    );
                }
            }
        }
        
        return null;
    }
    
    private void loadAllFiles(){
        DataObject dob[] = folder.getChildren();
        for (int i=0; i<dob.length; i++){
            InstanceCookie ic = (InstanceCookie)dob[i].getCookie(InstanceCookie.class);
            if (ic !=null){
                InstanceCookie instanceCookie[] = new InstanceCookie[]{ic};
                try{
                    createInstance(instanceCookie);
                }catch(ClassNotFoundException cnfe){
                    cnfe.printStackTrace();
                }catch(IOException ioex){
                    ioex.printStackTrace();
                }
            }
        }
    }
    
    /** Gets Multi Property Folder */
    MultiPropertyFolder getMPFolder(String folderName, boolean forceCreation){
        // check local map first
        MultiPropertyFolder mpFolder = (MultiPropertyFolder) mpFolderMap.get(folderName);
        if (mpFolder != null) return mpFolder;
        
        FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(folder.getPrimaryFile().getPackageName('/')+"/"+folderName);
        
        DataFolder subFolder = null;
        
        if(fo!=null)
            subFolder = folder.findFolder(fo);
        
        if (subFolder==null){
            if (forceCreation==false) return null;
            // let's create a DataFolder
            try{
                DataFolder.create(folder,folderName);
                fo = TopManager.getDefault().getRepository().getDefaultFileSystem().
                findResource(folder.getPrimaryFile().getPackageName('/')+"/"+folderName);
                if(fo!=null){
                    subFolder = folder.findFolder(fo);
                }
                if (subFolder==null){
                    return null;
                }
            }catch(IOException ioe){
                return null;
            }
        }
        
        if (KeyBindingsMultiPropertyFolder.FOLDER_NAME.equals(folderName)){
            mpFolder = new KeyBindingsMultiPropertyFolder(subFolder, base);
            mpFolderMap.put(folderName, mpFolder);
            return mpFolder;
        }
        
        return null;
    }
    
    /** Gets Multi Property folder properties
     *  @param folderName the name of multi property subFolder  */
    List getFolderProperties(String folderName){
        MultiPropertyFolder mpFolder = getMPFolder(folderName, false);
        return (mpFolder!=null) ? mpFolder.getProperties() : new ArrayList();
    }
    
    /** Sets Multi Property folder properties
     *  @param folderName the name of multi property subFolder
     *  @param props List of new properties values */
    void setFolderProperties(String folderName, List props){
        MultiPropertyFolder mpFolder = getMPFolder(folderName, true);
        if (mpFolder!=null){
            mpFolder.setProperties(props);
        }
    }

    /** Gets the data folder of this MIMEOptionFolder */
    protected DataFolder getDataFolder(){
        return folder;
    }
    
    /** Gets MIME Option subFolder from this folder
     *  @param subFolder the name of subFolder  */
    protected synchronized MIMEOptionFolder getFolder(String subFolder){

        if (subFolders.get(subFolder) != null){
            return (MIMEOptionFolder)subFolders.get(subFolder);
        }
        
        org.openide.filesystems.FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(folder.getPrimaryFile().getPackageName('/')+"/"+subFolder); // NOI18N
        if (f==null) return null;
        
            try {
                DataObject d = DataObject.find(f);
                DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                if (df != null) {
                    MIMEOptionFolder mof = new MIMEOptionFolder(df, base);
                    subFolders.put(subFolder, mof);
                    return mof;
                }
            } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }

        

        return null;
    }
    
    
    /** Gets MIME specific file located in this folder
     *  @param file Processor file class
     *  @param forceCreation if true, empty XML file will be created
     *  (used for saving the file ). False is used for loading file. */
    protected MIMEOptionFile getFile(Class file, boolean forceCreation){
        
        if (forceCreation && (!files.containsKey(file))){
            String publicID = null;
            String systemID = null;
            String tagRoot  = null;
            String fn  = null;
            
            if (FontsColorsMIMEProcessor.class.isAssignableFrom(file)){
                publicID = FontsColorsMIMEProcessor.PUBLIC_ID;
                systemID = FontsColorsMIMEProcessor.SYSTEM_ID;
                tagRoot  = FontsColorsMIMEOptionFile.TAG_ROOT;
                fn = FontsColorsMIMEOptionFile.FILENAME;
            }
            
            else if (AbbrevsMIMEProcessor.class.isAssignableFrom(file)){
                publicID = AbbrevsMIMEProcessor.PUBLIC_ID;
                systemID = AbbrevsMIMEProcessor.SYSTEM_ID;
                tagRoot  = AbbrevsMIMEOptionFile.TAG_ROOT;
                fn = AbbrevsMIMEOptionFile.FILENAME;
            }
            
            else if (MacrosMIMEProcessor.class.isAssignableFrom(file)){
                publicID = MacrosMIMEProcessor.PUBLIC_ID;
                systemID = MacrosMIMEProcessor.SYSTEM_ID;
                tagRoot  = MacrosMIMEOptionFile.TAG_ROOT;
                fn = MacrosMIMEOptionFile.FILENAME;
            }
            
            else if (KeyBindingsMIMEProcessor.class.isAssignableFrom(file)){
                publicID = KeyBindingsMIMEProcessor.PUBLIC_ID;
                systemID = KeyBindingsMIMEProcessor.SYSTEM_ID;
                tagRoot  = KeyBindingsMIMEOptionFile.TAG_ROOT;
                fn = KeyBindingsMIMEOptionFile.FILENAME;
            }
            
            else if (PropertiesMIMEProcessor.class.isAssignableFrom(file)){
                publicID = PropertiesMIMEProcessor.PUBLIC_ID;
                systemID = PropertiesMIMEProcessor.SYSTEM_ID;
                tagRoot  = PropertiesMIMEOptionFile.TAG_ROOT;
                fn = PropertiesMIMEOptionFile.FILENAME;
            }
            
            else{
                // providing possibility for other MIMEProcessor types
                Object inst = null;

                try{
                    inst = file.newInstance();
                }catch(InstantiationException ie){
                    return null;
                }catch(IllegalAccessException iae){
                    return null;
                }

                // Get rid of unknown processors
                if (!(inst instanceof MIMEProcessor)) return null;

                MIMEProcessor processorInst = (MIMEProcessor)inst;

                publicID = processorInst.getPublicID();
                systemID = processorInst.getSystemID();

                Class mofClass = processorInst.getAsociatedMIMEOptionFile();

                try{
                    Field tagRootField = mofClass.getDeclaredField("TAG_ROOT"); //NOI18N
                    if (tagRootField != null){
                        Object objFld = tagRootField.get(mofClass);
                        if ((objFld != null) && (objFld instanceof String)){
                            tagRoot = (String) objFld;
                        }
                    }

                    Field fnField = mofClass.getDeclaredField("FILENAME"); //NOI18N
                    if (fnField != null){
                        Object objFld = fnField.get(mofClass);
                        if ((objFld != null) && (objFld instanceof String)){
                            fn = (String) objFld;
                        }
                    }
                }catch (Exception exc){
                    return null;
                }
                
            }
            
            if(publicID == null || systemID == null || tagRoot == null || fn == null) return null;
            
            synchronized (this){
                createEmptyXMLFile(fn, tagRoot, publicID, systemID);
            }
        }
        return (MIMEOptionFile)files.get(file);
    }
    
    /** Creates empty XML file with given name, root tag, publis ID and system ID */
    private void createEmptyXMLFile(String fileName, String tagRoot, String publicID, String systemID){

        final String fn = fileName;
        
        final Document doc = XMLUtil.createDocument(tagRoot, null, publicID, systemID);
        
        try{
            final FileObject[] fileObj = new FileObject[1];
            folder.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    if( folder.getPrimaryFile().getFileObject(fn, "xml") != null) return; //NOI18N
                    // file doesn't exist, create it.
                    fileObj[0] = folder.getPrimaryFile().createData(fn, "xml"); // NOI18N
                    FileLock lock = fileObj[0].lock();
                    try {
                        OutputStream os = fileObj[0].getOutputStream(lock);
                        try {
                            XMLUtil.write(doc, os, null);
                            os.flush();                            
                        } finally {
                            os.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
            
            if (fileObj[0]==null) return;
            
            try{
                DataObject dobj = DataObject.find(fileObj[0]);
                if (dobj!=null){
                    InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
                    if (ic !=null){
                        InstanceCookie instanceCookie[] = new InstanceCookie[]{ic};
                        try{
                            createInstance(instanceCookie);
                        }catch(ClassNotFoundException cnfe){
                            cnfe.printStackTrace();
                        }catch(IOException ioex){
                            ioex.printStackTrace();
                        }
                    }
                }
            }catch(DataObjectNotFoundException donf){
                donf.printStackTrace();
            }
            
        }catch(FileStateInvalidException fsie){
            fsie.printStackTrace();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
}

