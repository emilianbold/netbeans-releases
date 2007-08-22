/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous
 */
public abstract class OperationsTestImpl extends DefaultTestCase {
    //{fileDataCreated, fileDeleted}
    private Boolean[] fileChanges = {false,false};
    private Thread[] fileChangeThreads = {null,null};
    private Exception[] exceptions = {null,null};
    
    private FileChangeListener fca;
    private FileObject modulesRoot;
    
    private Map<String, ModuleInfo> getModuleInfos () {
        return InstalledModuleProvider.getInstalledModules ();
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        getModuleInfos ();
        Repository.getDefault ().getDefaultFileSystem ().refresh (false);
        modulesRoot = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules"); // NOI18N
        fca = new FileChangeAdapter (){
            @Override
            public void fileDataCreated (FileEvent fe) {
                fileChanges[0] = true;
                fileChangeThreads[0] = Thread.currentThread ();
                exceptions[0] = new Exception ();
            }
            
            @Override
            public void fileDeleted (FileEvent fe) {
                fileChanges[1] = true;
                fileChangeThreads[1] = Thread.currentThread ();
                exceptions[1] = new Exception ();
            }
        };
        modulesRoot.addFileChangeListener (fca);
    }
    
    @Override
    protected void tearDown () throws Exception {
        super.tearDown ();
        if (modulesRoot != null && fca != null) {
            modulesRoot.removeFileChangeListener (fca);
        }
    }
    
    public OperationsTestImpl (String testName) {
        super (testName);
    }
    
    public abstract void testSelf () throws Exception;
    
    //    static List<UpdateUnit> getUpdateUnits() {
    //        UpdateManager mgr = UpdateManager.getDefault();
    //        assertNotNull(mgr);
    //        List<UpdateUnit> retval =  mgr.getUpdateUnits();
    //        assertNotNull(retval);
    //        assertTrue(retval.size() > 0);
    //        return retval;
    //    }
    //
    public UpdateElement installModule (UpdateUnit toInstall, UpdateElement installElement) throws Exception {
        return installModuleImpl (toInstall, installElement, true);
    }
    
    public UpdateElement installNativeComponent (UpdateUnit toInstall, UpdateElement installElement) throws Exception {
        installElement = (installElement != null) ? installElement : toInstall.getAvailableUpdates ().get (0);
        
        assertNotNull (toInstall);
        
        // XXX: assert same could be broken later
        assertSame (toInstall, Utilities.toUpdateUnit (toInstall.getCodeName ()));
        
        InstallSupport.Restarter r = null;
        
        OperationContainer<OperationSupport> container = OperationContainer.createForCustomInstallComponent ();
        OperationContainer.OperationInfo<OperationSupport> info = container.add (installElement);
        assertNotNull (info);
        container.add (info.getRequiredElements ());
        assertEquals (0,container.listInvalid ().size ());
        List<OperationContainer.OperationInfo<OperationSupport>> all =  container.listAll ();
        
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        
        return installElement;
    }
    
    /*public void installModuleDirect(UpdateUnit toInstall) throws Exception {
        installModuleImpl(toInstall, false);
    }*/
    
    
    private UpdateElement installModuleImpl (UpdateUnit toInstall, UpdateElement installElement, final boolean installSupport) throws Exception {
        fileChanges = new Boolean[]{false,false};
        installElement = (installElement != null) ? installElement : toInstall.getAvailableUpdates ().get (0);
        File f = InstallManager.findTargetDirectory(installElement.getUpdateUnit ().getInstalled (), Trampoline.API.impl(installElement),false);
        File configModules = new File (f, "config/Modules");
        File modules = new File (f, "modules");
        int configModulesSize = (configModules.listFiles () != null) ? configModules.listFiles ().length : 0;
        int modulesSize = (modules.listFiles () != null) ? modules.listFiles ().length : 0;
        assertFalse (fileChanges[0]);
        FileObject foConfigModules = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        assertNotNull (foConfigModules);
        int foConfigModulesSize = foConfigModules.getChildren ().length;
        assertNull (getModuleInfos ().get (toInstall.getCodeName ()));
        
        assertNotNull (toInstall);
        
        assertSame (toInstall, Utilities.toUpdateUnit (toInstall.getCodeName ()));
        
        OperationContainer container2 = null;
        InstallSupport.Restarter r = null;
        if (installSupport) {
            OperationContainer<InstallSupport> container = OperationContainer.createForInstall ();
            container2 = container;
            OperationContainer.OperationInfo<InstallSupport> info = container.add (installElement);
            assertNotNull (info);
            container.add (info.getRequiredElements ());
            boolean hiddenUpdate = false;
            for (OperationInfo<InstallSupport> i : container.listAll ()) {
                if (i.getUpdateUnit ().getInstalled () != null) {
                    hiddenUpdate = true;
                    break;
                }
            }
            InstallSupport support = container.getSupport ();
            assertNotNull (support);
            
            InstallSupport.Validator v = support.doDownload (null, false);
            assertNotNull (v);
            InstallSupport.Installer i = support.doValidate (v, null);
            assertNotNull (i);
            assertNull (support.getCertificate (i, installElement)); // Test NBM is not signed nor certificate
            assertFalse (support.isTrusted (i, installElement));
            assertFalse (support.isSigned (i, installElement));
            if (hiddenUpdate) {
                r = support.doInstall (i, null);
                assertNotNull ("If there is hiddenUpdate then returns Restarer.", r);
            } else {
                assertNull ("No Restarer when no hidden update.", support.doInstall (i, null));
            }
        } else {
            OperationContainer<OperationSupport> container = OperationContainer.createForDirectInstall ();
            container2 = container;
            OperationContainer.OperationInfo<OperationSupport> info = container.add (installElement);
            assertNotNull (info);
            container.add (info.getRequiredElements ());
            assertEquals (0,container.listInvalid ().size ());
            List all =  container.listAll ();
            
            OperationSupport support = container.getSupport ();
            assertNotNull (support);
            support.doOperation (null);
        }
        
        assertNotNull (toInstall.getInstalled ());
        
        if (r == null) {
            assertTrue ("Config module files are more than before Install test, " + Arrays.asList (configModules.listFiles ()), configModules.listFiles ().length > configModulesSize);
            assertTrue ("Installed modules are more than before Install test, " + Arrays.asList (modules.listFiles ()), modules.listFiles ().length > modulesSize);
            assertTrue (foConfigModules.getPath (), foConfigModules.getChildren ().length > foConfigModulesSize);
            assertEquals (configModules.listFiles ()[0], FileUtil.toFile (foConfigModules.getChildren ()[0]));
            
            assertTrue (fileChanges[0]);
            fileChanges[0]=false;
        }
        
        //TODO: to know why Thread.sleep(3000) must be present in these tests
        /*if (!Thread.currentThread().equals(fileChangeThreads[0])) {
            exceptions[0].printStackTrace();
        }
        assertEquals(Thread.currentThread(),fileChangeThreads[0]);
         */
        
        fileChangeThreads[0]=null;
        //if (! customInstaller) Thread.sleep(3000);
        List<OperationContainer.OperationInfo> all = container2.listAll ();
        for (OperationContainer.OperationInfo oi : all) {
            UpdateUnit toInstallUnit = oi.getUpdateUnit ();
            if (Trampoline.API.impl (toInstallUnit) instanceof ModuleUpdateUnitImpl) {
                assertInstalledModule (toInstallUnit);
            } else if (Trampoline.API.impl (toInstallUnit) instanceof FeatureUpdateUnitImpl) {
                FeatureUpdateUnitImpl fi = (FeatureUpdateUnitImpl) Trampoline.API.impl (toInstallUnit);
                assertNotNull ("Feature " + toInstallUnit + " is installed now.", fi.getInstalled ());
                FeatureUpdateElementImpl fe = (FeatureUpdateElementImpl) Trampoline.API.impl (fi.getInstalled ());
                for (ModuleUpdateElementImpl m : fe.getContainedModuleElements ()) {
                    assertInstalledModule (m.getUpdateUnit ());
                }
            }
        }
        return installElement;
    }
    
    private void assertInstalledModule (UpdateUnit toInstallUnit) {
        ModuleInfo info = getModuleInfos ().get (toInstallUnit.getCodeName ());
        assertNotNull (info);
        assertTrue (info.getCodeNameBase (), info.isEnabled ());
        assertNotNull (Utilities.toModule (toInstallUnit.getCodeName (), null));
        assertTrue (Utilities.toModule (toInstallUnit.getCodeName (), null).isEnabled ());
    }
    
    final UpdateElement updateModule (UpdateUnit toUpdate) throws Exception {
        return updateModule (toUpdate, true);
    }
    
    final void updateModuleDirect (UpdateUnit toUpdate) throws Exception {
        updateModule (toUpdate, false);
    }
    
    private UpdateElement updateModule (UpdateUnit toUpdate, final boolean installlSupport) throws Exception {
        File configModules = new File (getWorkDir (), "config/Modules");
        File modules = new File (getWorkDir (), "modules");
        assertFalse (fileChanges[0]);
        FileObject foConfigModules = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        assertNotNull (foConfigModules);
        assertTrue (configModules.listFiles () != null && configModules.listFiles ().length != 0);
        assertTrue (modules.listFiles () != null && modules.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toUpdate.getCodeName ()));
        
        assertNotNull (toUpdate);
        
        assertSame (toUpdate, Utilities.toUpdateUnit (toUpdate.getCodeName ()));
        
        UpdateElement upEl =  toUpdate.getAvailableUpdates ().get (0);
        assertNotSame (toUpdate.getInstalled (), upEl);
        
        OperationContainer container2 = null;
        if (installlSupport) {
            OperationContainer<InstallSupport> container = OperationContainer.createForUpdate ();
            container2 = container;
            assertNotNull (container.add (upEl));
            InstallSupport support = container.getSupport ();
            assertNotNull (support);
            
            InstallSupport.Validator v = support.doDownload (null, false);
            assertNotNull (v);
            InstallSupport.Installer i = support.doValidate (v, null);
            assertNotNull (i);
            //assertNotNull(support.getCertificate(i, upEl));
            assertFalse (support.isTrusted (i, upEl));
            assertFalse (support.isSigned (i, upEl));
            support.doInstall (i, null);
        } else {
            OperationContainer<OperationSupport> container = OperationContainer.createForDirectUpdate ();
            container2 = container;
            assertNotNull (container.add (upEl));
            OperationSupport support = container.getSupport ();
            support.doOperation (null);
        }
        //Thread.sleep(3000);
        assertNotNull (toUpdate.getInstalled ());
        assertSame (toUpdate.getInstalled (), upEl);
        // XXX need a separated test, mixing two tests together
        //UpdateUnitProviderFactory.getDefault().refreshProviders();
        UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        assertSame (toUpdate.getInstalled (), upEl);
        UpdateUnit uu = UpdateManagerImpl.getInstance ().getUpdateUnit (toUpdate.getCodeName ());
        assertNotNull (uu);
        assertEquals (toUpdate.toString (), uu.toString ());
        assertTrue ("UpdateUnit before update and after update are equals.", toUpdate.equals (uu));
        assertTrue (toUpdate.getAvailableUpdates ().isEmpty ());
        
        
        List<OperationContainer.OperationInfo> all = container2.listAll ();
        for (OperationContainer.OperationInfo oi : all) {
            UpdateUnit toUpdateUnit = oi.getUpdateUnit ();
            assertNotNull (getModuleInfos ().get (toUpdateUnit.getCodeName ()));
            ModuleInfo info = getModuleInfos ().get (toUpdateUnit.getCodeName ());
            assertNotNull (info);
            assertTrue (info.isEnabled ());
            assertNotNull (Utilities.toModule (toUpdateUnit.getCodeName (), null));
            assertTrue (Utilities.toModule (toUpdateUnit.getCodeName (), null).isEnabled ());
        }
        
        return upEl;
    }
    
    void disableModule (UpdateUnit toDisable) throws Exception {
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        File f = new File (getWorkDir (), "config/Modules");
        File f2 = new File (getWorkDir (), "modules");
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toDisable.getCodeName ()));
        
        assertNotNull (toDisable);
        
        assertSame (toDisable, Utilities.toUpdateUnit (toDisable.getCodeName ()));
        
        OperationContainer<OperationSupport> container = OperationContainer.createForDisable ();
        assertNotNull (container.add (toDisable.getInstalled ()));
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNotNull (toDisable.getInstalled ());
        
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertEquals (1, fo.getChildren ().length);
        assertEquals (f.listFiles ()[0], FileUtil.toFile (fo.getChildren ()[0]));
        
        assertNotNull (getModuleInfos ().get (toDisable.getCodeName ()));
        ModuleInfo info = getModuleInfos ().get (toDisable.getCodeName ());
        assertNotNull (info);
        assertFalse (info.isEnabled ());
        assertNotNull (Utilities.toModule (toDisable.getCodeName (), null));
        assertFalse (Utilities.toModule (toDisable.getCodeName (), null).isEnabled ());
    }
    
    void enableModule (UpdateUnit toEnable) throws Exception {
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        File f = new File (getWorkDir (), "config/Modules");
        File f2 = new File (getWorkDir (), "modules");
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertFalse (fileChanges[0]);
        assertNotNull (getModuleInfos ().get (toEnable.getCodeName ()));
        
        assertNotNull (toEnable);
        
        assertSame (toEnable, Utilities.toUpdateUnit (toEnable.getCodeName ()));
        
        OperationContainer<OperationSupport> container = OperationContainer.createForEnable ();
        assertNotNull (container.add (toEnable.getInstalled ()));
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNotNull (toEnable.getInstalled ());
        
        assertTrue (f.listFiles () != null && f.listFiles ().length != 0);
        assertTrue (f2.listFiles () != null && f2.listFiles ().length != 0);
        assertEquals (1, fo.getChildren ().length);
        assertEquals (f.listFiles ()[0], FileUtil.toFile (fo.getChildren ()[0]));
        
        //Thread.sleep(3000);
        assertNotNull (getModuleInfos ().get (toEnable.getCodeName ()));
        ModuleInfo info = getModuleInfos ().get (toEnable.getCodeName ());
        assertNotNull (info);
        assertTrue (info.isEnabled ());
        assertNotNull (Utilities.toModule (toEnable.getCodeName (), null));
        assertTrue (Utilities.toModule (toEnable.getCodeName (), null).isEnabled ());
    }
    
    
    void unInstallModule (final UpdateUnit toUnInstall) throws Exception {
        File configModules = new File (getWorkDir (), "config/Modules");
        File modules = new File (getWorkDir (), "modules");
        int configModulesSize = (configModules.listFiles () != null) ? configModules.listFiles ().length : 0;
        int modulesSize = (modules.listFiles () != null) ? modules.listFiles ().length : 0;
        FileObject foConfigModules = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        assertNotNull (foConfigModules);
        int foConfigModulesSize = foConfigModules.getChildren ().length;
        
        assertFalse (fileChanges[1]);
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules");
        assertNotNull (fo);
        assertTrue (fo.getChildren ().length > 0);
        assertNotNull (getModuleInfos ().get (toUnInstall.getCodeName ()));
        
        assertNotNull (toUnInstall);
        
        assertSame (toUnInstall, Utilities.toUpdateUnit (toUnInstall.getCodeName ()));
        UpdateElement installElement = toUnInstall.getInstalled();
        OperationContainer<OperationSupport> container = OperationContainer.createForUninstall ();
        OperationContainer.OperationInfo operationInfo = container.add (toUnInstall.getInstalled ());
        assertNotNull (operationInfo);
        operationInfo.getRequiredElements ();
        OperationSupport support = container.getSupport ();
        assertNotNull (support);
        support.doOperation (null);
        assertNull (toUnInstall.getInstalled ());

        if (Trampoline.API.impl(installElement).getInstallInfo ().getTargetCluster () == null) {
            assertTrue (configModules.listFiles ().length < configModulesSize);
            //assertTrue(modules.listFiles().length < modulesSize);
            assertTrue (foConfigModules.getPath (), foConfigModules.getChildren ().length < foConfigModulesSize);
        }
        
        assertTrue (fileChanges[1]);
        fileChanges[1]=false;
        //TODO: to know why Thread.sleep(3000) must be present in these tests
        /*if (!Thread.currentThread().equals(fileChangeThreads[1])) {
            exceptions[1].printStackTrace();
        }
        assertEquals(Thread.currentThread(),fileChangeThreads[1]);
         */
        fileChangeThreads[1]=null;
        
        
        //Thread.sleep(3000);
        ModuleInfo info = getModuleInfos ().get (toUnInstall.getCodeName ());
        assertNull (info);
        assertNull (Utilities.toModule (toUnInstall.getCodeName (), null));
    }
}