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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class DefaultSourceMap extends SourceFileMap {

    /**
     * Straight file mapping service.
     * Map a distribution path to a file using distribution path as relative path to a mapping root.
     */
    private J2eeModuleProvider provider;
    private HashSet rootFiles = new HashSet();
    
    /** Creates a new instance of DefaultFileMapping */
    public DefaultSourceMap(J2eeModuleProvider provider) {
        this.provider = provider;
        FileObject[] roots = provider.getSourceRoots();
        for (int i=0; i<roots.length; i++) {
            if (roots[i] != null) {
                rootFiles.add(FileUtil.toFile(roots[i]));
            }
        }
    }
    
    public String getContextName() {
        return provider.getDeploymentName();
    }

    public FileObject[] getSourceRoots() {
        return provider.getSourceRoots();
    }
    
    public File getEnterpriseResourceDir() {
        return provider.getEnterpriseResourceDirectory();
    }
    
    public File[] getEnterpriseResourceDirs() {
        ArrayList result = new ArrayList();
        result.add(provider.getEnterpriseResourceDirectory());
        if (provider instanceof J2eeAppProvider) {
            J2eeAppProvider jap = (J2eeAppProvider) provider;
            J2eeModuleProvider[] children = jap.getChildModuleProviders();
            for (int i=0; i<children.length; i++) {
                result.add(children[i].getEnterpriseResourceDirectory());
            }
        }
        return (File[]) result.toArray(new File[result.size()]);
    }
   
    public boolean add(String distributionPath, FileObject sourceFile) {
        return false;
    }
    
    public FileObject remove(String distributionPath) {
        return null;
    }
    
    public FileObject[] findSourceFile(String distributionPath) {
        ArrayList ret = new ArrayList();
        FileObject[] roots = getSourceRoots();
        String path = distributionPath.startsWith("/") ? distributionPath.substring(1) : distributionPath; //NOI18N
        for (int i=0; i<roots.length; i++) {
            FileObject fo = roots[i].getFileObject(path);
            if (fo != null)
                ret.add(fo);
        }
        return (FileObject[]) ret.toArray(new FileObject[ret.size()]);
    }
    
    public File getDistributionPath(FileObject sourceFile) {
        for (Iterator i=rootFiles.iterator(); i.hasNext();) {
            File rootFile = (File) i.next();
            FileObject root = FileUtil.toFileObject(rootFile);
            String relative = FileUtil.getRelativePath(root, sourceFile);
            if (relative != null && ! relative.trim().equals("")) { //NOI18N
                return new File(relative);
            }
        }
        return null;
    }
}


