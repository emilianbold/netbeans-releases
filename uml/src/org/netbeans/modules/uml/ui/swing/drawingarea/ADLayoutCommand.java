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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.command.TSELayoutCommand;
//import com.tomsawyer.layout.TSLayoutServer;
import com.tomsawyer.service.layout.jlayout.client.TSLayoutProxy;
import com.tomsawyer.service.layout.jlayout.*;
import com.tomsawyer.service.layout.TSLayoutOutputTailor;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.service.client.TSServiceProxy;
import com.tomsawyer.editor.service.TSEAllOptionsServiceInputData;

public class ADLayoutCommand extends TSELayoutCommand {

	private TSEGraphWindow m_graphWindow;

	//public ADLayoutCommand(TSEGraphWindow pGraphWindow, String pLayoutStyle, TSLayoutServer pLayoutServer, boolean pIncremental) {
	//	super(pGraphWindow, pLayoutStyle, pLayoutServer, pIncremental);        
	/* commented by jyothi
        public ADLayoutCommand(TSEGraphWindow pGraphWindow, TSLayoutProxy pLayoutServer, TSEAllOptionsServiceInputData pInputData, int pLayoutStyle) {
		super(pGraphWindow, pLayoutServer, pInputData, pLayoutStyle);        
        
		this.m_graphWindow = pGraphWindow;
	}
         */
       
        public ADLayoutCommand(TSEGraphWindow pGraphWindow, 
                        TSServiceProxy pServiceProxy,
                        TSEAllOptionsServiceInputData pInputData, 
                        int pLayoutStyle) {
            super(pGraphWindow, pServiceProxy, pInputData, pLayoutStyle);
            
            this.m_graphWindow = pGraphWindow;
        }
        
        public ADLayoutCommand(TSEGraphWindow pGraphWindow, 
                        TSServiceProxy pServiceProxy,
                        TSEAllOptionsServiceInputData pInputData) {
            super(pGraphWindow, pServiceProxy, pInputData);
            
            this.m_graphWindow = pGraphWindow;
        }
           
	protected void doAction() throws Throwable {
		ETGraph graph = (ETGraph) this.m_graphWindow.getGraph();
		
		// Send preLayoutEvent to graph
		graph.onGraphEvent(IGraphEventKind.GEK_PRE_LAYOUT,null,null,null);                

		super.doAction();

		// Send postLayoutEvent to graph
		graph.onGraphEvent(IGraphEventKind.GEK_POST_LAYOUT,null,null,null);
	}

}
