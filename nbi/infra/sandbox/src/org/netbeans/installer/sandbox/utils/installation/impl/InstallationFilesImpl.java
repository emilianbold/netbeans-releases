/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
