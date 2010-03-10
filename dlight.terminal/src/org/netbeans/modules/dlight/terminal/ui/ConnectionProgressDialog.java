/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.terminal.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
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

