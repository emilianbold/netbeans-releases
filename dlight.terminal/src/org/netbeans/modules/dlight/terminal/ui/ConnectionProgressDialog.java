/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.terminal.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author ak119685
 */
public final class ConnectionProgressDialog extends JDialog implements TaskListener, Runnable {

    private final ProgressHandle progressHandle;
    private Runnable worker;

    public ConnectionProgressDialog(final String title, final Runnable worker, final Frame parent) {
        super(parent, title, true);
        this.worker = worker;
        progressHandle = ProgressHandleFactory.createHandle(title); // NOI18N
    }

    @Override
    public void setVisible(boolean show) {
        if (show && worker != null) {
            JComponent c = ProgressHandleFactory.createProgressComponent(progressHandle);
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); //make sure the dialog is not closed during the project open
//            c.setPreferredSize(new Dimension(3 * c.getPreferredSize().width, 3 * c.getPreferredSize().height));
//            c.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            getContentPane().add(c);
            pack();
            Container parent = getParent();
            Rectangle bounds = (parent == null)
                    ? new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) : parent.getBounds();

            int middleX = bounds.x + bounds.width / 2;
            int middleY = bounds.y + bounds.height / 2;

            Dimension size = getPreferredSize();

            setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
//            Dimension me = getSize();
//            setLocation((screen.width - me.width) / 2, (screen.height - me.height) / 2);
            progressHandle.start();
            RequestProcessor.getDefault().post(worker).addTaskListener(this);
            worker = null;
        }

        super.setVisible(show);
    }

    @Override
    public void taskFinished(Task task) {
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        progressHandle.finish();
        setVisible(false);
        dispose();
    }

    public static void ensureConnected(final ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            return;
        }

        String title = NbBundle.getMessage(ConnectionProgressDialog.class, "ConnectionProgress", env.getDisplayName()); // NOI18N
        ConnectionProgressDialog progressDialog = new ConnectionProgressDialog(title, new Runnable() { 

            @Override
            public void run() {
                try {
                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        ConnectionManager.getInstance().connectTo(env);
                    }

                    HostInfoUtils.getHostInfo(env);
                } catch (Exception ex) {
                }
            }
        }, null);

        progressDialog.setVisible(true);
    }
}

