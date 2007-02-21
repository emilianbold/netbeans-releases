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


package org.netbeans.modules.bpel.design.model.connections;


import java.awt.Color;
import java.awt.Graphics2D;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;



/**
 *
 * @author aa160298
 */
public class MessageConnection extends Connection {

    private int number;
    private int count;

    /** 
     * Creates a new instance of MessageConnection.
     */
    
    public MessageConnection(Pattern p) {
        super(p);
        setPaintCircle(true);
        setPaintDashed(true);
    }

    
    protected double findXStep(double x1, double dx) {
        BorderElement border = ((ProcessPattern) getPattern().getModel()
                .getRootPattern()).getBorder();
        
        double xMax = border.getX() - LayoutManager.HSPACING / 4.0;
        double xMin = xMax - LayoutManager.HSPACING * (3.5);
        
        double k = (count == 1) ? 0.5 : (double) number / (count - 1);
        double x = xMin + k * (xMax - xMin);

        return x;
    }

    
    private boolean isPatternSelected() {
        Pattern p = getPattern();
        if (p == null) return false;
        
        DiagramModel model = p.getModel();
        if (model == null) return false;
        
        DesignView view = model.getView();
        if (view == null) return false;
        
        EntitySelectionModel selection = view.getSelectionModel();
        if (selection == null) return false;
        
        return p == selection.getSelectedPattern();
    }
    
    
    public void paint(Graphics2D g2) {
        Pattern p = getPattern();
        
        FPath path = getPath();
        assert (path != null): "Invalid connection(path is null) found on diagram: " + this;
        
        if (isPatternSelected()) {
            Connection.paintConnection(g2, path, isPaintDashed(), 
                    isPaintArrow(), isPaintSlash(), isPaintCircle(), 2, COLOR);
        } else {
            Connection.paintConnection(g2, path, isPaintDashed(), 
                    isPaintArrow(), isPaintSlash(), isPaintCircle(), null);
        }
    }    
    
    
    public void setNumber(int newNumber, int newCount) {
        this.number = newNumber;
        this.count = newCount;
    }
    
    
    private static final Color COLOR = new Color(0x5D985C);
}
