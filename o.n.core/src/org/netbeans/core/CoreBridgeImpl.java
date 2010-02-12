/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.core;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.core.startup.CoreBridge;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.ManifestSection;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/** Implements necessary callbacks from module system.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=CoreBridge.class)
public final class CoreBridgeImpl extends CoreBridge {

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
                MainLookup.register(s, convertor);
            } else {
                MainLookup.register(s);
            }
        } else {
            if (convertor != null) {
                MainLookup.unregister(s, convertor);
            } else {
                MainLookup.unregister(s);
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
            NbLoaderPool.add(s);
        } else {
            NbLoaderPool.remove((org.openide.loaders.DataLoader)s.getInstance());
        }
    }
    
    protected void loaderPoolTransaction (boolean begin) {
        if (begin) {
            NbLoaderPool.beginUpdates();
        } else {
            NbLoaderPool.endUpdates();
        }
    }
    
    public void setStatusText (String status) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(status);
    }

    public void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL) {
          org.netbeans.swing.plaf.Startup.run(uiClass, uiFontSize, themeURL);
    }

    @SuppressWarnings("deprecation")
    public org.openide.util.Lookup lookupCacheLoad () {
        FileObject services = FileUtil.getConfigFile("Services"); // NOI18N
        if (services != null) {
            StartLog.logProgress("Got Services folder"); // NOI18N
            DataFolder servicesF;
            try {
                servicesF = DataFolder.findFolder(services);
            } catch (RuntimeException e) {
                Exceptions.printStackTrace(e);
                return Lookup.EMPTY;
            }
            org.openide.loaders.FolderLookup f = new org.openide.loaders.FolderLookup(servicesF, "SL["); // NOI18N
            StartLog.logProgress("created FolderLookup"); // NOI18N
            return f.getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }

    public int cli(
        String[] args,
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
        return CLIOptions2.INSTANCE.cli(args);
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
        // use replacement hintable/internationalizable primitive editors - issues 20376, 5278
        PropertyEditorManager.registerEditor (Integer.TYPE, org.netbeans.beaninfo.editors.IntEditor.class);
        PropertyEditorManager.registerEditor (Boolean.TYPE, org.netbeans.beaninfo.editors.BoolEditor.class);
        editorsRegistered = true;
    }

}
