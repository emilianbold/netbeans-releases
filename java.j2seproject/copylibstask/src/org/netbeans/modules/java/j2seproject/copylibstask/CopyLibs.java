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

package org.netbeans.modules.java.j2seproject.copylibstask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Section;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipOutputStream;

/**
 *
 * @author Tomas Zezula
 */
public class CopyLibs extends Jar {

    private static final String LIB = "lib";    //NOI18N
    private static final String ATTR_CLASS_PATH = "Class-Path"; //NOI18N
    private static final String MANIFEST = "META-INF/MANIFEST.MF";  //NOI18N
    private static final String INDEX = "META-INF/INDEX.LIST";  //NOI18N

    Path runtimePath;

    private boolean rebase;

    /** Creates a new instance of CopyLibs */
    public CopyLibs () {
        this.rebase = true;
    }
    
    public void setRuntimeClassPath (final Path path) {
        assert path != null;
        this.runtimePath = path;
    }
    
    public Path getRuntimeClassPath () {
        return this.runtimePath;
    }

    public boolean isRebase() {
        return this.rebase;
    }

    public void setRebase(final boolean rebase) {
        this.rebase = rebase;
    }
    
    @Override
    public void execute() throws BuildException {
        if (this.runtimePath == null) {
            throw new BuildException ("RuntimeClassPath must be set.");
        }
        final String[] pathElements = this.runtimePath.list();
        final List<File> filesToCopy = new ArrayList<File>(pathElements.length);
        for (int i=0; i< pathElements.length; i++) {
            final File f = new File (pathElements[i]);
            if (!f.canRead()) {
                this.log(String.format("Not copying library %s , it can't be read.", f.getAbsolutePath()), Project.MSG_WARN);
            } else if (f.isDirectory()) {                
                this.log(String.format("Not copying library %s , it's a directory.", f.getAbsolutePath()), Project.MSG_WARN);
            }
            else {
                filesToCopy.add(f);
            }
        }        
        final File destFile = this.getDestFile();
        final File destFolder = destFile.getParentFile();
        assert destFolder != null && destFolder.canWrite();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.java.j2seproject.copylibstask.Bundle");  //NOI18N
            assert bundle != null;            
            final File readme = new File (destFolder,bundle.getString("TXT_README_FILE_NAME"));
            if (!readme.exists()) {
                readme.createNewFile();
            }
            final PrintWriter out = new PrintWriter (new FileWriter (readme));            
            try {
                final String content = bundle.getString("TXT_README_FILE_CONTENT");                
                out.println (MessageFormat.format(content,new Object[] {destFile.getName()}));
            } finally {
                out.close ();
            }
        } catch (IOException ioe) {
            this.log("Cannot generate readme file.",Project.MSG_VERBOSE);
        }        
        
        if (!filesToCopy.isEmpty()) {
            final File libFolder = new File (destFolder,LIB);
            if (!libFolder.exists()) {
                libFolder.mkdir ();
                this.log("Create lib folder " + libFolder.toString() + ".", Project.MSG_VERBOSE);
            }
            assert libFolder.canWrite();            
            FileUtils utils = FileUtils.getFileUtils();
            this.log("Copy libraries to " + libFolder.toString() + ".");
            for (final File fileToCopy : filesToCopy) {
                this.log("Copy " + fileToCopy.getName() + " to " + libFolder + ".", Project.MSG_VERBOSE);
                try {
                    File libFile = new File (libFolder,fileToCopy.getName());                    
                    if (!rebase(fileToCopy, libFile)) {
                        libFile.delete();
                        utils.copyFile(fileToCopy,libFile);
                    }
                } catch (IOException ioe) {
                    throw new BuildException (ioe);
                }
            }
            final FileSet fs = new FileSet();
            fs.setDir(libFolder);
            final Path p = new Path(getProject());
            p.addFileset(fs);
            addConfiguredIndexJars(p);
        }
        else {
            this.log("Nothing to copy.");
        }

        super.execute();
    }

    private boolean rebase(final File source, final File target) {
        if (!rebase) {
            return false;
        }
        try {
            Manifest manifest = null;
            final ZipFile zf = new ZipFile(source);
            try {
                if (zf.getEntry(INDEX) != null) {
                    return false;
                }
                final ZipEntry manifestEntry = zf.getEntry(MANIFEST);
                if (manifestEntry != null) {
                    final Reader in = new InputStreamReader(zf.getInputStream(manifestEntry), Charset.forName("UTF-8"));    //NOI18N
                    try {
                        manifest = new Manifest(in);
                    } finally {
                        in.close();
                    }
                }
                if (manifest == null) {
                    return false;
                }
                final Section mainSection = manifest.getMainSection();
                final String classPath = mainSection.getAttributeValue(ATTR_CLASS_PATH);   //NOI18N
                if (classPath == null) {
                    return false;
                }
                if (isSigned(manifest)) {
                    return false;
                }
                final StringBuilder result = new StringBuilder();
                boolean changed = false;
                for (String path : classPath.split(" ")) {  //NOI18N
                    if (result.length() > 0) {
                        result.append(' ');                 //NOI18N
                    }
                    int index = path.lastIndexOf('/');      //NOI18N
                    if (index >=0 && index < path.length()-1) {
                        path = path.substring(index+1);
                        changed = true;
                    }
                    result.append(path);
                }
                if (!changed) {
                    return false;
                }
                final Enumeration<? extends ZipEntry> zent = zf.entries();
                final ZipOutputStream out = new ZipOutputStream(target);
                try {
                    while (zent.hasMoreElements()) {
                        final ZipEntry entry = zent.nextElement();
                        final InputStream in = zf.getInputStream(entry);
                        try {
                            
                            if (MANIFEST.equals(entry.getName())) {
                                out.putNextEntry(new org.apache.tools.zip.ZipEntry(entry));
                                mainSection.removeAttribute(ATTR_CLASS_PATH);
                                mainSection.addAttributeAndCheck(new Manifest.Attribute(ATTR_CLASS_PATH, result.toString()));
                                final PrintWriter manifestOut = new PrintWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
                                manifest.write(manifestOut);
                                manifestOut.flush();
                            } else {
                                out.putNextEntry(new org.apache.tools.zip.ZipEntry(entry));
                                copy(in,out);
                            }
                        } finally {
                            in.close();
                        }
                    }
                    return true;
                } finally {
                    out.close();
                }
            } finally {
                zf.close();
            }
        } catch (Exception e) {
            this.log("Cannot fix dependencies for: " + target.getAbsolutePath(), Project.MSG_WARN);   //NOI18N
        }
        return false;
    }

    private static boolean isSigned(final Manifest manifest) {        
        Section section = manifest.getSection(MANIFEST);
        if (section != null) {
            final Enumeration<String> sectionKeys = (Enumeration<String>) section.getAttributeKeys();
            while (sectionKeys.hasMoreElements()) {
                if (sectionKeys.nextElement().endsWith("-Digest")) {    //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;
        for (;;) {
            len = in.read(BUFFER);
            if (len == -1) {
                return;
            }
            out.write(BUFFER, 0, len);
        }
    }
}
