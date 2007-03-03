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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;


/**
 * Provides support for retrieving instances from the system filesystem (SFS) folder.
 * Currently it supports retrieving <code>Action</code> instances.
 * <p>
 * Note: It doesn't support the retrieving of instances from the subfolders.
 * </p>
 *
 * @author Peter Zavadsky
 */
public final class SystemFileSystemSupport {

    /** Inteface defining the action provider. */
    interface ActionsProvider {
        Action[] getActions();
    }

    /** Dummy impl of <code>ActionsProvider</code> used for the cases
     * the folder in the system filesystem doesn't exist. */
    private static ActionsProvider DUMMY_ACTIONS_PROVIDER = new ActionsProvider() {
        public Action[] getActions() {
            return new Action[0];
        }
    };

    /** Maps <code>DataFolder</code> to <code>ActionsProvider</code>. */
    private static final Map dataFolder2actionsProvider = new WeakHashMap();


    private SystemFileSystemSupport() {
    }


    /** Provides the actions retrieved from the specified folder in SFS.
     * If the specified folder doesn't exist, an empty array is returned.
     * The <code>null</code> values in the array represent separators.
     * <p>
     * Note: It doesn't retrieve the actions from the subfolders.
     * </p>
     * @param folderPath specifies the path to the folder in SFS
     * @return Action[] */
    public static Action[] getActions(String folderPath) {
        return getActionProvider(folderPath).getActions();
    }

    /** Gets <code>ActionProvider</code> for specified folder in SFS.
     * @param folderPath specifies the path to the folder in SFS */
    private static ActionsProvider getActionProvider(String folderPath) {
        DataFolder dataFolder = getDataFolder(folderPath);
        if (dataFolder == null) {
            return DUMMY_ACTIONS_PROVIDER;
        }

        synchronized (dataFolder2actionsProvider) {
            ActionsProvider actionsProvider = (ActionsProvider)dataFolder2actionsProvider.get(dataFolder);
            if (actionsProvider == null) {
                actionsProvider = new DefaultActionsProvider(dataFolder);
                dataFolder2actionsProvider.put(dataFolder, actionsProvider);
            }
            return actionsProvider;
        }
    }


    private static DataFolder getDataFolder(String folderPath) {
        FileObject fileObject = Repository.getDefault().getDefaultFileSystem().findResource(folderPath);
        if (fileObject == null) {
            return null;
        }

        return DataFolder.findFolder(fileObject);
    }


    private static class DefaultActionsProvider extends FolderInstance implements ActionsProvider {

        public DefaultActionsProvider(DataFolder dataFolder) {
            super(dataFolder);
        }

        /** Gets the action array. */
        public Action[] getActions() {
            try {
                return (Action[])instanceCreate();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            return new Action[0];
        }

        /** Creates the actions. */
        protected Object createInstance(InstanceCookie[] cookies)
        throws IOException, ClassNotFoundException {
            List actions = new ArrayList();
            for (int i = 0; i < cookies.length; i++) {
                Class clazz = cookies[i].instanceClass();
                if (JSeparator.class.isAssignableFrom(clazz)) {
                    // XXX <code>null</code> is interpreted as a separator.
                    actions.add(null);
                    continue;
                }

                Object object;
                try {
                    object = cookies[i].instanceCreate();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    continue;
                } catch (ClassNotFoundException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    continue;
                }
                
                if (object instanceof Action) {
                    actions.add(object);
                    continue;
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("There is an unexpected object=" + object + // NOI18N
                            ", in the folder instance=" + this)); // NOI18N
                    continue;
                }
            }

            return actions.toArray(new Action[0]);
        }

        /** Currently not recursive. */
        protected InstanceCookie acceptFolder(DataFolder df) {
            return null;
        }
    } // End of DefaultActionsProvider class.

}

