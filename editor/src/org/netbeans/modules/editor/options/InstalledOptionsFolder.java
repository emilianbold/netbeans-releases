/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.TaskListener;


/** Folder of all installed BaseOptions subClasses, like JavaOptions,
 *  HTMLOptions ...
 *  Options can be initialized by XML layer for example JavaOptions are
 *  initialized via:
 *    <folder name="Editors">
 *       <folder name="Options">
 *           <folder name="Installed">
 *               <file name="org-netbeans-modules-editor-options-JavaOptions.instance">
 *                   <attr name="instanceClass" stringvalue="org.netbeans.modules.editor.options.JavaOptions"/>
 *                   <attr name="instanceCreate" methodvalue="org.netbeans.modules.editor.options.JavaOptions.JavaOptions"/>
 *               </file>
 *           </folder>
 *       </folder>
 *    </folder>
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class InstalledOptionsFolder extends org.openide.loaders.FolderInstance
implements TaskListener{
    
    /** folder for itutor options XML files */
    public static final String FOLDER = "Editors/Options/Installed"; // NOI18N
    
    private static Map globalMPFolder = new HashMap();
    
    /** instance of this class */
    private static InstalledOptionsFolder settingsFolder;
    
    /** Map of installed MIME Options */
    private static Map installedOptions = new Hashtable();
    
    private static PropertyChangeSupport propertySupport;
    
    public static final String INSTALLED_OPTIONS = "installedOptions"; // NOI18N
    
    private static Map installedOld = new HashMap();
    
    /** Creates new InstalledOptionsFolder */
    private InstalledOptionsFolder(DataFolder fld) {
        super(fld);
        propertySupport = new PropertyChangeSupport( this );
        addTaskListener(this);
        recreate();
    }
    
    /** Creates the only instance of InstalledOptionsFolder. */
    public static synchronized InstalledOptionsFolder getDefault(){
        if (settingsFolder!=null) return settingsFolder;
        
        org.openide.filesystems.FileObject f = Repository.getDefault().getDefaultFileSystem().
        findResource(FOLDER);
        if (f==null) return null;
        
        DataFolder df = DataFolder.findFolder(f);
        if (df != null){
            if (settingsFolder == null){
                settingsFolder = new InstalledOptionsFolder(df);
                return settingsFolder;
            }
        }
        return null;
    }
    
    /** Creates a new instance of XML files.
     *  In this folder are stored instances of MIME options like JavaOptions,
     *  HTMLOptions, PlainOptions ... */
    protected Object createInstance(InstanceCookie[] cookies)
    throws java.io.IOException, ClassNotFoundException {
        for (int i = 0; i < cookies.length; i++) {
            System.out.println("installing:"+cookies[i].instanceName()); // NOI18N
            if (!installedOptions.containsKey(cookies[i].instanceName())){
                Object instance = cookies[i].instanceCreate();
                if (!(instance instanceof BaseOptions)){
                    System.out.println("it is not instance of BO !!!"); // NOI18N
                    continue;
                }
                BaseOptions bop = (BaseOptions) instance;
                System.out.println("installed"); // NOI18N
                installedOptions.put(bop.getKitClass(), bop);
            }
        }
        return null;
    }
    
    /** Adds listener to this folder */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    /** Removes listener from this folder */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    /** Some MIME options were added or removed, fire the event */
    public void taskFinished(org.openide.util.Task task) {
        propertySupport.firePropertyChange(INSTALLED_OPTIONS, installedOld, installedOptions);
        installedOld.putAll(installedOptions);
    }
    
}
