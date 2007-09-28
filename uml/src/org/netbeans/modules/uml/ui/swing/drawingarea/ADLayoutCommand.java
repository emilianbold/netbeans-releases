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
