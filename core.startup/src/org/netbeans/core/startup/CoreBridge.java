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
package org.netbeans.core.startup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.openide.util.Lookup;

/** Interface to environment that the Module system needs around itself.
 *
 * @author Jaroslav Tulach
 */
public abstract class CoreBridge {
    private static CoreBridge fake;
    private static boolean lookupInitialized;

    public static CoreBridge getDefault () {
        CoreBridge b = Lookup.getDefault().lookup(CoreBridge.class);
        if (b == null && lookupInitialized) {
            if (fake == null) {
                fake = new FakeBridge();
            }
            b = fake;
        }
        assert b != null : "Bridge has to be registered"; // NOI18N
        return b;
    }
    
    static void lookupInitialized() {
        lookupInitialized = true;
    }
    
    static void conditionallyLoaderPoolTransaction(boolean begin) {
        CoreBridge b = Lookup.getDefault().lookup(CoreBridge.class);
        if (b != null) {
            b.loaderPoolTransaction(begin);
        }
    }
    static Lookup conditionallyLookupCacheLoad () {
        CoreBridge b = Lookup.getDefault().lookup(CoreBridge.class);
        if (b != null) {
            return b.lookupCacheLoad (); 
        } else {
            return Lookup.EMPTY;
        }
    }
    
    static void conditionallyPrintStatus (String txt) {
        CoreBridge b = Lookup.getDefault().lookup(CoreBridge.class);
        if (b != null) {
            b.setStatusText(txt);
        } else {
            System.err.println(txt);
        }
        
    }
    
    /** Attaches or detaches to current category of actions.
     * @param category name or null
     */
    protected abstract void attachToCategory (Object category);/*
        ModuleActions.attachTo(category);
    */
    
    protected abstract void loadDefaultSection (
        ManifestSection ms, 
        org.openide.util.lookup.InstanceContent.Convertor<ManifestSection,Object> convertor, 
        boolean add
    ); /*
        if (load) {
            if (convert) {
                NbTopManager.get().register(s, convertor);
            } else {
                NbTopManager.get().register(s);
            }
        } else {
            if (convert) {
                NbTopManager.get().unregister(s, convertor);
            } else {
                NbTopManager.get().unregister(s);
            }
        }
    */                                         
    
    protected abstract void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception;/* {
        if (load) {
            ModuleActions.add(s);
        } else {
            ModuleActions.remove(s);
        }
    }
    */
    
    protected abstract void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception;/* {
        if (load) {
            LoaderPoolNode.add(s);
        } else {
            LoaderPoolNode.remove((DataLoader)s.getInstance());
        }
    }
*/
    
    protected abstract void loaderPoolTransaction (boolean begin); /*
        LoaderPoolNode.beginUpdates();
        LoaderPoolNode.endUpdates();
    */

    /** Abstracts away from definition of property editors. 
     * @since 1.7 */
    public abstract void registerPropertyEditors();
    /** Abstracts away from loading of IDESettings.
     * @since 1.7 
     */
    protected abstract void loadSettings();

    public abstract Lookup lookupCacheLoad ();
    public abstract void lookupCacheStore (Lookup l) throws java.io.IOException;
    
    /** Delegates to status displayer.
     */
    public abstract void setStatusText (String status);
    
    public abstract void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL);

    public abstract void cliUsage(PrintWriter printWriter);

    public abstract int cli(
        String[] string, 
        InputStream inputStream, 
        OutputStream outputStream, 
        OutputStream errorStream, 
        File file
    );
    
    
    /** Default implementation of the bridge, so certain
     * applications can run without any bridge being present.
     */
    private static final class FakeBridge extends CoreBridge {
        /** Attaches or detaches to current category of actions.
         * @param category name or null
         */
        protected void attachToCategory (Object category) {

        }

        protected void loadDefaultSection (
            ManifestSection ms, 
            org.openide.util.lookup.InstanceContent.Convertor convertor, 
            boolean add
        ) {
        }

        protected void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception {
            s.getInstance();
        }

        protected void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception {
        }

        protected void loaderPoolTransaction (boolean begin) {
            // just ignore
        }

        protected void addToSplashMaxSteps (int cnt) {
        }
        protected void incrementSplashProgressBar () {
        }

        public Lookup lookupCacheLoad () {
            return Lookup.EMPTY;
        }
        public void lookupCacheStore (Lookup l) throws java.io.IOException {
        }

        public void setStatusText (String status) {
            System.err.println("STATUS: " + status);
        }

        public void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL) {
        }

        public void cliUsage(PrintWriter printWriter) {
        }

        public void registerPropertyEditors() {
        }

        protected void loadSettings() {
        }

        public int cli(String[] string, InputStream inputStream, OutputStream outputStream, OutputStream errorStream, File file) {
            return 0;
        }
    }
    
}
