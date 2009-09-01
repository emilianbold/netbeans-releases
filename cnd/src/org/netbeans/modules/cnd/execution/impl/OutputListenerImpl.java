package org.netbeans.modules.cnd.execution.impl;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public final class OutputListenerImpl implements OutputListener {

    private FileObject file;
    private int line;

    public OutputListenerImpl(FileObject file, int line) {
        super();
        this.file = file;
        this.line = line;
    }

    public void outputLineSelected(OutputEvent ev) {
        showLine(false);
    }

    public void outputLineAction(OutputEvent ev) {
        showLine(true);
    }

    public void outputLineCleared(OutputEvent ev) {
        ErrorAnnotation.getInstance().detach(null);
    }

    private void showLine(boolean openTab) {
        try {
            DataObject od = DataObject.find(file);
            LineCookie lc = od.getCookie(LineCookie.class);
            if (lc != null) {
                try {
                    // TODO: IZ#119211
                    // Preprocessor supports #line directive =>
                    // line number can be out of scope
                    Line l = lc.getLineSet().getOriginal(line);
                    if (!l.isDeleted()) {
                        if (openTab) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        } else {
                            l.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE);
                        }
                        ErrorAnnotation.getInstance().attach(l);
                    }
                } catch (IndexOutOfBoundsException ex) {
                }
            }
        } catch (DataObjectNotFoundException ex) {
        }
    }
}
