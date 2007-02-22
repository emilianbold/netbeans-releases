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

/*
 * CompositionEdgeRenderer.java
 *
 * Created on October 30, 2005, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse.render;

import java.awt.Polygon;
import prefuse.render.EdgeRenderer;

/**
 *
 * @author Jeri Lockhart
 */
public class CompositionEdgeRenderer
                extends EdgeRenderer {


    protected static final Polygon COMPOSITION_ARROW_HEAD =
            new Polygon(new int[] {0,-4,0,4,0}, new int[] {0,-8,-16,-8,0}, 5);

    /** Creates a new instance of CompositionEdgeRenderer */
    public CompositionEdgeRenderer() {
        super();
        m_arrowHead = COMPOSITION_ARROW_HEAD;
    }
    
}
