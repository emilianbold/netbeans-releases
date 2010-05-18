/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.uml.drawingarea.palette.context;

import java.awt.Color;
import javax.swing.UIManager;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author treyspiva
 */
public class RapidButton extends Widget
{
    
    private static final Border HOVER_BORDER = BorderFactory.createLineBorder(1, Color.LIGHT_GRAY);
    private static final Border NO_HOVER_BORDER = BorderFactory.createEmptyBorder(1);
    
    private ContextPaletteButtonModel descriptor = null;
    private Widget associatedWidget = null;
    
    public RapidButton(Widget target, ContextPaletteButtonModel descriptor)
    {
        super(target.getScene());
        associatedWidget = target;
        
        DesignerScene scene = (DesignerScene)target.getScene();
        
        this.descriptor = descriptor;
        
        WidgetAction[] actions = descriptor.createActions(scene);
        for(WidgetAction curAction : actions)
        {
            getActions().addAction(curAction);
        }
        
        getActions().addAction(getScene().createWidgetHoverAction());
        
        ImageWidget image = new ImageWidget(scene, descriptor.getImage());
//        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addChild(image);
        
        setBorder(NO_HOVER_BORDER);
        setBackground(UIManager.getColor("List.selectionBackground"));
        
        
    }

    
    public Widget getAssociatedWidget()
    {
        return associatedWidget;
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
//        super.notifyStateChanged(previousState, state);
//        if (!previousState.isHovered() && state.isHovered())
//        {
//            setOpaque(true);
//        }
//        if (previousState.isHovered() && !state.isHovered())
//        {
//            setOpaque(false);
//        }
    }
}
