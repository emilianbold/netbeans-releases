/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.actions;

import javax.swing.Action;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Sheryl Su
 */
public class SyncElementWithDataAction extends CookieAction
{

    private DesignerScene scene;
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        this.scene = actionContext.lookup(DesignerScene.class);
        return this;
    }
    
    
    protected void performAction(Node[] activatedNodes)
    {
        for (Node node : activatedNodes)
        {
            IPresentationElement pe = node.getCookie(IPresentationElement.class);
            Widget w = scene.findWidget(pe);
            if (w instanceof UMLWidget)
            {
                ((UMLWidget) w).refresh(false);
            }
        }
    }

    public String getName()
    {
        return NbBundle.getMessage(SyncElementWithDataAction.class, "CTL_SyncElementWithData");
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }


    protected Class<?>[] cookieClasses()
    {
        return new Class[] {IPresentationElement.class};
    }

    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    protected boolean enable(Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(scene.isReadOnly() == false)
        {
            retVal = super.enable(activatedNodes);
        }
        
        return retVal;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
