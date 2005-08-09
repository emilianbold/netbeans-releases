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

package org.netbeans.modules.editor.options;

import org.openide.loaders.FolderInstance;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.AnnotationType;
import java.util.LinkedList;
import javax.swing.Action;
import org.openide.filesystems.Repository;

/** Processing of folders with annotation types actions.
 *
 * @author  David Konecny
 * @since 08/2001
 */
public class AnnotationTypeActionsFolder extends FolderInstance{
    
    /** root folder for annotation type actions subfolders */
    private static final String FOLDER = "Editors/AnnotationTypes/"; // NOI18N
    
    private AnnotationType type;
    
    /** Creates new AnnotationTypesFolder */
    private AnnotationTypeActionsFolder(AnnotationType type, DataFolder fld) {
        super(fld);
        this.type = type;
        recreate();
        instanceFinished();
    }

    /** Factory method for AnnotationTypeActionsFolder instance. */
    public static boolean readActions(AnnotationType type, String subFolder) {

        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource(FOLDER + subFolder);
        if (f == null) {
            return false;
        }
        
        try {
            DataObject d = DataObject.find(f);
            DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
            if (df != null) {
                AnnotationTypeActionsFolder folder;
                folder = new AnnotationTypeActionsFolder(type, df);
                return true;
            }
        } catch (org.openide.loaders.DataObjectNotFoundException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return false;
        }
        return false;
    }

    /** Called for each XML file found in FOLDER directory */
    protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        LinkedList annotationActions = new LinkedList();

        for (int i = 0; i < cookies.length; i++) {
            Object o = cookies[i].instanceCreate();
            if (o instanceof Action) {
                Action action = (Action)o;
                annotationActions.add(action);
            }
        }
        
        // set all these types to AnnotationType static member
        type.setActions((Action[])annotationActions.toArray(new Action[0]));

        return null;
    }

}
