package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.modules.mercurial.ui.repository.Repository;

public class CloneRepositoryWizardPanel implements WizardDescriptor.Panel, PropertyChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private Repository repository;
    private int repositoryModeMask;
    private boolean valid;
    private String errorMessage;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CloneRepositoryPanel();
            if (repository == null) {
                repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
                String title = org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Location");       // NOI18N
                repository = new Repository(repositoryModeMask, title);
                repository.addPropertyChangeListener(this);
                CloneRepositoryPanel panel = (CloneRepositoryPanel)component;
                panel.repositoryPanel.setLayout(new BorderLayout());
                panel.repositoryPanel.add(repository.getPanel());
                valid();
            }
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    //public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
    //    return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    //}
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(repository.getMessage());
            } else {
                invalid(repository.getMessage());
            }
        }
    }

    /*
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    protected final void valid() {
        setValid(true, null);
    }

    protected final void valid(String extErrorMessage) {
        setValid(true, extErrorMessage);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    private void setValid(boolean valid, String errorMessage) {
        boolean fire = this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        this.valid = valid;
        this.errorMessage = errorMessage;
        if (fire) {
            fireChangeEvent();
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {}
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            ((WizardDescriptor) settings).putProperty("repository", repository.getSelectedRC().getUrl()); // NOI18N
        }
    }
}

