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
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=Analyzer.class)
public class RunFindBugs implements Analyzer {

    private static final String PREFIX_FINDBUGS = "findbugs:";
    private static final Logger LOG = Logger.getLogger(RunFindBugs.class.getName());
    
    @Override
    public Iterable<? extends ErrorDescription> analyze(Context ctx) {
        Collection<? extends FileObject> sourceRoots = ctx.getScope().getSourceRoots();//XXX: other Scope content!!!
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        int i = 0;

        ctx.start(sourceRoots.size());

        for (FileObject sr : sourceRoots) {
            result.addAll(runFindBugs(sr, null));
            ctx.progress(++i);
        }

        ctx.finish();

        return result;
    }

    public static List<ErrorDescription> runFindBugs(FileObject sourceRoot, Iterable<? extends String> classNames) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        try {
            FindBugs2 engine = new FindBugs2();
            Project p = new Project();
            BugCollectionBugReporter r = new BugCollectionBugReporter(p);
            Preferences settings = NbPreferences.forModule(RunFindBugs.class).node("global-settings");
            URL[] binaryRoots = BinaryForSourceQuery.findBinaryRoots(sourceRoot.toURL()).getRoots();

            if (classNames == null) {
                for (URL binary : binaryRoots) {
                    try {
                        p.addFile(new File(binary.toURI()).getAbsolutePath());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else {
                ClassPath binary = ClassPathSupport.createClassPath(binaryRoots);

                for (String className : classNames) {
                    FileObject classFO = binary.findResource(className.replace('.', '/') + ".class");

                    if (classFO != null) {
                        p.addFile(new File(classFO.toURI()).getAbsolutePath());
                    }
                }

                addCompileRootAsSource(p, sourceRoot);
            }

            ClassPath compile = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);

            for (FileObject compileRoot : compile.getRoots()) {
                addCompileRoot(p, compileRoot);
            }

            r.setPriorityThreshold(Integer.MAX_VALUE);
            r.setRankThreshold(Integer.MAX_VALUE);
            engine.setProject(p);
            engine.setBugReporter(r);
            engine.setUserPreferences(readPreferences(settings));
            engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
            engine.execute();

            for (BugInstance b : r.getBugCollection().getCollection()) {
                if (!settings.getBoolean(b.getBugPattern().getType(), isEnabledByDefault(b.getBugPattern()))) {
                    continue;
                }

                SourceLineAnnotation sourceLine = b.getPrimarySourceLineAnnotation();

                if (sourceLine != null) {
                    if (sourceLine.getStartLine() < 0) {
                        LOG.log(Level.WARNING, "{0}, location: {1}", new Object[]{b, sourceLine.getStartLine()});
                        continue;
                    }
                    FileObject sourceFile = sourceRoot.getFileObject(sourceLine.getSourcePath());

                    if (sourceFile != null) {
                        DataObject d = DataObject.find(sourceFile);
                        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                        Document doc = ec.openDocument();
                        result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), doc, sourceLine.getStartLine()));
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
        if (!id.startsWith(PREFIX_FINDBUGS)) return null;
        
        id = id.substring(PREFIX_FINDBUGS.length());

        for (DetectorFactory df : DetectorFactoryCollection.instance().getFactories()) {
            for (BugPattern bp : df.getReportedBugPatterns()) {
                if (id.equals(bp.getType())) return bp.getShortDescription();
            }
        }

        return id;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("edu/umd/cs/findbugs/gui2/bugSplash3.png");
    }

    private static UserPreferences readPreferences(Preferences settings) {
        UserPreferences prefs = UserPreferences.createDefaultUserPreferences();

        for (DetectorFactory df : DetectorFactoryCollection.instance().getFactories()) {
            boolean enable = false;

            for (BugPattern bp : df.getReportedBugPatterns()) {
                enable |= settings.getBoolean(bp.getType(), prefs.isDetectorEnabled(df));
            }

            prefs.enableDetector(df, enable);
        }

        return prefs;
    }

    public static boolean isEnabledByDefault(BugPattern bp) {
        DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

        for (DetectorFactory df : dfc.getFactories()) {
            if (df.getReportedBugPatterns().contains(bp)) {
                return UserPreferences.createDefaultUserPreferences().isDetectorEnabled(df);
            }
        }

        return false;
    }

    private static void addCompileRoot(Project p, FileObject compileRoot) {
        Result2 sources = SourceForBinaryQuery.findSourceRoots2(compileRoot.toURL());

        if (sources.preferSources()) {
            //XXX:
            if (sources.getRoots().length == 0) {
                addAuxCPEntry(p, compileRoot.toURL());
            } else {
                for (FileObject source : sources.getRoots()) {
                    addCompileRootAsSource(p, source);
                }
            }
        } else {
            addAuxCPEntry(p, compileRoot.toURL());
        }
    }

    private static void addCompileRootAsSource(Project p, FileObject source) {
        for (URL br : BinaryForSourceQuery.findBinaryRoots(source.toURL()).getRoots()) {
            addAuxCPEntry(p, br);
        }
    }

    private static void addAuxCPEntry(Project p, URL url) {
        //XXX: need more reliable way
        File f = FileUtil.archiveOrDirForURL(url);

        if (f == null) return ;

        p.addAuxClasspathEntry(f.getAbsolutePath());
    }
}
