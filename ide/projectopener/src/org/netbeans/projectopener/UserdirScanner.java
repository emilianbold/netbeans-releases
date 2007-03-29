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

package org.netbeans.projectopener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 *
 * @author Milan Kubec
 */
public class UserdirScanner {
    
    private static Logger LOGGER = WSProjectOpener.LOGGER;
    
    private static final String searchStr = "Installation;";
    
    //private static final String nbDirs[] = { "5.5.1beta", "dev" };
    //private static final String nbClusters[] = { "nb5.5", "nb6.0" };
    
    //private static Map clusterNames = new HashMap();
    
    UserdirScanner() {}
    
    /**
     * Might return null
     */
//    public static String findInstallDirPath(String homeDir, String reqVersion) throws FileNotFoundException, IOException {
//        File f = findUserDir(homeDir, reqVersion);
//        if (f != null) {
//            return getNBInstallDirPath(f.getAbsolutePath(), reqVersion);
//        }
//        return null;
//    }
    
//    private static File findUserDir(String homeDir, String reqVersion) {
//        File nbUserHome = new File(homeDir + File.separator + ".netbeans");
//        LOGGER.info("Looking for " + reqVersion + " in " + nbUserHome.getAbsolutePath());
//        if (nbUserHome.exists()) {
//            LOGGER.fine(nbUserHome.getAbsolutePath() + " exists");
//            String userDirs[] = nbUserHome.list();
//            for (int i = 0; i < userDirs.length; i++) {
//                LOGGER.fine("Testing directory: " + userDirs[i]);
//                if (userDirs[i].equals(reqVersion)) {
//                    // we found the right dir
//                    LOGGER.fine("Found it: " + userDirs[i]);
//                    return new File(nbUserHome, userDirs[i]);
//                }
//                // what to do if it's not reqVersion
//            }
//        }
//        return null;
//    }
    
    // install dir is found based on nb cluster path
//    private static String getNBInstallDirPath(String userDirPath, String reqVer) throws FileNotFoundException, IOException {
//        String dirPath = null;
//        initClusterNames();
//        File logFile = new File(new File(new File(userDirPath, "var"), "log"), "messages.log");
//        LOGGER.info("Parsing file: " + logFile.getAbsolutePath());
//        BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));
//        String line = logFileReader.readLine();
//        while (line != null) {
//            if (line.indexOf(searchStr) != -1) {
//                LOGGER.fine("Found line in messages.log file: " + line);
//                int index1 = line.indexOf('=') + 2;
//                int index2 = line.indexOf("; ", index1);
//                String subStr = line.substring(index1, index2);
//                LOGGER.fine("Found substring: " + subStr);
//                StringTokenizer tokenizer = new StringTokenizer(subStr, File.pathSeparator);
//                while (tokenizer.hasMoreTokens()) {
//                    String instPart = tokenizer.nextToken();
//                    LOGGER.info("Testing token: " + instPart);
//                    if (instPart.indexOf((String) clusterNames.get(reqVer)) != -1) {
//                        File f = new File(instPart).getParentFile();
//                        LOGGER.fine("Found file: " + f.getAbsolutePath());
//                        if (f.exists()) {
//                            dirPath = f.getAbsolutePath();
//                        }
//                    }
//                }
//            }
//            line = logFileReader.readLine();
//        }
//        return dirPath;
//    }
    
//    private static void initClusterNames() {
//        for (int i = 0; i < nbDirs.length; i++) {
//            clusterNames.put(nbDirs[i], nbClusters[i]);
//        }
//    }
    
    // ---
    
    public static NBInstallation[] suitableNBInstallations(File homeDir, String minVersion) {
        File nbUserHome = new File(homeDir, ".netbeans");
        List list = allNBInstallations(nbUserHome);
        if (minVersion.equals("dev")) {
            for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                NBInstallation nbi = (NBInstallation) iter.next();
                // 1.0 version means no version number exists
                if (nbi.numVersion().equals("1.0") && 
                        nbi.releaseType().equals("dev") && 
                        nbi.releaseVersion().equals("")) {
                    return new NBInstallation[] { nbi };
                }
            }
            return new NBInstallation[] { };
        }
        Collections.sort(list, NBInstallation.LAST_USED_COMPARATOR);
        for (ListIterator listIter = list.listIterator(); listIter.hasNext(); ) {
            NBInstallation nbi = (NBInstallation) listIter.next();
            if (Utils.compareVersions(minVersion, nbi.numVersion()) > 0) { // in case we don't want dev builds -> || nbi.releaseType().equals("dev")) {
                listIter.remove();
            }
        }
        Collections.reverse(list);
        return (NBInstallation[]) list.toArray(new NBInstallation[list.size()]);
    }
    
    // returns all valid installations of NB found in ${HOME}/.netbeans
    private static List allNBInstallations(File nbUserHome) {
        File files[] = nbUserHome.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        List list = new ArrayList();
        // files might be null here, e.g. if there is no .netbeans folder
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // creating NB installation is based on userdir
                NBInstallation nbi = new NBInstallation(files[i]);
                if (nbi.isValid()) {
                    list.add(nbi);
                }
            }
        }
        return list;
    }
    
    // ---
    
    public static void main(String[] args) {
        
        NBInstallation nbis[] = suitableNBInstallations(new File(System.getProperty("user.home")), "5.0");
        System.out.println(nbis.length);
        for (int i = 0; i < nbis.length; i++) {
            System.out.println(nbis[i].getInstallDir());
        }
        
    }
    
}
