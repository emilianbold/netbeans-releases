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

import java.awt.Font;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.InplaceEditorProvider.EditorController;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.DiagramEditorNameCollisionHandler;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sp153251
 */
public class EditableCompartmentWidget extends UMLLabelWidget {

    private InplaceEditorProvider.EditorController edcAction;
    
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    public static final String ID = "EditableCompartment";
    /**
     * Creates empty label
     * EC will use border of this widget and with model element derived from this widget
     * 
     * 
     * @param scene
     */
    public EditableCompartmentWidget(Scene scene)
    {
        this(scene,"",null,(Widget)null, ID, "");
    }
   
    
     public EditableCompartmentWidget(Scene scene, Widget toFit, String id)
    {
        this(scene, "", toFit, (IElement)null, id, null);
    }
    
    /**
     * Creates label with text
     * EC will use border of this widget and with model element derived from this widget
     * 
     * 
     * @param scene
     * @param text - text to label
     */
    public EditableCompartmentWidget(Scene scene,String text)
    {
        this(scene,text,null,(Widget)null, ID, "");
    }
    
    public EditableCompartmentWidget(Scene scene, IElement modelElement, String id, String displayName)
    {
        this(scene, "", null, modelElement, id, displayName);
    }
    
     public EditableCompartmentWidget(Scene scene,String id, String displayName)
    {
        this(scene,"",null,(Widget)null, id, displayName);
        
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
    public EditableCompartmentWidget(Scene scene,
                                     String text,
                                     Widget baseGraphWidget,
                                     Widget baseModelWidget,
                                     String propertyID,
                                     String displayName)
    {
        super(scene,text, propertyID, displayName);
        
        EditControlEditorProvider provider = new EditControlEditorProvider(baseGraphWidget,
                                                                           baseModelWidget);
        WidgetAction action = ActionFactory.createInplaceEditorAction(provider);
        if(action instanceof InplaceEditorProvider.EditorController)
        {
            edcAction=(InplaceEditorProvider.EditorController) action;
            lookupContent.add(edcAction);
        }
        
        createActions(DesignerTools.SELECT).addAction(action);//TBD need to add lock edit support
    }
    
    /**
     * @param baseGraphWidget border of toFit widget will be considered as bounds for edit control 
     * @param basModelWidget will be used to get presentation element (for example name will take class widget and appropriate presentation when attribute will take attribute widget with attribute presentation)
     * @param text  - text to label
    */
    public EditableCompartmentWidget(Scene scene,
                                     String text,
                                     Widget baseGraphWidget,
                                     IElement element,
                                     String id,
                                     String displayName)
    {
        super(scene,text, id, displayName);
        edcAction=(InplaceEditorProvider.EditorController) ActionFactory.createInplaceEditorAction(new EditControlEditorProvider(baseGraphWidget,element));
//        getActions().addAction((WidgetAction)edcAction);//TBD need to add lock edit support
        createActions(DesignerTools.SELECT).addAction((WidgetAction)edcAction);//TBD need to add lock edit support
        
    }
    
    public void switchToEditMode()
    {
        getScene().validate();
        if(edcAction.openEditor(this))WidgetAction.State.createLocked (this,(WidgetAction) edcAction);
        //((WidgetAction) edcAction).keyPressed(this, new WidgetAction.WidgetKeyEvent(0, new KeyEvent(getScene().getView(), 0, 0, 0, KeyEvent.VK_ENTER,(char)KeyEvent.VK_ENTER)));
    }
    
//    public void closeEditorNoChanges()
//    {
//        getScene().validate();
//        edcAction.closeEditor(false);
//    }
    public void closeEditorCommitChanges()
    {
        getScene().validate();
        edcAction.closeEditor(true);
    }

    
    @Override
    public Lookup getLookup()
    {
        return lookup;
    }
    
    
    private class EditControlEditorProvider implements InplaceEditorProvider<EditControlImpl>
    {
        private Widget baseFitWidget;
        private Widget basePresentationWidget;
        private IElement modelElement;
        private NameCollisionListener m_NameCollisionListener = new NameCollisionListener();
	private DiagramEditorNameCollisionHandler m_CollisionHandler = new DiagramEditorNameCollisionHandler();
				
        public EditControlEditorProvider()
        {
            m_NameCollisionListener.setHandler(m_CollisionHandler);
        }
        
        
        /**
         * @param toFit border of toFit widget will be considered as bounds for edit control jm=[]
         * @param presentationWidget will be used to get presentation element
         */
        public EditControlEditorProvider(Widget toFit,Widget presentationWidget)
        {
            this();
            baseFitWidget=toFit;
            basePresentationWidget=presentationWidget;
        }
        
        /**
         * @param toFit border of toFit widget will be considered as bounds for edit control 
         * @param element specification of corresponding model element
         */
        public EditControlEditorProvider(Widget toFit,IElement element)
        {
            this();
            baseFitWidget=toFit;
            modelElement=element;
        }

        public void notifyOpened(final EditorController controller, Widget widget, final EditControlImpl editor) {
            editor.setVisible(true);
            editor.setAssociatedParent(controller);
            m_NameCollisionListener.setEnabled(true);
            DocumentListener listener = new DocumentListener()
            {

                public void insertUpdate(DocumentEvent e)
                {
                    editor.revalidate();
                    controller.notifyEditorComponentBoundsChanged();
                }

                public void removeUpdate(DocumentEvent e)
                {
                    editor.revalidate();
                    controller.notifyEditorComponentBoundsChanged();
                }

                public void changedUpdate(DocumentEvent e)
                {
                    editor.revalidate();
                    controller.notifyEditorComponentBoundsChanged();
                }
            };

            editor.addDocumentListener(listener);
            
            Scene scene = widget.getScene();
            ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
            
            if(manager != null)
            {
                manager.cancelPalette();
            }
        }

        public void notifyClosing(EditorController controller, 
                                  Widget widget, 
                                  EditControlImpl editor, 
                                  boolean commit) 
        {
            if((editor.getModified() == true) && (commit == true)) 
            {
                editor.handleSave();
            }
            editor.setVisible(false);
            
            Scene scene = widget.getScene();
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
            
            m_NameCollisionListener.setEnabled(false);
        }

        public EditControlImpl createEditorComponent(EditorController controller, Widget widget)
        {
            
            
            DesignerScene scene = (DesignerScene) widget.getScene();
            
            Object data = scene.findObject(widget);
            Widget assocWidget = scene.findWidget(data);

            // When using the keystroke ENTER to open the inplaced editor the
            // logic states that only the focused widget can enter into edit
            // mode.  Without this logic the last editable field in the last
            // editable node always enter into edit mode when the enter key is
            // pressed.  Therefore the reason for the focus logic.
            //
            // However, In some case the widget can not have keyboard focus. For
            // example subpartitions in a partition.  However we do not want to
            // stop the user from entering into edit mode via the mouse.
            //
            // Therefore check if the user we are trying to enter into edit
            // mode via a mouse press or a keyboard action.
            boolean canEdit = assocWidget.getState().isFocused();
            if(canEdit == false)
            {
                if (controller instanceof TypedEditorController)
                {
                    TypedEditorController typedController = (TypedEditorController) controller;
                    EditorInvocationType type = typedController.getEditorInvocationType();
                    if((type == EditorInvocationType.CODE) ||
                       (type == EditorInvocationType.MOUSE))
                    {
                        canEdit = true;
                    }
                }
            }
            
            if((assocWidget == null) || (canEdit == false))
            {
                return null;
            }
            
            Widget toFit = widget;
            if (baseFitWidget != null)
                toFit = baseFitWidget;
            DiagramEditControl ret = new DiagramEditControl(toFit, controller);
//            ret.setOpaque(true);
            ret.setVisible(true);
            
            ret.setAutoExpand(true);
            
            IElement el = modelElement;
            if (el == null)
            {
                Widget presW = widget;
                if (basePresentationWidget != null)
                    presW = basePresentationWidget;

                el = ((IPresentationElement) scene.findObject(presW)).getFirstSubject();
            }

            double zoomFactor = scene.getZoomFactor();
            if (zoomFactor != 1.0)
            {
                Font font = getFont();
                font = font.deriveFont((float) (font.getSize2D() * zoomFactor));
                ret.setFont(font);
            }
            else
            {
                ret.setFont(getFont());
            }

            ret.setElement(el);
//            ret.selectAll();
            return ret;
        }

        public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, EditControlImpl editor, Rectangle viewBounds)
        {
            Widget toFit = widget;
            if (baseFitWidget != null)
            {
                toFit = baseFitWidget;
            }
            
            Rectangle tmp = toFit.getClientArea();
            tmp = toFit.convertLocalToScene(tmp);
            tmp = widget.getScene().convertSceneToView(tmp);
            tmp.x += 1;
            tmp.y += 1;
            tmp.width -= 2;  // 1 on the left and 1 on the right sides
            tmp.height -= 2;
            
            editor.setMinimumSize(tmp.getSize());
            return tmp;
        }

        public EnumSet<ExpansionDirection> getExpansionDirections(EditorController controller, Widget widget, EditControlImpl editor) {
//            return null;
            return EnumSet.of(ExpansionDirection.RIGHT);
        }

     }
}
