/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.uml.diagrams.actions;

import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class RemoveConnectionRouters extends CookieAction
{

    protected void performAction(Node[] activatedNodes)
    {
        Router router = RouterFactory.createDirectRouter();
        GraphScene scene = activatedNodes[0].getLookup().lookup(GraphScene.class);
        
        for(Object model : scene.getEdges())
        {
            ConnectionWidget widget = (ConnectionWidget)scene.findWidget(model);
            widget.setRouter(router);
        }
    }

    protected int mode()
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName()
    {
        return NbBundle.getMessage(RemoveConnectionRouters.class, "CTL_RemoveConnectionRouters");
    }

    protected Class[] cookieClasses()
    {
        return new Class[]
                {
                    GraphScene.class
                };
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }
}

