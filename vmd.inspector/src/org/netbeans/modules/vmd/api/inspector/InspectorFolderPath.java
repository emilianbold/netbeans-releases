/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        if (!Debug.isFriend("org.netbeans.modules.vmd.inspector.InspectorWrapperTree$1", "run")) { // NOI18N
            throw new IllegalStateException("This method is accessible only from InspectorWrapperTree.class"); //NOI18N
        }
        instance = new InspectorFolderPath();
        return instance;
    }

    /**
     * DO NOT USE THIS METHOD.This method is only accessible from InspectorWrapperTree.class
     */
    public InspectorFolderPath add(InspectorFolder pathElement) {
        if (!Debug.isFriend("org.netbeans.modules.vmd.inspector.InspectorWrapperTree$1", "run") && !Debug.isFriend(InspectorWrapperTree.class)) { // NOI18N
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

    @Override
    public String toString() {
        StringBuffer pathString = new StringBuffer();
        for (InspectorFolder folder : path) {
            pathString.append("/").append(folder.getTypeID()); // NOI18N
        }

        return pathString.toString();
    }
}
