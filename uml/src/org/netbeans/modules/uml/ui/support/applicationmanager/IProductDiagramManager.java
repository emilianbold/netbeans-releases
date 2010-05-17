/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IProductDiagramManager
{
    /**
     * Tell the gui to open a diagram.  If bMaximized then the diagram is opened maximized so it draws.
     */
    public IDiagram openDiagram( String sTOMFilename, boolean bMaximized, IDiagramCallback pDiagramCreatedCallback );
    
    /**
     * Tell the gui to open a diagram
     */
    public IDiagram openDiagram2( IProxyDiagram pProxyDiagram, boolean bMaximized, IDiagramCallback pDiagramCreatedCallback );
    
    /**
     * Tell the gui to close a diagram
     */
    public long closeDiagram( String sTOMFilename );
    
    /**
     * Tell the gui to close a diagram
     */
    public long closeDiagram2( IDiagram pDiagram );
    
    /**
     * Tell the gui to close this diagram
     */
    public long closeDiagram3( IProxyDiagram pProxyDiagram );
    
    /**
     * Tell the gui to open the new diagram dialog
     */
    public IDiagram newDiagramDialog( INamespace pNamespace, /* DiagramKind */ int nDefaultDiagram, int lAvailableDiagramKinds, IDiagramCallback pDiagramCreatedCallback );
    
    /**
     * Bring this diagram to the front
     */
    public long raiseWindow( IDiagram pOpenControl );
    
    /**
     * Returns the currently active diagram.
     */
    public IDiagram getCurrentDiagram();
    
    /**
     * Returns the diagram with this name, returns 0 if the diagram is not open.
     */
    public IDiagram getOpenDiagram( String sTOMFilename );
    
    /**
     * Create a new diagram
     */
    public IDiagram createDiagram( /* DiagramKind */ int nDiagramKind, INamespace pNamespace, String sDiagramName, IDiagramCallback pDiagramCreatedCallback );
    
    /**
     * Returns all the open diagrams.
     */
    public ETList<IProxyDiagram> getOpenDiagrams();
    
    /**
     * Tell the gui to minimize a diagram
     */
    public long minimizeDiagram( String sTOMFilename, boolean bMinimize );
    
    /**
     * Tell the gui to minimize a diagram
     */
    public long minimizeDiagram2( IDiagram pDiagram, boolean bMinimize );
    
    /**
     * Tell the gui to minimize this diagram
     */
    public long minimizeDiagram3( IProxyDiagram pProxyDiagram, boolean bMinimize );
    
    /** Refresh the diagram by reading in the contents of the diagram file. */
    public void refresh(IProxyDiagram proxy);
    
   /* close all diagrams
    */
    
    public void closeAllDiagrams();

    public void setDiagramDirty(IDiagram diagram,boolean b);
    
}
