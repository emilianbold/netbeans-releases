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
import java.util.ArrayList;
import java.util.Hashtable;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.options.MIMEOptionFolder;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.RequestProcessor;
import org.netbeans.editor.BaseKit;
import org.openide.loaders.DataObjectNotFoundException;
import java.util.StringTokenizer;
import org.openide.loaders.DataObjectExistsException;


/** Editor Settings main node folder.
 *  In this folder are stored global options such as global keybindings.
 *  Mime options are lazily initialized after loading appropriate kit
 *  (NbEditorKit.java) or after request of Option window to show
 *  the properties.
 *  Initialization starts with loading user's setting from
 *  XML files and then initializer is added to Settings and reseted.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class AllOptionsFolder extends org.openide.loaders.FolderInstance{
    
    /** folder for Editor Settings main node */
    public static final String FOLDER = "Editors";
    public static final String OPTION_FILE_NAME = "Settings.settings";
    
    /** Global Multi Property Folders registration map */
    private static Map globalMPFolder = new HashMap();
    
    /** instance of this class */
    private static AllOptionsFolder settingsFolder;
    
    private static boolean baseInitialized = false;
    
    private static Map subFolders = new Hashtable();
    private static Map defaultKeyBindings;
    private static DataFolder folder;
    private static MIMEOptionFolder mimeFolder;
    
    // List of already initialized options
    private static Map installedOptions = new Hashtable();
    
    
    
    /** Creates new AllOptionsFolder */
    private AllOptionsFolder(DataFolder fld) {
        super(fld);
        folder = fld;
        recreate();
        instanceFinished();
    }
    
    /** Gets the singleton of global options MIME folder */
    public synchronized MIMEOptionFolder getMIMEFolder(){
        if (mimeFolder!=null) return mimeFolder;
        
        FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(FOLDER+"/text/"+BaseOptions.BASE); //NOI18N
        
        // MIME folder doesn't exist, let's create it
        if (f==null){
            FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER);
            String fName = "text/"+BaseOptions.BASE; //NOI18N
            
            if (fo != null){
                try{
                    StringTokenizer stok = new StringTokenizer(fName,"/"); //NOI18N
                    while (stok.hasMoreElements()) {
                        String newFolder = stok.nextToken();
                        if (fo.getFileObject(newFolder) == null)
                            fo = fo.createFolder(newFolder);
                        else
                            fo = fo.getFileObject(newFolder);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
                
                f = TopManager.getDefault().getRepository().getDefaultFileSystem().
                findResource(AllOptionsFolder.FOLDER+"/text/"+BaseOptions.BASE); //NOI18N
            }
        }
        
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                if (df != null) {
                    mimeFolder = new MIMEOptionFolder(df, getBase());
                    return mimeFolder;
                }
            } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        return null;
    }
    
    /** Creates the only instance of AllOptionsFolder. */
    public static synchronized AllOptionsFolder getDefault(){
        // try to find the itutor XML settings
        if (settingsFolder!=null) return settingsFolder;
        org.openide.filesystems.FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(FOLDER);
        if (f==null) return null;
        
        DataFolder df = DataFolder.findFolder(f);
        if (df == null) {
        } else {
            if (settingsFolder == null){
                settingsFolder = new AllOptionsFolder(df);
                return settingsFolder;
            }
        }
        return null;
    }
    
    /** It should creates a new instance of XML files founded in this folder.
     *  But no known XML settings are stored in this folder. */
    protected Object createInstance(InstanceCookie[] cookies)
    throws java.io.IOException, ClassNotFoundException {
        return null;
    }
    
    /** Getter for KeyBingings */
    public List getKeyBindingList() {
        return getBase().getKeyBindingList();
    }
    
    /** Setter for KeyBindings */
    public void setKeyBindingList(List list) {
        getBase().setKeyBindingList(list);
    }
    
    /** Loads default global keyBindings List and initializes it.
     *  It is used mainly by other options for initializing global keyBindings */
    protected void loadDefaultKeyBindings(){
        getBase().getKeyBindingList();
    }
    
    /** Gets the singleton of BaseOptions and register it in Settings initializer,
     * if it wasn't been done before. */
    private BaseOptions getBase(){
        
        BaseOptions ret = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);
        
        synchronized (this){
            if (baseInitialized == false){
                // Add the initializer for the base options. It will not be removed
                Settings.addInitializer(ret.getSettingsInitializer(),
                Settings.OPTION_LEVEL);
                baseInitialized = true;
            }
        }
        
        return ret;
    }
    
    /** Gets the instance of BaseOptions from InstanceCookie */
    protected synchronized BaseOptions getBO(InstanceCookie ic){
        initInstance(ic);
        BaseOptions ret = null;
        try{
            ret = (installedOptions.get(ic.instanceClass()) instanceof BaseOptions) ? (BaseOptions) installedOptions.get(ic.instanceClass())
            : null;
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return ret;
    }
    
    /** Create the instance of appropriate BaseOption subclass */
    private synchronized void initInstance(InstanceCookie ic){
        try{
            if (installedOptions.containsKey(ic.instanceClass())) {
                return;
            }
            Object optionObj = ic.instanceCreate();
            if (!(optionObj instanceof BaseOptions)) return;
            installedOptions.put(ic.instanceClass(), (BaseOptions)optionObj);
            processInitializers((BaseOptions)optionObj, false);
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    /** Lazily inits MIME Option class */
    public synchronized void loadMIMEOption(Class kitClass){
        String contentType = BaseKit.getKit(kitClass).getContentType();
        if (contentType == null) return;
        FileObject optionFO = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(FOLDER+"/"+contentType+"/"+OPTION_FILE_NAME); //NOI18N
        if (optionFO == null) return;
        
        try{
            DataObject optionDO = DataObject.find(optionFO);
            if (optionDO == null) return;
            
            InstanceCookie ic = (InstanceCookie)optionDO.getCookie(InstanceCookie.class);
            if (ic == null) return;
            
            initInstance(ic);
            
        }catch(DataObjectNotFoundException donf){
            donf.printStackTrace();
        }
        
    }
    
    /** Updates MIME option initializer. Loads user's settings stored in XML
     *  files and updates Setting's initializers via reset method */
    private synchronized void processInitializers(BaseOptions bo, boolean remove) {
        
        Settings.Initializer si = bo.getSettingsInitializer();
        // Remove the old one
        Settings.removeInitializer(si.getName());
        if (!remove) { // add the new one
            Settings.addInitializer(si, Settings.OPTION_LEVEL);
        }

        // load all settings of this mime type from XML files
        bo.loadXMLSettings();
        
        /* Reset the settings so that the new initializers take effect
         * or the old are removed. */
        Settings.reset();
    }
    
}
