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
package org.netbeans.modules.bpel.nodes.navigator;

import java.util.Comparator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ImportComparator implements Comparator<Import> {
    private static final int EQUALS = 0;
    private static final int GREATER = 1;
    private static final int LESS = -1;
    private static final String PATH_SLASH = "/";
//    private static ImportComparator COMPARATOR;
    private Lookup myLookup;
    
    public ImportComparator(Lookup lookup) {
        myLookup = lookup;
    }
    
    private Lookup getLookup() {
        return myLookup;
    }
    
//    public synchronized static ImportComparator getInstance(Lookup lookup) {
//        if (COMPARATOR == null) {
//            COMPARATOR = new ImportComparator(lookup);
//        }
//        return COMPARATOR;
//    }
    
    public int compare(Import o1, Import o2) {
        //the order should be as the following:
        //1) in-project schema and wsdl documents
        //2) external schema and wsdl documents
        //3) schema and wsdl documents without specified location
        //4) schema and wsdl documents without specified location and namespace
        assert o1 != null;
        assert o2 != null;
        if (o1.equals(o2)) {
            return EQUALS;
        }
        
        String o1Path = getRelativePath(o1);
        String o2Path = getRelativePath(o2);
        if (o1Path != null && o2Path != null) {
            //both imports point to documents in the project
            return comparePaths(o1Path, o2Path);
                    
        } else if (o1Path == null && o2Path == null) {
            //both imports point to external documents
            return compareExternal(o1, o2);
            
        } else {
            //one of the imports is in the project - it's considered LESS
            return o1Path != null ? LESS : GREATER;
        }
    }
    
    /**
     * In case of the imported file is corrupted/renamed/moved/deleted 
     * the method returns the text "[invalid] importLocation"
     */ 
    private String getRelativePath(Import imprt) {
        assert imprt != null;
        FileObject ifo = ResolverUtility.getImportedFileObject(imprt);
        Project modelProject = Utils.safeGetProject(imprt.getBpelModel());
        return ResolverUtility.safeGetRelativePath(ifo, modelProject);
    }
    
    private int comparePaths(String relativePath1, String relativePath2) {
        String[] o1StArray = relativePath1.split(PATH_SLASH);
        String[] o2StArray = relativePath2.split(PATH_SLASH);
        if (o1StArray.length != o2StArray.length) {
            return o1StArray.length > o2StArray.length ? GREATER : LESS;
        }
        
        for (int i = 0; i < o1StArray.length; i++) {
            if (! o1StArray[i].equals(o2StArray[i])) {
                return o1StArray[i].compareTo(o2StArray[i]);
            }
        }
        return EQUALS;
    }
    
    private int compareExternal(Import import1, Import import2) {
        if (
                hasValue(import1.getLocation()) &&
                hasValue(import2.getLocation()))
        {
            return import1.getLocation().compareTo(import2.getLocation());
        } else if (
                !hasValue(import1.getLocation()) &&
                !hasValue(import2.getLocation()))
        {
            return compareNoLocation(import1, import2);
        } else {
            return hasValue(import1.getLocation()) ? LESS : GREATER;

        }
    }
    
    private int compareNoLocation(Import import1, Import import2) {
        if (
                hasValue(import1.getNamespace()) &&
                hasValue(import2.getNamespace()))
        {
            return import1.getNamespace().compareTo(import2.getNamespace());
        } else if (
                !hasValue(import1.getNamespace()) &&
                !hasValue(import2.getNamespace()))
        {
            return EQUALS;
        } else {
            return hasValue(import1.getNamespace()) ? LESS : GREATER;
}
    }
    
    private boolean hasValue(String str) {
        return str != null && str.length() != 0;
    }
}
