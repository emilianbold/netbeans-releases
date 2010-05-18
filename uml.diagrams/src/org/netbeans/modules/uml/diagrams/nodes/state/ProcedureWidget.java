/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.DiagramPopupMenuProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public abstract class ProcedureWidget extends FeatureWidget implements PropertyChangeListener{

    public ProcedureWidget(Scene scene)
    {
        super(scene);
        DiagramPopupMenuProvider menuProvider = new DiagramPopupMenuProvider();
        WidgetAction selectAction = ActionFactory.createSelectAction(
                new DiagramEngine.DesignSelectProvider());
        WidgetAction.Chain selectTool = createActions(DesignerTools.SELECT);

        selectTool.addAction(selectAction);
        selectTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
    }
    
    public ProcedureWidget(Scene scene, IElement e)
    {
        this(scene);
        initialize(e);
    }
    
    public void save(NodeWriter nodeWriter)
    {
        
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String eventName = evt.getPropertyName();
        if (eventName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString())) {
            updateUI();
        }
    }
    
    
    public static String loc(String key)
    {
        return NbBundle.getMessage(ProcedureWidget.class, key);
    }

    public static class EntryEventWidget extends ProcedureWidget
    {

        public EntryEventWidget(Scene scene, IElement e)
        {
            super(scene, e);
        }

        @Override
        protected String formatElement()
        {
            return loc("LBL_Entry") + " / " + getElement();
        }
    }

    public static class ExitEventWidget extends ProcedureWidget
    {

        public ExitEventWidget(Scene scene, IElement e)
        {
            super(scene, e);
        }

        @Override
        protected String formatElement()
        {
            return loc("LBL_Exit") + " / " + getElement();
        }
    }

    public static class DoEventWidget extends ProcedureWidget
    {

        public DoEventWidget(Scene scene, IElement e)
        {
            super(scene, e);
        }

        @Override
        protected String formatElement()
        {
            return loc("LBL_Do") + " / " + getElement();
        }
    }

}
