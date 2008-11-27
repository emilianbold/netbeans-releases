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

package org.netbeans.modules.maven.persistence;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.spi.customizer.ModelHandleUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class CPExtender extends ProjectClassPathModifierImplementation implements ProjectClassPathExtender {
    private static final String SL_15 = "1.5"; //NOI18N
    private Project project;
    /** Creates a new instance of CPExtender */
    public CPExtender(Project project) {
        this.project = project;
    }
    
    protected SourceGroup[] getExtensibleSourceGroups() {
        //the default one privides them.
        return new SourceGroup[0];
    }

    protected String[] getExtensibleClassPathTypes(SourceGroup arg0) {
        return new String[0];
    }

    protected boolean addLibraries(Library[] libs, SourceGroup arg1, String arg2) throws IOException,
                                                                                         UnsupportedOperationException {
        boolean added = false;
        for (Library l : libs) {
            added = added || addLibrary(l);
        }
        return added;
    }

    protected boolean removeLibraries(Library[] arg0, SourceGroup arg1,
                                      String arg2) throws IOException,
                                                          UnsupportedOperationException {
        return false;
    }

    protected boolean addRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                 UnsupportedOperationException {
        return false;
    }

    protected boolean removeRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                    UnsupportedOperationException {
        return false;
    }

    protected boolean addAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                      SourceGroup arg2, String arg3) throws IOException,
                                                                            UnsupportedOperationException {
        return false;
    }

    protected boolean removeAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                         SourceGroup arg2, String arg3) throws IOException,
                                                                               UnsupportedOperationException {
        return false;
    }

    public boolean addLibrary(Library library) throws IOException {
        if ("toplink".equals(library.getName())) { //NOI18N
            //TODO would be nice if the toplink lib shipping with netbeans be the same binary
            // then we could just copy the pieces to local repo.
            try {
                //not necessary any more. toplink will be handled by default library impl..            
                //TODO would be nice if the toplink lib shipping with netbeans be the same binary
                // then we could just copy the pieces to local repo.
                ModelHandle handle = ModelHandleUtils.createModelHandle(project);
                
                // checking source doesn't work anymore, the wizard requires the level to be 1.5 up front.
                ModelUtils.checkSourceLevel(handle, SL_15);
                ModelHandleUtils.writeModelHandle(handle, project);
                
                //shall not return true, needs processing by the fallback impl as well.
                return false;
            } catch (XmlPullParserException ex) {
                //not going to happen XmlPull for nbactions.xml parsing.
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    public boolean addArchiveFile(FileObject arg0) throws IOException {
        return false;
    }
    
    public boolean addAntArtifact(AntArtifact arg0, URI arg1) throws IOException {
        return false;
    }
    
}
