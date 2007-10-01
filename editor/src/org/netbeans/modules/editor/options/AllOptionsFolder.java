/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

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
public class AllOptionsFolder{
    
    /** folder for Editor Settings main node */
    public static final String FOLDER = "Editors"; // NOI18N
    public static final String OPTION_FILE_NAME = "Settings.settings"; // NOI18N
    
    /** instance of this class */
    private static AllOptionsFolder settingsFolder;
    
    private static boolean baseInitialized = false;
    
    private static MIMEOptionFolder mimeFolder;
    
    // List of already initialized options
    private static Map installedOptions = new Hashtable();
    
    /** Listens to changes on the Modules folder */
    private static FileChangeListener moduleRegListener;

    
    /** Creates new AllOptionsFolder */
    private AllOptionsFolder() {
    }
    
    /** Gets the singleton of global options MIME folder */
    public MIMEOptionFolder getMIMEFolder(){
        synchronized (Settings.class){
            if (mimeFolder!=null) return mimeFolder;

            FileObject f = Repository.getDefault().getDefaultFileSystem().
            findResource(FOLDER+"/text/"+BaseOptions.BASE); //NOI18N

            // MIME folder doesn't exist, let's create it
            if (f==null){
                FileObject fo = Repository.getDefault().getDefaultFileSystem().
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

                    f = Repository.getDefault().getDefaultFileSystem().
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
    }
    
    /** Returns list of installed Options. Values = options classes */
    public List getInstalledOptions(){
        
        // first XMLized options
        
        List retList = new ArrayList();
        String[] MIMES = new String[] {"text", "application"};  //#25246 application/xml-dtd // NOI18N
        for (int in = 0; in<MIMES.length; in++) {
            FileObject mainFolderFO = Repository.getDefault().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER+"/" + MIMES[in]); //NOI18N
            if (mainFolderFO != null){
                DataFolder mainFolder = DataFolder.findFolder(mainFolderFO);
                if (mainFolder != null){
                    DataObject subFolders[] = mainFolder.getChildren();
                    for (int i=0; i<subFolders.length; i++){
                        if (!(subFolders[i] instanceof DataFolder)) continue;
                        DataFolder subFolder = (DataFolder) subFolders[i];
                        FileObject optionInstance = Repository.getDefault().getDefaultFileSystem().
                            findResource(subFolder.getPrimaryFile().getPath()+"/"+AllOptionsFolder.OPTION_FILE_NAME); // NOI18N
                        if (optionInstance == null) continue;
                        try{
                            DataObject optionDO = DataObject.find(optionInstance);
                            if (optionDO == null) continue;
                            InstanceCookie ic = (InstanceCookie)optionDO.getCookie(InstanceCookie.class);
                            if (ic == null) continue;
                            BaseOptions bo = AllOptionsFolder.getDefault().getBO(ic);
                            if (bo == null) continue;
                            retList.add(bo.getClass());
                        }catch(DataObjectNotFoundException donf){
                            donf.printStackTrace();
                        }
                    }
                }
            }
        }
        
        // Now old SystemOptions options
        AllOptions allOptions
        = (AllOptions)AllOptions.findObject(AllOptions.class, true);
        
        if (allOptions == null) return retList;
        
        SystemOption[] sos = allOptions.getOptions();
        if (sos == null) return retList;
        
        for (int i=0; i<sos.length; i++){
            
            if (!(sos[i] instanceof BaseOptions)) continue;
            
            BaseOptions bo = (BaseOptions) sos[i];
            if (retList.contains(bo.getClass())) retList.remove(bo.getClass());
            if (BaseKit.getKit(bo.getKitClass()).getContentType() != null){
                retList.add(bo.getClass());
                processInitializers(bo, false);
            }else{
                final String kitClazz = bo.getKitClass().toString();
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        NotifyDescriptor msg = new NotifyDescriptor.Message(
                        
                        NbBundle.getMessage( AllOptionsFolder.class, "ERR_NoContentTypeDefined", kitClazz),
                        NotifyDescriptor.WARNING_MESSAGE
                        );
                        
                        org.openide.DialogDisplayer.getDefault().notify(msg);
                    }
                }
                );
            }
        }
        
        return retList;
    }
    
    public static void unregisterModuleRegListener(){
        FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N

        if (moduleRegistry !=null){ //NOI18N
            if (moduleRegListener!=null)
                moduleRegistry.removeFileChangeListener(moduleRegListener);
        }
    }
    
    /** Creates the only instance of AllOptionsFolder. */
    public static AllOptionsFolder getDefault(){
        synchronized (Settings.class) {
            if (settingsFolder == null) {
                settingsFolder = new AllOptionsFolder();

                // attach listeners for module registry for listening on addition or removal of modules in IDE
                if(moduleRegListener == null) {
                    moduleRegListener = new FileChangeAdapter() {
                        public void fileChanged(FileEvent fe){
                            updateOptions();
                        }
                    };

                    FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N

                    if (moduleRegistry !=null){ //NOI18N
                        moduleRegistry.addFileChangeListener(moduleRegListener);
                    }
                }
            }
            
            return settingsFolder;
        }
    }
    
    /** Getter for KeyBingings */
    public List getKeyBindingList() {
        return getBase().getKeyBindingList();
    }
    
    /** Setter for KeyBindings */
    public void setKeyBindingList(List list) {
        getBase().setKeyBindingList(list);
    }
    
    public boolean isToolbarVisible() {
        return getBase().isToolbarVisible();
    }
    
    public void setToolbarVisible(boolean toolbarVisible) {
        getBase().setToolbarVisible(toolbarVisible);
    }

    public boolean getLineNumberVisible(){
        return getBase().getLineNumberVisible();
    }
    
    public void setLineNumberVisible(boolean lineVisible) {
        getBase().setLineNumberVisible(lineVisible);
    }
    
    public boolean isTextAntialiasing() {
        return getBase().isTextAntialiasing();
    }
    
    public void setTextAntialiasing(boolean textAntialiasing) {
        getBase().setTextAntialiasing(textAntialiasing);
    }

    /** Loads default global keyBindings List and initializes it.
     *  It is used mainly by other options for initializing global keyBindings */
    protected void loadDefaultKeyBindings(){
        getBase().getKeyBindingList();
    }
    
    /** Returns kitClass of uninstalled option */ 
    private static Class uninstallOption(){
        List updatedInstalledOptions = AllOptionsFolder.getDefault().getInstalledOptions();
        synchronized (Settings.class){
            Iterator i = installedOptions.keySet().iterator();
            while (i.hasNext()){
                Object obj = i.next();
                if(obj instanceof Class){
                    if (!updatedInstalledOptions.contains(obj)){
                        installedOptions.remove(obj);
                        return (Class)obj;
                    }
                }
            }
            return null;            
        }
    }
    
    private static void updateOptions(){
        uninstallOption();        
        List installedOpts = new ArrayList(installedOptions.values());
        Iterator i = installedOpts.iterator();        
        while (i.hasNext()){
            Object obj = i.next();
            if (obj instanceof BaseOptions){
                BaseOptions bo = (BaseOptions)obj;
                if (bo != null){
                    bo.initPopupMenuItems();
                }
            }
        }
    }
    
    /** Returns true if BaseOptions has been initialized */
    public boolean baseInitialized(){
        return baseInitialized;
    }
    
    /** Gets the singleton of BaseOptions and register it in Settings initializer,
     * if it wasn't been done before. */
    private BaseOptions getBase(){
        
        BaseOptions ret = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);
        
        synchronized (Settings.class){
            if (baseInitialized == false){
                // Add the initializer for the base options. It will not be removed
                Settings.addInitializer(ret.getSettingsInitializer(),
                Settings.OPTION_LEVEL);
                baseInitialized = true;
                Settings.reset();
            }
        }
        
        return ret;
    }
    
    /** Gets the instance of BaseOptions from InstanceCookie */
    protected BaseOptions getBO(InstanceCookie ic){
        initInstance(ic);
        BaseOptions ret = null;
        try{
            synchronized (Settings.class){
                ret = (installedOptions.get(ic.instanceClass()) instanceof BaseOptions) ? (BaseOptions) installedOptions.get(ic.instanceClass())
                : null;
            }
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return ret;
    }
    
    /** Create the instance of appropriate BaseOption subclass */
    private void initInstance(InstanceCookie ic){
        try{
            Object optionObj;
            synchronized (Settings.class){
                if (installedOptions.containsKey(ic.instanceClass())) {
                    return;
                }
                optionObj = ic.instanceCreate();
                if (!(optionObj instanceof BaseOptions)) return;
                installedOptions.put(ic.instanceClass(), (BaseOptions)optionObj);
            }
            processInitializers((BaseOptions)optionObj, false);
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    /** 
     * Lazily inits MIME Option class. Calls <code>loadMIMEOption(kitClass, true)</code>.
     * 
     * @param kitClass The editor kit class you want to load options for.
     * 
     * @deprecated See {@link loadMimeOption(Class, boolean)} for details.
     */
    public void loadMIMEOption(Class kitClass){
        loadMIMEOption(kitClass, true);
    }
    
    /** 
     * Lazily inits MIME Option class. If processOldTypeOption is true initializers 
     * for this option will be processed.
     * 
     * @param kitClass The editor kit class you want to load options for.
     * @param processOldTypeOptions Internal magic, if you really want to call
     *   this method, you should probably set this to <code>true</code>.
     * 
     * @deprecated There is no reason you should call this method. It should have
     *   never been made public. Use <code>MimeLookup.getLookup(MimePath.parse(your-mime-type)).lookup(BaseOptions.class)</code>
     *   for accessing <code>BaseOptions</code> for your mime type.
     */
    public void loadMIMEOption(Class kitClass, boolean processOldTypeOption){
        String contentType = BaseKit.getKit(kitClass).getContentType();
        if (contentType == null) return;
        FileObject optionFO = Repository.getDefault().getDefaultFileSystem().
        findResource(FOLDER+"/"+contentType+"/"+OPTION_FILE_NAME); //NOI18N
        if (optionFO == null) {
            // old type of BaseOptions.
            // Options weren't transfered to XML form for this kitClass yet.
            // We have to find them via BaseOptions.getOptions and process initializers.
            if (processOldTypeOption){
                BaseOptions oldBO = BaseOptions.getOptions(kitClass);
                if (oldBO != null){
                    boolean process = false;
                    synchronized (Settings.class){
                        if (!installedOptions.containsKey(kitClass)){
                            installedOptions.put(kitClass, oldBO);
                            process = true;
                        }
                    }
                    if (process){
                        processInitializers(oldBO, false);
                    }
                }
            }
            return;
        }

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
    private void processInitializers(BaseOptions bo, boolean remove) {
        //synchronized (BaseKit.class){
            synchronized (Settings.class){
                Settings.Initializer si = bo.getSettingsInitializer();
                // Remove the old one
                Settings.removeInitializer(si.getName());
                if (!remove) { // add the new one
                    Settings.addInitializer(si, Settings.OPTION_LEVEL);
                }

                // load all settings of this mime type from XML files
                bo.loadXMLSettings();

                //initialize popup menu
                bo.initPopupMenuItems();

                /* Reset the settings so that the new initializers take effect
                 * or the old are removed. */
                Settings.reset();
            }
        //}
    }
    
}
