/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import org.openide.loaders.FolderInstance;
import org.openide.cookies.InstanceCookie;
import java.lang.ClassNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.lang.String;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.AnnotationType;
import java.util.Iterator;

/** Representation of the "Editors/AnnotationTypes" folder. All
 * instances created through the createInstance() method are
 * stored in Map and passed to AnnotationType.setTypes(). This
 * class should only be responsible for processing of the folder, 
 * listening of the changes in folder etc. Clients should use 
 * AnnotationType.getType and other methods in AnnotationType 
 * for access to AnnotationTypes.
 *
 * @author  David Konecny
 * @since 07/2001
 */
public class AnnotationTypesFolder extends FolderInstance{
    
    /** folder for itutor options XML files */
    private static final String FOLDER = "Editors/AnnotationTypes";
    
    /** instance of this class */
    private static AnnotationTypesFolder folder;

    /** map of annotationtype_name <-> AnnotationType_instance*/
    private Map annotationTypes;
    
    /** Creates new AnnotationTypesFolder */
    private AnnotationTypesFolder(DataFolder fld) {
        super(fld);
        recreate();
        instanceFinished();
    }

    /** Gets AnnotationTypesFolder singleton instance. */
    public static synchronized AnnotationTypesFolder getAnnotationTypesFolder(){
        if (folder != null) {
            return folder;
        }
        
        FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource(FOLDER);
        if (f == null) {
            return null;
        }
        
        try {
            DataObject d = DataObject.find(f);
            DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
            if (df != null)
                    folder = new AnnotationTypesFolder(df);
        } catch (org.openide.loaders.DataObjectNotFoundException ex) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                ex.printStackTrace();
            return null;
        }
        return folder;
    }

    /** Called for each XML file found in FOLDER directory */
    protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        annotationTypes = new HashMap(cookies.length * 4 / 3);
        
        for (int i = 0; i < cookies.length; i++) {
            Object o = cookies[i].instanceCreate();
            if (o instanceof AnnotationType) {
                AnnotationType type = (AnnotationType)o;
                annotationTypes.put(type.getName(), type);
            }
        }
        
        // set all these types to AnnotationType static member
        AnnotationType.setTypes(annotationTypes);
        
        return null;
    }

}
