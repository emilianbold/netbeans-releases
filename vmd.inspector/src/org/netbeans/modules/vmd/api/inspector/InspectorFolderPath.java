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

package org.netbeans.modules.vmd.api.inspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.inspector.InspectorWrapperTree;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class keep tracking status of of the Mobility Visual Designer Navigator structure tree.
 *
 */
public final class InspectorFolderPath {

    private final List<InspectorFolder> path;
    private static InspectorFolderPath instance;

    private InspectorFolderPath() {
        path = new ArrayList<InspectorFolder>();
    }

    /**
     * DO NOT USE THIS METHOD.This method is only accessible from InspectorWrapperTree.class
     */
    public static InspectorFolderPath createInspectorPath() {
        if (!Debug.isFriend("org.netbeans.modules.vmd.inspector.InspectorWrapperTree$1", "run")) {
            //NOI18N
            throw new IllegalStateException("This method is accessible only from InspectorWrapperTree.class"); //NOI18N
        }
        instance = new InspectorFolderPath();
        return instance;
    }

    /**
     * DO NOT USE THIS METHOD.This method is only accessible from InspectorWrapperTree.class
     */
    public InspectorFolderPath add(InspectorFolder pathElement) {
        if (!Debug.isFriend("org.netbeans.modules.vmd.inspector.InspectorWrapperTree$1", "run") && !Debug.isFriend(InspectorWrapperTree.class)) {
            throw new IllegalStateException("This method is accessible only from InspectorWrapperTree.class"); //NOI18N
        }
        path.add(pathElement);
        return this;
    }

    /**
     * DO NOT USE THIS METHOD.This method is only accessible from InspectorWrapperTree.class
     */
    public void remove(InspectorFolder pathElement) {
        if (!Debug.isFriend("org.netbeans.modules.vmd.inspector.InspectorWrapperTree$1", "run") && !Debug.isFriend(InspectorWrapperTree.class)) {
            throw new IllegalStateException("This method is accessible only from InspectorWrapperTree.class"); //NOI18N
        }
        assert path.lastIndexOf(pathElement) == (path.size() - 1) : "Path error"; // NOI18N
        path.remove(path.size() - 1);
    }

    /**
     * Returns current path of the Mobility Visual Designer Navigator as a List<InspectorFolder>
     * @return returns current path as List<InspectorFolder>
     */
    public List<InspectorFolder> getPath() {
        return Collections.<InspectorFolder>unmodifiableList(path);
    }

    /**
     * Returns last element of the current path of the Mobility Visual Designer Navigator as InspectorFolder
     * @return returns last element od path as InspectorFolder
     */
    public InspectorFolder getLastElement() {
        int index = path.size() - 1;
        if (index >= 0) {
            return Collections.<InspectorFolder>unmodifiableList(path).get(index);
        } else {
            return null;
        }
    }

    public String toString() {
        StringBuffer pathString = new StringBuffer();
        for (InspectorFolder folder : path) {
            pathString.append("/").append(folder.getTypeID()); // NOI18N
        }

        return pathString.toString();
    }
}