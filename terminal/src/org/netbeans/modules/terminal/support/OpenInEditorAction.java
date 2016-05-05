package org.netbeans.modules.terminal.support;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author igromov
 */
public final class OpenInEditorAction implements Runnable {
    private static final RequestProcessor RP = new RequestProcessor("Open in Editor"); //NOI18N
    
    private final FileObject fo;
    private final int lineNumber;
    private LineCookie lc;
    
    public static void post(FileObject fo, int lineNumber) {
        RP.post(new OpenInEditorAction(fo, lineNumber));
    }

    private OpenInEditorAction(FileObject fo, int lineNumber) {
        this.fo = fo;
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
        if (fo == null) {
            return;
        }
        try {
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
