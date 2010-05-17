/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.integration;




import javax.swing.SwingUtilities;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.ide.DefaultSinkManager;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
//import org.netbeans.modules.uml.integration.netbeans.FileSystemInitializer;
import org.netbeans.modules.uml.integration.netbeans.FileSystemListener;
//import org.netbeans.modules.uml.integration.netbeans.NBEventProcessor;
//import org.netbeans.modules.uml.integration.netbeans.NBOperationListener;
//import org.netbeans.modules.uml.integration.netbeans.NBSourceNavigator;



public class IDEIntegrationModule  extends ModuleInstall
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final int CLOSE_UNINSTALL  = 0;
    /**
     *  The id of NetBeans' Save All action, which we need to override to
     *  save the workspace as well.
     */
    
    private static final String SAVE_ALL_ID =
        "org-openide-actions-SaveAllAction";
    /* NB60TBD
    private static NBEventProcessor eventProcessor =
        new NBEventProcessor();
    */
    public void init()
    {
//        initializeSinkManager();
//        attachRoundtripListeners();
//        intializeNBListeners();
//        intializeJMI();
    }
    
//    private void attachRoundtripListeners()
//    {
//        UMLSupport.getUMLSupport().setAttributeChangeListener(
//            eventProcessor);
//        
//        UMLSupport.getUMLSupport().setClassChangeListener(eventProcessor);
//        
//        UMLSupport.getUMLSupport().setOperationChangeListener(
//            eventProcessor);
//        
//        UMLSupport.getUMLSupport().setEnumLiteralChangeListener(
//            eventProcessor);
//    }
    
//    private void initializeSinkManager()
//    {
//        DefaultSinkManager sinkMan =
//            new DefaultSinkManager(
//            UMLSupport.getProduct().getEventDispatchController());
//        
//        UMLSupport.getUMLSupport().initializeSinkManager(sinkMan);
//        sinkMan.setSourceNavigator(new NBSourceNavigator());
//        sinkMan.initializeAll();
//        
//    }
    
//    private void intializeNBListeners()
//    {
//        SwingUtilities.invokeLater(FileSystemInitializer.
//            getFileSystemInitializer());
//        
//        DataLoaderPool pool = 
//            (DataLoaderPool) Lookup.getDefault().lookup(DataLoaderPool.class);
//        
//        pool.addOperationListener(new NBOperationListener(pool));
//        FileSystemListener.getInstance().addTo(Repository.getDefault());
//    }
    
//    private void intializeJMI()
//    {
//        IMDRListener lsn = new MDRListener();
//        MDREventProcessor pro = MDREventProcessor.getInstance();
//        pro.addListener(lsn);
//    }
    
//    private void initializeProduct()
//    {
//        
//    }
    
//    @Override
//    protected boolean clearSharedData() {
//        // TODO Auto-generated method stub
//        return super.clearSharedData();
//    }
//
//    @Override
//    public void close() {
//        // TODO Auto-generated method stub
//        super.close();
//    }
//
//    @Override
//    public boolean closing() {
//        // TODO Auto-generated method stub
//        return super.closing();
//    }
//
//    @Override
    public void restored()
    {
        // TODO Auto-generated method stub
        super.restored();
//		installMenus();
        init();
    }
    
    /**
     * Installs the menu items used by Describe integration.  If the menu items
     * already exist in NetBeans then the menu items are not installed again.
     */
//    protected void installMenus() {
//        Log.entry("Entering function DescribeModule::installMenus");
//
//        try {
//            DataFolder menuRoot = NbPlaces.getDefault().menus();
//            DataFolder toolBarRoot  = NbPlaces.getDefault().toolbars().getFolder();
//
////            // Install View Menu Items
//            DataFolder fileMenu = getFolder(menuRoot, "File");
//            DataFolder systemActions = getFolder(toolBarRoot, "Toolbars");
//			DataFolder sActions = getFolder(systemActions, "File");
//            updateFileMenu(fileMenu);
//
//			if (sActions != null)
//                swapAction(sActions, SAVE_ALL_ID, NBSaveAllAction.ID,
//                NBSaveAllAction.class);
//
//        } catch (Exception e) {
//            Log.stackTrace(e);
//        }
//    }
    
//    protected DataFolder getFolder(DataFolder root, String name)
//    {
//        Log.entry("Entering function DescribeModule::getFolder");
//        
//        DataFolder retVal = null;
//        
//        DataObject[] children = root.getChildren();
//        for(int i = 0; i < children.length; i++)
//        {
//            if(name.equals(children[i].getName()) == true)
//            {
//                if(children[i] instanceof DataFolder)
//                    retVal = (DataFolder)children[i];
//            }
//        }
//        
//        return retVal;
//    }
    
//    protected boolean swapAction(DataFolder folder, String oldId, String newId,
//		    Class newClass) {
//		        DataObject[] children = folder.getChildren();
//		        boolean rep = false;
//		        Log.out("swapAction: Looking to swap " + oldId + " with " + newId
//		        + " in folder " + folder.getName());
//		        for (int i = 0; i < children.length; ++i) {
//		            Log.out("swapAction: Examining child name " + children[i].getName());
//		            if (children[i].getName().equals(oldId)) {
//		                Log.out("swapAction: Replacing " + children[i].getName() + " with "
//		                + newId);
//		                InstanceDataObject kid = (InstanceDataObject) children[i];
//		                try {
//		                    // Swap the CallableSystemAction here with our own. Grmpf.
//		                    InstanceDataObject ido = InstanceDataObject.create(folder,
//		                    newId,
//		                    newClass);
//		                    try {
//		                        kid.delete();
//		                    } catch (Exception e) {
//		                        Log.stackTrace(e);
//		                    }
//		                    children[i] = ido;
//		                    rep = true;
//		                    break;
//		                } catch (Exception e) {
//		                    Log.stackTrace(e);
//		                    return false;
//		                }
//		            }
//		        }
//		        if (rep) {
//		            try {
//		                folder.setOrder(children);
//		            } catch (Exception e) {
//		                Log.stackTrace(e);
//		                return false;
//		            }
//		        }
//		        return rep;
//	}
    
    /**
     * Update the file menu. Here we overwrite the Save and SaveAll action of the editor
     */
//    private void updateFileMenu(DataFolder fileMenu) throws IOException {
//        if (fileMenu == null)
//            return;
//
//        InstanceDataObject saveAll = getIDO(fileMenu, SAVE_ALL_ID);
//        Log.out("installMenus: Found saveAll action: " + saveAll);
//
//		if (saveAll != null) {
//            try {
//                NBSaveAllAction.setDefaultSaveAllAction(
//                saveAll.instanceClass());
//            }
//            catch (Exception e) {
//                //ignore
//                Log.stackTrace(e);
//            }
//            Log.out("installMenus: Swapping save all action with NBSaveAllAction");
//            swapAction(fileMenu, SAVE_ALL_ID,
//            NBSaveAllAction.ID,
//            NBSaveAllAction.class);
//        }
//    } //end of updateFileMenu
    
    protected InstanceDataObject getIDO(DataFolder folder, String id)
    {
        DataObject[] children = folder.getChildren();
        for (int i = 0; i < children.length; ++i)
        {
            if (children[i].getName().equals(id))
                return (InstanceDataObject) children[i];
        }
        return null;
    }
    
    public void uninstalled()
    {
        uninstalled(CLOSE_UNINSTALL);
    }
    
    public void uninstalled(int mode)
    {
        Log.entry("Entering function DescribeModule::uninstalled");
        
        //NBOperationListener.removeListeners();
//        ResourceBundle bundle = NbBundle.getBundle(DescribeModule.class);
        
//        removeMenus();
    }
    
//    protected void removeMenus() {
//        Log.entry("Entering function DescribeModule::removeMenus");
//
//        try {
//            DataFolder menuRoot = NbPlaces.getDefault().menus();
//            DataFolder toolbarRoot  = NbPlaces.getDefault().toolbars();
//
//            // Install View Menu Items
//            DataFolder fileMenu = getFolder(menuRoot, "File");
//            DataFolder actions = getFolder(toolbarRoot, "System");
//
//            if(fileMenu != null) {
//				if (NBSaveAllAction.getDefaultSaveAllAction() != null)
//					swapAction(fileMenu, NBSaveAllAction.ID, SAVE_ALL_ID,
//							NBSaveAllAction.getDefaultSaveAllAction());
//			}
//
//			if (actions != null && NBSaveAllAction.getDefaultSaveAllAction() != null)
//                swapAction(actions, NBSaveAllAction.ID, SAVE_ALL_ID,
//                NBSaveAllAction.getDefaultSaveAllAction());
//        }
//        catch(Exception ioE) {
//        }
//    }
    
//    @Override
//    public void uninstalled() {
//        // TODO Auto-generated method stub
//        super.uninstalled();
//    }
//
//    @Override
//    public void validate() throws IllegalStateException {
//        // TODO Auto-generated method stub
//        super.validate();
//    }

    /* NB60TBD
    public static NBEventProcessor getEventProcessor()
    {
        return eventProcessor;
    }
    */
    
    
}
