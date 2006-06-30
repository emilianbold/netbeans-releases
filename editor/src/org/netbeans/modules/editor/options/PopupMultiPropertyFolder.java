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
