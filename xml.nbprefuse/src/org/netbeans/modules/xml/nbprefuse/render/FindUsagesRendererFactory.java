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

/*
 * FindUsagesRendererFactory.java
 *
 * Created on March 3, 2006, 7:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse.render;

import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class FindUsagesRendererFactory implements RendererFactory {
    private NbLabelRenderer nodeRenderer;
    private NbLabelRenderer fileNodeRenderer;
    private NbLabelRenderer queryNodeRenderer;
    private Renderer generalizationRenderer;
    private Renderer compositionRenderer;
    private Renderer referenceRenderer;
    private Renderer fileEdgeRenderer;
    private Renderer aggregateRenderer;
     
    
    /**
     * Construcotr for Graph that doesn't use aggregate items
     *
     */
    public FindUsagesRendererFactory(
            NbLabelRenderer nodeRenderer,
            NbLabelRenderer fileNodeRenderer,
            Renderer generalizationRenderer,
            Renderer compositionRenderer,
            Renderer referenceRenderer,
            Renderer fileEdgeRenderer
            ){
        this(nodeRenderer, fileNodeRenderer, generalizationRenderer,
               compositionRenderer, referenceRenderer,fileEdgeRenderer, null );
    
    }
    
    /**
     * Construcotr for Graph that uses aggregate items
     *
     */
    public FindUsagesRendererFactory(
            NbLabelRenderer nodeRenderer,
            NbLabelRenderer fileNodeRenderer,
            Renderer generalizationRenderer,
            Renderer compositionRenderer,
            Renderer referenceRenderer,
            Renderer fileEdgeRenderer,
            Renderer aggregateRenderer
            ) {
        this.nodeRenderer = nodeRenderer;
        nodeRenderer.setVerticalPadding(5);
        nodeRenderer.setHorizontalPadding(5);
        this.fileNodeRenderer = fileNodeRenderer;
        this.aggregateRenderer = aggregateRenderer;
        fileNodeRenderer.setRoundedCorner(10,10); // arc width, arc height
        fileNodeRenderer.setVerticalPadding(5);
        fileNodeRenderer.setHorizontalPadding(5);
        this.queryNodeRenderer = new NbLabelRenderer();
        queryNodeRenderer.setRoundedCorner(20,20);
        queryNodeRenderer.setVerticalPadding(10);
        queryNodeRenderer.setHorizontalPadding(10);
        this.generalizationRenderer = generalizationRenderer;
        this.compositionRenderer = compositionRenderer;
        this.referenceRenderer = referenceRenderer;
        this.fileEdgeRenderer = fileEdgeRenderer;
    } //
    
    public Renderer getRenderer(VisualItem visualItem) {
        if (visualItem instanceof AggregateItem){
            return aggregateRenderer;
        }
        if ( visualItem instanceof NodeItem ) {
            if (visualItem.canGetBoolean(AnalysisConstants.IS_FILE_NODE) && 
                    visualItem.getBoolean(AnalysisConstants.IS_FILE_NODE)){
                return fileNodeRenderer;
            } 
            else if (visualItem.canGetBoolean(AnalysisConstants.IS_QUERY_NODE) &&
                    visualItem.getBoolean(AnalysisConstants.IS_QUERY_NODE)){
                return queryNodeRenderer;
            }
            else {
                return nodeRenderer;
            }
        }else if ( visualItem instanceof EdgeItem ) {
            String type = visualItem.getString(AnalysisConstants.EDGE_TYPE);
            if (type != null && type.equals(AnalysisConstants.GENERALIZATION)){
                return generalizationRenderer;
            }else if (type != null && type.equals(AnalysisConstants.COMPOSITION)){
                return compositionRenderer;
            }else if (type != null && type.equals(AnalysisConstants.REFERENCE)){
                return referenceRenderer;
            }
            if (visualItem.getString(AnalysisConstants.EDGE_TYPE) != null &&
                    visualItem.getString(AnalysisConstants.EDGE_TYPE).equals(AnalysisConstants.FILE_EDGE_TYPE) ){
                return fileEdgeRenderer;
            }
        }
        return fileEdgeRenderer;
    } //
} // end of inner class FindUsagesRendererFactory
