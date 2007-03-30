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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 *
 * @author Milan Kubec
 */
public class UserdirScanner {
    
    private static Logger LOGGER = WSProjectOpener.LOGGER;
    
    UserdirScanner() {}
    
    public static NBInstallation[] suitableNBInstallations(File homeDir, String minVersion, Comparator comp) {
        File nbUserHome = new File(homeDir, ".netbeans");
        List list = allNBInstallations(nbUserHome);
        LOGGER.info("All found NetBeans installations: " + list);
        
        NBInstallation devNbi = null;
        // find dev NBInstallation
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            NBInstallation nbi = (NBInstallation) iter.next();
            // 1.0 version means no version number exists
            if (nbi.numVersion().equals("1.0") && 
                    nbi.releaseType().equals("dev") && 
                    nbi.releaseVersion().equals("")) {
                devNbi = nbi;
            }
        }
        if (minVersion.equals("dev")) {
            if (devNbi != null) {
                return new NBInstallation[] { devNbi };
            }
            return new NBInstallation[] { };
        }
        
        Collections.sort(list, comp);
        for (ListIterator listIter = list.listIterator(); listIter.hasNext(); ) {
            NBInstallation nbi = (NBInstallation) listIter.next();
            if (Utils.compareVersions(minVersion, nbi.numVersion()) > 0) { // in case we don't want dev builds -> || nbi.releaseType().equals("dev")) {
                listIter.remove();
            }
        }
        Collections.reverse(list);
        // add dev to the end of the list here
        if (devNbi != null) {
            list.add(devNbi);
        }
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
    
}
