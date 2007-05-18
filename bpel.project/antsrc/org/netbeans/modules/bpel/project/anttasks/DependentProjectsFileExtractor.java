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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/*
import org.netbeans.modules.compapp.models.bpelmodel.BPELDocument;
import org.netbeans.modules.compapp.models.bpelmodel.BPELDocumentParseFactory;
import org.netbeans.modules.compapp.models.bpelmodel.BPELParseContext;
import org.netbeans.modules.compapp.models.bpelmodel.Import;
import org.netbeans.modules.compapp.models.bpelmodel.ProjectBasedWSDLResolver;
import org.netbeans.modules.compapp.models.bpelmodel.ProjectBasedWSDLResolverFactory;
import org.netbeans.modules.compapp.models.bpelmodel.ProjectBasedXSDResolver;
import org.netbeans.modules.compapp.models.bpelmodel.ProjectBasedXSDResolverFactory;
import org.netbeans.modules.compapp.models.wsdlmodel.FastWSDLDefinitions;
import org.netbeans.modules.compapp.models.wsdlmodel.FastWSDLDefinitionsFactory;
*/

/**
 * @author radval
 *
 * Ant task to extract wsdl/xsd from dependent projects.
 */
public class DependentProjectsFileExtractor extends Task {

	private static final String WSDL_FILE_EXTENSION = "wsdl";
	
	private static final String XSD_FILE_EXTENSION = "xsd";
	
	private static final String BPEL_FILE_EXTENSION = "bpel";
	
	
	private String mBuildDirectory;
	
	private String mProjectDirectory;
	
	private String mProjectClassPath;
	
	private String mSourceDirectory;
	
	private File mProjectSrcDir;
	
	private File mBuildDir;
	
	private List mDependentProjectSourceDirs;
	
	
	// private static ProjectFileFilter projectFileFilter = new ProjectFileFilter();
	
	// private static BpelFileFilter bpelFileFilter = new BpelFileFilter();
	
	public void setBuildDirectory(String buildDirectory) {
		this.mBuildDirectory = buildDirectory;
	}
	
	public void setSourceDirectory(String srcDir) {
    	this.mSourceDirectory = srcDir;
    }
	
	public void setProjectClassPath(String projectClassPath) {
    	this.mProjectClassPath = projectClassPath;
    }
	
	public void setProjectDirectory(String srcDir) {
    	this.mProjectDirectory = srcDir;
    }

	public void execute() throws BuildException {
/*
		if(this.mBuildDirectory == null
		   || this.mBuildDirectory.trim().equals("")) {
			throw new BuildException("Missing build directory.");
		}
		
		if(this.mProjectDirectory == null) {
    		throw new BuildException("No project directory is set.");
    	}
    	
    	File projectDirectory = null;
    	
    	//create file object for project source directory
    	try {
    		projectDirectory = new File(this.mProjectDirectory);
    	} catch(Exception ex) {
    		throw new BuildException("Failed to get File object for project directory "+ this.mProjectDirectory, ex);
    	}
    	
//    	create file object for project source directory
    	try {
    		this.mProjectSrcDir = new File(this.mSourceDirectory);
    	} catch(Exception ex) {
    		throw new BuildException("Failed to get File object for project source directory "+ this.mSourceDirectory, ex);
    	}
    	
//		read project classpath
    	//TODO: refactor this to use wsdl classpath
    	this.mDependentProjectSourceDirs = new ArrayList();
    	if(this.mProjectClassPath != null 
    	   && !this.mProjectClassPath.trim().equals("")
		   && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
	    	StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
	        while (st.hasMoreTokens()) {
	            String spath = st.nextToken();
	            try {
	            	
	            	File sFile =  new File(projectDirectory.getCanonicalPath() + File.separator + spath);
	            	
	            	File srcFolder = new File(sFile.getParentFile().getParentFile().getCanonicalFile(), "src");
	            	this.mDependentProjectSourceDirs.add(srcFolder);
	            } catch(Exception ex) {
	            	throw new BuildException("Failed to create File object for dependent project path "+ spath);
	            }
	        }
    	}
    	
    	this.mBuildDir = new File(this.mBuildDirectory);
		
//    	Iterator it = this.mDependentProjectSourceDirs.iterator();
//    	while(it.hasNext()) {
//    		File pDir = (File) it.next();
//    		processDirectory(pDir, this.mBuildDir, this.mProjectSrcDir);
//    	}
    	ArrayList projectSourceDirs = new ArrayList();
    	projectSourceDirs.add(this.mProjectSrcDir);
    	processBpelFiles(projectSourceDirs, this.mDependentProjectSourceDirs);
*/
	}
/*
	private void processBpelFiles(List projectSourceDirs, 
								  List dependProjectSourceDirs) {
		
		Iterator it = projectSourceDirs.iterator();
		while(it.hasNext()) {
			File srcDir = (File) it.next();
			processBpelFiles(srcDir, dependProjectSourceDirs);
		}
	}
	
	private void processBpelFiles(File dir, List depedentProjectSourceDirs) {
		File[] children = dir.listFiles(bpelFileFilter);
		for(int i = 0; i < children.length; i++) {
			File child = children[i];
			if(child.isDirectory()) {
				processBpelFiles(child, depedentProjectSourceDirs);
			} else {
				//load existing bpel file 
				BPELDocument document = loadExistingBusinessProcess(child);
				if(document != null) {
					List imports = document.getDocumentProcess().getImports();
					
					Iterator it = imports.iterator();
					while(it.hasNext()) {
						Import imp = (Import) it.next();
						//is this imported file already available in current project
						//the we do nothing
						boolean isExists = isFileAvailableInCurrentProject(imp, this.mProjectSrcDir);
						if(isExists) {
							continue;
						} else {
							extractImportedFileFromDepedentProject(imp, this.mDependentProjectSourceDirs);
						}
					}
				}
			}
		}
	}
	
	private boolean isFileAvailableInCurrentProject(Import imp, File dir) {
		boolean result = false;
		String location = imp.getLocation();
		
		File[] children = dir.listFiles(projectFileFilter);
		for(int i =0; i < children.length; i++) {
			File child = children[i];
			if(child.isDirectory()) {
				result = isFileAvailableInCurrentProject(imp, child);
			} else if(location.equals(child.getName())) {
				result = true;
			}
			
			if(result) {
				break;
			}
		}
		
		return result;
	}
	
	private void extractImportedFileFromDepedentProject(Import imp, List dependentProjectSourceDirs) {
		Iterator it = dependentProjectSourceDirs.iterator();
		while(it.hasNext()) {
			File dDir = (File) it.next();
			extractImportedFileFromDepedentProject(imp, dDir);
		}
		
	}
	
	private void extractImportedFileFromDepedentProject(Import imp, File dependentProjectSourceDir) {
		String location = imp.getLocation();
		boolean isWsdlFile = false;
		if(Import.WSDL_IMPORT_TYPE.equals(location)) {
			extractWsdlFromDependentProject(imp, dependentProjectSourceDir);
		} else {
			extractXsdFromDepedentProject(imp, dependentProjectSourceDir);
		}
		
	}
	
	private void extractWsdlFromDependentProject(Import imp, File dSrcDir) {
		String fileName = imp.getLocation();
		File[] children = dSrcDir.listFiles(projectFileFilter);
		for(int i =0; i < children.length; i++) {
			File from = children[i];
			if(from.isDirectory()) {
				extractWsdlFromDependentProject(imp, from);
			} else if(from.getName().equals(fileName)) {
				File to = new File(this.mBuildDir, from.getName());
				//if file exists log to user, it means we are trying to import
				//same file in two different bpel.
				if(to.exists()) {
					//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
					this.log("Skipping File "+ to.getName() + ", a file with this name has already been extracted in "+ mBuildDir);
					continue;
				}
				
				try {
					writeToFile(from, to);
					
					//now load fastwsdl definitions
					//and also look for import within a wsdl
					FastWSDLDefinitions def = 
						FastWSDLDefinitionsFactory.getInstance().newFastWSDLDefinitions(from.getAbsolutePath(), true);
					if(def.getParseErrorMessage() != null) {
						throw new BuildException(def.getParseErrorMessage());
					}
					Iterator it = def.getImports().iterator();
					while(it.hasNext()) {
						Import im = (Import) it.next();
						//TODO: we need to also look for files in any dependent project
						//of this dependent project
						extractImportedFileFromDepedentProject(im, dSrcDir);
					}
				} catch(Exception ex) {
					throw new BuildException("Error writing to file "+ to, ex);
				}
			}
		}
	}
	
	private void extractXsdFromDepedentProject(Import imp, File dSrcDir) {
		String fileName = imp.getLocation();
		File[] children = dSrcDir.listFiles(projectFileFilter);
		for(int i =0; i < children.length; i++) {
			File from = children[i];
			if(from.isDirectory()) {
				extractXsdFromDepedentProject(imp, from);
			} else if(from.getName().equals(fileName)) {
				File to = new File(this.mBuildDir, from.getName());
				//if file exists log to user, it means we are trying to import
				//same file in two different bpel.
				if(to.exists()) {
					//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
					this.log("Skipping File "+ to.getName() + ", a file with this name has already been extracted in "+ mBuildDir);
					continue;
				}
				
				try {
					writeToFile(from, to);
					
//					//now load fastxsd 
//					//and also look for import within an xsd
//					FastWSDLDefinitions def = 
//						FastWSDLDefinitionsFactory.getInstance().newFastWSDLDefinitions(from.getAbsolutePath(), true);
//					if(def.getParseErrorMessage() != null) {
//						throw new BuildException(def.getParseErrorMessage());
//					}
//					Iterator it = def.getImports().iterator();
//					while(it.hasNext()) {
//						Import im = (Import) it.next();
//						//TODO: we need to also look for files in any dependent project
//						//of this dependent project
//						extractImportedFileFromDepedentProject(im, dSrcDir);
//					}
					
//					File projectDir = dSrcDir.getParentFile();
//					findProjectClasspath(projectDir);
				} catch(Exception ex) {
					throw new BuildException("Error writing to file "+ to, ex);
				}
			}
		}
	}
	
	private void extractFileFromDepedentProject(String fileName, File dSrcDir) {
		File[] children = dSrcDir.listFiles(projectFileFilter);
		for(int i =0; i < children.length; i++) {
			File from = children[i];
			if(from.isDirectory()) {
				extractFileFromDepedentProject(fileName, from);
			} else if(from.getName().equals(fileName)) {
				File to = new File(this.mBuildDir, from.getName());
				//if file exists log to user, it means we are trying to import
				//same file in two different bpel.
				if(to.exists()) {
					//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
					this.log("Skipping File "+ to.getName() + ", a file with this name has already been extracted in "+ mBuildDir);
					continue;
				}
				
				try {
					writeToFile(from, to);
				} catch(Exception ex) {
					throw new BuildException("Error writing to file "+ to, ex);
				}
			}
		}
	}
	
	private void processDirectory(File depedentProjectSrcDir, File buildDir, File sourceDirectory) {
		File[] children = depedentProjectSrcDir.listFiles(projectFileFilter);
		for(int i =0; i < children.length; i++) {
			File from = children[i];
			
			File to = new File(buildDir, from.getName());
			//if file exists throw exception
			if(to.exists()) {
				//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
				this.log("Skipping File "+ to.getName() + ", a file with this name has already been extracted in "+ buildDir);
				continue;
			} else if(isFileExist(sourceDirectory, to.getName())) {
				this.log("Skipping File "+ to.getName() + ", a file with this name already exists in project source directory "+ sourceDirectory);
				continue;
			}
			
			try {
				writeToFile(from, to);
			} catch(Exception ex) {
				throw new BuildException("Error writing to file "+ to, ex);
			}
		}
		
		
	}
	
	private boolean isFileExist(File SourceDirectory, String fileName) {
		boolean result = false;
		File[] children = SourceDirectory.listFiles(projectFileFilter);
		for(int i = 0; i < children.length; i++) {
			File child = children[i];
			if(child.getName().equals(fileName)) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	private void writeToFile(File from, File to) throws Exception {
		BufferedInputStream bIn = new BufferedInputStream( new FileInputStream(from));
		BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(to));
		
		byte[] buf = new byte[10000];
		
		int state = 0;
		
		while(state != -1) {
			state = bIn.read(buf);
			bOut.write(buf);
		}
		
		bOut.flush();
		bOut.close();
	}
	
	private BPELDocument loadExistingBusinessProcess(File bpelFile) throws BuildException {
		BPELDocument document = null;
		try {
			FileReader fReader = new FileReader(new File(bpelFile.getPath()));
			BPELParseContext parseContext = new BPELParseContext.DefaultParseContext();
			ProjectBasedWSDLResolver wsdlLoader = ProjectBasedWSDLResolverFactory.getInstance().newWSDLResolver(bpelFile.toURI().toString(), parseContext);
			parseContext.setWSDLResolver(wsdlLoader);
			
                        ProjectBasedXSDResolver xsdResolver = ProjectBasedXSDResolverFactory.getInstance().newXSDResolver(bpelFile.toURI().toString(), parseContext);
			parseContext.setXSDResolver(xsdResolver);
			
			//do not load imported schemas
			parseContext.setLoadOnlyPartnersAndImports(true);
			parseContext.setLoadImportedWsdls(false);
			parseContext.setLoadImportedXsds(false);
			parseContext.getWSDLParseContext().setParseInlineSchema(false);
			parseContext.getWSDLParseContext().setParseImportedSchemas(false);
			parseContext.getWSDLParseContext().setEnableEvents(false);
			parseContext.setEnableEvents(false);
			
			document = BPELDocumentParseFactory.getInstance().load(fReader, parseContext);
			
		} catch (Exception ex) {
			throw new BuildException ("Failed to load bpel process " + bpelFile.getPath(), ex);
		}
		return document;
	}
	
//	private String findProjectClasspath(File projectDir) {
//		String projectClasspath =  null;
//		
//		Project project = new Project();
//		project.init();
//		File buildFile = new File(projectDir, "build.xml");
//		ProjectHelper pHelper = ProjectHelper.getProjectHelper();
//		pHelper.parse(project, buildFile);
//		
//		//now look for "javac.classpath"
//		projectClasspath = project.getProperty("javac.classpath");
//		
//		System.out.println("project classpath "+ projectClasspath );
//		return projectClasspath;
//	}
	
	 static class ProjectFileFilter implements FileFilter {
    	
    	public boolean accept(File pathname) {
    		boolean result = false;
    		if(pathname.isDirectory()) {
    			return true;
    		}
    		
    		String fileName = pathname.getName();
    		String fileExtension = null;
    		int dotIndex = fileName.lastIndexOf('.');
    		if(dotIndex != -1) {
    			fileExtension = fileName.substring(dotIndex +1);
    		}
    		
    		if(fileExtension != null 
    		   && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION) || fileExtension.equalsIgnoreCase(XSD_FILE_EXTENSION))) {
    			result = true;
    		}
    		
    		return result;
		}
	 }
	 
	 static class BpelFileFilter implements FileFilter {
    	
    	public boolean accept(File pathname) {
    		boolean result = false;
    		if(pathname.isDirectory()) {
    			return true;
    		}
    		
    		String fileName = pathname.getName();
    		String fileExtension = null;
    		int dotIndex = fileName.lastIndexOf('.');
    		if(dotIndex != -1) {
    			fileExtension = fileName.substring(dotIndex +1);
    		}
    		
    		if(fileExtension != null 
    		   && (fileExtension.equalsIgnoreCase(BPEL_FILE_EXTENSION))) {
    			result = true;
    		}
    		
    		return result;
		}
	 }
*/
}
