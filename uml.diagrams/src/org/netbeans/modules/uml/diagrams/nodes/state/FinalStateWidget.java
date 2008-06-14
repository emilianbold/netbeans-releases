/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.state;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.uml.diagrams.nodes.UMLLabelNodeWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IFinalState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class FinalStateWidget extends UMLLabelNodeWidget
{

    public static final String CONTEXTPATH = "UML/context-palette/FinalState";
    private IFinalState element;
    private Widget view;

    public FinalStateWidget(Scene scene)
    {
        super(scene);
        addToLookup(initializeContextPalette());
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(CONTEXTPATH);
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {
            if (presentation.getFirstSubject() instanceof IFinalState)
            {
                if (presentation.getFirstSubject() instanceof IFinalState)
                {
                    element = (IFinalState) presentation.getFirstSubject();
                    if (element.getExpandedElementType().equals("AbortedFinalState"))
                    {
                        view = new AbortedFinalStateWidget(getScene(), 10, getWidgetID(), loc("LBL_BodyColor"));
                    } else
                    {
                        view = new DoubleCircleWidget(getScene(), 5, 10, getWidgetID(), loc("LBL_BodyColor"));
                    }
                    setCurrentView(view);
                }
            }
        }
    }

    
    @Override
    protected void notifyStateChanged(ObjectState previousState,
                                       ObjectState state)
    {
        boolean select = state.isSelected();
        boolean wasSelected = previousState.isSelected();

        if (select && !wasSelected)
        {
            setBorder(UMLWidget.NON_RESIZABLE_BORDER);
        }
        else
        {
            if (!select && wasSelected)
            {
                setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
    
    private String loc(String key)
    {
        return NbBundle.getMessage(FinalStateWidget.class, key);
    }

    public String getWidgetID()
    {
        if (element != null && element.getExpandedElementType().equals("AbortedFinalState"))
        {
            return UMLWidget.UMLWidgetIDString.ABORTEDFINALSTATEWIDGET.toString();
        }
        return UMLWidget.UMLWidgetIDString.FINALSTATEWIDGET.toString();
    }
}
