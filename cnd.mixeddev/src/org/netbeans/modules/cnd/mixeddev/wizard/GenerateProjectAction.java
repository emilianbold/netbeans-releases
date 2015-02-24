/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.mixeddev.wizard;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Alexander Simon
 */
public class GenerateProjectAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        final FileObject fo = activatedNodes[0].getLookup().lookup(FileObject.class);
        ProgressUtils.runOffEventDispatchThread(new Runnable() {

            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    actionPerformedImpl(fo);
                } else {
                    Generator generator = new Generator(null, fo);
                    String validate = generator.validate();
                    if (validate == null) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(validate, NotifyDescriptor.WARNING_MESSAGE));
                    }
                }
            }
        }, NbBundle.getMessage(GenerateProjectAction.class, "Generator_Validating"), new AtomicBoolean(), false);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            //DataObject dao = activatedNodes[0].getLookup().lookup(DataObject.class);
            FileObject fo = activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                List<String> jniClasses = JNISupport.getJNIClasses(fo);
                return jniClasses != null && !jniClasses.isEmpty();
            }
        }
        
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GenerateProjectAction.class, "Action.Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("MixedDevelopment");
    }

    public void actionPerformedImpl(FileObject fo) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new LocationJNIWizardPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(GenerateProjectAction.class, "Wizard.Name"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            try {
                new Generator(wiz, fo).instantiate();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }    
}
