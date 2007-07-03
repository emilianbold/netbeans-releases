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
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JSeparator;

import org.netbeans.modules.gsf.Language;
import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;


public class GsfDataNode extends DataNode {
    // XXX Shouldn't this be static?
    private static Map<String, Action[]> mimeTypeToActions = new HashMap<String, Action[]>();

    public GsfDataNode(GsfDataObject basDataObject, Language language) {
        super(basDataObject, Children.LEAF);
        setIconBaseWithExtension(language.getIconBase());
    }

    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

    /** Get actions for this data object.
     * (Copied from LanguagesDataNode in languages/engine)
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    public Action[] getActions(boolean context) {
        String mimeType = getDataObject().getPrimaryFile().getMIMEType();

        if (!mimeTypeToActions.containsKey(mimeType)) {
            List<Action> actions = new ArrayList<Action>();

            try {
                FileObject fo =
                    Repository.getDefault().getDefaultFileSystem()
                              .findResource("Loaders/" + mimeType + "/Actions"); // NOI18N

                if (fo != null) {
                    DataFolder df = DataFolder.findFolder(fo);
                    DataObject[] dob = df.getChildren();
                    int i;
                    int k = dob.length;

                    for (i = 0; i < k; i++) {
                        InstanceCookie ic = dob[i].getCookie(InstanceCookie.class);
                        Class clazz = ic.instanceClass();

                        if (JSeparator.class.isAssignableFrom(clazz)) {
                            actions.add(null);
                        } else {
                            actions.add((Action)ic.instanceCreate());
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }

            if (!actions.isEmpty()) {
                mimeTypeToActions.put(mimeType, actions.toArray(new Action[actions.size()]));
            } else {
                mimeTypeToActions.put(mimeType, super.getActions(context));
            }
        }

        return mimeTypeToActions.get(mimeType);
    }
}
