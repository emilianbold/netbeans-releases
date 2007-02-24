/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * Created on Sep 7, 2004
 *
 */
package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;



/**
 * @author josephg
 *
 * CrossDiagramPasteTool definition
 */
public class CrossDiagramPasteTool extends ADPasteState
{
    private IDiagram m_parentDiagram = null;

    public IDiagram getParentDiagram()
    {
        return m_parentDiagram;
    }

    public void setParentDiagram(IDiagram diagram)
    {
        m_parentDiagram = diagram;
    }

    public void onMouseReleased(java.awt.event.MouseEvent mouseEvent) 
    {
        if(m_parentDiagram!=null)
        {
            
            IDrawingAreaControl control = getDrawingArea();
            if(control != null)
            {
                control.crossDiagramPaste(mouseEvent.getPoint());
            }
        }
        cancelAction();
    }
    
    public void paint(com.tomsawyer.editor.graphics.TSEGraphics tSEGraphics) {
        //super.paint(tSEGraphics);
    }
    
}
