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

package org.netbeans.modules.cnd.discovery.wizard.checkedtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Alexander Simon
 */
public class UnusedFactory {
    
    private UnusedFactory() {
    }

    public static AbstractRoot createRoot(Set<String> set){
        AbstractRoot root = makeRoot(set);
        consolidateRoot(root);
        return root;
    }
    
    private static List<String> consolidateRoot(AbstractRoot root){
        List<String> files = root.getFiles();
        for(AbstractRoot current : root.getChildren()){
            List<String> fp = consolidateRoot(current);
            if (files == null) {
                files = new ArrayList<String>();
                ((Root)root).setFiles(files);
            }
        }
        return files;
    }
    
    private static AbstractRoot makeRoot(Set<String> set){
        AbstractRoot root = new Root("");
        for(String path : set){
            ((Root)root).addChild(path);
        }
        while(root.getFiles()== null){
            Collection<AbstractRoot> children = root.getChildren();
            if (children.size()==1){
                AbstractRoot child = children.iterator().next();
                root = child;
                continue;
            }
            break;
        }
        return root;
    }
    
}
