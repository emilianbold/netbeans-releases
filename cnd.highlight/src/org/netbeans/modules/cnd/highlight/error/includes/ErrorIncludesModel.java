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

package org.netbeans.modules.cnd.highlight.error.includes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;


/**
 *
 * @author Alexander Simon
 */
public class ErrorIncludesModel implements ListModel {
    private List<String> names = new ArrayList<String>();
    private List<List<CsmInclude>> includeList = new ArrayList<List<CsmInclude>>();
    private int errorFiles;
    private int errorIncludes;
    public ErrorIncludesModel(List<CsmInclude> includes){
        errorIncludes = includes.size();
        Map<String, List<CsmInclude>> tree = new TreeMap<String,List<CsmInclude>>();
        Set<CsmFile> files = new HashSet<CsmFile>();
        for (Iterator<CsmInclude> it = includes.iterator(); it.hasNext(); ){
            CsmInclude incl = it.next();
            files.add(incl.getContainingFile());
            String name;
            if (incl.isSystem()){
                name = "<"+incl.getIncludeName()+">"; // NOI18N
            } else {
                name = "\""+incl.getIncludeName()+"\""; // NOI18N
            }
            List<CsmInclude> list = tree.get(name);
            if (list == null){
                list = new ArrayList<CsmInclude>();
                tree.put(name,list);
            }
            list.add(incl);
        }
        for (Iterator<Entry<String, List<CsmInclude>>> it = tree.entrySet().iterator(); it.hasNext(); ){
            Entry<String, List<CsmInclude>> entry = it.next();
            names.add(entry.getKey());
            includeList.add(entry.getValue());
        }
        errorFiles = files.size();
    }
    
    public int getFailedIncludesSize(){
        return errorIncludes;
    }

    public int getFailedFilesSize(){
        return errorFiles;
    }
    
    public int getSize() {
        return names.size();
    }
    
    public Object getElementAt(int index) {
        return names.get(index);
    }
    
    public List<CsmInclude> getElementList(int index){
        return includeList.get(index);
    }
    
    public void addListDataListener(ListDataListener l) {
    }
    
    public void removeListDataListener(ListDataListener l) {
    }
}
