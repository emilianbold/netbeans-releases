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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.TableLayout;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.actions.NewAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


public class PartWidget extends AbstractWidget<Part> {
    
    private LabelWidget nameWidget;
    private PartTypeChooserWidget typeWidget;
	private WidgetAction editorAction;
    
    public PartWidget(Scene scene, Part part, Lookup lookup) {
        super(scene, part, lookup);

        setLayout(ROW_LAYOUT);
        setBorder(BORDER);
        
        createContent();
    }
    
    
    private void removeContent() {
        removeChildren();
    }
    
    PartTypeChooserWidget getPartChooserWidget() {
        return typeWidget;
    }
    
    private void createContent() {
        Part part = getWSDLComponent();
        String name = part.getName();
        
        if (name == null) {
            name = NbBundle.getMessage(PartWidget.class, "LBL_Undefined"); // NOI18N
        } else if (name.trim().equals("")) { // NOI18N
            name = NbBundle.getMessage(PartWidget.class, "LBL_Empty"); // NOI18N
        }

        nameWidget = createLabelWidget(getScene(), name);
        typeWidget = new PartTypeChooserWidget(getScene(), part);
        
        
        getActions().addAction(new WidgetAction.Adapter() {
    		
        	@Override
			public State keyPressed (Widget widget, WidgetKeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_F2) {
			        InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
			        if (inplaceEditorController.openEditor (nameWidget)) {
			        	return State.createLocked (widget, this);
			        }
			        return State.CONSUMED;
				}
				return State.REJECTED;
			}
		
		});
        
        addChild(nameWidget);
        addChild(typeWidget);
    }
    
    
    public void updateContent() {
        removeContent();
        createContent();
    }    
    
    
    private LabelWidget createLabelWidget(Scene scene, String text) {
        LabelWidget result = new LabelWidget(scene, text);
        result.setBorder(CELL_BORDER);
        result.setFont(scene.getDefaultFont());
        result.setAlignment(LabelWidget.Alignment.LEFT);
        result.setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
/*        result.getActions().addAction(ActionFactory.createInplaceEditorAction(
                (InplaceEditorProvider<JTextField>) new PartNameInplaceEditorProvider()));*/
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {
    		
        	public boolean isEnabled(Widget widget) {
                Part part = getPart(widget);
                if (part != null) {
                    return XAMUtils.isWritable(part.getModel());
                }
                return false;
            }

            
            public String getText(Widget widget) {
                Part part = getPart(widget);
                String name = (part != null) ? part.getName() : null;
                return (name == null) ? "" : name; // NOI18N
            }

            
            public void setText(Widget widget, String text) {
                Part part = getPart(widget);
                if (part != null && !part.getName().equals(text)) {
                    // try rename silent and locally
                    SharedUtils.locallyRenameRefactor(part, text);
                }
            }
		
            private Part getPart(Widget widget) {
                PartWidget partWidget = getPartWidget(widget);
                return (partWidget == null) ? null : partWidget.getWSDLComponent();
            }
            
            
            private PartWidget getPartWidget(Widget widget) {
                for (Widget w = widget; w != null; w = w.getParentWidget()) {
                    if (w instanceof PartWidget) {
                        return (PartWidget) w;
                    }
                }
                return null;
            }
		}, null);
        result.getActions().addAction(editorAction);
        return result;
    }


    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        if (state.isSelected()) {
            MessageWidget messageWidget = getMessageWidget();
            if (messageWidget != null) {
                messageWidget.updateButtonState();
            }
        } else {
            MessageWidget messageWidget = getMessageWidget();
            if (messageWidget != null) {
                messageWidget.updateButtonState();
            }
        }
    }
    

    private MessageWidget getMessageWidget() {
        for (Widget w = this; w != null; w = w.getParentWidget()) {
            if (w instanceof MessageWidget) return (MessageWidget) w;
        }
        return null;
    }
    
    
    public static final Layout ROW_LAYOUT = new TableLayout(2, 1, 0, 100);
    public static final Border CELL_BORDER = new FilledBorder(0, 0, 1, 8, null, 
            Color.WHITE);

    
    private static final Border BORDER = new FilledBorder(
            new Insets(1, 0, 0, 0), new Insets(0, 0, 0, 0),
            new Color(0x999999), null);
    

    protected Shape createSelectionShape() {
        Rectangle rect = getBounds();
        return new Rectangle2D.Double(rect.x + 1, rect.y + 2, rect.width - 2, 
                rect.height - 3);
    }

    @Override
    protected void updateActions(List<Action> actions) {
        super.updateActions(actions);
        ListIterator<Action> liter = actions.listIterator();
        while (liter.hasNext()) {
            Action action = liter.next();
            if (action instanceof NewAction) {
                liter.remove();
            }
        }
    }
}
