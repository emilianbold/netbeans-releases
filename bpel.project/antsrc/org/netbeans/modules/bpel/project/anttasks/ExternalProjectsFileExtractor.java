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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
public class ExternalProjectsFileExtractor extends Task {
	
	private static final String WSDL_FILE_EXTENSION = "wsdl";
	
	private static final String XSD_FILE_EXTENSION = "xsd";
	
	private static final String BPEL_FILE_EXTENSION = "bpel";
	
	
	private String mBuildDirectory;
	
	private String mProjectDirectory;
	
	private String mProjectClassPath;
	
	private String mSourceDirectory;
	
	private File mProjectSrcDir;
	
	private File mBuildDir;
	
	private List mDependentProjectArtifactJars;
	
	
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
    	this.mDependentProjectArtifactJars = new ArrayList();
    	if(this.mProjectClassPath != null 
    	   && !this.mProjectClassPath.trim().equals("")
		   && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
	    	StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
	        while (st.hasMoreTokens()) {
	            String spath = st.nextToken();
	            try {
	            	
	            	File jarFile =  new File(projectDirectory.getCanonicalPath() + File.separator + spath);
	            	this.mDependentProjectArtifactJars.add(jarFile);
	            } catch(Exception ex) {
	            	throw new BuildException("Failed to create File object for dependent project path "+ spath);
	            }
	        }
    	}
    	
    	this.mBuildDir = new File(this.mBuildDirectory);

    	ArrayList projectSourceDirs = new ArrayList();
    	projectSourceDirs.add(this.mProjectSrcDir);
    	processBpelFiles(projectSourceDirs);
*/
    }
/*
	private void processBpelFiles(List projectSourceDirs) {
		
		Iterator it = projectSourceDirs.iterator();
		while(it.hasNext()) {
			File srcDir = (File) it.next();
			processBpelFiles(srcDir);
		}
	}
	
	private void processBpelFiles(File dir) {
		File[] children = dir.listFiles(bpelFileFilter);
		for(int i = 0; i < children.length; i++) {
			File child = children[i];
			if(child.isDirectory()) {
				processBpelFiles(child);
			} else {
				//load existing bpel file 
				BPELDocument document = loadExistingBusinessProcess(child);
				if(document != null) {
					List imports = document.getDocumentProcess().getImports();
					
					Iterator it = imports.iterator();
					while(it.hasNext()) {
						Import imp = (Import) it.next();
						String location = imp.getLocation();
						String importType = imp.getImportType();
						if(importType == null || importType.trim().equals("")) {
							throw new BuildException("Missing importType "+ imp + " in "+ child);
						}
						
						if(location == null || location.trim().equals("")) {
							throw new BuildException("Missing location "+ imp + " in "+ child);
						}
						
						//is this imported file already available in current project
						//the we do nothing
						boolean isExists = isFileAvailableInCurrentProject(imp, this.mProjectSrcDir);
						if(isExists) {
							continue;
						} else {
							extractImportedFileFromDepedentProject(imp, this.mDependentProjectArtifactJars);
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
	
	private void extractImportedFileFromDepedentProject(Import imp, 
														List dependentProjectArtifactJars) {
		Iterator it = dependentProjectArtifactJars.iterator();
		while(it.hasNext()) {
			File jarFile = (File) it.next();
			try {
				JarFile seJarFile = new JarFile(jarFile);
				extractImportedFileFromDepedentProject(imp, seJarFile);
			} catch(Exception ex) {
				throw new BuildException(ex);
			}
			
		}
		
	}
	
	private void extractImportedFileFromDepedentProject(Import imp, JarFile jarFile) {
		String importType = imp.getImportType();
		boolean isWsdlFile = false;
		if(Import.WSDL_IMPORT_TYPE.equals(importType)) {
			extractWsdlFromDependentProject(imp, jarFile);
		} else {
			extractXsdFromDepedentProject(imp, jarFile);
		}
		
	}
	
	private void extractWsdlAndOtherImportedFiles(ZipEntry from, JarFile seJarFile) {
		File to = new File(this.mBuildDir, from.getName());
		//if file exists log to user, it means we are trying to import
		//same file in two different bpel.
		if(to.exists()) {
			//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
			this.log("Skipping File "+ from + ", a file with this name has already been extracted in "+ mBuildDir);
			return;
		}
		
		try {
			writeToFile(seJarFile.getInputStream(from), to);
			
			//now load fastwsdl definitions
			//and also look for import within a wsdl
			FastWSDLDefinitions def = 
				FastWSDLDefinitionsFactory.getInstance().newFastWSDLDefinitions(seJarFile.getInputStream(from), true);
			if(def.getParseErrorMessage() != null) {
				throw new BuildException(def.getParseErrorMessage());
			}
			Iterator it = def.getImports().iterator();
			while(it.hasNext()) {
				Import im = (Import) it.next();
				String location = im.getLocation();
				String importType = im.getImportType();
				if(importType == null || importType.trim().equals("")) {
					throw new BuildException("Missing importType "+ im + " in "+ from);
				}
				
				if(location == null || location.trim().equals("")) {
					throw new BuildException("Missing location "+ im + " in "+ from);
				}
				
				extractImportedFileFromDepedentProject(im, seJarFile);
			}
		} catch(Exception ex) {
			throw new BuildException("Error writing to file "+ to, ex);
		}
	}
	
	private void extractXsdAndOtherImportedFiles(ZipEntry from, JarFile seJarFile) {
		File to = new File(this.mBuildDir, from.getName());
		//if file exists log to user, it means we are trying to import
		//same file in two different bpel.
		if(to.exists()) {
			//throw new BuildException("Can not extract file "+ to + " to " + buildDir + ", A file already exist with this name.");
			this.log("Skipping File "+ to.getName() + ", a file with this name has already been extracted in "+ mBuildDir);
			return;
		}
		//TODO: look into xsd imports and also copy those
		throw new BuildException("Need to implement: look into xsd imports and also copy those");
//		try {
//			InputStream zipIn = seJarFile.getInputStream(from);
//			writeToFile(zipIn, to);
//			
//			//now load fastxsd definitions
//			//and also look for import within a xsd
//			FastWSDLDefinitions def = 
//				FastWSDLDefinitionsFactory.getInstance().newFastWSDLDefinitions(zipIn, true);
//			if(def.getParseErrorMessage() != null) {
//				throw new BuildException(def.getParseErrorMessage());
//			}
//			Iterator it = def.getImports().iterator();
//			while(it.hasNext()) {
//				Import im = (Import) it.next();
//				extractImportedFileFromDepedentProject(im, seJarFile);
//			}
//		} catch(Exception ex) {
//			throw new BuildException("Error writing to file "+ to, ex);
//		}
	}
	
	private void extractWsdlFromDependentProject(Import imp, JarFile seJarFile) {
		String wsdlFileName = imp.getLocation();
		try {
            if(seJarFile != null) {
                Enumeration enumeration = seJarFile.entries();
                while(enumeration.hasMoreElements() == true) {
                	ZipEntry entry = (ZipEntry) enumeration.nextElement();
                    if(entry != null) {
                        String fileName = entry.getName();
                        //file name matches
                        if((fileName.equals(wsdlFileName))) {
                        	extractWsdlAndOtherImportedFiles(entry, seJarFile);
                        }
                    }
                }
            }
	        
       } catch(Exception ex) {
			throw new BuildException(ex);
	   }
	}
	
	private void extractXsdFromDepedentProject(Import imp, JarFile seJarFile) {
		String wsdlFileName = imp.getLocation();
		try {
            if(seJarFile != null) {
                Enumeration enumeration = seJarFile.entries();
                while(enumeration.hasMoreElements() == true) {
                	ZipEntry entry = (ZipEntry) enumeration.nextElement();
                    if(entry != null) {
                        String fileName = entry.getName().toLowerCase();
                        //file name matches
                        if((fileName.equals(wsdlFileName))) {
                        	extractXsdAndOtherImportedFiles(entry, seJarFile);
                        }
                    }
                }
            }
	        
       } catch(Exception ex) {
			throw new BuildException(ex);
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
	
	private void writeToFile(InputStream from, File to) throws Exception {
		BufferedInputStream bIn = new BufferedInputStream(from);
		BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(to));
		int buffersize = 1024;
		byte[] buf = new byte[buffersize];
		
		int count = 0;
		
		while(( count = bIn.read(buf, 0, buffersize)) != -1) {
			bOut.write(buf,0, count);
		}
		
		bOut.flush();
		bOut.close();
		bIn.close();
		
	}
	
	private void writeToFile(File from, File to) throws Exception {
		writeToFile(new FileInputStream(from), to);
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
