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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.lookup.Lookups;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.BuildException;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates BPEL Module
 * @author Sreenivasan Genipudi
 */
public class ValidateBPELProject extends Task {
    private String mSourceDirectory;
    private String mProjectClassPath;
    private String mBuildDirectory;
    private String mBuildDependentProjectFilesDirectory;
    private List mDependentProjectDirs;
    private File mSourceDir;
    private File mBuildDir;
    private Map mBpelFileNamesToFileInBuildDir = new HashMap();
    private boolean isFoundErrors = false;
    private boolean mAllowBuildWithError = false;

    public ValidateBPELProject() {}
    
    public void setSourceDirectory(String srcDir) {
      this.mSourceDirectory = srcDir;
    }
    
    public void setBuildDirectory(String buildDir) {
      this.mBuildDirectory = buildDir;
    }

    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        mAllowBuildWithError = !mAllowBuildWithError;
    }
            
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                mAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                mAllowBuildWithError = true;
            }
        }
    }  
            
    public void setClasspathRef(Reference ref) {
    }    

    public void setProjectClassPath(String projectClassPath) {
      this.mProjectClassPath = projectClassPath;
    }
    
    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
      this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }
    
    public boolean isFoundErrors() {
        return this.isFoundErrors;
    }
    
    public void execute() throws BuildException {
//System.out.println("11");
        if(this.mSourceDirectory == null) {
                throw new BuildException("No directory is set for source files.");
        }
        
        if(this.mBuildDirectory == null) {
                throw new BuildException("No build directory is set.");
        }
        
        if(this.mBuildDependentProjectFilesDirectory == null) {
                throw new BuildException("No dependentProjectFiles directory is set.");
        }
//System.out.println("22");
        try {
                this.mSourceDir = new File(this.mSourceDirectory);
                CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);
                
        } catch(Exception ex) {
                throw new BuildException("Failed to get File object for project source directory "+ this.mSourceDirectory, ex);
        }
//System.out.println("33");
        try {
                this.mBuildDir = new File(this.mBuildDirectory);
        } catch(Exception ex) {
                throw new BuildException("Failed to get File object for project build directory "+ this.mBuildDirectory, ex);
        }
//System.out.println("44");
        ArrayList projectDirs = new ArrayList();

        if(this.mProjectClassPath != null && !this.mProjectClassPath.trim().equals("") && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
                StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
                while (st.hasMoreTokens()) {
                    String spath = st.nextToken();
                    try {
                        
                        File sFile =  new File(mSourceDir.getParentFile().getCanonicalPath() + File.separator + spath);
                        
                        File srcFolder = new File(sFile.getParentFile().getParentFile().getCanonicalFile(), "src");
                        projectDirs.add(srcFolder);
                    } catch(Exception ex) {
                        throw new BuildException("Failed to create File object for dependent project path "+ spath);
                    }
                }
        }
//System.out.println("55");
        processBpelFilesFolderInBuildDir(this.mBuildDir);
        this.mDependentProjectDirs = projectDirs;
        ArrayList sourceDirs = new ArrayList();
        sourceDirs.add(this.mSourceDir);
//System.out.println("66");
        processSourceDirs(sourceDirs);
//System.out.println("77");
    }
    
    private void processBpelFilesFolderInBuildDir(File folder) {
        File files[] = folder.listFiles(new Util.BpelFileFilter());
                for(int i =0; i < files.length; i++) {
                                File file = files[i];
                                if(file.isFile()) {
                                        processBpelFilesInBuildDir(file);
                                } else {
                                        processBpelFilesFolderInBuildDir(file);
                                }
                }
    }    
    
    private void processBpelFilesInBuildDir(File bpelFile) {
        String relativePath = RelativePath.getRelativePath(this.mBuildDir, bpelFile);
                this.mBpelFileNamesToFileInBuildDir.put(relativePath, bpelFile);
    }
    
    private void processSourceDirs(List sourceDirs) {
                Iterator it = sourceDirs.iterator();
                while(it.hasNext()) {
                        File sourceDir = (File) it.next();
                        processSourceDir(sourceDir);
                }
                
        }
        
        private void processSourceDir(File sourceDir) {
                processFileObject(sourceDir);
        }
        
        private void processFileObject(File file) {
                if (file.isDirectory()) {
                        processFolder(file);
                } else {
                        //processFile(file);
                }
        }
        
        private void processFolder(File fileDir) {
                File[] bpelFiles = fileDir.listFiles(new Util.BpelFileFilter());
                processBpelFiles(bpelFiles);
        }
        
        private void processFile(File file) {
                String fileName = file.getName();
                String fileExtension = null;
                int dotIndex = fileName.lastIndexOf('.');
                if(dotIndex != -1) {
                        fileExtension = fileName.substring(dotIndex +1);
                }
                
                if (fileExtension != null) {
                        if(fileExtension.equalsIgnoreCase("bpel")) {
                                processBpelFile(file);
                        } 
                }
        }
        
        private void processBpelFiles(File[]bpelFiles) {
                for (int i = 0; i < bpelFiles.length; i++) {
                        if(bpelFiles[i].isFile()) {
                                processBpelFile(bpelFiles[i]);
                        } else {
                                processFolder(bpelFiles[i]);
                        }
                }
        }
        
        private void processBpelFile(File bpelFile) {
                        if(isBpelFileModified(bpelFile)) {
                                loadAndValidateExistingBusinessProcess(bpelFile);
                        }
        }
        
        private boolean isBpelFileModified(File bpelFile) {
                boolean modified = true;
                String relativePath = RelativePath.getRelativePath(this.mSourceDir, bpelFile);
                File bpelFileInBuildDir = (File) this.mBpelFileNamesToFileInBuildDir.get(relativePath);
                
                if(bpelFileInBuildDir != null) {
                        if(bpelFileInBuildDir.lastModified() == bpelFile.lastModified()) {
                                modified = false;
                        }
                }
                return modified;
        }
        
        private void validateBPEL(File bpel) throws BuildException {
//System.out.println("111");
            BpelModel model = null;
            
            try {
//System.out.println("222");
                model = BPELCatalogModel.getDefault().getBPELModel(bpel.toURI());
//System.out.println("333");
            }
            catch (Exception e) {
                throw new RuntimeException("Error while trying to create BPEL Model", e);
            }
//System.out.println("444");
            Validation validation = new Validation();
            validation.validate((org.netbeans.modules.xml.xam.Model) model, ValidationType.COMPLETE);
            Collection col = validation.getValidationResult();
            boolean isError = false;

//System.out.println("555");
            for (Iterator itr = col.iterator(); itr.hasNext();) {
               ResultItem resultItem = (ResultItem) itr.next();
               logValidationErrors(bpel, resultItem);

               if(resultItem.getType() == Validator.ResultType.ERROR) {
                   isError = true;
               }
            }
//System.out.println("666");
            if (isError) {
                this.isFoundErrors = true;
            }
        }
        
        private void logValidationErrors(File bpelFile, ResultItem resultItem) {
          int lineNumber = 0;
          int columnNumber = 0;
          String errorDescription = resultItem.getDescription();
          String msgType = resultItem.getType().name();
          Component component = resultItem.getComponents();
          File file = null;

          if(component != null) {
            lineNumber = ModelUtil.getLineNumber(component);
            columnNumber = ModelUtil.getColumnNumber(component);
            file = (File) component.getModel().getModelSource().getLookup().lookup(File.class);        
            showError(file,columnNumber, lineNumber, errorDescription, msgType);
          }
          else {
            columnNumber = resultItem.getColumnNumber();
            lineNumber = resultItem.getLineNumber(); 
            file =(File)resultItem.getModel().getModelSource().getLookup().lookup(File.class);  
            showError(file,columnNumber, lineNumber, errorDescription, msgType);
          }
        }
        
        private void showError(File file, int columnNumber, int lineNumber, String errorDescription, String msgType) {
            StringBuffer lineNumStr = new StringBuffer(5);
            StringBuffer columnNumStr = new StringBuffer(5);

            if(lineNumber != -1) {
              lineNumStr.append(":");
              lineNumStr.append(lineNumber);
              lineNumStr.append(",");
            }
            if(columnNumber != -1) {
              columnNumStr.append(" column:"); 
              columnNumStr.append(columnNumber);
              columnNumStr.append(" ");
            }
            msgType = msgType + ": "; 
            StringBuffer msg = new StringBuffer(100);
            msg.append(msgType);

            if (file != null) {
              msg.append(file.getPath());
            }
            msg.append(lineNumStr);
            msg.append(columnNumStr + "\n");
            msg.append(errorDescription);

            if ( !mAllowBuildWithError) {
              System.out.println(msg);
              System.out.println();
            }
        }
        
        private void loadAndValidateExistingBusinessProcess(File bpelFile) throws BuildException {
                try {
                    validateBPEL(bpelFile);
                } catch (Throwable e) {
//System.out.println();
//System.out.println();
//System.out.println("---------");
//System.out.println();
//System.out.println();
//e.printStackTrace();
                        if ( !mAllowBuildWithError) {
                            throw new BuildException(e);
                        }
                }
        }
}
