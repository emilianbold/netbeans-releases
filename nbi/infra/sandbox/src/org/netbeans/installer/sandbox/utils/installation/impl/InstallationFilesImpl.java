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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.sandbox.utils.installation.InstallationFileObject;
import org.netbeans.installer.sandbox.utils.installation.InstallationFiles;
import org.netbeans.installer.sandbox.utils.installation.conditions.AndCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.DefaultFileCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.EmptyDirectoryCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;
import org.netbeans.installer.utils.installation.conditions.*;

/**
 *
 * @author Dmitry Lipin
 */
public class InstallationFilesImpl implements InstallationFiles {
    private List <InstallationFileObject> filesList;
    private ConditionsPool conditionPool;
    private FileCondition defaultCondition;
    
    public InstallationFilesImpl() {
        filesList =  new LinkedList <InstallationFileObject> ();
        conditionPool = new ConditionsPool();
        setDefaultCondition(new AndCondition(
                new FileCondition [] {
            new DefaultFileCondition(),
            new EmptyDirectoryCondition() } ));
        
    }
    public void add(File file) {
        add(file, defaultCondition);
    }
    
    public void add(File file,FileCondition  cond) {
        add(new InstallationFileObject(file, cond));
    }
    public void add(InstallationFileObject fo) {
        if(fo.getFileCondition()==null) {
            FileCondition fc = conditionPool.getCondition(fo.getCondition());
            if(fc==null) {
                fc = defaultCondition;
            }
            fo.setFileCondition(fc);
        }
        registerCondition(fo.getFileCondition());
        filesList.add(fo);
    }
    public int size() {
        return filesList.size();
    }
    public InstallationFileObject get(int index) {
        return filesList.get(index);
    }
    public void update(File file) {
        InstallationFileObject fo = getInstallationFileObject(file);
        if(fo!=null) {
            fo.initDataFromFile();
        }
    }
    public void updateConditions(File file,FileCondition  cond) {
        InstallationFileObject fo = getInstallationFileObject(file);
        if(fo!=null) {
            registerCondition(cond);
            fo.setFileCondition(cond);
        }
    }
    public void delete(File file) {
        filesList.remove(file);
    }
    
    public void registerCondition(FileCondition fcc) {
        conditionPool.addCondition(fcc);
    }
    
    private InstallationFileObject getInstallationFileObject(File file) {
        for(int i=0;i<filesList.size();i++) {
            if(filesList.get(i).getFile().equals(file)) {
                return filesList.get(i);
            }
        }
        return null;
    }
    private int getFileObjectIndex(File file) {
        for(int i=0;i<filesList.size();i++) {
            if(filesList.get(i).getFile().equals(file)) {
                return i;
            }
        }
        return -1;
    }
    public void setDefaultCondition(FileCondition fc) {
        if(fc!=null) {
            defaultCondition = fc;
        }
    }
    public FileCondition getDefaultCondition() {
        return defaultCondition;
    }
}
