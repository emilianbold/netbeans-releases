/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=Analyzer.class)
public class RunFindBugs implements Analyzer {

    @Override
    public Iterable<? extends ErrorDescription> analyze(Collection<? extends FileObject> sourceRoots, ProgressContributor progress) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        try {
            for (FileObject sr : sourceRoots) {
                FindBugs2 engine = new FindBugs2();
                Project p = new Project();
                BugCollectionBugReporter r = new BugCollectionBugReporter(p);

                for (URL binary : BinaryForSourceQuery.findBinaryRoots(sr.toURL()).getRoots()) {
                    try {
                        p.addFile(new File(binary.toURI()).getAbsolutePath());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                r.setPriorityThreshold(Integer.MAX_VALUE);
                r.setRankThreshold(Integer.MAX_VALUE);
                engine.setProject(p);
                engine.setBugReporter(r);
                engine.setUserPreferences(UserPreferences.createDefaultUserPreferences());
                engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
                engine.execute();

                for (BugInstance b : r.getBugCollection().getCollection()) {
                    SourceLineAnnotation sourceLine = b.getPrimarySourceLineAnnotation();

                    if (sourceLine != null) {
                        FileObject sourceFile = sr.getFileObject(sourceLine.getSourcePath());

                        if (sourceFile != null) {
                            DataObject d = DataObject.find(sourceFile);
                            EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                            Document doc = ec.openDocument();
                            result.add(ErrorDescriptionFactory.createErrorDescription("findbugs:" + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getAbridgedMessage(), ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), doc, sourceLine.getStartLine()));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    @Override
    @Messages("DN_FindBugs=FindBugs")
    public String getDisplayName() {
        return Bundle.DN_FindBugs();
    }

    @Override
    public String getDisplayName4Id(String id) {
        return id;
    }

    @Override
    public Image getIcon() {
        return null;
    }
}
