/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MavenAnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    private final NbMavenProjectImpl prj;

    public MavenAnnotationProcessingQueryImpl(NbMavenProjectImpl prj) {
        this.prj = prj;
    }

    public @Override Result getAnnotationProcessingOptions(FileObject file) {
        return new Result() {
            public @Override Set<? extends Trigger> annotationProcessingEnabled() {
                // XXX should perhaps only be on for maven-compiler-plugin 2.2+
                return EnumSet.allOf(Trigger.class);
            }
            public @Override Iterable<? extends String> annotationProcessorsToRun() {
                // XXX read from maven-compiler-plugin config
                return null;
            }
            public @Override URL sourceOutputDirectory() {
                return FileUtil.urlForArchiveOrDir(new File(FileUtil.toFile(prj.getProjectDirectory()), "target/generated-sources/annotations")); // NOI18N
            }
            public @Override Map<? extends String, ? extends String> processorOptions() {
                 Map<String, String> options = new LinkedHashMap<String, String>();
                 //here should be some parsing for ap parameters, but for now it's just a solution for issue 192101
                 //after ap paremeters support in maven and nb maven project will be implemented this workaround should be moved to PersistenceScopesProviderImpl
                String key = "eclipselink.canonicalmodel.use_static_factory";//NOI18N
                String value = "false";//NOI18N
                options.put(key, value);
                return options;
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
        };
    }

}
