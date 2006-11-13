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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.ui.warmup;

import java.lang.reflect.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;
import java.util.LinkedHashSet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.openide.windows.WindowManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;

/**
 * A menu preheating task. It is referenced from the layer and may be performed
 * by the core after the startup.
 * 
 * Plus hooked WindowListener on main window (see {@link NbWindowsAdapter})
 */
public final class MenuWarmUpTask implements Runnable {

    private Component[] comps;
    
    /** Actually performs pre-heat.
     */
    public void run() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Frame main = WindowManager.getDefault().getMainWindow();
                    
                    assert main != null;
                    main.addWindowListener(new NbWindowsAdapter());
                    
                    if (main instanceof JFrame) {
                        comps = ((JFrame) main).getJMenuBar().getComponents();
                    }
                }
            });
        } catch (Exception e) { // bail out!
            return;
        }


        if (comps != null) {
            walkMenu(comps);
            comps = null;
        }

        // tackle the Tools menu now? How?
    }

    private void walkMenu(Component[] items) {
        for (int i=0; i<items.length; i++) {
            if (! (items[i] instanceof JMenu)) continue;
            try {
                Class cls = items[i].getClass();
                Method m = cls.getDeclaredMethod("doInitialize");
                m.setAccessible(true);
                m.invoke(items[i]);
                walkMenu(((JMenu)items[i]).getMenuComponents()); // recursive?
            } catch (Exception e) {// do nothing, it may happen for user-provided menus
            }
        }
    }

    /**
     * After activation of window is refreshed filesystem   
     */ 
    private static class NbWindowsAdapter extends WindowAdapter implements Runnable{
        private static final RequestProcessor rp = new RequestProcessor ("Refresh-After-WindowActivated");//NOI18N        
        private RequestProcessor.Task task = null;
        
        public void windowActivated(WindowEvent e) {
            synchronized (rp) {
                if (task != null) {
                    task.cancel();
                } else {
                    task = rp.create(this);                    
                }
                task.schedule(1500);
            }
        }

        public void windowDeactivated(WindowEvent e) {
            synchronized (rp) {
                if (task != null) {
                    task.cancel();
                }
            }
        }

        public void run() {         
            FileSystem[] all = getFileSystems();
            for (int i = 0; i < all.length; i++) {
                FileSystem fileSystem = all[i];
                fileSystem.refresh(false);
            }
            
            synchronized (rp) {
                task = null;
            }
        }        
        
        //copy - paste programming
        //http://ant.netbeans.org/source/browse/ant/src-bridge/org/apache/tools/ant/module/bridge/impl/BridgeImpl.java.diff?r1=1.15&r2=1.16
        //http:/java.netbeans.org/source/browse/java/javacore/src/org/netbeans/modules/javacore/Util.java    
        //http://core.netbeans.org/source/browse/core/ui/src/org/netbeans/core/ui/MenuWarmUpTask.java
        //http://core.netbeans.org/source/browse/core/src/org/netbeans/core/actions/RefreshAllFilesystemsAction.java
        //http://java.netbeans.org/source/browse/java/api/src/org/netbeans/api/java/classpath/ClassPath.java
        
        private static FileSystem[] fileSystems;
        
        private static FileSystem[] getFileSystems() {
            if (fileSystems != null) {
                return fileSystems;
            }
            File[] roots = File.listRoots();
            Set<FileSystem> allRoots = new LinkedHashSet<FileSystem>();
            assert roots != null && roots.length > 0 : "Could not list file roots"; // NOI18N
            
            for (int i = 0; i < roots.length; i++) {
                File root = roots[i];
                FileObject random = FileUtil.toFileObject(root);
                if (random == null) continue;
                
                FileSystem fs;
                try {
                    fs = random.getFileSystem();
                    allRoots.add(fs);
                    
                    /*Because there is MasterFileSystem impl. that provides conversion to FileObject for all File.listRoots
                    (except floppy drives and empty CD). Then there is useless to convert all roots into FileObjects including
                    net drives that might cause performance regression.
                    */
                    
                    if (fs != null) {
                        break;
                    }
                } catch (FileStateInvalidException e) {
                    throw new AssertionError(e);
                }
            }
            FileSystem[] retVal = new FileSystem [allRoots.size()];
            allRoots.toArray(retVal);
//            assert retVal.length > 0 : "Could not get any filesystem"; // NOI18N
            
            return fileSystems = retVal;
        }
        
    }
    
}
