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
 * ObfuscateTask.java
 *
 * Created on 15. prosinec 2003, 9:13
 */
package org.netbeans.mobility.antext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Obfuscate Task adds support for jar file obfuscation. Currently it supports ProGuard obfuscator only.
 *
 * <p>Attributes:<ol>
 * <li>SrcJar - Required. Source jar file location for obfuscation.
 * <li>DestJar - Required. Destination (obfuscated) jar file location.
 * <li>ObfuscatorType - Optional. One of these obfuscator names: NONE, ProGuard. Task automatically checks ObfuscatorClassPath. NONE type just copies SrcJar file to DestJar file. Default: PROGUARD
 * <li>ClassPath - Optional. Classpath required by SrcJar classes. It supports nested classpath tag too.
 * <li>ClassPathRef - Optional. Classpath specified by reference required by SrcJar classes.
 * <li>ObfuscatorClassPath - Semi-required. Classpath required by obfuscator to run. There must be obfuscator classpath defined because of automatic check for obfuscators (if they exists). Additionally this task takes value of "libs.&lt;Lower_Case_Ofuscator_Type&gt;.classpath" and adds it into classpath.
 * <li>ObfuscatorClassPathRef - Semi-required. Classpath specified by reference required by obfuscator to run.
 * <li>Nested ObfuscatorClassPath - Semi-required. Classpath specified by nested element required by obfuscator to run.
 * <li>Exclude - Optional. Comma separated list of classes which should be excluded from obfuscator process.
 * <li>ObfuscationLevel - Optional. Level of obfuscation. Valid values: 0-9. Default: 0.
 * <li>ExtraScript - Optional. An extra obfuscators commands.
 * </ol>
 *
 * <p>Up-to-date check: Destination (obfuscated) jar file is created if and only if source jar file is newer than destination jar file.
 *
 * <p>Obfuscation levels (= What is obfuscated):<ol>
 * <li>ObfuscationLevel = 0/default: Nothing.
 * <li>ObfuscationLevel = 1: Custom obfuscation level - use ExtraScript to define all commands.
 * <li>ObfuscationLevel = 2: Private fields and methods.
 * <li>ObfuscationLevel = 3-4: Private/default fields, methods, and classes.
 * <li>ObfuscationLevel = 5-6: Private/default/protected fields, methods, and classes. Used mainly for libraries.
 * <li>ObfuscationLevel = 7-8: Everything except public fields and methods of public classes. Used mainly for libraries and applications.
 * <li>ObfuscationLevel = 9: Everything except public methods of MIDlet classes. Used mainly for applications.
 * </ol>
 *
 * @author  Adam Sotona, David Kaspar
 */
public class ObfuscateTask extends Task
{
    
    private static final boolean DELETE_SCRIPT = true;
    
    private static final String LIBS_OBFUSCATOR_CLASSPATH_PROPERTY_NAME = "libs.{0}.classpath"; //NOI18N
    
    private static final String OBFUSCATOR_TYPE_NONE = "NONE"; //NOI18N
    
    /** Holds value of property srcJar. */
    private File srcJar;
    
    /** Holds value of property destJar. */
    private File destJar;
    
    /** Holds value of property obfuscatorType. */
    private String obfuscatorType = "PROGUARD"; //NOI18N
    
    /** Holds value of property classPath. */
    private Path classPath;
    
    /** Holds value of property exclude. */
    private String exclude;
    
    /** Holds value of property extraScript. */
    private String extraScript;
    
    /**
     * Holds value of property obfuscatorClassPath.
     */
    private Path obfuscatorClassPath;
    
    /** Holds value of property obfuscationLevel. */
    private int obfuscationLevel;
    
    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during jar file obfuscation.
     */
    public void execute() throws BuildException
    {
        if (srcJar == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "srcJar")); // NO I18N
        if (destJar == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "destJar")); // NO I18N
        if (destJar.exists()  &&  srcJar.lastModified() <= destJar.lastModified())
        {
            log(Bundle.getMessage("MSG_JarIsUpToDate", destJar.getAbsolutePath()), Project.MSG_WARN); // NO I18N
            return;
        }
        if (obfuscatorClassPath == null)
            createObfuscatorClassPath();
        final ArrayList obfuscators = Collections.list(new StringTokenizer(Bundle.getMessage("LIST_Obfuscators"), ",")); // NO I18N
        final ArrayList<String> foundObfuscators = new ArrayList<String>();
        for (int a = 0; a < obfuscators.size(); a ++)
        {
            final String obfs = (String) obfuscators.get(a);
            String[] jointClassPaths = obfuscatorClassPath.list();
            final String libsObfsClassPath = getProject().getProperty(MessageFormat.format(LIBS_OBFUSCATOR_CLASSPATH_PROPERTY_NAME, new Object[] { obfs.toLowerCase() }));
            if (libsObfsClassPath != null)
                jointClassPaths = joinPaths(jointClassPaths, new Path(getProject(), libsObfsClassPath).list());
            try
            {
                if (!isPresent(jointClassPaths, Bundle.getMessage("COND_" + obfs))) // NO I18N
                    continue;
            }
            catch (MissingResourceException e)
            {
            }
            foundObfuscators.add(obfs);
        }
        if (obfuscatorType == null  ||  !foundObfuscators.contains(obfuscatorType))
        {
            if (foundObfuscators.size() <= 0)
                throw new BuildException(Bundle.getMessage("ERR_NoObfuscator")); // NO I18N
            final String old = obfuscatorType;
            obfuscatorType = foundObfuscators.get(0);
            if (old == null)
                log(Bundle.getMessage("WARN_ObfuscatorTypeNullMissing", obfuscatorType), Project.MSG_WARN); // NO I18N
            else
                log(Bundle.getMessage("WARN_ObfuscatorTypeMissing", old, obfuscatorType), Project.MSG_WARN); // NO I18N
        }
        if (!obfuscators.contains(obfuscatorType)) throw new BuildException(Bundle.getMessage("ERR_UnknownObfuscator", obfuscatorType)); // NO I18N
        
        if (obfuscationLevel < 0  ||  obfuscationLevel >= 10)
        {
            log(Bundle.getMessage("WARN_InvalidObfuscationLevel", "" + obfuscationLevel), Project.MSG_WARN); // NO I18N
            obfuscationLevel = 0;
        }
        
        // NONE obfuscatorType
        if (OBFUSCATOR_TYPE_NONE.equals(obfuscatorType) || obfuscationLevel == 0)
        {
            doNoObfuscation();
            return;
        }
        
        String commandLine = Bundle.getMessage("CMD_" + obfuscatorType); // NO I18N
        final HashMap<String,Object> args = new HashMap<String,Object>();
        args.put("javahome", System.getProperty("java.home")); // NO I18N
        if (obfuscatorType != null)
        {
            final String p = getProject().getProperty(MessageFormat.format(LIBS_OBFUSCATOR_CLASSPATH_PROPERTY_NAME, new Object[] { obfuscatorType.toLowerCase() })); // NO I18N
            if (p != null)
                createObfuscatorClassPath().add(new Path(getProject(), p));
        }
        if (obfuscatorClassPath != null)
        {
            final String tmp = obfuscatorClassPath.toString();
            if (tmp.length() > 0)
                args.put("obfuscatorclasspath", tmp); // NO I18N
        }
        if (classPath != null)
        {
            final String tmp = classPath.toString();
            if (tmp.length() > 0)
            {
                args.put("classpath", tmp); // NO I18N
                final String[] paths = classPath.list();
                final StringBuffer sb = new StringBuffer();
                if (paths != null  &&  paths.length > 0) {
                    for (int a = 0; a < paths.length; a ++) {
                        if (new File(paths[a]).exists()) {
                            if (sb.length() > 0) sb.append(File.pathSeparator);
                            sb.append("'" + paths[a] + "'"); // NO I18N
                        } else {
                            log(Bundle.getMessage("MSG_SkippingPathElement", paths[a]), Project.MSG_VERBOSE); // NO I18N
                        }
                    }
                    args.put("quotedclasspath", sb.toString()); // NO I18N
                }
            }
        }
        args.put("srcjar", srcJar.getAbsolutePath()); // NO I18N
        args.put("destjar", destJar.getAbsolutePath()); // NO I18N
        args.put(":", File.pathSeparator); // NO I18N
        args.put("/", File.separator); // NO I18N
        
        // exclude classes
        final ArrayList<String> excludeClasses = new ArrayList<String>();
        if (exclude != null  &&  !"".equals(exclude))
        { // NO I18N
            final StringTokenizer e = new StringTokenizer(exclude, ","); // NO I18N
            while (e.hasMoreTokens())
                excludeClasses.add(e.nextToken());
        }
        log(Bundle.getMessage("MSG_ExcludingClasses", excludeClasses.toString()), Project.MSG_VERBOSE); // NOI18N
        
        // opening output obfuscator script
        File script;
        PrintWriter pw;
        try
        {
            script = File.createTempFile("obfuscator.script.", null); // NO I18N
        }
        catch (IOException e)
        {
            throw new BuildException(Bundle.getMessage("ERR_CannotCreateTempFile", System.getProperty("java.io.tmpdir"))); // NO I18N
        }
        try
        {
            pw = new PrintWriter(new FileOutputStream(script));
        }
        catch (FileNotFoundException e)
        {
            if (DELETE_SCRIPT) script.delete();
            throw new BuildException(Bundle.getMessage("ERR_IOException", script.getAbsolutePath())); // NO I18N
        }
        
        // processing input obfuscator script
        final String obfuscatorScriptName = "obfuscators/" + obfuscatorType.toLowerCase() + "."  + obfuscationLevel + ".txt"; // NO I18N
        final InputStream is = getClass().getResourceAsStream(obfuscatorScriptName);
        if (is == null)
            throw new BuildException(Bundle.getMessage("ERR_ObfuscatorScriptIsMissing", obfuscatorScriptName)); // NO I18N
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try
        {
            for (;;)
            {
                final String readLine = br.readLine();
                if (readLine == null)
                    break;
                pw.println(readLine);
            }
        }
        catch (IOException e)
        {
            throw new BuildException(Bundle.getMessage("ERR_ErrorReadingObfsScript", obfuscatorScriptName)); // NO I18N
        }
        finally
        {
            try
            {
                br.close();
            }
            catch (IOException ioe)
            {}
        }
        
        if (extraScript != null) pw.println(extraScript);
        
        // adding exclude classes
        final HashMap<String,Object> excludeClassesMap = new HashMap<String,Object>();
        excludeClassesMap.put("leftbrace", "{"); // NO I18N
        excludeClassesMap.put("rightbrace", "}"); // NO I18N
        for (int a = 0; a < excludeClasses.size(); a ++)
        {
            excludeClassesMap.put("classname", excludeClasses.get(a)); // NO I18N
            excludeClassesMap.put("slashedclassname", (excludeClasses.get(a)).replace('.', '/')); // NO I18N
            pw.println(EMapFormat.format(Bundle.getMessage("SCR_ExcludeClasses_" + obfuscatorType), excludeClassesMap)); // NO I18N
        }
        if (pw.checkError())
        {
            pw.close();
            if (DELETE_SCRIPT) script.delete();
            throw new BuildException(Bundle.getMessage("ERR_IOException", script.getAbsolutePath())); // NO I18N
        }
        pw.close();
        
        args.put("script", script.getAbsolutePath()); // NO I18N
        commandLine = EMapFormat.format(commandLine, args);
        final String[] commands = Commandline.translateCommandline(commandLine);
        log(Bundle.getMessage("MSG_ExecCmd", commandLine), Project.MSG_VERBOSE); // NO I18N
        for (int a = 0; a < commands.length; a ++)
            log(">" + commands[a] + "<", Project.MSG_VERBOSE); // NO I18N
        try
        {
            final Execute exec = new Execute(new PumpStreamHandler(System.out, System.out)); //to avoid red colored text in obfuscator output
            exec.setAntRun(getProject());
            exec.setVMLauncher(true);
            exec.setCommandline(commands);
            final int i = exec.execute();
            if (i != 0)
            {
                if (DELETE_SCRIPT) script.delete();
                throw new BuildException(Bundle.getMessage("ERR_ObfuscationFailed", String.valueOf(i))); // NO I18N
            }
        }
        catch (IOException ioe)
        {
            throw new BuildException(ioe);
        }
        finally
        {
            if (DELETE_SCRIPT) script.delete();
        }
    }
    
    private void doNoObfuscation()
    {
        log(Bundle.getMessage("MSG_CopyingJarFile", srcJar.getAbsolutePath(), destJar.getAbsolutePath()), Project.MSG_INFO); //NOI18N
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try
        {
            fis = new FileInputStream(srcJar);
            fos = new FileOutputStream(destJar);
            final byte[] buffer = new byte[16384];
            for (;;)
            {
                final int len = fis.read(buffer);
                if (len < 0)
                    break;
                fos.write(buffer, 0, len);
            }
        }
        catch (IOException e)
        {
            throw new BuildException(Bundle.getMessage("ERR_CopyingJarFile", srcJar.getAbsolutePath(), destJar.getAbsolutePath()), e); //NOI18N
        }
        finally
        {
            if (fis != null) try
            { fis.close(); }
            catch (IOException e)
            {}
            if (fos != null) try
            { fos.close(); }
            catch (IOException e)
            {}
        }
    }
    
    private static String[] joinPaths(final String[] classPath, final String[] obfuscatorPath)
    {
        int size = 0;
        if (classPath != null)
            size += classPath.length;
        if (obfuscatorPath != null)
            size += obfuscatorPath.length;
        final String[] ret = new String[size];
        int pos = 0;
        if (classPath != null)
        {
            System.arraycopy(classPath, 0, ret, pos, classPath.length);
            pos += classPath.length;
        }
        if (obfuscatorPath != null)
        {
            System.arraycopy(obfuscatorPath, 0, ret, pos, obfuscatorPath.length);
            // pos += obfuscatorPath.length;
        }
        return ret;
    }
    
    /**
     * Checks if all conditions (files) are present on given classpath.
     * @param classPath classpath
     * @param conditions Comma-separated list of files
     * @return true if all conditions are satisfied
     */
    private static boolean isPresent(final String[] classPath, final String conditions)
    {
        if (conditions == null)
            return true;
        final StringTokenizer tokens = new StringTokenizer(conditions, ","); //NOI18N
        while (tokens.hasMoreTokens())
        {
            final String condition = tokens.nextToken();
            boolean found = false;
            if (classPath != null) for (int a = 0; a < classPath.length; a ++)
            {
                if (classPath[a] == null)
                    continue;
                final File part = new File(classPath[a]);
                if (!part.exists())
                    continue;
                if (part.isDirectory())
                {
                    final File classFile = new File(part, condition);
                    if (classFile.exists()  &&  classFile.isFile())
                    {
                        found = true;
                        break;
                    }
                }
                else
                {
                    ZipFile zip = null;
                    try
                    {
                        zip = new ZipFile(part);
                        final ZipEntry entry = zip.getEntry(condition);
                        if (entry != null  &&  !entry.isDirectory())
                        {
                            found = true;
                            break;
                        }
                    }
                    catch (IOException e)
                    {
                    }
                    finally
                    {
                        if (zip != null)
                            try
                            { zip.close(); }
                            catch (IOException e)
                            {}
                    }
                }
            }
            if (!found)
                return false;
        }
        return true;
    }
    
    /** Setter for property srcJar.
     * @param srcJar New value of property srcJar.
     *
     */
    public void setSrcJar(final File srcJar)
    {
        this.srcJar = srcJar;
    }
    
    /** Setter for property destJar.
     * @param destJar New value of property destJar.
     *
     */
    public void setDestJar(final File destJar)
    {
        this.destJar = destJar;
    }
    
    /**
     * Setter for property obfuscatorType.
     * @param obfuscatorType New value of property obfuscatorType.
     */
    public void setObfuscatorType(final String obfuscatorType)
    {
        this.obfuscatorType = obfuscatorType.toUpperCase();
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
    
    /** Setter for property exclude.
     * @param exclude New value of property exclude.
     */
    public void setExclude(final String exclude)
    {
        this.exclude = exclude;
    }
    
    /**
     * Setter for property obfuscatorClassPath.
     * @param obfuscatorClassPath New value of property obfuscatorClassPath.
     */
    public void setObfuscatorClassPath(final Path obfuscatorClassPath)
    {
        createObfuscatorClassPath().append(obfuscatorClassPath);
    }
    
    /**
     * Creates ObfuscatorClassPath.
     * @return Created Path
     */
    public Path createObfuscatorClassPath()
    {
        if (obfuscatorClassPath == null)
        {
            obfuscatorClassPath = new Path(getProject());
        }
        return obfuscatorClassPath.createPath();
    }
    
    /**
     * Setter for property obfuscatorClassPathRef.
     * @param obfuscatorClassPathRef New value of property obfuscatorClassPathRef.
     */
    public void setObfuscatorClassPathRef(final Reference obfuscatorClassPathRef)
    {
        createObfuscatorClassPath().setRefid(obfuscatorClassPathRef);
    }
    
    /**
     * Setter for property obfuscationLevel.
     * @param obfuscationLevel New value of property obfuscationLevel.
     */
    public void setObfuscationLevel(final int obfuscationLevel)
    {
        this.obfuscationLevel = obfuscationLevel;
    }
    
    
    /**
     * Setter for property extraScript.
     * @param extraScript New value of property extraScript.
     */
    public void setExtraScript(final String extraScript)
    {
        this.extraScript = extraScript;
    }
    
}
