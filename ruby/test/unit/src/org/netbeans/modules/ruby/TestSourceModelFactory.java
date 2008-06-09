package org.netbeans.modules.ruby;

import java.io.IOException;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

public class TestSourceModelFactory extends SourceModelFactory {

    @Override
    public SourceModel getModel(FileObject fo) {
        return new TestSourceModel(fo);
    }
    
    public static RubyTestBase currentTest;

    private class TestSourceModel implements SourceModel {

        private FileObject fo;

        TestSourceModel(FileObject fo) {
            this.fo = fo;
        }

        public void runUserActionTask(CancellableTask<CompilationInfo> task, boolean shared) throws IOException {
            try {
                String text = RubyTestBase.read(fo);
                BaseDocument doc = RubyTestBase.createDocument(text);
                if (currentTest == null) {
                    throw new RuntimeException("You must set TestSourceModelFactory.currentTest before running this test!");
                }
                TestCompilationInfo testInfo = new TestCompilationInfo(currentTest, fo, doc, text);

                task.run(testInfo);
            } catch (Exception ex) {
                ex.printStackTrace();
                IOException ioe = new IOException();
                ioe.initCause(ex);
                
                throw ioe;
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
