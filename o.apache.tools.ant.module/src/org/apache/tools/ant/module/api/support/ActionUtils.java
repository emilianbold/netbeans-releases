/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.api.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * Makes it easier to implement <code>org.netbeans.spi.project.ActionProvider</code> in a standard way
 * by running targets in Ant scripts.
 * @see <a href="@PROJECTS/PROJECTAPI@/org/netbeans/spi/project/ActionProvider.html"><code>ActionProvider</code></a>
 * @author Jesse Glick
 */
public final class ActionUtils {
    
    private ActionUtils() {}
    
    /**
     * Runs an Ant target (or a sequence of them).
     * @param buildXml an Ant build script
     * @param targetNames one or more targets to run; or null for the default target
     * @param properties any Ant properties to define, or null
     * @return a task tracking the progress of Ant
     * @throws IOException if there was a problem starting Ant
     * @throws IllegalArgumentException if you did not provide any targets
     */
    public static ExecutorTask runTarget(FileObject buildXml, String[] targetNames, Properties properties) throws IOException, IllegalArgumentException {
        if (buildXml == null) {
            throw new NullPointerException("Must pass non-null build script"); // NOI18N
        }
        if (targetNames != null && targetNames.length == 0) {
            throw new IllegalArgumentException("No targets supplied"); // NOI18N
        }
        AntProjectCookie apc = TargetLister.getAntProjectCookie(buildXml);
        AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
        if (properties != null) {
            Properties p = execenv.getProperties();
            p.putAll(properties);
            execenv.setProperties(p);
        }
        return AntTargetExecutor.createTargetExecutor(execenv).execute(apc, targetNames);
    }
    
    /**
     * Convenience method to find a file selection in a selection (context).
     * All files must exist on disk (according to {@link FileUtil#toFile}).
     * If a constraining directory is supplied, they must also be contained in it.
     * If a constraining file suffix is supplied, the base names of the files
     * must end with that suffix.
     * The return value is null if there are no matching files; or if the strict
     * parameter is true and some of the files in the selection did not match
     * the constraints (disk files, directory, and/or suffix).
     * <p class="nonnormative">
     * Typically {@link org.openide.loaders.DataNode}s will form a node selection
     * which will be placed in the context. This method does <em>not</em> directly
     * look for nodes in the selection; but generally the lookups of the nodes in
     * a node selection are spliced into the context as well, so the {@link DataObject}s
     * should be available. A corollary of not checking nodes directly is that any
     * nodes in the context which do not correspond to files at all (i.e. do not have
     * {@link DataObject} as a cookie) are ignored, even with the strict parameter on;
     * and that multiple nodes in the context with the same associated file are treated
     * as a single entry.
     * </p>
     * @param context a selection as provided to e.g. <code>ActionProvider.isActionEnabled(...)</code>
     * @param dir a constraining parent directory, or null to not check for a parent directory
     * @param suffix a file suffix (e.g. <samp>.java</samp>) to constrain files by,
     *               or null to not check suffixes
     * @param strict if true, all files in the selection have to be accepted
     * @return a nonempty selection of disk files, or null
     * @see <a href="@PROJECTS/PROJECTAPI@/org/netbeans/spi/project/ActionProvider.html#isActionEnabled(java.lang.String,%20org.openide.util.Lookup)"><code>ActionProvider.isActionEnabled(...)</code></a>
     */
    public static FileObject[] findSelectedFiles(Lookup context, FileObject dir, String suffix, boolean strict) {
        if (dir != null && !dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        if (suffix != null && suffix.indexOf('/') != -1) {
            throw new IllegalArgumentException("Cannot includes slashes in suffix: " + suffix); // NOI18N
        }
        Collection/*<FileObject>*/ files = new LinkedHashSet(); // #50644: remove dupes
        Iterator it = context.lookup(new Lookup.Template(DataObject.class)).allInstances().iterator();
        // XXX this should perhaps also check for FileObject's...
        while (it.hasNext()) {
            DataObject d = (DataObject)it.next();
            FileObject f = d.getPrimaryFile();
            boolean matches = FileUtil.toFile(f) != null;
            if (dir != null) {
                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
            }
            if (suffix != null) {
                matches &= f.getNameExt().endsWith(suffix);
            }
            // Generally only files from one project will make sense.
            // Currently the action UI infrastructure (PlaceHolderAction)
            // checks for that itself. Should there be another check here?
            if (matches) {
                files.add(f);
            } else if (strict) {
                return null;
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        return (FileObject[])files.toArray(new FileObject[files.size()]);
    }
    
    /**
     * Map files of one kind in a source directory to files of another kind in a target directory.
     * You may use regular expressions to remap file names in the process.
     * Only files which actually exist in the target directory will be returned.
     * <span class="nonnormative">(If you expect the target files to be created
     * by Ant you do not need this method, since Ant's mappers suffice.)</span>
     * The file paths considered by the regular expression (if supplied) always use
     * <samp>/</samp> as the separator.
     * <p class="nonnormative">
     * Typical usage to map a set of Java source files to corresponding tests:
     * <code>regexpMapFiles(files, srcDir, Pattern.compile("/([^/]+)\\.java"), testSrcDir, "/\\1Test.java", true)</code>
     * </p>
     * @param fromFiles a list of source files to start with (may be empty)
     * @param fromDir a directory in which all the source files reside
     * @param fromRx a regular expression to match against the source files
     *               (or null to keep the same relative file names); only one
     *               match (somewhere in the path) is checked for; failure to match
     *               prevents the file from being included
     * @param toDir a target directory that results will reside in
     * @param toSubst replacement text for <code>fromRx</code> (may include regexp references),
     *                or must be null if <code>fromRx</code> was null
     * @param strict true to return null in case some starting files did not match any target file
     * @return a list of corresponding target files (may be empty), or null if in strict mode
     *         and there was at least one source file which did not match a target file

     * @throws IllegalArgumentException in case some source file is not in the source directory
     */
    public static FileObject[] regexpMapFiles(FileObject[] fromFiles, FileObject fromDir, Pattern fromRx, FileObject toDir, String toSubst, boolean strict) throws IllegalArgumentException {
        List/*<FileObject>*/ files = new ArrayList();
        for (int i = 0; i < fromFiles.length; i++) {
            String path = FileUtil.getRelativePath(fromDir, fromFiles[i]);
            if (path == null) {
                throw new IllegalArgumentException("The file " + fromFiles[i] + " is not in " + fromDir); // NOI18N
            }
            String toPath;
            if (fromRx != null) {
                Matcher m = fromRx.matcher(path);
                toPath = m.replaceFirst(toSubst);
                if (toPath.equals(path) && !m.find(0)) {
                    // Did not match the pattern.
                    if (strict) {
                        return null;
                    } else {
                        continue;
                    }
                }
            } else {
                toPath = path;
            }
            FileObject target = toDir.getFileObject(toPath);
            if (target == null) {
                if (strict) {
                    return null;
                } else {
                    continue;
                }
            }
            files.add(target);
        }
        return (FileObject[])files.toArray(new FileObject[files.size()]);
    }
    
    /**
     * Create an "includes" string such as is accepted by many Ant commands
     * as well as filesets.
     * <samp>/</samp> is always used as the separator in the relative paths.
     * @param files a list of files or folders to include, in the case of folder
     * the generated include contains recursively all files under the folder.
     * @param dir a directory in which all the files reside
     * @return a comma-separated list of relative file paths suitable for use by Ant
     *         (the empty string in case there are no files)
     * @throws IllegalArgumentException in case some file is not in the directory
     */
    public static String antIncludesList(FileObject[] files, FileObject dir) throws IllegalArgumentException {
        return antIncludesList (files, dir, true);
    }

    /**
     * Create an "includes" string such as is accepted by many Ant commands
     * as well as filesets.
     * <samp>/</samp> is always used as the separator in the relative paths.
     * @param files a list of files or folders to include, in the case of folder
     * the generated include contains recursively all files under the folder.
     * @param dir a directory in which all the files reside
     * @param recursive if true the include list for directory is recursive
     * @return a comma-separated list of relative file paths suitable for use by Ant
     *         (the empty string in case there are no files)
     * @throws IllegalArgumentException in case some file is not in the directory
     * @since org.apache.tools.ant.module/3 3.16
     */
    public static String antIncludesList(FileObject[] files, FileObject dir, boolean recursive) throws IllegalArgumentException {
        if (!dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            String path = FileUtil.getRelativePath(dir, files[i]);
            if (path == null) {
                throw new IllegalArgumentException("The file " + files[i] + " is not in " + dir); // NOI18N
            }
            if (i > 0) {
                b.append(',');
            }            
            b.append(path);
            if (files[i].isFolder()) {
                // files[i] == dir, cannot use "/".
                if (path.length() > 0) {                    
                    b.append('/');  //NOI18N
                }
                b.append('*');  //NOI18N
                if (recursive) {
                    b.append('*'); //NOI18N
                }
            }
        }
        return b.toString();
    }
    
}
