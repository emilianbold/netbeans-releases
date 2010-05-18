/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * PreverifyTask.java
 *
 * Created on 15. prosinec 2003, 9:12
 */
package org.netbeans.mobility.antext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.MissingResourceException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Preverify Task adds support for classes preverification.
 *
 * <p>Attributes:<ol>
 * <li>SrcDir - Required. Source directory location with classes for preverification.
 * <li>DestDir - Required. Destination (preverified) directory location.
 * <li>PlatformHome - Required. Home directory location of platform/emulator.
 * <li>PlatformType - Optional. Platform type - one of these: "UEI 1.0", "UEI 1.0.1" (default), Custom. When Custom is set, Configuration is ignored.
 * <li>Configuration - Optional. Device configuration - one of these: "CLDC-1.0", "CLDC-1.1". Default value depends on platform.
 * <li>ClassPath - Optional. Classpath required by classes in SrcDir. It supports nested classpath tag too.
 * <li>ClassPathRef - Optional. Classpath specified by reference required by SrcJar classes.
 * <li>CommandLine - Semi-required. Command line for running preverification. Required when PlatformType is Custom. See Command Line paragraph for more info.
 * </ol>
 *
 * <p>Up-to-date check: Classes are preverified if and only if any file in the source directory is newer than any file in the target directory or there is no file in target directory.
 *
 * <p>CommandLine attribute allows you to fully customize command line for running preverification.
 * Command Line string processed using EMapFormat class (see its javadoc for formatting hints). This task presets following identificators:<ul>
 * <li>platformhome - always set - value of PlatformHome attribute
 * <li>srcdir - value of SrcDir attribute
 * <li>destdir - value of DestDir attribute
 * <li>classpath - classpath string composed from ClassPath, ClassPathRef, nested ClassPath
 * <li>/ - value of File.separator
 * </ul>
 * If value of attribute is not set, its value is not passed to Command Line string formatter.
 *
 * @author  Adam Sotona, David Kaspar
 */
public class PreverifyTask extends Task
{
    
    private static final String DEFAULT_PLATFORM_TYPE = "UEI-1.0"; // NO I18N
    
    /** Holds value of property srcDir. */
    private File srcDir;
    
    /** Holds value of property destDir. */
    private File destDir;
    
    /** Holds value of property classPath. */
    private Path classPath;
    
    /** Holds value of property configuration. */
    private String configuration;
    
    /** Holds value of property platformHome. */
    private File platformHome;
    
    /** Holds value of property platformType. */
    private String platformType;
    
    /** Holds value of property commandLine. */
    private String commandLine = null;
    
    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during updating classes preverification.
     */
    public void execute() throws BuildException
    {
        if (platformHome == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "platformHome")); // NO I18N
        if (srcDir == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "srcDir")); // NO I18N
        if (destDir == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "destDir")); // NO I18N
        if (!platformHome.isDirectory()) throw new BuildException(Bundle.getMessage("ERR_MissingPlatformHome", platformHome.toString())); // NO I18N
        if (!srcDir.isDirectory()) throw new BuildException(Bundle.getMessage("ERR_MissingSrcDir", srcDir.toString())); // NO I18N
        if (!destDir.isDirectory()) throw new BuildException(Bundle.getMessage("ERR_MissingDestDir", destDir.toString())); // NO I18N
        
        FileSet fileset;
        DirectoryScanner ds;
        File baseDir;
        String[] files;
        long srcLastModified = Long.MIN_VALUE;
        long destLastModified = Long.MAX_VALUE;
        
        fileset = new FileSet();
        fileset.setDir(srcDir);
        ds = fileset.getDirectoryScanner(getProject());
        baseDir = ds.getBasedir();
        files = ds.getIncludedFiles();
        final long srcFilesCount = files.length;
        for (int a = 0; a < files.length; a ++)
        {
            final long time = new File(baseDir, files[a]).lastModified();
            if (time > srcLastModified)
                srcLastModified = time;
        }
        fileset = new FileSet();
        fileset.setDir(destDir);
        ds = fileset.getDirectoryScanner(getProject());
        baseDir = ds.getBasedir();
        files = ds.getIncludedFiles();
        for (int a = 0; a < files.length; a ++)
        {
            final long time = new File(baseDir, files[a]).lastModified();
            if (time < destLastModified)
                destLastModified = time;
        }
        if (destLastModified != Long.MAX_VALUE  &&  srcLastModified <= destLastModified)
        {
            log(Bundle.getMessage("MSG_PreverifiedClassesAreUpToDate", destDir.getAbsolutePath()), Project.MSG_VERBOSE); // No I18N
            return;
        }
        log(Bundle.getMessage("MSG_Preverifying", "" + srcFilesCount, destDir.getAbsolutePath()), Project.MSG_INFO); // NO I18N
        if (platformType == null)
        {
            log(Bundle.getMessage("WARN_DefaultPlatform", DEFAULT_PLATFORM_TYPE), Project.MSG_WARN); // No I18N
            platformType = DEFAULT_PLATFORM_TYPE;
        }
        if (Bundle.getMessage("NAME_CUSTOM").equals(platformType))
        { // NO I18N
            if (commandLine == null)
            {
                log(Bundle.getMessage("WARN_MissingCommandLine", DEFAULT_PLATFORM_TYPE), Project.MSG_WARN); // No I18N
                platformType = DEFAULT_PLATFORM_TYPE;
            }
        }
        else
            commandLine = null;
        if (commandLine == null)
        {
            String defCfg;
            try
            {
                defCfg = Bundle.getMessage("CFG_Preverify_" + platformType); // No I18N
            }
            catch (MissingResourceException mre)
            {
                throw new BuildException(Bundle.getMessage("ERR_UnsupportedPlatform", platformType)); // No I18N
            }
            if (configuration != null) try
            {
                commandLine = Bundle.getMessage("CMD_Preverify_" + platformType + "_" + configuration); // No I18N
            }
            catch (MissingResourceException mre)
            {
                log(Bundle.getMessage("WARN_UnsupportedConfig", platformType, configuration, defCfg), Project.MSG_WARN); // No I18N
                commandLine = Bundle.getMessage("CMD_Preverify_" + platformType + "_" + defCfg); //NOI18N
            }
            else
            {
                log(Bundle.getMessage("MSG_DefaultConfig", platformType, defCfg), Project.MSG_WARN); // No I18N
                commandLine = Bundle.getMessage("CMD_Preverify_" + platformType + "_" + defCfg); // No I18N
            }
        }
        final HashMap<String,Object> args = new HashMap<String,Object>();
        args.put("platformhome", platformHome.getAbsolutePath()); // No I18N
        if (classPath != null)
            args.put("classpath", classPath.toString()); // No I18N
        args.put("destdir", destDir.getAbsolutePath()); // No I18N
        args.put("srcdir", srcDir.getAbsolutePath()); // No I18N
        args.put("/", File.separator); // NO I18N
        commandLine = EMapFormat.format(commandLine, args);
        final String[] commands = Commandline.translateCommandline(commandLine);
        log(Bundle.getMessage("MSG_ExecCmd", commandLine), Project.MSG_VERBOSE); // No I18N
        for (int a = 0; a < commands.length; a ++)
            log(">" + commands[a] + "<", Project.MSG_VERBOSE); // NO I18N
        try
        {
            final Execute exec = new Execute();
            exec.setAntRun(getProject());
            exec.setVMLauncher(true);
            exec.setCommandline(commands);
            final int i = exec.execute();
            if (i != 0) throw new BuildException(Bundle.getMessage("ERR_PreverifyFailed", String.valueOf(i))); // No I18N
        }
        catch (IOException ioe)
        {
            throw new BuildException(ioe);
        }
    }
    
    /**
     * Setter for property srcDir.
     * @param srcDir New value of property srcDir.
     */
    public void setSrcDir(final File srcDir)
    {
        this.srcDir = srcDir;
    }
    
    /** Setter for property destDir.
     * @param destDir New value of property destDir.
     *
     */
    public void setDestDir(final File destDir)
    {
        this.destDir = destDir;
    }
    
    /**
     * Setter for property classPath.
     * @param classPath New value of property classPath.
     */
    public void setClassPath(final Path classPath)
    {
        createClassPath().append(classPath);
    }
    
    /**
     * Creates ClassPath.
     * @return Created Path
     */
    public Path createClassPath()
    {
        if (classPath == null)
        {
            classPath = new Path(getProject());
        }
        return classPath.createPath();
    }
    
    /**
     * Setter for property classPathRef.
     * @param classPathRef New value of property classPathRef.
     */
    public void setClassPathRef(final Reference classPathRef)
    {
        createClassPath().setRefid(classPathRef);
    }
    
    /** Setter for property configuration.
     * @param configuration New value of property configuration.
     *
     */
    public void setConfiguration(final String configuration)
    {
        this.configuration = configuration.toUpperCase();
    }
    
    /** Setter for property platformHome.
     * @param platformHome New value of property platformHome.
     *
     */
    public void setPlatformHome(final File platformHome)
    {
        this.platformHome = platformHome;
    }
    
    /** Setter for property platformType.
     * @param platformType New value of property platformType.
     *
     */
    public void setPlatformType(final String platformType)
    {
        this.platformType = platformType.toUpperCase();
    }
    
    /**
     * Setter for property commandLine.
     * @param commandLine New value of property commandLine.
     */
    public void setCommandLine(final String commandLine)
    {
        this.commandLine = commandLine;
    }
    
}
