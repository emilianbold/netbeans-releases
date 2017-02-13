package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import static org.netbeans.core.multiview.SplitAction.splitWindow;
import org.openide.windows.TopComponent;

public class SplitDocumentAction extends AbstractAction {
    private Reference<TopComponent> tcRef;
    private int orientation;
    
    protected void setTopComponent(TopComponent tc) {
        // Replaced by weak ref since strong ref led to leaking of editor panes
        tcRef = new WeakReference<TopComponent>(tc);
    }
    
    protected void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {}

    public void splitDocument() {
        TopComponent tc = tcRef.get();

        if (tc != null) {
            splitWindow(tc, orientation);
        }
    }
}
