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

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import java.util.List;
import java.util.ArrayList;


/**
 * @author  Martin Roskanin
 */
public class PopupMultiPropertyFolder extends MultiPropertyFolder{
    
    private boolean settingsLoaded = false;
    
    public final static String FOLDER_NAME = "Popup"; //NOI18N
    

    public PopupMultiPropertyFolder(DataFolder fld, BaseOptions option) {
        super(fld, option);
    }
    
    
    /** Gets folder properties */
    List getProperties(){
        List newSettings = new ArrayList();
        DataObject dob[] = folder.getChildren();
        
        for (int i=0; i<dob.length; i++){
            newSettings.add(dob[i]);
        }
        
        return newSettings;
    }
    
    /** Set changed properties to XML files */
    void setProperties(List newProps){
    }
    
}
