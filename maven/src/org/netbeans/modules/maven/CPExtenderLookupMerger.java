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

package org.netbeans.modules.maven;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@SuppressWarnings("deprecation")
public class CPExtenderLookupMerger implements LookupMerger<ProjectClassPathExtender> {
    
    private CPExtender fallback;
    private Extender instance;
    
    /** Creates a new instance of CPExtenderLookupMerger */
    public CPExtenderLookupMerger(CPExtender fallbck) {
        fallback = fallbck;
        assert fallback != null;
    }
    
    public Class<ProjectClassPathExtender> getMergeableClass() {
        return ProjectClassPathExtender.class;
    }

    public synchronized ProjectClassPathExtender merge(Lookup lookup) {
        if (instance == null) {
            instance =  new Extender();
        }
        instance.setLookup(lookup);
        return instance;
    }

    private class Extender implements ProjectClassPathExtender {
        
        private Lookup context;
        
        private Extender() {
            this.context = context;
        }
        private void setLookup(Lookup context) {
            this.context = context;
        }
    
        public boolean addLibrary(Library arg0) throws IOException {
            Collection<? extends ProjectClassPathExtender> list = context.lookupAll(ProjectClassPathExtender.class);
            for (ProjectClassPathExtender ext : list) {
                boolean added = ext.addLibrary(arg0);
                if (added) {
                    return added;
                }
            }
            return fallback.addLibrary(arg0);
        }

        public boolean addArchiveFile(FileObject arg0) throws IOException {
            Collection<? extends ProjectClassPathExtender> list = context.lookupAll(ProjectClassPathExtender.class);
            for (ProjectClassPathExtender ext : list) {
                boolean added = ext.addArchiveFile(arg0);
                if (added) {
                    return added;
                }
            }
            return fallback.addArchiveFile(arg0);
        }

        public boolean addAntArtifact(AntArtifact arg0, URI arg1) throws IOException {
            Collection<? extends ProjectClassPathExtender> list = context.lookupAll(ProjectClassPathExtender.class);
            for (ProjectClassPathExtender ext : list) {
                boolean added = ext.addAntArtifact(arg0, arg1);
                if (added) {
                    return added;
                }
            }
            return fallback.addAntArtifact(arg0, arg1);
        }

    }
}
