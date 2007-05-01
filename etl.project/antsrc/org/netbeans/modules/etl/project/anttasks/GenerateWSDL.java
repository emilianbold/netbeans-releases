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
/*
 * GenerateWSDL.java
 *
 * Created on May 20, 2005, 7:47 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.etl.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

/**
 *
 */
public class GenerateWSDL extends Task {
	private String mSrcDirectoryLocation;

	private String mBuildDirectoryLocation;

	/** Creates a new instance of GenerateWSDL */
	public GenerateWSDL() {
	}

	/**
	 * @return Returns the srcDirectoryLocation.
	 */
	public String getSrcDirectoryLocation() {
		return mSrcDirectoryLocation;
	}

	/**
	 * @param srcDirectoryLocation
	 *            The srcDirectoryLocation to set.
	 */
	public void setSrcDirectoryLocation(String srcDirectoryLocation) {
		mSrcDirectoryLocation = srcDirectoryLocation;
	}

	/**
	 * @return Returns the srcDirectoryLocation.
	 */
	public String getBuildDirectoryLocation() {
		return mBuildDirectoryLocation;
	}

	/**
	 * @param buildDirectoryLocation
	 *            The srcDirectoryLocation to set.
	 */
	public void setBuildDirectoryLocation(String buildDirectoryLocation) {
		mBuildDirectoryLocation = buildDirectoryLocation;
	}

	public static List getFilesRecursively(File dir, FileFilter filter) {
		List ret = new ArrayList();
		if (!dir.isDirectory()) {
			return ret;
		}
		File[] fileNdirs = dir.listFiles(filter);
		for (int i = 0, I = fileNdirs.length; i < I; i++) {
			if (fileNdirs[i].isDirectory()) {
				ret.addAll(getFilesRecursively(fileNdirs[i], filter));
			} else {
				ret.add(fileNdirs[i]);
			}
		}
		return ret;
	}

	public static List getFilesRecursively(File dir, String[] extensions) {
		FileFilter filter = null;
		if (extensions[0].equals(".etl"))
			filter = new ETLExtensionFilter(extensions);
		else
			filter = new EngineExtensionFilter(extensions);
		return getFilesRecursively(dir, filter);
	}

	public void execute() throws BuildException {
		File srcDir = new File(mSrcDirectoryLocation);
		File bulldDir = getBuildDir();
		if (!srcDir.exists()) {
			throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
		}

		String[] etlExt = new String[] { ".etl" };
		List etlFiles = getFilesRecursively(srcDir, etlExt);

		generateEngineFiles(etlFiles);

		String[] engineExt = new String[] { "_engine.xml" };
		List etlEngineFiles = getFilesRecursively(bulldDir, engineExt);
		Map<String, Definition> wsdlMap = new HashMap<String, Definition>();
		
		try {
			for (int i = 0, I = etlEngineFiles.size(); i < I; i++) {
				File f = (File) etlEngineFiles.get(i);
				// TODO generate endpoint and jbi artifacts based on projectName
				// and Engine file name
				// String engineFileName =
				// srcDir.getAbsoluteFile().getParentFile().getName() +
				// f.getName().substring(0, f.getName().indexOf(".xml"));
				String engineFileName = f.getName().substring(0, f.getName().indexOf(".xml"));

				WsdlGenerator wg = new WsdlGenerator(f,engineFileName, srcDir.getCanonicalPath());
				Definition def = wg.generateWsdl();
				wsdlMap.put(def.getQName().getLocalPart(), def);
			}

			ETLMapWriter ew = new ETLMapWriter(wsdlMap, bulldDir.getCanonicalPath());
			ew.writeMap();

			// todo: This needed to be done ONLY at the build time....

			if (bulldDir.exists()) {
				// create META-INF if needed..
				String metaDirFile = bulldDir.getCanonicalPath() + "/META-INF";
				File metaDir = new File(metaDirFile);
				if (!metaDir.exists()) {
					metaDir.mkdir();
				}

			}
			JBIFileWriter fw = new JBIFileWriter(bulldDir.getCanonicalPath() + "/META-INF/jbi.xml",
					bulldDir.getCanonicalPath() + "/etlmap.xml");
			fw.writeJBI();

		} catch (Exception e) {
			throw new BuildException(e.getMessage());
		}
	}

	private void generateEngineFiles(List etlFiles) {
		Iterator iterator = etlFiles.iterator();
		EngineFileGenerator g = new EngineFileGenerator();
		while (iterator.hasNext()) {
			File element = (File) iterator.next();
			try {
				g.generateEngine(element, getBuildDir());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}

	private File getBuildDir() {
		File bulldDir = null;

		Project p = this.getProject();
		if (p != null) {
			String projPath = p.getProperty("basedir") + File.separator;
			String buildDirLoc = projPath + p.getProperty(IcanproProjectProperties.BUILD_DIR);
			bulldDir = new File(buildDirLoc);
		} else {
			bulldDir = new File(mBuildDirectoryLocation);
		}
		return bulldDir;
	}

	public static void main(String[] args) {
		GenerateWSDL task = new GenerateWSDL();
		task.setSrcDirectoryLocation("test");
		task.setBuildDirectoryLocation("test/build");
		task.execute();

	}
}
