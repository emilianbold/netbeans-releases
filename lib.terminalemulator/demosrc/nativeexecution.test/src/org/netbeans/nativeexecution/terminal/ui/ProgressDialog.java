/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.nativeexecution.terminal.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author ak119685
 */
public final class ProgressDialog extends JDialog implements TaskListener, Runnable {

    private final ProgressHandle progressHandle;
    private Runnable worker;

    public ProgressDialog(final String title, final Runnable worker, final Frame parent) {
        super(parent, title, true);
        this.worker = worker;
        progressHandle = ProgressHandleFactory.createHandle(""); // NOI18N
    }

    @Override
    public void setVisible(boolean show) {
        if (show && worker != null) {
            JComponent c = ProgressHandleFactory.createProgressComponent(progressHandle);
            c.setPreferredSize(new Dimension(3 * c.getPreferredSize().width, 3 * c.getPreferredSize().height));
            c.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            getContentPane().add(c);
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension me = getSize();
            setLocation((screen.width - me.width) / 2, (screen.height - me.height) / 2);
            progressHandle.start();
            RequestProcessor.getDefault().post(worker).addTaskListener(this);
            worker = null;
        }

        super.setVisible(show);
    }

    public void taskFinished(Task task) {
        SwingUtilities.invokeLater(this);
    }

    public void run() {
        progressHandle.finish();
        setVisible(false);
        dispose();
    }
}

