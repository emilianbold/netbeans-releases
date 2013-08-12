/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.ko4j.debugging;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * 
 * @author Jan Stola
 */
@ActionID(category="Project", id="org.netbeans.modules.ko4j.debugging.InspectAction") // NOI18N
@ActionRegistration(displayName="#ACT_Inspect", lazy=false) // NOI18N
@ActionReference(path="Projects/org-netbeans-modules-maven/Actions", position=987) // NOI18N
@NbBundle.Messages("ACT_Inspect=Inspect") // NOI18N
public class InspectAction extends AbstractAction implements ContextAwareAction {
    private static final String INSPECT_ACTION = "inspect"; // NOI18N
    private Lookup context;
    
    public InspectAction() {
        putValue(NAME, Bundle.ACT_Inspect());
    }

    private InspectAction(Lookup context) {
        this();
        this.context = context;
    }

    @Override
    public boolean isEnabled() {
        boolean enable = false;
        if (context != null) {
            Project project = context.lookup(Project.class);
            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider != null) {
                enable = provider.isActionEnabled(INSPECT_ACTION, context);
            }
        }
        return enable;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = context.lookup(Project.class);
        ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
        if (provider != null
//                && Arrays.asList(provider.getSupportedActions()).contains(INSPECT_ACTION)
                && provider.isActionEnabled(INSPECT_ACTION, context)) {
            Server.getInstance().acceptClient();
            provider.invokeAction(INSPECT_ACTION, context);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new InspectAction(actionContext);
    }
    
}
