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

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.Action;
import org.netbeans.modules.uml.diagrams.nodes.UMLLabelNodeWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.netbeans.modules.uml.drawingarea.view.WidgetViewManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class PseudoStateWidget extends UMLLabelNodeWidget
{

    private IPseudoState element;
    public static final String CONTEXTPATH = "UML/context-palette/State";
    public static final String FORK_CONTEXTPATH = "UML/context-palette/ForkState";

    public PseudoStateWidget(Scene scene)
    {
        super(scene);
    }

    private DefaultContextPaletteModel initializeContextPalette(String path)
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(path);
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        Widget view = null;
        if (presentation != null)
        {
            element = (IPseudoState) presentation.getFirstSubject();
            Scene scene = getScene();
            String path = CONTEXTPATH;
            
            switch (element.getKind())
            {

                case IPseudostateKind.PK_SHALLOWHISTORY:
                    view = new HistoryStateWidget(getScene(), 15, getWidgetID(), loc("LBL_BodyColor"), "H");
                    break;
                case IPseudostateKind.PK_CHOICE:
                    view = new ChoicePseudoStateWidget(getScene(), getWidgetID(), loc("LBL_BodyColor"));
                    break;
                case IPseudostateKind.PK_DEEPHISTORY:
                    view = new HistoryStateWidget(getScene(), 15, getWidgetID(), loc("LBL_BodyColor"), " H*");
                    break;
                case IPseudostateKind.PK_FORK:
                case IPseudostateKind.PK_JOIN:
                    path = FORK_CONTEXTPATH;
                    view = new JoinForkStateWidget(getScene(), getWidgetID(), loc("LBL_BodyColor"));
                    forkViewManager.switchViewTo("HORIZONTAL");
                    addToLookup(forkViewManager);
                    break;
                case IPseudostateKind.PK_INITIAL:
                case IPseudostateKind.PK_JUNCTION:
                case IPseudostateKind.PK_ENTRYPOINT:
                default:
                    view = new CircleWidget(scene, 10, getWidgetID(), loc("LBL_BodyColor"));
            }
            addToLookup(initializeContextPalette(path));
            setCurrentView(view);
        }
    }

    protected String loc(String key)
    {
        return NbBundle.getMessage(PseudoStateWidget.class, key);
    }

    public String getWidgetID()
    {
        if (element != null)
        {
            switch (element.getKind())
            {
                case IPseudostateKind.PK_CHOICE:
                    return UMLWidget.UMLWidgetIDString.CHOICEPSEUDOSTATEWIDGET.toString();
                case IPseudostateKind.PK_DEEPHISTORY:
                    return UMLWidget.UMLWidgetIDString.DEEPHISTORYSTATEWIDGET.toString();
                case IPseudostateKind.PK_FORK:
                    return UMLWidget.UMLWidgetIDString.FORKSTATEWIDGET.toString();
                case IPseudostateKind.PK_INITIAL:
                    return UMLWidget.UMLWidgetIDString.INITIALSTATEWIDGET.toString();
                case IPseudostateKind.PK_JOIN:
                    return UMLWidget.UMLWidgetIDString.FORKSTATEWIDGET.toString();
                case IPseudostateKind.PK_JUNCTION:
                    return UMLWidget.UMLWidgetIDString.JUNCTIONSTATEWIDGET.toString();
                case IPseudostateKind.PK_SHALLOWHISTORY:
                    return UMLWidget.UMLWidgetIDString.SHALLOWHISTORYSTATEWIDGET.toString();
                case IPseudostateKind.PK_ENTRYPOINT:
                    return UMLWidget.UMLWidgetIDString.ENTRYPOINTSTATEWIDGET.toString();
            }
        }
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }
    
    
    private WidgetViewManager forkViewManager = new WidgetViewManager()
    {
        private int WIDTH = 90;
        private int HEIGHT = 10;

        public void switchViewTo(String view)
        {
            Rectangle newBounds;
            if (getBounds() == null || getPreferredBounds().width == 0)
            {
                newBounds = Orientation.valueOf(view) == Orientation.VERTICAL ? new Rectangle(HEIGHT, WIDTH) : new Rectangle(WIDTH, HEIGHT);
                setPreferredBounds(newBounds);
                revalidate();
            } else
            {
                Orientation orientation = getPreferredBounds().width > getPreferredBounds().height ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                if (Orientation.valueOf(view) != orientation)
                {
                    Rectangle bounds = getBounds();
                    newBounds = bounds == null ? new Rectangle(HEIGHT, WIDTH) : new Rectangle(bounds.height, bounds.width);

                    setPreferredBounds(newBounds);
                    revalidate();
                }
            }
        }

        public Action[] getViewActions()
        {
            return new Action[0];
        }
    };

    protected Dimension getNodeMinSizeForPreferredBounds()
    {
        return new Dimension(10, 10);
    }
}
