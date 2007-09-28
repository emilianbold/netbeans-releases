package org.netbeans.modules.ruby;

import java.io.IOException;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.SourceModel;
import org.netbeans.api.gsf.SourceModelFactory;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

public class TestSourceModelFactory extends SourceModelFactory {

    @Override
    public SourceModel getModel(FileObject fo) {
        return new TestSourceModel(fo);
    }

    private class TestSourceModel implements SourceModel {

        private FileObject fo;

        TestSourceModel(FileObject fo) {
            this.fo = fo;
        }

        public void runUserActionTask(CancellableTask<CompilationInfo> task, boolean shared) throws IOException {
            try {
                String text = RubyTestBase.readFile(fo);
                BaseDocument doc = RubyTestBase.createDocument(text);
                TestCompilationInfo testInfo = new TestCompilationInfo(null, fo, doc, text);

                task.run(testInfo);
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

        public FileObject getFileObject() {
            return fo;
        }

        public boolean isScanInProgress() {
            return false;
        }
    }
}