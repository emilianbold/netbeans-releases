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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.openide.loaders.ExtensionList;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class FileSystemFactory {
    
    private FileSystemFactory() {
    }
    
    public static List<Set<String>> getUnusedFiles(String root, Set<String> used){
        Set<String> sourceSuffixes = getSourceSuffixes();
        Set<String> headerSuffixes = getHeaderSuffixes();
        HashSet<String> set = new HashSet<String>();
        gatherSubFolders(new File(root), set);
        Set<String> sources = new HashSet<String>();
        Set<String> headers = new HashSet<String>();
        List<Set<String>> list = new ArrayList<Set<String>>(2);
        list.add(sources);
        list.add(headers);
        for (Iterator it = set.iterator(); it.hasNext();){
            File d = new File((String)it.next());
            if (d.isDirectory()){
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    if (ff[i].isFile()) {
                        String name = ff[i].getName();
                        int j = name.lastIndexOf('.');
                        if (j>0){
                            String suffix = name.substring(j+1);
                            if (sourceSuffixes.contains(suffix)){
                                String path = ff[i].getAbsolutePath();
                                if (Utilities.isWindows()) {
                                    path = path.replace('\\', '/');
                                }
                                if (!used.contains(path)) {
                                    sources.add(path);
                                }
                            } else if (headerSuffixes.contains(suffix)){
                                String path = ff[i].getAbsolutePath();
                                if (Utilities.isWindows()) {
                                    path = path.replace('\\', '/');
                                }
                                if (!used.contains(path)) {
                                    headers.add(path);
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
    
    private static void gatherSubFolders(File d, HashSet<String> set){
        if (d.isDirectory()){
            String path = d.getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.endsWith("/SCCS") || path.endsWith("/CVS")) {  // NOI18N
                return;
            }
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    gatherSubFolders(ff[i], set);
                }
            }
        }
    }

    public static Set<String> createExtensionSet(){
        if (CASE_INSENSITIVE) {
            return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        } else {
            return new TreeSet<String>();
        }
    }
    
    public static Set<String> getSourceSuffixes() {
        Set<String> suffixes = createExtensionSet(); 
        addSuffices(suffixes, CCDataLoader.getInstance().getExtensions());
        addSuffices(suffixes, CDataLoader.getInstance().getExtensions());
        return suffixes;
    }
    
    public static Set<String> getHeaderSuffixes() {
        Set<String> suffixes = createExtensionSet(); 
        addSuffices(suffixes, HDataLoader.getInstance().getExtensions());
        return suffixes;
    }
    
    private static void addSuffices(Set<String> suffixes, ExtensionList list) {
        for (Enumeration e = list.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            suffixes.add(ex);
        }
    }
    
    private static final boolean CASE_INSENSITIVE =
        (Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2)) || Utilities.getOperatingSystem() == Utilities.OS_VMS;

}
