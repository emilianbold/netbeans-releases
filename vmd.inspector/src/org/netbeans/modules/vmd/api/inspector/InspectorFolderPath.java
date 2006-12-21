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
public final class InspectorFolderPath {

    private final List<InspectorFolder> path;
    private static InspectorFolderPath instance;

    private InspectorFolderPath(){
        path = new ArrayList<InspectorFolder>();
    }

    public static InspectorFolderPath createInspectorPath(){
        //assert Debug.isFriend(InspectorFolderTree.class);
        instance = new InspectorFolderPath();
        return instance;
    }
    
    public InspectorFolderPath add(InspectorFolder pathElement){
        //assert Debug.isFriend(InspectorFolderTree.class);
        path.add(pathElement);
        return this;
    }
    
    public void remove(InspectorFolder pathElement){
        assert Debug.isFriend(InspectorWrapperTree.class);
        assert path.lastIndexOf(pathElement) == (path.size() - 1) : "Path error" ;  // NOI18N
        path.remove( path.size() - 1 );
    }
    
    public List<InspectorFolder> getPath(){
        return Collections.<InspectorFolder>unmodifiableList(path);
    }
    
    public InspectorFolder getLastElement(){
        int index = path.size() - 1;
        if (index >= 0)
            return Collections.<InspectorFolder>unmodifiableList(path).get(index);
        else
            return null;
    }
    
    public String toString(){
        StringBuffer pathString = new StringBuffer();
        for (InspectorFolder folder : path){
            pathString.append("/").append(folder.getTypeID());  // NOI18N
        }
        
        return pathString.toString();
    }
    
}


