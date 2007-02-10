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

package org.netbeans.modules.editor;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseSettingsInitializer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsInitializer;
import org.netbeans.modules.editor.options.OptionUtilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.BasePrintOptions;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettingsInitializer extends Settings.AbstractInitializer {

    private static final Logger LOG = Logger.getLogger(NbEditorSettingsInitializer.class.getName());
    
    public static final String NAME = "nb-editor-settings-initializer"; // NOI18N

    private static final KitsTracker kitsTracker = new KitsTracker();
    
    private static boolean mainInitDone;

    public static void init() {
        if (!mainInitDone) {
            mainInitDone = true;
            Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new NbEditorSettingsInitializer());

            Settings.reset();
            
            // Start listening on addition/removal of print options
            BasePrintOptions bpo = (BasePrintOptions) BasePrintOptions.findObject(BasePrintOptions.class, true);
            bpo.init();
        }
    }

    public NbEditorSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == BaseKit.class) {
            settingsMap.put(BaseOptions.TOOLBAR_VISIBLE_PROP, Boolean.TRUE);
            settingsMap.put(BaseOptions.LINE_NUMBER_VISIBLE_PROP, SettingsDefaults.defaultLineNumberVisible);
            
	    //Fix for IZ bug #53744:
	    //On MAC OS X, Ctrl+left click has the same meaning as the right-click.
	    //The hyperlinking should be enabled for the Command key on MAC OS X, for Ctrl on others:
            int activationMask;
            
            activationMask = Utilities.isMac()? InputEvent.META_MASK: InputEvent.CTRL_DOWN_MASK;
            settingsMap.put(SettingsNames.HYPERLINK_ACTIVATION_MODIFIERS, Integer.valueOf(activationMask));
        }

        if (kitClass == NbEditorKit.class) {
            // init popup menu items from layer folder
            if (AllOptionsFolder.getDefault().baseInitialized()){
                // put to the settings map only if base options has been initialized. See #19470
                settingsMap.put(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
                    OptionUtilities.getPopupStrings(OptionUtilities.getGlobalPopupMenuItems())
                );
            }
        }

        List mimeTypes = kitsTracker.getMimeTypesForKitClass(kitClass);
        for(Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
            String mimeType = (String) i.next();
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Initializing settings for '" + mimeType + "', " + kitClass);
            }
            
            // Lookup BaseOptions for the given mime type so that it can hook up its
            // own settings initializer.
            MimePath mimePath = MimePath.parse(mimeType);
            BaseOptions bo = (BaseOptions) MimeLookup.getLookup(mimePath).lookup(BaseOptions.class);
            if (bo == null) {
                LOG.info("Top level mime type '" + mimeType + "' with no BaseOptions."); //NOI18N
            }
        }
    }

    // TODO: Ideally we should have this in BaseKit and use it from BaseKit.getKit(Class)
    // to load kits through MimeLookup (at least for kits with 1-1 mapping).
    private static final class KitsTracker extends FileChangeAdapter {
        
        // The map of mime type -> kit class
        private final HashMap/*<String, Class>*/ mimeType2kitClass = new HashMap();
        private final ArrayList/*<FileObject>*/ eventSources = new ArrayList();
        private boolean needsReloading = true;
        
        public KitsTracker() {
            
        }
        
        public List getMimeTypesForKitClass(Class kitClass) {
            synchronized (mimeType2kitClass) {
                if (needsReloading) {
                    reload();
                }
                
                ArrayList list = new ArrayList();
                for(Iterator i = mimeType2kitClass.keySet().iterator(); i.hasNext(); ) {
                    String mimeType = (String) i.next();
                    Class clazz = (Class) mimeType2kitClass.get(mimeType);
                    if (kitClass == clazz) {
                        list.add(mimeType);
                    }
                }
                
                return list;
            }
        }

        // ------------------------------------------------------------------
        // FileChangeAdapter
        // ------------------------------------------------------------------
        
        public void fileFolderCreated(FileEvent fe) {
            invalidateCache();
        }

        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }

        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }
        
        // ------------------------------------------------------------------
        // private implementation
        // ------------------------------------------------------------------
        
        private void reload() {
            // Stop listening
            for(Iterator i = eventSources.iterator(); i.hasNext(); ) {
                FileObject fo = (FileObject) i.next();
                fo.removeFileChangeListener(this);
            }
            
            // Clear the cache
            mimeType2kitClass.clear();
            
            // Get the root of the MimeLookup registry
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Editors"); //NOI18N
            
            // Go through mime type types
            FileObject [] types = fo.getChildren();
            for(int i = 0; i < types.length; i++) {
                if (!types[i].isFolder()) {
                    continue;
                }
                
                // Go through mime type subtypes
                FileObject [] subTypes = types[i].getChildren();
                for(int j = 0; j < subTypes.length; j++) {
                    if (!subTypes[j].isFolder()) {
                        continue;
                    }
                    
                    String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N
                    MimePath mimePath = MimePath.parse(mimeType);
                    EditorKit kit = (EditorKit) MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
                    
                    if (kit != null) {
                        String genericMimeType;
                        if (!kit.getContentType().equals(mimeType) && 
                            !(null != (genericMimeType = getGenericPartOfCompoundMimeType(mimeType)) && genericMimeType.equals(kit.getContentType())))
                        {
                            LOG.warning("Inconsistent mime type declaration for the kit: " + kit + //NOI18N
                                "; mimeType from the kit is '" + kit.getContentType() + //NOI18N
                                ", but the kit is registered for '" + mimeType + "'"); //NOI18N
                        }
                        mimeType2kitClass.put(mimeType, kit.getClass());
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("No kit for '" + mimeType + "'");
                        }
                    }
                }
                
                types[i].addFileChangeListener(this);
                eventSources.add(types[i]);
            }
            
            fo.addFileChangeListener(this);
            eventSources.add(fo);
            
            needsReloading = false;
        }

        private void invalidateCache() {
            synchronized (mimeType2kitClass) {
                needsReloading = true;
            }
        }
        
        private static String getGenericPartOfCompoundMimeType(String mimeType) {
            int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
            if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
                int slashIdx = mimeType.indexOf('/'); //NOI18N
                String prefix = mimeType.substring(0, slashIdx + 1);
                String suffix = mimeType.substring(plusIdx + 1);

                // fix for #61245
                if (suffix.equals("xml")) { //NOI18N
                    prefix = "text/"; //NOI18N
                }

                return prefix + suffix;
            } else {
                return null;
            }
        }
    } // End of KitsTracker class
}
