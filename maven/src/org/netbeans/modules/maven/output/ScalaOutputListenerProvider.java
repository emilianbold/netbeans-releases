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
package org.netbeans.modules.maven.output;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.MavenSourcesImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;




/**
 * compilation output processing
 * @author  Milos Kleint
 */
public class ScalaOutputListenerProvider implements OutputProcessor {
    
    private static final String[] SCALAGOALS = new String[] {
        "mojo-execute#scala:compile", //NOI18N
        "mojo-execute#scala:testCompile" //NOI18N
    };
    private Pattern failPattern;
    
    public ScalaOutputListenerProvider() {
        failPattern = Pattern.compile("\\s*(?:\\[WARNING\\])?\\s*(.*)\\.scala\\:([0-9]*)\\:(.*)", Pattern.DOTALL); //NOI18N
    }
    
    public void processLine(String line, OutputVisitor visitor) {
            Matcher match = failPattern.matcher(line);
            if (match.matches()) {
                String clazz = match.group(1);
                String lineNum = match.group(2);
                String text = match.group(3);
                File clazzfile = FileUtil.normalizeFile(new File(clazz + ".scala")); //NOI18N
                visitor.setOutputListener(new CompileAnnotation(clazzfile, lineNum,
                        text), true); 
                FileUtil.refreshFor(clazzfile);
                FileObject file = FileUtil.toFileObject(clazzfile);
                String newclazz = clazz;
                if (file != null) {
                    Project prj = FileOwnerQuery.getOwner(file);
                    if (prj != null) {
                        Sources srcs = prj.getLookup().lookup(Sources.class);
                        if (srcs != null) {
                            for (SourceGroup grp : srcs.getSourceGroups(MavenSourcesImpl.TYPE_SCALA)) {
                                if (FileUtil.isParentOf(grp.getRootFolder(), file)) {
                                    newclazz = FileUtil.getRelativePath(grp.getRootFolder(), file);
                                    if (newclazz.endsWith(".scala")) { //NOI18N
                                        newclazz = newclazz.substring(0, newclazz.length() - ".scala".length()); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
                line = line.replace(clazz, newclazz); //NOI18N
                visitor.setLine(line);
            }
    }

    public String[] getRegisteredOutputSequences() {
        return SCALAGOALS;
    }

    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }

    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
    }
    
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
}
