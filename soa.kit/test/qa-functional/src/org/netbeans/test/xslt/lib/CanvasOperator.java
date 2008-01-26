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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xslt.lib;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;

/**
 *
 * @author ca@netbeans.org
 */

public class CanvasOperator extends JComponentOperator {
    
    private BasicCanvasView m_canvasView;
    
    /** Creates a new instance of CanvasOperator */
    public CanvasOperator(JComponentOperator opContainer) {
        super(opContainer, new ComponentChooser() {
            final String strClassName = "org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasView";
            
            public boolean checkComponent(java.awt.Component comp) {
                return comp.getClass().toString().equals("class " + strClassName);
            }
            
            public String getDescription() {
                return strClassName;
            }
        });
        
        m_canvasView = (BasicCanvasView) getSource();
    }
    
    public MethoidOperator findMethoid(String strName, int index) {
        int counter = 0;
        
        List nodeList = m_canvasView.getNodes();
        Iterator iterator = nodeList.iterator();
        
        while(iterator.hasNext()) {
            Object jgoObj = iterator.next();
            
            if (!(jgoObj instanceof ICanvasMethoidNode)) {
                continue;
            }
            
            ICanvasMethoidNode methoidCanvasNode = (ICanvasMethoidNode) jgoObj;
            
            if (methoidCanvasNode.getTitle().equals(strName)) {
                if (counter++ == index) {
                    return new MethoidOperator(methoidCanvasNode);
                }
            }
        }
        
        return null;
    }
    
    public void listCanvasObjects() {
        List nodes = m_canvasView.getNodes();
        
        Iterator iter = nodes.iterator();
        
        while(iter.hasNext()) {
            Object obj = iter.next();
            Helpers.writeJemmyLog("Object: " + obj.toString());
        }
    }
}
