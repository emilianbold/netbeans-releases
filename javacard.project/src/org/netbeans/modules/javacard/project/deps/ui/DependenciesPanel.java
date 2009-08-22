
package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class DependenciesPanel extends JPanel {
    private final JCProjectProperties props;
    private ResolvedDependencies deps;
    private final Object lock = new Object();
    private Cancellable cancel;
    public DependenciesPanel (JCProjectProperties props) {
        this.props = props;
        setLayout (new BorderLayout());
        JLabel lbl = new JLabel (NbBundle.getMessage(DependenciesPanel.class, "MSG_LOADING"));
        add (lbl, BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (deps == null) {
            synchronized (lock) {
                cancel = props.getDependencies(new R());
            }
        }
    }

    @Override
    public void removeNotify() {
        synchronized(lock) {
            if (cancel != null) {
                cancel.cancel();
                cancel = null;
            }
        }
        super.removeNotify();
    }

    void setDependencies (ResolvedDependencies deps) {
        System.err.println("DependenciesPanel setDependencies " + deps);
        synchronized (lock) {
            this.deps = deps;
            if (cancel == null) {
                //Already removed, don't do any work
                return;
            }
            cancel = null;
        }
        System.err.println("Now updating ui");
        if (deps != null) {
            System.err.println("Adding a new editor panel");
            removeAll();
            add (new DependenciesEditorPanel(props.getProject(), deps), BorderLayout.CENTER);
        } else {
            System.err.println("adding error message");
            removeAll();
            add (new JLabel(NbBundle.getMessage(DependenciesPanel.class, "MSG_LOAD_FAILED")));
        }
        invalidate();
        revalidate();
        repaint();
    }

    ResolvedDependencies getDependencies() {
        synchronized (lock) {
            return deps;
        }
    }

    private class R implements DependenciesProvider.Receiver, Runnable {
        private volatile ResolvedDependencies deps;
        public void receive(ResolvedDependencies deps) {
            System.err.println("Panel received deps " + deps);
            this.deps = deps;
            EventQueue.invokeLater(this);
        }

        public boolean failed(Throwable failure) {
            setDependencies(null);
            return true;
        }

        public void run() {
            System.err.println("Now setting dependencies on panel to " + deps);
            setDependencies(deps);
        }

    }
}
