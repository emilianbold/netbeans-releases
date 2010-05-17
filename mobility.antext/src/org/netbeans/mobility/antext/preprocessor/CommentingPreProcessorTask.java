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
 * Created on Jan 8, 2004
 *
 *
 */
package org.netbeans.mobility.antext.preprocessor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.netbeans.mobility.antext.Bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** An Ant task that will preprocess a java source file. Lines marked with tags recognized by
 * the preprocessor will be commented out or in depending on if they are marked as being a part
 * of the current configuration.
 *
 * <p>Attributes:<ol>
 * <li>SrcDir - Semi-required. Source directory location with java source files for preprocessing. At least one of SrcDir, nested fileset, or FileSetRef have to be set.
 * <li>Nested fileset - Semi-required. Fileset with sources for preprocessing. Even if fileset contains other files, this task preprocesses .java files only.
 * <li>FileSetRef - Semi-required. Fileset specified by reference.
 * <li>DestDir - Optional. Destination directory location. If not specified, files are modified in-place.
 * <li>PreprocessFor - Optional. For what classes should be preprocess. Default is default.
 * <li>Encoding - Optional. Specifies encoding used for reading and writing files.
 * </ol>
 *
 * <p>Up-to-date check: Check is performed only if DestDir is specified. File is preprocessed if and only if DestDir is specified and the file newer than the one in destination directory.
 *
 * @author Greg Crawley, David Kaspar, Adam Sotona
 *
 */
public class CommentingPreProcessorTask extends Task
{
    
    /** holds value of source directory. */
    ArrayList<FileSet> sources=null;
    
    /** holds value of destination directory. */
    File destDir=null;
    
    /** the configuration that we're currently parsing for. */
    String abilities;
    
    /** the enconding that we're using for reading and writing file. */
    String encoding = System.getProperty("file.encoding"); //NOI18N
    
    
    /** Setter for configuration name. Value should match string literals used in if-defs.
     * @param preprocessFor Comma delimited list containing all valid identifiers to preprocess for (i.e., configuration name and all abilities for that configuration)
     */
    public void setPreprocessFor(final String abilities)
    {
        this.abilities = abilities;
    }
    
    
    /** Setter for srcDir.
     * @param srcDir New value of srcDir.
     */
    public void setSrcDir(final File srcDir)
    {
        final FileSet fs = createFileSet();
        fs.setDir(srcDir);
        fs.setIncludes("**/*.java"); //NOI18N
    }
    
    /** Creates FileSet.
     * @return Created FileSet
     */
    public FileSet createFileSet()
    {
        if (sources == null)
            sources = new ArrayList<FileSet>();
        final FileSet source = new FileSet();
        sources.add(source);
        return source;
    }
    
    /** Setter for fileSetRef.
     * @param fileSetRef New value of fileSetRef.
     */
    public void setFileSetRef(final Reference fileSetRef)
    {
        createFileSet().setRefid(fileSetRef);
    }
    
    /** Setter for destDir.
     * @param destDir New value of destDir.
     */
    public void setDestDir(final File destDir)
    {
        this.destDir=destDir;
    }
    
    /** Setter for encoding.
     * @param encoding New value of encoding.
     */
    public void setEncoding(final String encoding)
    {
        this.encoding=encoding;
    }
    
    /**
     * Preprocess the files.
     * @throws BuildException if attribute is missing or there is a problem during files preprocessing.
     */
    public void execute() throws BuildException
    {
        // long startTime=System.currentTimeMillis();
        
        if (sources==null)
            throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "srcDir, fileSetRef, or nested fileset")); // NO I18N
        
        // if the source and destination directories are the same, then trigger overwrite-mode
        final ArrayList<File> preprocessQueue = new ArrayList<File>();
        
        for (int a = 0; a < sources.size(); a ++)
        {
            final DirectoryScanner ds = sources.get(a).getDirectoryScanner(getProject());
            final String baseDir = ds.getBasedir().getAbsolutePath();
            //            log ("BaseDir: >" + baseDir + "<", Project.MSG_VERBOSE);
            final String[] included = ds.getIncludedFiles();
            for (int b = 0; b < included.length; b ++)
            {
                final int lastdot = included[b].lastIndexOf('.');
                if (lastdot < 0  ||  ! "java".equals(included[b].substring(lastdot + 1).toLowerCase())) //NOI18N
                    continue;
                //                log ("File no. " + b + ": >" + included[b] + "<", Project.MSG_VERBOSE);
                final File srcFile = new File(baseDir + File.separator + included[b]);
                final File destFile = new File(destDir.getAbsolutePath() + File.separator + included[b]);
                // check if destFile is up-to-date
                if (!destFile.exists()  ||  srcFile.lastModified() > destFile.lastModified())
                {
                    preprocessQueue.add(srcFile);
                    preprocessQueue.add(destFile);
                }
            }
        }
        log(Bundle.getMessage("MSG_PreprocessingXFiles", "" + (preprocessQueue.size() / 2), destDir.getAbsolutePath()), Project.MSG_INFO); // NO I18N
        int errors = 0, warnings = 0;
        for (int a = 0; a < preprocessQueue.size();)
        {
            final File srcFile = preprocessQueue.get(a ++);
            final File destFile = preprocessQueue.get(a ++);
            try
            {
                log(Bundle.getMessage("MSG_PreprocessingFile", srcFile.getAbsolutePath()), Project.MSG_VERBOSE); // NO I18N
                
                final CommentingPreProcessor cpp = new CommentingPreProcessor(new FileSource(srcFile), new FileDestination(destFile), abilities);
                cpp.run();
                
                for ( final PPLine ppl : cpp.getLines() ) {
                    for ( final PPLine.Error e : ppl.getErrors() ) {
                        log(srcFile.getAbsolutePath() + ':' + e.toString(), e.warning ? Project.MSG_WARN : Project.MSG_ERR);
                        if (e.warning) warnings++;
                        else errors++;
                    }
                }
            }
            catch (Exception e)
            {
                throw new BuildException(e);
            }
        }
        if (warnings > 0) log(Bundle.getMessage("MSG_Warnings", String.valueOf(warnings)), Project.MSG_WARN);//NOI18N
        if (errors > 0)
        {
            log(Bundle.getMessage("MSG_Errors", String.valueOf(errors)), Project.MSG_ERR);//NOI18N
            throw new BuildException(Bundle.getMessage("MSG_PreprocessingError"));//NOI18N
        }
    }
    
    private final class FileSource implements CommentingPreProcessor.Source
    {
        private final File f;
        
        public FileSource(File f)
        {
            this.f = f;
        }
        
        public Reader createReader() throws IOException
        {
            return new InputStreamReader(new FileInputStream(f), encoding);
        }
        
    }
    
    private final class FileDestination implements CommentingPreProcessor.Destination
    {
        private final File f;
        
        public FileDestination(File f)
        {
            this.f = f;
        }
        
        public void doInsert(@SuppressWarnings("unused")
		final int line, @SuppressWarnings("unused")
		final String s) throws IOException
        {
        }
        
        public void doRemove(@SuppressWarnings("unused")
		final int line, @SuppressWarnings("unused")
		final int column, @SuppressWarnings("unused")
		final int length) throws IOException
        {
        }
        
        public Writer createWriter(final boolean validOutput) throws IOException
        {
            if (!validOutput) return null;
            f.getParentFile().mkdirs();
            return new OutputStreamWriter(new FileOutputStream(f), encoding);
        }
        
    }
}
