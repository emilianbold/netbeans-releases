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

package org.netbeans.modules.uml.diagrams.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Widget.Dependency;
import org.netbeans.modules.uml.diagrams.nodes.MovableLabelWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.drawingarea.actions.IterateSelectAction;
import org.netbeans.modules.uml.drawingarea.actions.Selectable;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.Lookup;

/**
 *
 * @author jyothi
 */
public class NodeLabelIteratorAction extends IterateSelectAction {

    @Override
    protected Widget getTargetWidget(Widget widget)
    {
        Widget retVal = null;
        if (widget instanceof UMLNodeWidget)
            retVal = widget;
        else
        {
            //is it a nodeLabel?
            if (widget instanceof MovableLabelWidget)
            {
                Widget attWid = ((MovableLabelWidget)widget).getAttachedNodeWidget();
                if (attWid instanceof UMLNodeWidget)
                {
                    retVal = attWid;
                }
                else
                {
                    //get the parent UMLNodeWidget of attWid
                    retVal = PersistenceUtil.getParentUMLNodeWidget(attWid);
                }
            }          
        }
        return retVal;
    }

    @Override
    public List<Widget> getAllSelectableWidgets(Widget widget)
    {
        ArrayList < Widget > retVal = new ArrayList < Widget >();
        Lookup lookup = widget.getLookup();
        if(lookup != null && lookup.lookup(Selectable.class) != null)
        {
            if (widget.isVisible())
            {
                retVal.add(widget);
            }
        }
        
        for (Widget child : widget.getChildren())
        {
            if (child instanceof UMLNodeWidget)
            {
                break;
            }
            List<Widget> selectables = getAllSelectableWidgets(child);
            if (selectables.size() > 0)
            {
                retVal.addAll(selectables);
            }
            //now get all node labels 
            for (Dependency dep : widget.getDependencies())
            {
                if (dep instanceof LabelWidget)
                {
                    List<Widget> nodeLabels = getAllSelectableWidgets((Widget) dep);
                    if (nodeLabels.size() > 0)
                    {
                        retVal.addAll(nodeLabels);
                    }
                }
            }
        }
        
        return retVal;

    }
    
}
