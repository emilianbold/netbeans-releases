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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 * Fold manager factory provider that obtains the factories
 * by reading the xml layer.
 * <br>
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/FoldManager
 * </pre>
 * For example java fold manager factories should be registered in
 * <pre>
 *     Editors/text/x-java/FoldManager
 * </pre>
 *

 *
 * @author Miloslav Metelka, Martin Roskanin
 */
class LayerProvider extends FoldManagerFactoryProvider {

    private static final String FOLDER_NAME = "FoldManager"; //NOI18N
    
    private Map kit2factoryList;
    
    LayerProvider() {
        kit2factoryList = new WeakHashMap();
    }

    public List getFactoryList(FoldHierarchy hierarchy) {
        List factoryList = null; // result
        JTextComponent editorComponent = hierarchy.getComponent();
        EditorKit kit = editorComponent.getUI().getEditorKit(editorComponent);
        if (kit != null) {
            factoryList = (List)kit2factoryList.get(kit);
            if (factoryList == null) { // not cached yet
                String mimeType = kit.getContentType();
                if (mimeType != null) {
                    MimeLookup mimeLookup = MimeLookup.getMimeLookup(mimeType);
                    factoryList = new ArrayList();
                    factoryList.addAll(mimeLookup.lookup(new Lookup.Template(FoldManagerFactory.class)).allInstances());
                }
            } // not yet cached
        }
        
        if (factoryList == null) {
            return Collections.EMPTY_LIST;
        }
        return factoryList;
    }
    
}
