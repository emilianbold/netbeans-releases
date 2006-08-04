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

package org.netbeans.mobility.antext;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;

/**
 * ExtractTask Task adds support for extracting libraries from classpath.
 *
 * <p>Attributes:<ol>
 * <li>ClassPath - Semi-required. Specifies classpath. All archives on classpath. It supports jar and zip archives only, not directories. If this attribute is not specified, you need to specified either ClassPathRef attribute or nested ClassPath tag.
 * <li>ClassPathRef - Semi-required. Specifies classpath by reference.
 * <li>Nested ClassPath - Semi-required. Specifies classpath using nested element.
 * <li>Dir - Required. Specifies target directory in which all files from classpath should be extracted to.
 * <li>ExcludeManifest - Optional. Specifies if META-INF/Manifest.mf files should be excluded from extraction. Default: false.
 * </ol>
 *
 * @author David Kaspar
 */
public class ExtractTask extends Task
{
    
    /** Holds value of property classpath. */
    private Path classPath;
    
    /** Holds value of property dir. */
    private File dir;
    
    /** Holds value of property excludeManifest. */
    private boolean excludeManifest = false;
    
    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during creating or managing empty-api archives.
     */
    public void execute() throws BuildException
    {
        if (classPath == null)
            throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "classPath")); // NO I18N
        if (dir == null)
            throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "dir")); // NO I18N
        if (!dir.exists()  ||  !dir.isDirectory())
            throw new BuildException(Bundle.getMessage("ERR_Extract_InvalidDir", dir != null ? dir.getAbsolutePath() : null)); // NO I18N
        final String[] archives = classPath.list();
        
        long srcLastModified = Long.MIN_VALUE;
        long destLastModified = Long.MAX_VALUE;
        
        if (archives != null) for (int a = 0; a < archives.length; a ++)
        {
            final File source = new File(archives[a]);
            if (! source.exists())
                continue;
            if (source.isFile())
            {
                final String name = source.getName().toLowerCase();
                if (name.endsWith(".zip")  ||  name.endsWith(".jar"))
                { //NOI18N
                    final long time = source.lastModified();
                    if (time > srcLastModified)
                        srcLastModified = time;
                }
            }
            else if (source.isDirectory())
            {
                final FileSet fileset = new FileSet();
                fileset.setDir(source);
                final DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
                final File basedir = ds.getBasedir();
                final String[] files = ds.getIncludedFiles();
                for (int b = 0; b < files.length; b ++)
                {
                    final long time = new File(basedir, files[b]).lastModified();
                    if (time > srcLastModified)
                        srcLastModified = time;
                }
            }
        }
        
        final FileSet targetfileset = new FileSet();
        targetfileset.setDir(dir);
        final DirectoryScanner targetds = targetfileset.getDirectoryScanner(getProject());
        final File targetbasedir = targetds.getBasedir();
        final String[] targetfiles = targetds.getIncludedFiles();
        for (int a = 0; a < targetfiles.length; a ++)
        {
            final long time = new File(targetbasedir, targetfiles[a]).lastModified();
            if (time < destLastModified)
                destLastModified = time;
        }
        if (destLastModified != Long.MAX_VALUE  &&  srcLastModified <= destLastModified)
        {
            log(Bundle.getMessage("MSG_Extract_UpToDate", dir.getAbsolutePath()), Project.MSG_VERBOSE); // No I18N
            return;
        }
        
        if (archives != null) for (int a = 0; a < archives.length; a ++)
        {
            final File source = new File(archives[a]);
            log(Bundle.getMessage("MSG_Extract_ProcessingPath", source.getAbsolutePath()), Project.MSG_VERBOSE); // NO I18N
            if (!source.exists())
            {
                log(Bundle.getMessage("WARN_Extract_IgnoringPath", source.getAbsolutePath()), Project.MSG_WARN); // NO I18N
                continue;
            }
            if (source.isFile())
            {
                final String name = source.getName().toLowerCase();
                if (! name.endsWith(".zip")  &&  ! name.endsWith(".jar"))
                { //NOI18N
                    log(Bundle.getMessage("WARN_Extract_IgnoringPath", source.getAbsolutePath()), Project.MSG_WARN); // NO I18N
                    continue;
                }
                extractZip(source, dir);
            }
            else if (source.isDirectory())
            {
                copyDir(source, dir);
            }
        }
    }
    
    private void extractZip(final File source, final File target) throws BuildException
    {
        final Expand e = new Expand();
        e.setProject(getProject());
        if (excludeManifest)
        {
            final PatternSet ps = new PatternSet();
            ps.setExcludes("META-INF,META-INF/MANIFEST.MF"); //NOI18N
            e.addPatternset(ps);
        }
        e.setSrc(source);
        e.setDest(target);
        e.execute();
    }
    
    private void copyDir(final File source, final File target) throws BuildException
    {
        final Copy c = new Copy();
        c.setProject(getProject());
        final FileSet fileset = new FileSet();
        fileset.setDir(source);
        c.addFileset(fileset);
        c.setTodir(target);
        c.execute();
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
    
    /**
     * Setter for property dir.
     * @param dir New value of property dir.
     */
    public void setDir(final File dir)
    {
        this.dir = dir;
    }
    
    /**
     * Setter for property excludeManifest.
     * @param excludeManifest New value of property excludeManifest.
     */
    public void setExcludeManifest(final boolean excludeManifest)
    {
        this.excludeManifest = excludeManifest;
    }
    
}
