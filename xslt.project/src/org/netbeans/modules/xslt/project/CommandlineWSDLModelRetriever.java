/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xslt.project;

import java.io.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.tmap.model.spi.ExternalModelRetriever;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vitaly Bychkov
 */
public class CommandlineWSDLModelRetriever implements ExternalModelRetriever {

    private Logger logger = Logger.getLogger(CommandlineWSDLModelRetriever.class.getName());    
    private List<File> myDependentProjectDirs;
    private List<File> mySourceDirs;
    private FileFilter myWsdlFilter;
    private FileFilter myFolderFilter;
    private boolean initialized = false;

    public CommandlineWSDLModelRetriever() {
    }

    public void init(List<File> depedentProjectDirs , List<File> sourceDirs) {
        myDependentProjectDirs = depedentProjectDirs;
        mySourceDirs = sourceDirs;
        initialized = true;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public Collection<WSDLModel> getWSDLModels(TMapModel model, String namespace) {
        List<WSDLModel> filtredModels = new ArrayList<WSDLModel>();

        if (!isInitialized() || model == null || namespace == null) {
            return filtredModels;
        }
        List<WSDLModel> allModels = getWsdlModels();
        for (WSDLModel wSDLModel : allModels) {
            assert wSDLModel != null;
            Definitions def = wSDLModel.getDefinitions();
            if (def == null) {
                continue;
            }
            if (namespace.equals(def.getTargetNamespace())) {
                filtredModels.add(wSDLModel);
            }
        }

        return filtredModels;
    }


    protected WSDLModel getWsdlModel(File wsdlFile) {
        if (wsdlFile == null) {
            return null;
        }
        WSDLModel wsdlModel = null;
        try {
            wsdlModel = CommandlineTransformmapCatalogModel.getDefault().
                                                getWsdlModel(wsdlFile.toURI());
        }catch (Exception ex) {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while getting WSDL Model ", ex);
            throw new RuntimeException("Error while getting WSDL Model ",ex);
        }
        
        if (wsdlModel != null 
                && !WSDLModel.State.VALID.equals(wsdlModel.getState())) 
        {
            return null;
        }
        return wsdlModel;
    }

    private List<File> collectWSDLs(File folder) {
        List<File> wsdls = new ArrayList<File>();
        if (folder == null || !folder.isDirectory()) {
            return wsdls;
        }
        
        File[] childWsdls = folder.listFiles(getWsdlFilter());
        if (childWsdls != null && childWsdls.length > 0) {
            wsdls.addAll(Arrays.asList(childWsdls));
        }
        
        File[] childFolders = folder.listFiles(getFolderFilter());
        if (childFolders != null && childFolders.length > 0 ) {
            for (File childFolder : childFolders) {
                wsdls.addAll(collectWSDLs(childFolder));
            }
        }
        
        return wsdls;
    }
    
    private List<WSDLModel> getWsdlModels() {
        List<WSDLModel> models = new ArrayList<WSDLModel>();
        List<File> sourceDirs = getSourceDirs();
        if (sourceDirs == null || sourceDirs.size() < 1) {
            return models;
        }
        List<File> wsdls = new ArrayList<File>();
        for (File src : sourceDirs) {
            wsdls.addAll(collectWSDLs(src));
        }
        
        for (File file : wsdls) {
            WSDLModel curWsdlModel = getWsdlModel(file);
            if (curWsdlModel != null) {
                models.add(curWsdlModel);
            }
        }

        return models;
    }


    public List<File> getDepedentProjectDirs() {
        return myDependentProjectDirs;
    }
    
    public List<File> getSourceDirs() {
        return mySourceDirs;
    }

    /**
     * Isn't thread safety
     */ 
    private FileFilter getFolderFilter() {
        if (myFolderFilter == null) {
            myFolderFilter = new FoldersFilter();
        }
        return myFolderFilter;
    }
    
    /**
     * Isn't thread safety
     */ 
    private FileFilter getWsdlFilter() {
        if (myWsdlFilter == null) {
            myWsdlFilter = new WsdlFileFilter();
        }
        return myWsdlFilter;
    }

    private class FoldersFilter implements FileFilter {

        public boolean accept(File f) {
            if (f == null) {
                return false;
            }
            
            return f.isDirectory();
        }
    }
    
    private class WsdlFileFilter implements FileFilter {

        public boolean accept(File f) {
            if (f == null || f.isDirectory()) {
                return false;
            }
            
            return "wsdl".equals(FileUtil.getExtension(f.getName()));
        }
    }

}
