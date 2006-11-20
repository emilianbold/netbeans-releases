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
package org.netbeans.core;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ManifestSection;

/** Implements necessary callbacks from module system.
 *
 * @author Jaroslav Tulach
 */
public final class CoreBridgeImpl extends org.netbeans.core.startup.CoreBridge
implements Runnable {
    /** counts the number of CLI invocations */
    private int numberOfCLIInvocations;


    protected void attachToCategory (Object category) {
        ModuleActions.attachTo(category);
    }
    
    protected void loadDefaultSection (
        org.netbeans.core.startup.ManifestSection s, 
        org.openide.util.lookup.InstanceContent.Convertor<ManifestSection,Object> convertor, 
        boolean load
    ) {
        if (load) {
            if (convertor != null) {
                NbTopManager.get().register(s, convertor);
            } else {
                NbTopManager.get().register(s);
            }
        } else {
            if (convertor != null) {
                NbTopManager.get().unregister(s, convertor);
            } else {
                NbTopManager.get().unregister(s);
            }
        }
    }
    
    protected void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception {
        if (load) {
            ModuleActions.add(s);
        } else {
            ModuleActions.remove(s);
        }
    }
    
    protected void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception {
        if (load) {
            LoaderPoolNode.add(s);
        } else {
            LoaderPoolNode.remove((org.openide.loaders.DataLoader)s.getInstance());
        }
    }
    
    protected void loaderPoolTransaction (boolean begin) {
        if (begin) {
            LoaderPoolNode.beginUpdates();
        } else {
            LoaderPoolNode.endUpdates();
        }
    }
    
    public void setStatusText (String status) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(status);
    }

    public void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL) {
          org.netbeans.swing.plaf.Startup.run(uiClass, uiFontSize, themeURL);
    }

    public org.openide.util.Lookup lookupCacheLoad () {
        return LookupCache.load ();
    }
    public void lookupCacheStore (org.openide.util.Lookup l) throws java.io.IOException {
        LookupCache.store (l);
    }

    public void cliUsage(PrintWriter printWriter) {
        // nothing for now
    }

    public int cli(
        String[] string, 
        InputStream inputStream, 
        OutputStream outputStream, 
        OutputStream errorStream, 
        File file
    ) {
        /*
        try {
            org.netbeans.api.sendopts.CommandLine.getDefault().parse(
                string, inputStream, outputStream, file
            );
            for (int i = 0; i < string.length; i++) {
                string[i] = null;
            }
        } catch (CommandException ex) {
            ex.printStackTrace();
            return ex.getExitCode();
        }
         */
        
        if (numberOfCLIInvocations++ == 0) return 0;
        
        /*
        for (int i = 0; i < args.length; i++) {
            if ("--nofront".equals (args[i])) {
                return 0;
            }
        }
         */
        javax.swing.SwingUtilities.invokeLater (this);
        
        return 0;
    }
    
    public void run () {
        java.awt.Frame f = org.openide.windows.WindowManager.getDefault ().getMainWindow ();

        // makes sure the frame is visible
        f.setVisible(true);
        // uniconifies the frame if it is inconified
        if ((f.getExtendedState () & java.awt.Frame.ICONIFIED) != 0) {
            f.setExtendedState (~java.awt.Frame.ICONIFIED & f.getExtendedState ());
        }
        // moves it to front and requests focus
        f.toFront ();
        
    }

    public void registerPropertyEditors() {
        doRegisterPropertyEditors();
    }

    /**Flag to avoid multiple adds of the same path to the
     * of PropertyEditorManager if multiple tests call 
     * registerPropertyEditors() */
    private static boolean editorsRegistered=false;
    /** Register NB specific property editors.
     *  Allows property editor unit tests to work correctly without 
     *  initializing full NetBeans environment.
     *  @since 1.98 */
    private static final void doRegisterPropertyEditors() {
        //issue 31879
        if (editorsRegistered) return;
        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
        String[] nbpesp = new String[] {
            "org.netbeans.beaninfo.editors", // NOI18N
            "org.openide.explorer.propertysheet.editors", // NOI18N
        };
        String[] allpesp = new String[syspesp.length + nbpesp.length];
        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
        PropertyEditorManager.setEditorSearchPath(allpesp);
        PropertyEditorManager.registerEditor (java.lang.Character.TYPE, org.netbeans.beaninfo.editors.CharEditor.class);
        PropertyEditorManager.registerEditor(String[].class, org.netbeans.beaninfo.editors.StringArrayEditor.class); 
        // bugfix #28676, register editor for a property which type is array of data objects
        PropertyEditorManager.registerEditor(org.openide.loaders.DataObject[].class, org.netbeans.beaninfo.editors.DataObjectArrayEditor.class);
        // use replacement hintable/internationalizable primitive editors - issues 20376, 5278
        PropertyEditorManager.registerEditor (Integer.TYPE, org.netbeans.beaninfo.editors.IntEditor.class);
        PropertyEditorManager.registerEditor (Boolean.TYPE, org.netbeans.beaninfo.editors.BoolEditor.class);
        editorsRegistered = true;
    }

    protected void loadSettings() {}

}
