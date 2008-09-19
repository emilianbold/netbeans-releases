/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.util.concurrent.Callable;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.support.api.GroovySettings;

public final class GenerateAllAction extends NodeAction {
    
    private final Logger LOG = Logger.getLogger(GenerateAllAction.class.getName());
    
    protected void performAction(Node[] activatedNodes) {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }

        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);

        GrailsProject prj = (GrailsProject) FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());

        String command = "generate-all"; // NOI18N
        ProjectInformation inf = prj.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(prj), dataObject.getPrimaryFile().getName()); // NOI18N

        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .controllable(true).frontWindow(true).inputVisible(true).showProgress(true);
        descriptor = descriptor.postExecution(new RefreshProjectRunnable(prj));
        descriptor = descriptor.optionsPath(GroovySettings.GROOVY_OPTIONS_CATEGORY);

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    public String getName() {
        return NbBundle.getMessage(GenerateAllAction.class, "CTL_GenerateAllAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        
        if (dataObject == null) {
            return false;
        }
        String name = dataObject.getFolder().getName();
        return "domain".equals(name);
    }

}

