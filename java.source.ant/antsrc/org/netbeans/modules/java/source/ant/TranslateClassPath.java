/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class TranslateClassPath extends Task {

    private String classpath;
    private String targetProperty;
    private boolean clean;
    
    public void setClasspath(String cp) {
        this.classpath = cp;
    }
    
    public void setTargetProperty(String tp) {
        this.targetProperty = tp;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    @Override
    public void execute() throws BuildException {
        if (classpath == null) {
            throw new BuildException("Classpath must be set.");
        }
        if (targetProperty == null) {
            throw new BuildException("Target property must be set.");
        }
        
        Project p = getProject();

        String translated = translate(classpath);
        
        p.setProperty(targetProperty, translated);
    }
    
    private String translate(String classpath) {
        StringBuilder cp = new StringBuilder();
        boolean first = true;

        Object o = getProject().getProperties().get("maven.disableSources");
        Boolean disableSources;
        if (o instanceof Boolean) {
            disableSources = (Boolean) o;
        } else {
            disableSources = Boolean.FALSE;
        }        
        
        for (String path : PropertyUtils.tokenizePath(classpath)) {
            File[] files = translateEntry(path, disableSources);

            if (files.length == 0) {
                //TODO: log
//                LOG.log(Level.FINE, "cannot translate {0} to file", e.getURL().toExternalForm());
                continue;
            }

            for (File f : files) {
                if (!first) {
                    cp.append(File.pathSeparatorChar);
                }

                cp.append(f.getAbsolutePath());
                first = false;
            }
        }

        return cp.toString();
    }
    
    private File[] translateEntry(String path, Boolean disableSources) throws BuildException {
        File entryFile = new File(path);
        try {
            URL entry = FileUtil.urlForArchiveOrDir(entryFile);
            
            SourceForBinaryQuery.Result2 r = SourceForBinaryQuery.findSourceRoots2(entry);
            boolean appendEntry = false;

            if (!disableSources && r.preferSources() && r.getRoots().length > 0) {
                List<File> translated = new LinkedList<File>();
                
                for (FileObject source : r.getRoots()) {
                    File sourceFile = FileUtil.toFile(source);

                    if (sourceFile == null) {
                        log("Source URL: " + source.getURL().toExternalForm() + " cannot be translated to file, skipped", Project.MSG_WARN);
                        appendEntry = true;
                        continue;
                    }

                    Boolean bamiResult = clean ? BuildArtifactMapperImpl.clean(sourceFile.toURI().toURL())
                                               : BuildArtifactMapperImpl.ensureBuilt(sourceFile.toURI().toURL(), false);

                    if (bamiResult == null) {
                        appendEntry = true;
                        continue;
                    }
                    
                    if (!bamiResult) {
                        throw new UserCancel();
                    }
                    
                    for (URL binary : BinaryForSourceQuery.findBinaryRoots(source.getURL()).getRoots()) {
                        FileObject binaryFO = URLMapper.findFileObject(binary);
                        File cache = binaryFO != null ? FileUtil.toFile(binaryFO) : null;

                        if (cache != null) {
                            translated.add(cache);
                        }

                        if (sourceFile != null) {
                            translated.add(sourceFile);
                        }
                    }
                }

                if (appendEntry) {
                    translated.add(entryFile);
                }
                
                return translated.toArray(new File[0]);
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        return new File[] {entryFile};
    }

}
