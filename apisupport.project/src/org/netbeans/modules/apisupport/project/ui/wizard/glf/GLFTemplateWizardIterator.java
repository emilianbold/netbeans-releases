package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import java.awt.Component;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;


public final class GLFTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels () {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new GLFTemplateWizardPanel2 (this)
                // XXX should have a name & location panel
            };
            String[] steps = createSteps ();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent ();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName ();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    // Sets steps names for a panel
                    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    public Set instantiate () throws IOException {
        CreatedModifiedFiles cmf = ((GLFTemplateWizardPanel2) panels[0]).getComponent().getCreatedModifiedFiles();
        cmf.run();
        return BasicWizardIterator.getCreatedFiles(cmf, Templates.getProject(wizard));
    }

    public void initialize (WizardDescriptor wizard) {
        this.wizard = wizard;
        System.out.println (wizard);
    }

    public void uninitialize (WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current () {
        return getPanels ()[index];
    }

    public String name () {
        return index + 1 + ". from " + getPanels ().length;
    }

    public boolean hasNext () {
        return index < getPanels ().length - 1;
    }

    public boolean hasPrevious () {
        return index > 0;
    }

    public void nextPanel () {
        if (!hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }

    public void previousPanel () {
        if (!hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener (ChangeListener l) {}
    public void removeChangeListener (ChangeListener l) {}

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps () {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA);
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent ().getName ();
            }
        }
        return res;
    }


    Project getProject () {
        return (Project) wizard.getProperty ("project");
    }

    WizardDescriptor getWizardDescriptor () {
        return wizard;
    }
}
