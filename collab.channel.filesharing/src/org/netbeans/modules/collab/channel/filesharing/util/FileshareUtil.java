/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.util;

import com.sun.collablet.CollabException;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

import java.io.File;
import java.io.IOException;

import java.util.*;

import javax.swing.Action;

import org.netbeans.api.project.Project;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.ui.ProjectsRootNode.ProjectNode;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabCleanAndBuildAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabCompileAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabInstallAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabProjectAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabRunAction;
import org.netbeans.modules.collab.core.Debug;


/**
 * general util class
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FileshareUtil implements FilesharingConstants {
    /**
     *
     * @param range
     * @return
     */
    public synchronized static long getRandomCount(long range) {
        Random rand = new Random();
        long randomCount = rand.nextInt((int) range);

        return randomCount;
    }

    /**
     * setProjectActions
     *
     * @param        projectNode
     * @param        pNodeactions
     * @param        fromProject
     * @return        actions
     */
    public static SystemAction[] setProjectActions(
        Node projectNode, Action[] pNodeactions, Project fromProject, FilesharingContext context
    ) {
        if ((projectNode == null) || (pNodeactions == null)) {
            return new SystemAction[] {  };
        }

        HashMap newactions = new HashMap();

        for (int i = 0; i < pNodeactions.length; i++) {
            Action pAction = pNodeactions[i];
            String actionName = (String) pAction.getValue(Action.NAME);
            String actionString = pAction.toString();
            Debug.out.println("project action: " + actionString); //NoI18n			

            if (
                strContains(actionString, "org.netbeans.modules.project.ui.actions.ProjectAction") || //NoI18n
                    strContains(actionString, "org.netbeans.modules.apisupport.project.Actions") || //NoI18n
                    pAction instanceof CollabProjectAction
            ) {
                SystemAction action = null;

                if (
                    strContains(actionName, "Build") && !strContains(actionName, "Clean") && //NoI18n
                        !strContains(actionName, "with") && //NoI18n
                        !newactions.containsKey(actionName)
                ) {
                    //newactions.put(actionName+"null1", null);//NoI18n
                    action = new CollabCompileAction();
                    ((CollabCompileAction) action).setName(actionName);
                    ((CollabCompileAction) action).setCallbackAction(
                        new CollabProjectAction(
                            CollabProjectAction.COMMAND_BUILD, fromProject, pAction, context, projectNode
                        )
                    );
                    newactions.put(actionName, action);

                    //newactions.put(actionName+"null2", null);//NoI18n
                } else if (
                    strContains(actionName, "Build") && strContains(actionName, "Clean") && //NoI18n
                        !newactions.containsKey(actionName)
                ) {
                    //newactions.put(actionName+"null1", null);//NoI18n
                    action = new CollabCleanAndBuildAction();
                    ((CollabCleanAndBuildAction) action).setName(actionName);
                    ((CollabCleanAndBuildAction) action).setCallbackAction(
                        new CollabProjectAction(
                            CollabProjectAction.COMMAND_REBUILD, fromProject, pAction, context, projectNode
                        )
                    );
                    newactions.put(actionName, action);

                    //newactions.put(actionName+"null2", null);//NoI18n
                } else if (strContains(actionName, "Install") && //NoI18n
                        !newactions.containsKey(actionName)) {
                    //newactions.put(actionName+"null1", null);//NoI18n
                    action = new CollabInstallAction();
                    ((CollabInstallAction) action).setName(actionName);
                    ((CollabInstallAction) action).setCallbackAction(
                        new CollabProjectAction(
                            CollabProjectAction.COMMAND_INSTALL, fromProject, pAction, context, projectNode
                        )
                    );
                    newactions.put(actionName, action);

                    //newactions.put(actionName+"null2", null);//NoI18n
                } else if (strContains(actionName, "Run") && //NoI18n
                        !newactions.containsKey(actionName)) {
                    //newactions.put(actionName+"null1", null);//NoI18n
                    action = new CollabRunAction();
                    ((CollabRunAction) action).setName(actionName);
                    ((CollabRunAction) action).setCallbackAction(
                        new CollabProjectAction(
                            CollabProjectAction.COMMAND_RUN, fromProject, pAction, context, projectNode
                        )
                    );
                    newactions.put(actionName, action);

                    //newactions.put(actionName+"null2", null);//NoI18n
                }
            }
        }

        Debug.log("ProjectsRootNode", "ProjectsRootNode, newactions length:" + newactions.size()); //NoI18n

        SystemAction[] actions = (SystemAction[]) newactions.values().toArray(new SystemAction[0]);
        ((ProjectNode) projectNode).setActions(actions);

        return actions;
    }

    /**
     * return dataObject for this file that is handled
     *
     * @throws CollabException
     * @return dataObject
     */
    public static DataObject getDataObject(FileObject fileObject)
    throws CollabException {
        if (fileObject == null) {
            return null;
        }

        // Get the DataObject
        DataObject dataObject = null;

        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {
            throw new CollabException(e);
        }

        if (dataObject == null) {
            throw new IllegalArgumentException("No DataObject found for file \"" + fileObject.getNameExt() + "\"");
        }

        return dataObject;
    }

    /**
     * getter for EditorCookie
     * @throws CollabException
     * @return EditorCookie
     */
    public static EditorCookie getEditorCookie(DataObject dataObject)
    throws CollabException {
        EditorCookie cookie = null;

        try {
            if (dataObject == null) {
                return null;
            }

            // return the Editor Cookie for the dataobject
            cookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        } catch (Exception ex) {
            throw new CollabException(ex);
        } catch (java.lang.Throwable ex) {
            throw new CollabException(ex);
        }

        return cookie;
    }

    /**
     *
     * @param fileName
     * @throws CollabException
     * @return
     */
    public static String getFileExt(String fileName) throws CollabException {
        String fileExt = null;

        try {
            int lastIndex = fileName.lastIndexOf(".");

            if (lastIndex != -1) {
                fileExt = fileName.substring(lastIndex + 1, fileName.length());
            }
        } catch (IndexOutOfBoundsException iob) {
            throw new CollabException(iob);
        }

        return fileExt;
    }

    /*
     *getNormalizedPath
     *
     * @param path
     * @return normalizedPath
     */
    public static String getNormalizedPath(String path) {
        String normalizedPath = path.replace('\\', FILE_SEPERATOR_CHAR);

        return normalizedPath.replace('/', FILE_SEPERATOR_CHAR);
    }

    /*
     *getNormalizedPath
     *
     * @param path
     * @return normalizedPath
     */
    public static void deleteFileLink(String fileName, FilesharingContext context) {
        File cRoot = ((CollabFilesystem) context.getCollabFilesystem()).getCollabRoot();
        File flnk = null;
        int lastIndex = fileName.lastIndexOf('.');

        if (lastIndex != -1) {
            flnk = new File(cRoot, fileName.substring(0, lastIndex) + ".shadow"); //NoI18n
        } else {
            flnk = new File(cRoot, fileName + ".shadow"); //NoI18n
        }

        final File dLnk = flnk;

        try {
            context.getCollabFilesystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        Debug.out.println("File Link Delete: " + dLnk.getPath());

                        FileObject fObj = FileUtil.toFileObject(dLnk);

                        if (fObj != null) {
                            Debug.out.println("fObj Delete: " + fObj.getPath());

                            DataObject d = (DataObject) DataObject.find(fObj);
                            Node node = null;

                            if (d != null) {
                                node = d.getNodeDelegate();
                            }

                            if (node != null) {
                                node.destroy();
                            }
                        }

                        dLnk.delete();
                    }
                }
            );
        } catch (IOException iox) {
            Debug.out.println("File Link Delete::ex: " + iox);
            iox.printStackTrace(Debug.out);
        } catch (Throwable th) {
            Debug.out.println("File Link Delete::ex: " + th);
            th.printStackTrace(Debug.out);
        }
    }

    private static boolean strContains(String str, String pattern) {
        return str.indexOf(pattern) != -1;
    }
}
