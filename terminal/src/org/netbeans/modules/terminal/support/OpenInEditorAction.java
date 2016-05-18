package org.netbeans.modules.terminal.support;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author igromov
 */
public final class OpenInEditorAction implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor("Open in Editor"); //NOI18N

    private final URL url;
    private final int lineNumber;
    private LineCookie lc;

    public static void post(URL url, int lineNumber) {
        RP.post(new OpenInEditorAction(url, lineNumber));
    }

    public static void post(String filePath, int lineNumber) {
        try {
            RP.post(new OpenInEditorAction(new URL("file://" + filePath), lineNumber));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private OpenInEditorAction(URL url, int lineNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
    }

    @Override
    public void run() {
        if (SwingUtilities.isEventDispatchThread()) {
            doEDT();
        } else {
            doWork();
        }
    }

    private void doEDT() {
        if (lc != null) {
            // XXX opens +-1 line
            Line l = lc.getLineSet().getOriginal(lineNumber);
            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        }
    }

    private void doWork() {
        if (url == null) {
            return;
        }
        try {
            FileObject fo;
            if (url.getProtocol().equals("file")) { //NOI18N
                fo = FileUtil.toFileObject(new File(url.getPath()));
            } else {
                fo = URLMapper.findFileObject(url); //NOI18N
            }
            DataObject dobj = DataObject.find(fo);
            EditorCookie ed = dobj.getLookup().lookup(EditorCookie.class);
            if (ed != null && fo == dobj.getPrimaryFile()) {
                if (lineNumber == -1) {
                    ed.open();
                } else {
                    lc = (LineCookie) dobj.getLookup().lookup(LineCookie.class);
                    SwingUtilities.invokeLater(this);
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception ex) {
            // ignore
        }
    }

}
