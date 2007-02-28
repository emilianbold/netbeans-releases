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

package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vita Stejskal
 */
public final class ToolbarActionsProvider extends ActionsList implements Class2LayerFolder, InstanceProvider {

    private static final String TOOLBAR_ACTIONS_FOLDER_NAME = "Toolbars/Default"; //NOI18N
    private static final String TEXT_BASE_PATH = "Editors/text/base/"; //NOI18N
    
    public static List getToolbarItems(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        ActionsList provider;
        if (mimeType.equals("text/base")) { //NOI18N
            provider = MimeLookup.getLookup(mimePath).lookup(LegacyToolbarActionsProvider.class);
        } else {
            provider = MimeLookup.getLookup(mimePath).lookup(ToolbarActionsProvider.class);
        }
        return provider == null ? Collections.emptyList() : provider.getAllInstances();
    }
    
    public ToolbarActionsProvider() {
        super(null);
    }

    private ToolbarActionsProvider(List keys) {
        super(keys, true);
    }
    
    public Class getClazz(){
        return ToolbarActionsProvider.class;
    }

    public String getLayerFolderName(){
        return TOOLBAR_ACTIONS_FOLDER_NAME;
    }

    public InstanceProvider getInstanceProvider() {
        return new ToolbarActionsProvider();
    }
    
    public Object createInstance(List fileObjectList) {
        return new ToolbarActionsProvider(fileObjectList);
    }
    
    // XXX: This is here to help NbEditorToolbar to deal with legacy code
    // that registered toolbar actions in text/base. The artificial text/base
    // mime type is deprecated and should not be used anymore.
    public static final class LegacyToolbarActionsProvider extends ActionsList implements Class2LayerFolder, InstanceProvider {

        public LegacyToolbarActionsProvider() {
            super(null);
        }

        private LegacyToolbarActionsProvider(List keys) {
            super(keys);
        }

        public Class getClazz(){
            return LegacyToolbarActionsProvider.class;
        }

        public String getLayerFolderName(){
            return TOOLBAR_ACTIONS_FOLDER_NAME;
        }

        public InstanceProvider getInstanceProvider() {
            return new LegacyToolbarActionsProvider();
        }

        public Object createInstance(List fileObjectList) {
            ArrayList<FileObject> textBaseFilesList = new ArrayList<FileObject>();

            for(Object o : fileObjectList) {
                FileObject fileObject = null;

                if (o instanceof DataObject) {
                    fileObject = ((DataObject) o).getPrimaryFile();
                } else if (o instanceof FileObject) {
                    fileObject = (FileObject) o;
                } else {
                    continue;
                }

                String fullPath = fileObject.getPath();
                int idx = fullPath.lastIndexOf(TOOLBAR_ACTIONS_FOLDER_NAME);
                assert idx != -1 : "Expecting files with '" + TOOLBAR_ACTIONS_FOLDER_NAME + "' in the path: " + fullPath; //NOI18N

                String path = fullPath.substring(0, idx);
                if (TEXT_BASE_PATH.equals(path)) {
                    textBaseFilesList.add(fileObject);
                }
            }

            return new LegacyToolbarActionsProvider(textBaseFilesList);
        }
    } // End of LegacyToolbarActionsProvider class
}
