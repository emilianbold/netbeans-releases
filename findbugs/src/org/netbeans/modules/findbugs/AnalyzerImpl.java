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

import edu.umd.cs.findbugs.BugCategory;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugsProgress;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.findbugs.options.FindBugsPanel;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class AnalyzerImpl implements Analyzer {

    private static final int DEFAULT_DIRECTORY_SIZE = 100;
    private final AnalyzerFactoryImpl factory;
    private final Context ctx;

    public AnalyzerImpl(AnalyzerFactoryImpl factory, Context ctx) {
        this.factory = factory;
        this.ctx = ctx;
    }

    private Thread processingThread;
    private final AtomicBoolean cancel = new AtomicBoolean();

    @Override
    @Messages({"ERR_CompiledWithErrors=Some Files Compiled with Errors",
               "# {0} - HTML encoded list of Java files with error",
               "DESC_CompiledWithErrors=Some of the analyzed files were compiled with errors. This may lead to incorrect or missing warnings from FindBugs. Files compiled with errors: {0}"
    })
    public Iterable<? extends ErrorDescription> analyze() {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        List<URL> uncompilable = new ArrayList<URL>();
        int[] elementsSize = new int[ctx.getScope().getSourceRoots().size() + ctx.getScope().getFolders().size() + ctx.getScope().getFiles().size()];
        int i = 0;
        int total = 0;
        
        for (FileObject sr : ctx.getScope().getSourceRoots()) {
            ClassIndex ci = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(sr)).getClassIndex();
            total += elementsSize[i++] = ci.getDeclaredTypes("", NameKind.PREFIX, EnumSet.of(SearchScope.SOURCE)).size();
        }
        
        for (NonRecursiveFolder nrf : ctx.getScope().getFolders()) {
            total += elementsSize[i++] = nrf.getFolder().getChildren().length;
        }
        
        for (FileObject file : ctx.getScope().getFiles()) {
            if (file.isData()) {
                total += elementsSize[i++] = 1;
            } else {
                total += elementsSize[i++] = DEFAULT_DIRECTORY_SIZE; //XXX: size? should not typically happen?
            }
        }

        RunFindBugs.LOG.log(Level.FINE, "estimated costs per todo item: {0}", Arrays.toString(elementsSize));

        ctx.start(total);
        
        i = 0;
        total = 0;

        for (FileObject sr : ctx.getScope().getSourceRoots()) {
            if (cancel.get()) return Collections.emptyList();
            result.addAll(doRunFindBugs(sr, total, elementsSize[i], uncompilable));
            total += elementsSize[i++];
        }

        for (FileObject file : ctx.getScope().getFiles()) {
            if (cancel.get()) return Collections.emptyList();
            ClassPath source = ClassPath.getClassPath(file, ClassPath.SOURCE);
            FileObject sr = source != null ? source.findOwnerRoot(file) : null;
            if (sr != null) {
                for (ErrorDescription ed : doRunFindBugs(sr, total, elementsSize[i], uncompilable)) {
                    if (FileUtil.isParentOf(file, ed.getFile()) || file == ed.getFile()) {
                        result.add(ed);
                    }
                }
            } else {
                //XXX: what can be done?
            }
            total += elementsSize[i++];
        }

        for (NonRecursiveFolder nrf : ctx.getScope().getFolders()) {
            if (cancel.get()) return Collections.emptyList();
            ClassPath source = ClassPath.getClassPath(nrf.getFolder(), ClassPath.SOURCE);
            FileObject sr = source != null ? source.findOwnerRoot(nrf.getFolder()) : null;
            if (sr != null) {
                for (ErrorDescription ed : doRunFindBugs(sr, total, elementsSize[i], uncompilable)) {
                    if (nrf.getFolder() == ed.getFile().getParent()) {
                        result.add(ed);
                    }
                }
            } else {
                //XXX: what can be done?
            }
            total += elementsSize[i++];
        }

        if (!uncompilable.isEmpty()) {
            boolean hasErroneousJava = false;
            StringBuilder sb = new StringBuilder();
            sb.append("<ul style='list-style-image: url(\"nbres:/org/netbeans/modules/java/resources/class.png\");'>");
            for (URL url : uncompilable) {
                if (!url.getPath().endsWith(".java")) continue;
                FileObject file = URLMapper.findFileObject(url);
                sb.append("<li><a href='")
                  .append(url.toString())
                  .append("'>")
                  .append(file != null ? FileUtil.getFileDisplayName(file) : url.toString())
                  .append("</a></li>");
                hasErroneousJava = true;
            }
            sb.append("</ul>");
            if (hasErroneousJava) {
                ctx.reportAnalysisProblem(Bundle.ERR_CompiledWithErrors(), Bundle.DESC_CompiledWithErrors(sb.toString()));
            }
        }
        
        ctx.finish();
        
        Set<FileObject> files2Clear = new HashSet<>();
        
        synchronized (factory.filesWithOpenedWarnings) {
            files2Clear.addAll(factory.filesWithOpenedWarnings);
            factory.filesWithOpenedWarnings.clear();
        }
        
        for (FileObject file : files2Clear) {
            HintsController.setErrors(file, RunInEditor.HINTS_KEY, Collections.<ErrorDescription>emptyList());
        }

        return result;
    }

    private List<ErrorDescription> doRunFindBugs(final FileObject sourceRoot, final int start, final int size, List<URL> uncompilable) {
        try {
            uncompilable.addAll(ErrorsCache.getAllFilesInError(sourceRoot.toURL()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Thread.interrupted();//clear interrupted flag
        synchronized (this) {
            processingThread = Thread.currentThread();
        }
        try {
            final FindBugsProgress progress = new FindBugsProgress() {
                private double[] sizePerPart;
                private int doneParts;
                private double perTick;
                private int doneTicks;
                private double incrementalDone = start;
                @Override public void reportNumberOfArchives(int i) {
                    RunFindBugs.LOG.log(Level.FINE, "reportNumberOfArchives({0})", i);
                }
                @Override public void startArchive(String string) {
                    RunFindBugs.LOG.log(Level.FINE, "startArchive({0})", string);
                }
                @Override public void finishArchive() {
                    RunFindBugs.LOG.log(Level.FINE, "finishArchive");
                }
                @Override public void predictPassCount(int[] partSize) {
                    int total = 0;
                    
                    for (int size : partSize) {
                        total += size;
                    }
                    
                    sizePerPart = new double[partSize.length];
                    
                    if (total > 0) {
                        for (int i = 0; i < sizePerPart.length; i++) {
                            sizePerPart[i] = (double) size * ((double) partSize[i] / (double) total);
                        }
                    }
                    
                    RunFindBugs.LOG.log(Level.FINE, "predictPassCount({0})", Arrays.toString(partSize));
                    RunFindBugs.LOG.log(Level.FINE, "predictPassCount.sizePerPart={0}", Arrays.toString(sizePerPart));
                }
                @Override public void startAnalysis(int i) {
                    perTick = sizePerPart[doneParts] / i;
                    doneTicks = 0;
                    RunFindBugs.LOG.log(Level.FINE, "startAnalysis({0})", i);
                    RunFindBugs.LOG.log(Level.FINE, "predictPassCount.sizePerPart={0}", Arrays.toString(sizePerPart));
                }
                @Override public void finishClass() {
                    RunFindBugs.LOG.log(Level.FINE, "finishClass");
                    int done = (int) (incrementalDone + ++doneTicks * perTick);
                    ctx.progress(done);
                }
                @Override public void finishPerClassAnalysis() {
                    RunFindBugs.LOG.log(Level.FINE, "finishPerClassAnalysis");
                    incrementalDone += sizePerPart[doneParts++];
                    ctx.progress((int) incrementalDone);
                }
            };
            final List<ErrorDescription> result = new ArrayList<ErrorDescription>();
            JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY)).runUserActionTask(new Task<CompilationController>() {
                @Override public void run(CompilationController parameter) throws Exception {
                    result.addAll(RunFindBugs.runFindBugs(null, ctx.getSettings(), ctx.getSingleWarningId(), sourceRoot, null, progress, null, null));
                }
            }, true);
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            synchronized(this) {
                processingThread = null;
            }
            Thread.interrupted();//clear interrupted flag
        }
    }

    @Override
    public boolean cancel() {
        cancel.set(true);
        synchronized(this) {
            if (processingThread != null) {
                processingThread.interrupt();
            }
        }
        return false;
    }

    @ServiceProvider(service=AnalyzerFactory.class, supersedes="org.netbeans.modules.findbugs.installer.FakeAnalyzer$FakeAnalyzerFactory")
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {

        private final Set<FileObject> filesWithOpenedWarnings = new WeakSet<>();
        
        @Messages("DN_FindBugs=FindBugs")
        public AnalyzerFactoryImpl() {
            super("findbugs", Bundle.DN_FindBugs(), makeTransparent());
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            List<WarningDescription> result = new ArrayList<WarningDescription>();
            DetectorCollectionProvider.initializeDetectorFactoryCollection();
            DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

            for (DetectorFactory df : dfc.getFactories()) {
                for (BugPattern bp : df.getReportedBugPatterns()) {
                    BugCategory c = dfc.getBugCategory(bp.getCategory());

                    if (c.isHidden()) continue;

                    result.add(WarningDescription.create(RunFindBugs.PREFIX_FINDBUGS + bp.getType(), bp.getShortDescription(), bp.getCategory(), c.getShortDescription()));
                }
            }

            return result;
        }

        @Override
        public CustomizerProvider<Void, FindBugsPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, FindBugsPanel>() {
                @Override public Void initialize() {
                    return null;
                }
                @Override public FindBugsPanel createComponent(CustomizerContext<Void, FindBugsPanel> context) {
                    FindBugsPanel result = context.getPreviousComponent();

                    if (result == null) {
                        result = new FindBugsPanel(null, null, context);
                    }

                    result.setSettings(context.getSettings());

                    return result;
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new AnalyzerImpl(this, context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            if (NbPreferences.forModule(RunInEditor.class).getBoolean(RunInEditor.RUN_IN_EDITOR, RunInEditor.RUN_IN_EDITOR_DEFAULT)) return;
            
            FileObject file = warning.getFile();

            synchronized(filesWithOpenedWarnings) {
                filesWithOpenedWarnings.add(file);
            }
            
            HintsController.setErrors(file, RunInEditor.HINTS_KEY, Collections.singleton(warning));
        }

    }
    
    private static Image makeTransparent() {
        Image original = ImageUtilities.loadImage("edu/umd/cs/findbugs/gui2/bugSplash3.png");
        int w = original.getWidth(null);
        int h = original.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        
        bi.createGraphics().drawImage(original, 0, 0, null);
        
        WritableRaster raster = bi.getRaster();
        int[] buffer = new int[4];
        
        for (int hi = 0; hi < h; hi++) {
            for (int wi = 0; wi < w; wi++) {
                buffer = raster.getPixel(wi, hi, buffer);
                
                if (buffer[0] == 255 && buffer[1] == 255 && buffer[2] == 255 && buffer[3] == 255) {
                    buffer[3] = 0;
                    raster.setPixel(wi, hi, buffer);
                }
            }
        }
        
        return bi;
    }
}
