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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Alexander Simon
 */
public class Root implements AbstractRoot {
    private Map<String,AbstractRoot> children = new HashMap<String,AbstractRoot>();
    private String name;
    private List<String> files;
    
    public Root(String name){
        this.name = name;
    }
    
    public Collection<AbstractRoot> getChildren(){
        return children.values();
    }
    
    public String getName(){
        return name;
    }
    
    private Root getChild(String child){
        return (Root)children.get(child);
    }
    
    public List<String> getFiles(){
        return files;
    }
    
    public void setFiles(List<String> files){
        if (this.files == null) {
            this.files = files;
        }
    }
    
    public Root addChild(String child){
        Root current = this;
        StringTokenizer st = new StringTokenizer(child,"/\\"); // NOI18N
        while(st.hasMoreTokens()){
            String segment = st.nextToken();
            if (st.hasMoreTokens()) {
                Root found = current.getChild(segment);
                if (found == null) {
                    found = new Root(segment);
                    current.children.put(segment, found);
                }
                current = found;
            } else {
                List<String> fileList = current.getFiles();
                if (fileList == null){
                    fileList = new ArrayList<String>();
                    current.setFiles(fileList);
                }
                fileList.add(child);
            }
        }
        return current;
    }
}
