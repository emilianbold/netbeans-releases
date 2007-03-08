/*
 * OperationWidget.java
 *
 * Created on March 7, 2007, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends LabelWidget{
    
    private static final Border BORDER_4 = BorderFactory.createLineBorder(4, new Color(128,191,255));

    private WidgetAction moveAction = ActionFactory.createMoveAction ();

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param label 
     */
    public OperationWidget(Scene scene, String label) {
        super(scene,label);
        setBorder(BORDER_4);
        getActions().addAction(moveAction);
        setOpaque(true);
        setBackground(new Color(191,255,255));
    }
    
}
