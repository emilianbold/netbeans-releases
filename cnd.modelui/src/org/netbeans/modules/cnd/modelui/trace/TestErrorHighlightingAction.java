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

package org.netbeans.modules.cnd.modelui.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Action;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Tests error highlighting providers
 * @author Vladimir Kvashin
 */
public class TestErrorHighlightingAction extends TestProjectActionBase {

    @Override
    protected void performAction(Collection<NativeProject> projects) {
        if (projects != null) {
            for (NativeProject p : projects) {
                try {
                    testProject(p);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestErrorHighlighting"); // NOI18N
    }

    public static Action getInstance() {
        return SharedClassObject.findObject(TestErrorHighlightingAction.class, true);
    }

    private void testProject(NativeProject project) {
        String taskName = "Testing Error Highlighting - " + project.getProjectDisplayName(); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
        io.select();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final AtomicBoolean canceled = new AtomicBoolean(false);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(taskName, new Cancellable() {
            public boolean cancel() {
                canceled.set(true);
                return true;
            }
        });

        handle.start();

        long time = System.currentTimeMillis();

        CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
        BaseStatistics statistics[] = new BaseStatistics[] {
            new MessageStatistics(),
            new FileStatistics(csmProject)
        };

        if( ! csmProject.isStable(null) ) {
            io.getOut().printf("Waiting until the project is parsed"); //NOI18N
            csmProject.waitParse();
        }

        Collection<CsmFile> files = csmProject.getAllFiles();
        handle.switchToDeterminate(files.size());

        int processed = 0;
        for (CsmFile file : files) {
            handle.progress(file.getName().toString(), processed++);
            if (canceled.get()) {
                break;
            }
            testFile(file, out, err, canceled, statistics);
        }

        handle.finish();
        out.printf("%s\n", canceled.get() ? "Cancelled" : "Done"); //NOI18N
        out.printf("%s took %d ms\n", taskName, System.currentTimeMillis() - time); // NOI18N

        for (int i = 0; i < statistics.length; i++) {
            statistics[i].print(out);
        }

        err.flush();
        out.flush();
    }

    private void testFile(final CsmFile file,
            final OutputWriter out, final OutputWriter err,
            AtomicBoolean cancelled, final BaseStatistics statistics[]) {

        for (int i = 0; i < statistics.length; i++) {
            statistics[i].startFile(file);
        }

        RequestImpl request = new RequestImpl(file, cancelled);

        final LineConverter lineConv = new LineConverter(file);

        long time = System.currentTimeMillis();
        out.printf("\nChecking file %s    %s\n", file.getName(), file.getAbsolutePath()); // NOI18N

        final AtomicInteger cnt = new AtomicInteger(0);

        CsmErrorProvider.Response response = new CsmErrorProvider.Response() {
            public void addError(CsmErrorInfo errorInfo) {
                if (errorInfo.getSeverity() == CsmErrorInfo.Severity.ERROR) {
                    reportError(file, errorInfo, err, lineConv);
                    for (int i = 0; i < statistics.length; i++) {
                        statistics[i].consume(file, errorInfo);
                    }
                    cnt.incrementAndGet();
                }
            }
            public void done() {}
        };

        CsmErrorProvider.getDefault().getErrors(request, response);
        out.printf("Error count %d for file %s. The check took %d ms\n", cnt.get(), file.getName(), System.currentTimeMillis() - time); // NOI18N
    }

    private static OutputListener getOutputListener(final CsmFile file, final CsmErrorInfo errorInfo) {
        final CsmOffsetable dummyCsmObject = new OffsetableImpl(file, errorInfo);
        return new OutputAdapter() {
            @Override
            public void outputLineAction(OutputEvent ev) {
                CsmUtilities.openSource(dummyCsmObject);
            }
        };
    }

    private void reportError(final CsmFile file, final CsmErrorInfo errorInfo,
            OutputWriter err, LineConverter lineConv) {

        LineColumn lc = lineConv.getLineColumn(errorInfo.getStartOffset());

        String text = String.format("%s: %s %d:%d in %s", //NOI18N
                errorInfo.getSeverity(),
                errorInfo.getMessage(),
                lc.line, lc.column,
                file.getName());

        try {
            err.println(text, getOutputListener(file, errorInfo));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static int getLineCount(CsmFile file) {
        CharSequence text = file.getText();
        String lfString = System.getProperty("line.separator");
        char lfChar = lfString.length() > 1 ? lfString.charAt(1) : lfString.charAt(0);
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == lfChar) {
                lines++;
            }
        }
        return lines;
    }

    private static class OutputAdapter implements OutputListener {
        public void outputLineAction(OutputEvent ev) {}
        public void outputLineCleared(OutputEvent ev) {}
        public void outputLineSelected(OutputEvent ev) {}
    }

    private static class OffsetableImpl implements CsmOffsetable {

        private CsmUID<CsmFile> fileUID;
        private CsmErrorInfo errorInfo;

        public OffsetableImpl(CsmFile file, CsmErrorInfo errorInfo) {
            this.fileUID = file.getUID();
            this.errorInfo = errorInfo;
        }

        public CsmFile getContainingFile() {
            return fileUID.getObject();
        }

        public int getEndOffset() {
            return errorInfo.getEndOffset();
        }

        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public int getStartOffset() {
            return errorInfo.getEndOffset();
        }

        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

    }

    private static class RequestImpl implements CsmErrorProvider.Request {

        private CsmFile file;
        private AtomicBoolean cancelled;

        public RequestImpl(CsmFile file, AtomicBoolean cancelled) {
            this.file = file;
            this.cancelled = cancelled;
        }

        public CsmFile getFile() {
            return file;
        }

        public boolean isCancelled() {
            return cancelled.get();
        }

        public Document getDocument() {
            return null;
        }
    }

    private static class LineColumn {

        public final int line;
        public final int column;

        public LineColumn(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    private static class LineConverter {

        private CsmFile file;
        private CharSequence text;

        public LineConverter(CsmFile file) {
            this.file = file;
        }

        public LineColumn getLineColumn(int offset) {
            if (text == null) {
                text = file.getText();
            }
            int line = 1;
            int col = 1;
            char nl = System.getProperty("line.separator").charAt(0); //NOI18N
            for (int i = 0; i < offset; i++) {
                if( text.charAt(i) == nl)  { //NOI18N
                    line++;
                    col = 1;
                } else {
                    col++;
                }
            }
            return new LineColumn(line, col);
        }
    }

    private static abstract class BaseStatistics {

        protected abstract class Element {

            protected int cnt;
            private CsmUID<CsmFile> fileUID;

            protected Element(CsmFile file) {
                fileUID = file.getUID();
            }

            protected abstract OutputListener getOutputListener();

            protected CsmFile getFile() {
                return fileUID.getObject();
            }

            public void consume() {
                cnt++;
                BaseStatistics.this.total++;
            }

            public int getCount() {
                return cnt;
            }

            public void print(OutputWriter out, int total, CharSequence key) {
                try {
                    out.println(formatMessage(key), getOutputListener());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            protected String formatMessage(CharSequence key) {
                int percent = cnt * 100 / total;
                String text = String.format("%8d  %2d%%  %s", cnt, percent, key); // NOI18N
                return text;
            }
        }

        protected Map<CharSequence, Element> data = new HashMap<CharSequence, Element>();
        protected int total = 0;

        protected abstract String getTitle();
        protected abstract CharSequence getId(CsmFile file, CsmErrorInfo info);
        protected abstract Element createEntry(CsmFile file, CsmErrorInfo info);

        public void startFile(CsmFile file) {
        }

        public void consume(CsmFile file, CsmErrorInfo info) {
            getCreateEntry(file, info).consume();
        }

        protected Element getCreateEntry(CsmFile file, CsmErrorInfo info) {
            CharSequence id = getId(file, info);
            Element entry = data.get(id);
            if (entry == null) {
                entry = createEntry(file, info);
                data.put(id, entry);
            }
            return entry;
        }

        public void print(OutputWriter out) {

            out.printf("\n%s\n", getTitle()); // NOI18N

            List<Map.Entry<CharSequence, Element>> entries = new ArrayList<Map.Entry<CharSequence, Element>>(data.entrySet());

            Collections.sort(entries, new Comparator<Map.Entry<CharSequence, Element>>() {
                public int compare(Map.Entry<CharSequence, Element> o1, Map.Entry<CharSequence, Element> o2) {
                    return o2.getValue().getCount() - o1.getValue().getCount();
                }
            });

            for (Map.Entry<CharSequence, Element> entry : entries) {
                Element element = entry.getValue();
                if ( element.getCount() <= 0) {
                    break;
                }
                element.print(out, total, entry.getKey());
            }
            printTotal(out);
        }

        protected void printTotal(OutputWriter out) {
            out.printf("%8d TOTAL\n", total); //NOI18N
        }
    }

    private static class MessageStatistics extends BaseStatistics {

        private class MessageElement extends Element {

            private CsmErrorInfo info;

            public MessageElement(CsmFile file, CsmErrorInfo info) {
                super(file);
                this.info = info;
            }

            @Override
            public OutputListener getOutputListener() {
                return TestErrorHighlightingAction.getOutputListener(getFile(), info);
            }
        }

        @Override
        protected String getTitle() {
            return "Statistics by message"; //NOI18N
        }

        @Override
        protected CharSequence getId(CsmFile file, CsmErrorInfo info) {
            return info.getMessage();
        }

        @Override
        protected Element createEntry(CsmFile file, CsmErrorInfo info) {
            return new MessageElement(file, info);
        }
    }

    private static class FileStatistics extends BaseStatistics {

        private class FileElement extends Element {

            private int lineCount;

            public FileElement(CsmFile file) {
                super(file);
                lineCount = getLineCount(file);
                totalLineCount += lineCount;
            }

            @Override
            protected OutputListener getOutputListener() {
                return new OutputAdapter() {
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        CsmFile file = getFile();
                        if (file != null) {
                            CsmUtilities.openSource(file, 0, 0);
                        }
                    }
                };
            }

            @Override
            protected String formatMessage(CharSequence key) {
                int percent = cnt * 100 / total;
                float ratio = ((float) cnt) * 1000f / (float) lineCount;
                String text = String.format("%8d  %2d%% %8d %8.2f  %s", cnt, percent, lineCount, ratio, key); // NOI18N
                return text;
            }
        }

        private CsmProject project;
        private int totalLineCount = 0;

        public FileStatistics(CsmProject project) {
            this.project = project;
        }

        @Override
        protected String getTitle() {
            return "Statistics by file\n  Errors    %    Lines   Per 1K lines"; //NOI18N
        }

        @Override
        protected CharSequence getId(CsmFile file, CsmErrorInfo info) {
            return file.getAbsolutePath();
        }

        @Override
        protected Element createEntry(CsmFile file, CsmErrorInfo info) {
            return new FileElement(file);
        }

        @Override
        public void startFile(CsmFile file) {
            getCreateEntry(file, null);
        }

        @Override
        protected void printTotal(OutputWriter out) {
            float ratio = ((float) total) * 1000f / (float) totalLineCount;
            out.printf("%8d      %8d %8.2f \n", total, totalLineCount, ratio); //NOI18N
            out.printf("TOTAL for %s:\n%d errors    %d lines    %d files    %.2f errors per 1K lines \n\n", //NOI18N
                    project.getName(), total, totalLineCount, data.size(), ratio); //NOI18N
        }

    }

}
