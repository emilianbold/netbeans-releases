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

package org.netbeans.modules.j2me.common.ant;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
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
 * <li>ExcludeClassPath - Optional. Specifies classpath to exclude from extraction.
 * <li>ExcludeClassPathRef - Optional. Specifies classpath to exclude by reference.
 * <li>Nested ExcludeClassPath - Optional. Specifies classpath to exclude using nested element.
 * <li>Dir - Required. Specifies target directory in which all files from classpath should be extracted to.
 * <li>ExcludeManifest - Optional. Specifies if META-INF/Manifest.mf files should be excluded from extraction. Default: false.
 * </ol>
 *
 * @author David Kaspar, Adam Sotona
 */
public class ExtractTask extends Task
{

    /** Holds value of property classpath and exclasspath. */
    private Path classPath, exClassPath;
    
    /** Holds value of property dir. */
    private File dir;
    
    /** Holds value of property excludeManifest. */
    private boolean excludeManifest = false;

    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during creating or managing empty-api archives.
     */
    @Override
    public void execute() throws BuildException
    {
        if (classPath == null)
            throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "classPath")); // NO I18N
        if (dir == null)
            throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "dir")); // NO I18N
        if (!dir.exists()  ||  !dir.isDirectory())
            throw new BuildException(Bundle.getMessage("ERR_Extract_InvalidDir", dir != null ? dir.getAbsolutePath() : null)); // NO I18N
        final String[] archives = classPath.list();
        final Set excludes = exClassPath == null ? Collections.EMPTY_SET : new HashSet(Arrays.asList(exClassPath.list()));
        final Map<String, Boolean> libletsInProject = LibletUtils.loadLibletsInProject(getProject());

        if (archives != null) for (int a = 0; a < archives.length; a ++)
        {
            if (excludes.contains(archives[a])) continue;
            final Map<Object, Object> manifestAttributes = LibletUtils.getJarManifestAttributes(archives[a]);
            if (LibletUtils.isJarLiblet(manifestAttributes)
                    && !libletsInProject.get(LibletUtils.getLibletDetails(manifestAttributes))) {
                //do not extract this LIBlet
                continue;
            }
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
        e.setOverwrite(false);
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
        c.setOverwrite(false);
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
     * Setter for property classPath.
     * @param classPath New value of property classPath.
     */
    public void setExcludeClassPath(final Path classPath)
    {
        createExcludeClassPath().append(classPath);
    }
    
    /**
     * Creates ClassPath.
     * @return Created Path
     */
    public Path createExcludeClassPath()
    {
        if (exClassPath == null)
        {
            exClassPath = new Path(getProject());
        }
        return exClassPath.createPath();
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
     * Setter for property classPathRef.
     * @param classPathRef New value of property classPathRef.
     */
    public void setExcludeClassPathRef(final Reference classPathRef)
    {
        createExcludeClassPath().setRefid(classPathRef);
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
