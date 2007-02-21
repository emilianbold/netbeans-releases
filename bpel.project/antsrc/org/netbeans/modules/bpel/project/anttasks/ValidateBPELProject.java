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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates BPEL Module
 * @author Sreenivasan Genipudi
 */
public class ValidateBPELProject extends Task {
    //Member variable representing source directory
    /**
     * Source directory
     */
    private String mSourceDirectory;
    //Member variable representing project classpath
    /**
     * Project classpath
     */
    private String mProjectClassPath;
    //Member variable representing build directory
    /**
     * Build directory
     */
    private String mBuildDirectory;
    //Member variable representing dependent project files directory
    /**
     * List fo dependent project files
     */
    private String mBuildDependentProjectFilesDirectory;
    //Member variable representing dependent project directories
    /**
     * Dependent project directories
     */
    private List mDependentProjectDirs;
    //Member variable representing source dir
    private File mSourceDir;
    //Member variable representing build dir
    private File mBuildDir;
    //Member variable representing map of file names to BPEL file
    // used to check if the file was changed since lasttime the 
    // file was validated.
    private Map mBpelFileNamesToFileInBuildDir = new HashMap();
    
    private boolean isFoundErrors = false;
    
    private boolean mAllowBuildWithError = false;
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(ValidateBPELProject.class.getName());

    /**
     * Constructor
     */
    public ValidateBPELProject() {
    }
    
    
    /**
     * Set the source directory
     * @param srcDir Source directory
     */
    public void setSourceDirectory(String srcDir) {
    this.mSourceDirectory = srcDir;
    }
    
    /**
     * Set the build directory
     * @param buildDir build directory
     */
    public void setBuildDirectory(String buildDir) {
    this.mBuildDirectory = buildDir;
    }
    /**
     * Run validation
     * @param srcDir source directory
     */
    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        mAllowBuildWithError = !mAllowBuildWithError;
    }
            
    /**
     * Run validation
     * @param srcDir source directory
     */
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                mAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                mAllowBuildWithError = true;
            }
        }
        
    }  
            
    
    /**
     * Set the classpath reference
     * @param ref Classpath Reference
     */
    public void setClasspathRef(Reference ref) {

    }    
    
    /**
     * Set the project classpath
     * @param projectClassPath Project classpath
     */
    public void setProjectClassPath(String projectClassPath) {
    this.mProjectClassPath = projectClassPath;
    }
    
    /**
     * Set the dependent project files directory
     * @param dependentProjectFilesDir dependent project files directory
     */
    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
    this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }
    
    public boolean isFoundErrors() {
    	return this.isFoundErrors;
    }
    
    /**
     * Validate the BPEL Model
     */
    public void execute() throws BuildException {
       
        if(this.mSourceDirectory == null) {
                throw new BuildException("No directory is set for source files.");
        }
        
        if(this.mBuildDirectory == null) {
                throw new BuildException("No build directory is set.");
        }
        
        if(this.mBuildDependentProjectFilesDirectory == null) {
                throw new BuildException("No dependentProjectFiles directory is set.");
        }
        
                
        //create file object for project source directory
        try {
                this.mSourceDir = new File(this.mSourceDirectory);
                CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);
                
        } catch(Exception ex) {
                throw new BuildException("Failed to get File object for project source directory "+ this.mSourceDirectory, ex);
        }
        
        //create file object for project build directory
        try {
                this.mBuildDir = new File(this.mBuildDirectory);
        } catch(Exception ex) {
                throw new BuildException("Failed to get File object for project build directory "+ this.mBuildDirectory, ex);
        }
        
        
        //read project classpath
        //TODO: refactor this to use wsdl classpath. also we are assuming that source
        //are in src we should look into project properties. but this is done like 
        //this in all icanpro projects??
        ArrayList projectDirs = new ArrayList();
        if(this.mProjectClassPath != null 
           && !this.mProjectClassPath.trim().equals("")
                   && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
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
        
        processBpelFilesFolderInBuildDir(this.mBuildDir);
        this.mDependentProjectDirs = projectDirs;
        
        ArrayList sourceDirs = new ArrayList();
        sourceDirs.add(this.mSourceDir);
        
        processSourceDirs(sourceDirs);
        
  //      if(foundValidationErrors) {
   //             throw new BuildException("Found compilation errors in project files.");
    //    }   
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
        //always get relative path from build directory.
        //this relative path later is compared with relative path of 
        //a file from source dir
        //(for a same file both should be equal)
        //example : c:\bpelproject\build\bank\bank.bpel
        //and  c:\bpelproject\src\bank\bank.bpel
        //so relative path will be bank\bank.bpel
        //from build as well as from src directory.
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
            BpelModel model = null;
            try {
                model = BPELCatalogModel.getDefault().getBPELModel(bpel.toURI());
            }catch (Exception ex) {
             //   ex.printStackTrace();
                throw new RuntimeException(" Error while trying to create BPEL Model ",ex);
            }
            //Validator validator = (Validator) Lookups.metaInfServices(getClass().getClassLoader()).lookup(Validator.class);
             Validation validation = new Validation();
             validation.validate((org.netbeans.modules.xml.xam.Model)model,  ValidationType.COMPLETE);
            Collection col  =validation.getValidationResult();
            boolean isError = false;
            //Collection col = validation.getValidationResult();
            for (Iterator itr = col.iterator(); itr.hasNext();) {
               ResultItem resultItem = (ResultItem) itr.next();
               logValidationErrors(bpel, resultItem);
               if(resultItem.getType() == Validator.ResultType.ERROR) {
                   isError = true;
               }
            }
            
            if(isError) {
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
                    showError(file,columnNumber, lineNumber,errorDescription,msgType );
                

            }else {
                columnNumber = resultItem.getColumnNumber();
                lineNumber = resultItem.getLineNumber(); 
                file =(File)resultItem.getModel().getModelSource().getLookup().lookup(File.class);  
                showError(file,columnNumber, lineNumber,errorDescription ,msgType);
            }
        
        }
        
        private void showError(File file, int columnNumber, int lineNumber, String errorDescription, String msgType) {

            StringBuffer lineNumStr = new StringBuffer(5);
            StringBuffer columnNumStr = new StringBuffer(5);

            if(lineNumber != -1) {
            lineNumStr.append(":");
            lineNumStr.append(lineNumber);
            lineNumStr.append(":");

            }

            if(columnNumber != -1) {
            columnNumStr.append(" column:"); 
            columnNumStr.append(columnNumber);
            columnNumStr.append(" ");
            }

            
            msgType = msgType + ": "; 

            StringBuffer msg = new StringBuffer(100);
            if(file != null) {
                msg.append(file.getPath());
            }

            msg.append(lineNumStr);
            msg.append(columnNumStr);
            msg.append(msgType);
            msg.append(errorDescription);


            System.out.println(msg.toString());            
        }
        
        private void loadAndValidateExistingBusinessProcess(File bpelFile) throws BuildException {
                try {
                    validateBPEL(bpelFile);
  
                } catch (Throwable ex) {
                        System.out.println("Validation has errors on "+bpelFile.getAbsolutePath());
                        logger.log(Level.SEVERE, "Validation has errors on "+bpelFile.getAbsolutePath() );
                        System.out.println("Error Message - "+ ex.getMessage());
                        
                        if ( ex.getMessage() != null) {
                            logger.severe( ex.getMessage());
                        }
                        if (!mAllowBuildWithError) {
                            StringWriter writer = new StringWriter();
                            PrintWriter pWriter = new PrintWriter(writer);
                            ex.printStackTrace(pWriter);
                            throw new BuildException(ex);
                        }
                       // logValidationErrors(ToDoEvent.Severity.ERROR, bpelFile, "Error compiling bpel process \n"+ writer.toString(), "please check if bpel file is valid", 1, 1 );
                }
                
        }
        
}
