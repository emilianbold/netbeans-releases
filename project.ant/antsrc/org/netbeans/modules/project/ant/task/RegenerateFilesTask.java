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

package org.netbeans.modules.project.ant.task;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.*;

// XXX should this task also do XML Schema validation of project.xml?

/**
 * Ant task to regenerate project metadata files (build scripts).
 * Currently semantics are identical to that of opening the project
 * in the IDE's GUI: i.e. both <code>build.xml</code> and <code>build-impl.xml</code>
 * will be regenerated if they are missing, or if they are out of date with
 * respect to either <code>project.xml</code> or the stylesheet (and are
 * not modified according to <code>genfiles.properties</code>).
 * @author Jesse Glick
 */
public final class RegenerateFilesTask extends Task {
    
    /** Standard constructor. */
    public RegenerateFilesTask() {}
    
    private File buildXsl;
    /**
     * Set the stylesheet to use for the main build script.
     * @param f a <code>build.xsl</code> file
     */
    public void setBuildXsl(File f) {
        // XXX could also support jar: URIs etc.
        buildXsl = f;
    }
    
    private File buildImplXsl;
    /**
     * Set the stylesheet to use for the automatic build script.
     * @param f a <code>build-impl.xsl</code> file
     */
    public void setBuildImplXsl(File f) {
        buildImplXsl = f;
    }
    
    private File projectDir;
    /**
     * Set the project directory to regenerate files in.
     * @param f the top directory of an Ant-based project
     */
    public void setProject(File f) {
        projectDir = f;
    }
    
    public void execute() throws BuildException {
        if (projectDir == null) {
            throw new BuildException("Must set 'project' attr", getLocation());
        }
        // XXX later may provide more control here...
        if (buildXsl == null && buildImplXsl == null) {
            throw new BuildException("Must set either 'buildxsl' or 'buildimplxsl' attrs or both", getLocation());
        }
        try {
            // Might be running inside IDE, in which case already have a mount...
            FileObject projectFO = FileUtil.toFileObject(projectDir);
            if (projectFO == null) {
                // Probably not running inside IDE, so mount it.
                // XXX for some reason including masterfs.jar in <taskdef> does not work. Why?
                // Possibly a bug in AntClassLoader.getResources(String)?
                LocalFileSystem lfs = new LocalFileSystem();
                lfs.setRootDirectory(projectDir);
                Repository.getDefault().addFileSystem(lfs);
                projectFO = lfs.getRoot();
                assert projectFO != null;
            }
            GeneratedFilesHelper h = new GeneratedFilesHelper(projectFO);
            if (buildXsl != null && h.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH, buildXsl.toURI().toURL(), true)) {
                log("Regenerating " + new File(projectDir, GeneratedFilesHelper.BUILD_XML_PATH.replace('/', File.separatorChar)).getAbsolutePath());
            }
            if (buildImplXsl != null && h.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH, buildImplXsl.toURI().toURL(), true)) {
                log("Regenerating " + new File(projectDir, GeneratedFilesHelper.BUILD_IMPL_XML_PATH.replace('/', File.separatorChar)).getAbsolutePath());
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        } catch (PropertyVetoException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
