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

package org.netbeans.modules.editor.options;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.editor.Settings;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.util.Vector;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import java.lang.ClassNotFoundException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.TopManager;
import java.io.IOException;
import org.openide.options.SystemOption;
import org.netbeans.editor.BaseKit;
import org.openide.NotifyDescriptor;
import javax.swing.SwingUtilities;
import java.text.MessageFormat;
import org.netbeans.modules.editor.options.MIMEOptionNode;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.WeakListener;


/** Node representing the Editor Settings main node.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */

public class AllOptionsNode extends FilterNode {
    
    /** Creates new AllOptionsNode as BeanNode with Children.Array */
    public AllOptionsNode() throws IntrospectionException {
        super(new BeanNode(AllOptionsFolder.getDefault()), new EditorSubnodes());
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return NbBundle.getMessage(AllOptionsNode.class, "OPTIONS_all"); //NOI18N
    }

    // #7925
    public boolean canDestroy() {
        return false;
    }        
    
    
    /** Class representing subnodes of Editor Settings node.*/
    private static class EditorSubnodes extends Children.Keys {

        /** Listens to changes on the Modules folder */
        private FileChangeListener moduleRegListener;
        
        /** Constructor.*/
        EditorSubnodes() {
            super();
        }        
        
        private void mySetKeys() {
            setKeys(computeMyKeys());
        }
        
        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }
        
        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        protected void addNotify() {
            mySetKeys();
            
            // listener
            if(moduleRegListener == null) {
                moduleRegListener = new FileChangeAdapter() {
                    public void fileChanged(FileEvent fe){
                        mySetKeys();
                    }
                };
                
                FileObject moduleRegistry = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource("Modules");
                
                if (moduleRegistry !=null){ //NOI18N
                    moduleRegistry.addFileChangeListener(
                    WeakListener.fileChange(moduleRegListener, moduleRegistry ));
                }
            }
        }
       
        
        /** Initialize the collection with results of parsing of all installed MIME type directories */
        private java.util.Collection computeMyKeys() {
            List list = new ArrayList();
            FileObject mainFolderFO = TopManager.getDefault().getRepository().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER+"/text"); //NOI18N
            if (mainFolderFO == null) return list;
            
            DataFolder mainFolder = DataFolder.findFolder(mainFolderFO);
            if (mainFolder == null) return list;
            
            DataObject subFolders[] = mainFolder.getChildren();
            
            for (int i=0; i<subFolders.length; i++){
                if (!(subFolders[i] instanceof DataFolder)) continue;
                
                DataFolder subFolder = (DataFolder) subFolders[i];
                FileObject optionInstance = TopManager.getDefault().getRepository().getDefaultFileSystem().
                findResource(subFolder.getPrimaryFile().getPackageName('/')+"/"+AllOptionsFolder.OPTION_FILE_NAME);
                if (optionInstance == null) continue;
                
                try{
                    DataObject optionDO = DataObject.find(optionInstance);
                    if (optionDO == null) continue;
                    
                    InstanceCookie ic = (InstanceCookie)optionDO.getCookie(InstanceCookie.class);
                    if (ic == null) continue;
                    
                    BaseOptions bo = AllOptionsFolder.getDefault().getBO(ic);
                    if (bo == null) continue;
                    list.add(bo.getClass());
                }catch(DataObjectNotFoundException donf){
                    donf.printStackTrace();
                }
            }
            
            AllOptions allOptions
            = (AllOptions)AllOptions.findObject(AllOptions.class, true);

            if (allOptions == null) return list;
            
            SystemOption[] sos = allOptions.getOptions();    
            if (sos == null) return list;

            for (int i=0; i<sos.length; i++){
                
                if (!(sos[i] instanceof BaseOptions)) continue;
                
                BaseOptions bo = (BaseOptions) sos[i];
                if (!list.contains(bo.getClass())){
                    if (BaseKit.getKit(bo.getKitClass()).getContentType() != null){
                        list.add(bo.getClass());
                        processInitializers(bo, false);
                    }else{
                        final String kitClazz = bo.getKitClass().toString();
                        SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                NotifyDescriptor msg = new NotifyDescriptor.Message(
                                
                                MessageFormat.format(
                                NbBundle.getBundle( AllOptions.class ).getString("ERR_NoContentTypeDefined"),
                                new Object[] {kitClazz}),
                                NotifyDescriptor.WARNING_MESSAGE
                                );
                                
                                TopManager.getDefault().notify(msg);
                            }
                        }
                        );
                    }
                }else{
                    allOptions.removeOption(bo);
                }
            }

            return list;
        }
        
        /** Updates MIME option initializer. Loads user's settings stored in XML
         *  files and updates Setting's initializers via reset method */
        private void processInitializers(BaseOptions bo, boolean remove) {
            
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
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            if(key == null)
                return null;

            if(!(key instanceof Class))
                return null;            
            
            BaseOptions baseOptions
            = (BaseOptions)BaseOptions.findObject((Class)key, true);
            
            if (baseOptions == null) return null;
            
            return new Node[] {baseOptions.getMimeNode()};                
        }
        
    }
    
}
