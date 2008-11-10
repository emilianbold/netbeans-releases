package org.netbeans.modules.gsf;

import java.io.IOException;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.editor.BaseDocument;
//import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.openide.filesystems.FileObject;

public class TestSourceModelFactory extends SourceModelFactory {

    public static GsfTestBase currentTest;

    @Override
    public SourceModel getModel(FileObject fo) {
        return new TestSourceModel(fo);
    }
    
    @Override
    public Index getIndex(FileObject fileInProject, String mimeType) {
        //ClasspathInfo cp = ClasspathInfo.create(fileInProject);
        //return (cp != null) ? cp.getClassIndex(mimeType) : null;
        // Hack!
        return GsfTestCompilationInfo.mostRecentIndex;
    }

    private class TestSourceModel implements SourceModel {

        private FileObject fo;

        TestSourceModel(FileObject fo) {
            this.fo = fo;
        }

        public void runUserActionTask(CancellableTask<CompilationInfo> task, boolean shared) throws IOException {
            try {
                String text = GsfTestBase.read(fo);
                BaseDocument doc = GsfTestBase.createDocument(text);
                if (currentTest == null) {
                    throw new RuntimeException("You must set TestSourceModelFactory.currentTest before running this test!");
                }
                GsfTestCompilationInfo testInfo = new GsfTestCompilationInfo(currentTest, fo, doc, text);

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
