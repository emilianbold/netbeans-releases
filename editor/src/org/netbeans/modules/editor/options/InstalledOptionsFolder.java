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
