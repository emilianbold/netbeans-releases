/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;


public final class LDAPServerBrowserWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wizard;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new LDAPServerBrowserWizardPanel1(),
                new LDAPServerBrowserWizardPanel2(),
                new LDAPServerBrowserWizardPanel3()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;

    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        WizardDescriptor.Panel wp=current();
        if(wp instanceof LDAPServerBrowserWizardPanel1){
            LDAPServerBrowserWizardPanel1 wp1=(LDAPServerBrowserWizardPanel1)wp;
            String str1=wp1.validate();
            if(!"".equals(str1)){
                JOptionPane.showMessageDialog(null, str1, "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
         /* if(wp instanceof LDAPServerBrowserWizardPanel2){
            LDAPServerBrowserWizardPanel2 wp2=(LDAPServerBrowserWizardPanel2)wp;
          String str2=wp2.validate();
            if(!"".equals(str2)){
                JOptionPane.showMessageDialog(null, str2, "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }*/
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
     */
}
