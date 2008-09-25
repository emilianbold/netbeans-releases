/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Insets;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.InplaceEditorProvider.EditorController;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLMultilineLabelWidget;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;

/**
 *
 * @author sp153251
 */
public class MultilineEditableCompartmentWidget extends UMLMultilineLabelWidget
{
    private InplaceEditorProvider.EditorController edcAction;

    /**
     * Creates empty label
     * EC will use border of this widget and with model element derived from this widget
     * 
     * 
     * @param scene
     */
    public MultilineEditableCompartmentWidget(Scene scene, 
            String propId, String propDisplayName)
    {
        this(scene, "", null, (Widget) null, propId, propDisplayName);
    }

    /**
     * Creates label with text
     * EC will use border of this widget and with model element derived from this widget
     * 
     * 
     * @param scene
     * @param text - text to label
     */
    public MultilineEditableCompartmentWidget(Scene scene, String text, 
            String propId, String propDisplayName)
    {
        this(scene, text, null, (Widget) null, propId, propDisplayName);
    }

    public MultilineEditableCompartmentWidget(Scene scene, IElement modelElement,
            String propId, String propDisplayName)
    {
        this(scene, "", null, modelElement, propId, propDisplayName);
    }

    /**
     * @param baseGraphWidget border of toFit widget will be considered as 
     *                        bounds for edit control 
     * @param basModelWidget will be used to get presentation element (for 
     *                       example name will take class widget and appropriate
     *                       presentation when attribute will take attribute 
     *                       widget with attribute presentation)
     * @param text  - text to label
     */
    public MultilineEditableCompartmentWidget(Scene scene,
                                               String text,
                                               Widget baseGraphWidget,
                                               Widget baseModelWidget, 
                                               String propId, 
                                               String propDisplayName)
    {
        super(scene, text, propId, propDisplayName);
        setAlignment(Alignment.CENTER);
        EditControlEditorProvider provider = new EditControlEditorProvider(baseGraphWidget,
                baseModelWidget);
        WidgetAction action = ActionFactory.createInplaceEditorAction(provider);
        if (action instanceof InplaceEditorProvider.EditorController)
        {
            edcAction = (InplaceEditorProvider.EditorController) action;
        }

        createActions(DesignerTools.SELECT).addAction(action);//TBD need to add lock edit support
    }

    /**
     * @param baseGraphWidget border of toFit widget will be considered as bounds for edit control 
     * @param basModelWidget will be used to get presentation element (for example name will take class widget and appropriate presentation when attribute will take attribute widget with attribute presentation)
     * @param text  - text to label
     */
    public MultilineEditableCompartmentWidget(Scene scene, 
            String text, 
            Widget baseGraphWidget, 
            IElement element,
            String propId,
            String propDisplayName)
    {
        super(scene, text, propId, propDisplayName);
        edcAction = (InplaceEditorProvider.EditorController) ActionFactory.createInplaceEditorAction(new EditControlEditorProvider(baseGraphWidget, element));
        createActions(DesignerTools.SELECT).addAction((WidgetAction) edcAction);//TBD need to add lock edit support
    }

    public void switchToEditMode()
    {
    //    edcAction.openEditor(this);
    }

    private class EditControlEditorProvider implements InplaceEditorProvider<EditControlImpl>
    {

        private Widget baseFitWidget;
        private Widget basePresentationWidget;
        private IElement modelElement;

        public EditControlEditorProvider()
        {

        }

        /**
         * @param toFit border of toFit widget will be considered as bounds for edit control 
         * @param presentationWidget will be used to get presentation element
         */
        public EditControlEditorProvider(Widget toFit, Widget presentationWidget)
        {
            baseFitWidget = toFit;
            basePresentationWidget = presentationWidget;
        }

        /**
         * @param toFit border of toFit widget will be considered as bounds for edit control 
         * @param element specification of corresponding model element
         */
        public EditControlEditorProvider(Widget toFit, IElement element)
        {
            baseFitWidget = toFit;
            modelElement = element;
        }

        public void notifyOpened(final EditorController controller, Widget widget, EditControlImpl editor)
        {
            editor.setVisible(true);
            editor.setAssociatedParent(controller);
        }

        public void notifyClosing(EditorController controller,
                                   Widget widget,
                                   EditControlImpl editor,
                                   boolean commit)
        {
            if ((editor.getModified() == true) && (commit == true))
            {
                editor.handleSave();
            }
            editor.setVisible(false);
            
            // In the case of only allowing the edit control grow down, I want
            // to make sure that the widget is the same hieght after the edit.
//            setPreferredSize(editor.getSize());
//            revalidate();
             if (widget != null)
            {
                Scene scene = widget.getScene();
                scene.validate();
            
                //Fix #138735. Reselect the object when finishing editing to update the property sheet.
                if ( scene instanceof DesignerScene)
                {
                    DesignerScene dScene = (DesignerScene)scene;
                    Set<Object> selectedObjs = (Set<Object>) dScene.getSelectedObjects();
                    if (selectedObjs != null && selectedObjs.size() == 1)
                    {
                        dScene.userSelectionSuggested(selectedObjs, false);
                    }
                }
            }
        }

        public EditControlImpl createEditorComponent(EditorController controller, Widget widget)
        {
            DesignerScene scene = (DesignerScene) widget.getScene();
            Widget toFit = widget;
            if (baseFitWidget != null)
                toFit = baseFitWidget;
            //DiagramEditControl ret = new DiagramEditControl(toFit, true, controller);
           EditControlImpl ret = new EditControlImpl(controller, true);
            ret.setVisible(true);
            
            IElement el = modelElement;
            Widget presW = widget;
            if (el == null)
            {
                if (basePresentationWidget != null)
                    presW = basePresentationWidget;

                el = ((IPresentationElement) scene.findObject(presW)).getFirstSubject();
            }
            ret.setElement(el);
            ret.setFont(getFont());
            ret.setForeColor(widget.getForeground());
            return ret;
        }

        public Rectangle getInitialEditorComponentBounds(EditorController controller, 
                                                         Widget widget, 
                                                         EditControlImpl editor, 
                                                         Rectangle viewBounds)
        {
            Widget toFit = widget;
            if (baseFitWidget != null)
            {
                toFit = baseFitWidget;
            }
            
            Rectangle tmp = toFit.getBounds();
            //System.out.println("MultilineEditableCompartmentWidget - widgetbounds: "+ tmp.toString());
            if(getBorder() != null)
            {
                Insets insets = getBorder().getInsets();
                
                // I need to adjust by 1 each side, so that I am not on top of the border.
                tmp.x += ( insets.left > 0  ? insets.left  : 1); 
                tmp.y +=( insets.top > 0  ? insets.top  : 1); 
                int deltaLen = insets.right + insets.left;
                tmp.width -= (deltaLen > 0 ?  deltaLen : 2);
                deltaLen = insets.bottom + insets.top;
                tmp.height -= (deltaLen > 0 ?  deltaLen : 2);
            }
            tmp = toFit.convertLocalToScene(tmp);
            tmp = widget.getScene().convertSceneToView(tmp);
            
            editor.setMinimumSize(tmp.getSize());
            return tmp;
        }

        public EnumSet<ExpansionDirection> getExpansionDirections(EditorController controller,
                                                                  Widget widget, 
                                                                  EditControlImpl editor)
        {
//            return EnumSet.of(ExpansionDirection.RIGHT, ExpansionDirection.BOTTOM);
//            return null;
            return EnumSet.of(ExpansionDirection.BOTTOM);
        }
    }
}
