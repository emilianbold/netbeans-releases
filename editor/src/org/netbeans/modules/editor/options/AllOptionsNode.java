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


/** Node representing the Editor Settings main node.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */

public class AllOptionsNode extends FilterNode {
    
    private static AllOptionsNode instance;
    
    /** Creates new AllOptionsNode as BeanNode with Children.Array */
    private AllOptionsNode() throws IntrospectionException {
        super(new BeanNode(AllOptionsFolder.getDefault()), new EditorSubnodes());
    }
    
    /** Gets the default instance of this singleton */
    public static synchronized AllOptionsNode getDefault(){
        if (instance==null){
            try{
                instance = new AllOptionsNode();
            }catch(IntrospectionException ie){
                ie.printStackTrace();
            }
        }
        return instance;
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return NbBundle.getMessage(AllOptionsNode.class, "OPTIONS_all"); //NOI18N
    }
    
    /** Class representing subnodes of Editor Settings node.*/
    private static class EditorSubnodes extends Children.Array {
        
        /** Initialize the collection with results of parsing of all installed MIME type directories */
        protected java.util.Collection initCollection() {
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
                    list.add(bo.getMimeNode());
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
                if (!list.contains(bo.getMimeNode())){
                    if (BaseKit.getKit(bo.getKitClass()).getContentType() != null){
                        list.add(bo.getMimeNode());
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
                }
            }

            return list;
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
    
}
