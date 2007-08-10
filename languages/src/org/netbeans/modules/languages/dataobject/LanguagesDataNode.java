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

package org.netbeans.modules.languages.dataobject;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LanguagesDataNode extends DataNode {

    public LanguagesDataNode(LanguagesDataObject obj) {
        super(obj, Children.LEAF);
        String mimeType = obj.getPrimaryFile ().getMIMEType ();
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("Editors/" + mimeType + "/language.nbs");
        String icon = (String) fo.getAttribute ("icon");
        if (icon == null)
            icon = "org/netbeans/modules/languages/resources/defaultIcon.png";
        setIconBaseWithExtension (icon);
    }

//    /** Creates a property sheet. */
//    protected Sheet createSheet() {
//        Sheet s = super.createSheet();
//        Sheet.Set ss = s.get(Sheet.PROPERTIES);
//        if (ss == null) {
//            ss = Sheet.createPropertiesSet();
//            s.put(ss);
//        }
//        // TODO add some relevant properties: ss.put(...)
//        return s;
//    }

    private Map<String,Action[]> mimeTypeToActions = new HashMap<String,Action[]> ();
    
    /** Get actions for this data object.
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    public Action[] getActions (boolean context) {
        String mimeType = getDataObject ().getPrimaryFile ().getMIMEType ();
        if (!mimeTypeToActions.containsKey (mimeType)) {
            List<Action> actions = new ArrayList<Action> ();
            try {
                FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
                    findResource ("Loaders/" + mimeType + "/Actions");
                if (fo != null) {
                    DataFolder df = DataFolder.findFolder (fo);
                    DataObject[] dob = df.getChildren ();
                    int i, k = dob.length;
                    for (i = 0; i < k; i++) {
                        InstanceCookie ic = dob [i].getCookie(InstanceCookie.class);
                        Class clazz = ic.instanceClass ();
                        if (JSeparator.class.isAssignableFrom (clazz))
                            actions.add (null);
                        else
                            actions.add ((Action) ic.instanceCreate ());
                    }
                }
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault ().notify (ex);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
            if (!actions.isEmpty ())
                mimeTypeToActions.put (mimeType, actions.toArray (new Action [actions.size ()]));
            else
                mimeTypeToActions.put (mimeType, super.getActions (context));
        }
        return mimeTypeToActions.get(mimeType);
    }
}




