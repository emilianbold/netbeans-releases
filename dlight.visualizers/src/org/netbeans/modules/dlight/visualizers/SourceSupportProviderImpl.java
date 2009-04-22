/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.Container;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;

import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = SourceSupportProvider.class)
public class SourceSupportProviderImpl implements SourceSupportProvider {

    private static String loc(String key, String... arg) {
        return NbBundle.getMessage(SourceSupportProviderImpl.class, key, arg);
    }

    public void showSource(SourceFileInfo lineInfo, boolean isReadOnly) {
        File f = new File(lineInfo.getFileName());
        FileObject fo = FileUtil.toFileObject(f);
        try {
            new ROEditor(fo).open();
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    final class ROEditor extends DataEditorSupport {

        private ROEditor(DataObject d) {
            super(d, new E(d));
        }

        public ROEditor(FileObject fo) throws DataObjectNotFoundException {
            this(DataObject.find(fo));
        }
    }

    private static final class E extends DataEditorSupport.Env {

        public E(DataObject d) {
            super(d);
        }

        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws IOException {
            throw new IOException("No way!"); // NOI18N

        }
    }

    /**
     *
     * @param lineInfo
     */
    public void showSource(final SourceFileInfo lineInfo) {
        if (lineInfo == null) {
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.NoInfo")); // NOI18N
            return;
        }

        if (!lineInfo.isSourceKnown()) {
            // Perhaps it's better to show some dialog here?
            // Or, even better to have special GUI icon that indicates availability
            // of source code...
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.UnknownSource")); // NOI18N
            return;
        }

        //String[] lineInfo = getLineInfo(state);
//        String fname = lineInfo[0];
//        int line = Integer.parseInt(lineInfo[1]);

//    final int lineNum = lineInfo.getLine() < 1 ? 1 : lineInfo.getLine();
        String fileName = lineInfo.getFileName();

//        // xxx remove after testing
//        if (!window.get_parent().is_embedded)
//            return;

        try {
//      File f = new File(lineInfo.getFileName());
//      FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
//      if (fo == null) {
//        return;
//      }
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fileName)));
            if (fo == null) {
                InputStream inputStream = null;
                try {
                    URI uri = new URI(lineInfo.getFileName());
                    if (uri.getScheme() != null && uri.getScheme().equals("file")) { // NOI18N

                        fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(uri)));
                    }
                    if (fo == null) {
                        String rowPath = uri.getRawPath();
                        int lastIndexOfSeparator = rowPath.lastIndexOf('/'); // NOI18N
                        if (lastIndexOfSeparator == -1) {
                            return;
                        }
                        String file = rowPath.substring(lastIndexOfSeparator, rowPath.length());
                        inputStream = uri.toURL().openStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                        String line = null;
                        StringBuffer buffer = new StringBuffer();
                        while ((line = in.readLine()) != null) {
                            buffer.append(line + "\n"); // NOI18N

                        }//end while

                        in.close();
                        inputStream.close();
                        //we have file content in buffer
                        fileName = System.getProperty("java.io.tmpdir") + File.separator + file; // NOI18N

                        File tempFile = new File(fileName);
                        tempFile.deleteOnExit();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                        writer.write(buffer.toString());
                        writer.flush();
                        writer.close();
                        fo = FileUtil.toFileObject(FileUtil.normalizeFile(tempFile));
                    }
                } catch (Throwable e) {
                    //catch it and show message that it is impossible to open source file
                    Throwable t = ErrorManager.getDefault().annotate(e, loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, t);
                    StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                //Exceptions.printStackTrace(e);
                }
            }

            if (fo == null) {
                StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
                return;
            }

            DataObject dob = DataObject.find(fo);

            final EditorCookie.Observable ec = dob.getCookie(EditorCookie.Observable.class);
            if (ec != null) {
//                StatusDisplayer.getDefault().setStatusText(
//                        I18n.getMessage("MSG_OpeningElement", // NOI18N
//                        getElementJumpName(element))
//                        );
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        JEditorPane[] panes = ec.getOpenedPanes();
                        if (panes == null || panes.length < 0) {
                            ec.open();
                            panes = ec.getOpenedPanes();
                        }
                        JEditorPane pane = panes[0];
                        // try to activate outer TopComponent
                        Container temp = pane;
                        while (!(temp instanceof TopComponent)) {
                            temp = temp.getParent();
                        }
                        ((TopComponent) temp).requestActive();

                        jumpToLine(pane, lineInfo);
                    }
                });
            }
        } catch (DataObjectNotFoundException e) {
            e.printStackTrace(System.err);
            StatusDisplayer.getDefault().setStatusText(loc("SourceSupportProviderImpl.CannotOpenFile", fileName)); // NOI18N
        }
    }

    private static void jumpToLine(JEditorPane pane, SourceFileInfo sourceFileInfo) {
        int caretPos = pane.getCaretPosition();
        Container parent = pane.getParent();
        Point viewPos = parent instanceof JViewport ? ((JViewport) parent).getViewPosition()
                : null;
        long start;
        if (sourceFileInfo.hasOffset()) {
            start = sourceFileInfo.getOffset();
        } else {
            start = lineToPosition(pane, sourceFileInfo.getLine());
        }

        if (start > 0 && pane.getCaretPosition() == caretPos &&
                pane.getDocument() != null && start < pane.getDocument().getLength() &&
                (viewPos == null || viewPos.equals(((JViewport) parent).getViewPosition()))) {
            pane.setCaretPosition((int) start);
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }

    private static int lineToPosition(JEditorPane pane, int line) {
        Document doc = pane.getDocument();
        int len = doc.getLength();
        int lineSt = 0;
        try {
            String text = doc.getText(0, len);
            boolean afterEOL = false;
            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    line--;
                    if (line == 0) {
                        return lineSt;
                    }
                    afterEOL = true;
                } else if (afterEOL) {
                    lineSt = i;
                    afterEOL = false;
                }
            }
        } catch (BadLocationException e) {
        }
        return lineSt;
    }
}
