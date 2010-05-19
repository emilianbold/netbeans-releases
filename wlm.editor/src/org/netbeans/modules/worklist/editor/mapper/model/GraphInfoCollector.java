/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;

/**
 * An auxiliary class which collects information about a graph, which 
 * is used for preparing changes to WLM model.
 *
 * @author nk160297
 */
public class GraphInfoCollector {
        
    private Graph mGraph;

    // Contains vertex roots which are connected to the right tree
    private ArrayList<Vertex> mPrimaryRoots;

    // Contains vertex roots which are unconnected to the right tree
    private ArrayList<Vertex> mSecondryRoots;

    // Contains links from the left to the right tree
    private ArrayList<Link> mTransitLink;
//    private boolean isXmlLiteral = false;

    public GraphInfoCollector(Graph graph) {
        mGraph = graph;
    }

    public ArrayList<Vertex> getPrimaryRoots() {
        if (mPrimaryRoots == null) {
            calculate();
        }
        return mPrimaryRoots;
    }

    public ArrayList<Vertex> getSecondryRoots() {
        if (mSecondryRoots == null) {
            calculate();
        }
        return mSecondryRoots;
    }

    public ArrayList<Link> getTransitLinks() {
        if (mTransitLink == null) {
            calculate();
        }
        return mTransitLink;
    }

//    public boolean isXmlLiteral() {
//        if (mPrimaryRoots == null) {
//             calculate();
//        }
//        return isXmlLiteral;
//    }
    /**
     * Indicates if there isn't any links between the left and right trees.
     * @return
     */
    public boolean noLinksAtAll() {
        return getPrimaryRoots().isEmpty() && getTransitLinks().isEmpty();
    }
    
    /**
     * Indicates if there is the only one link between the left and right trees.
     * @return
     */
    public boolean onlyOneTransitLink() {
        return getSecondryRoots().isEmpty() && 
               getPrimaryRoots().isEmpty() && 
               getTransitLinks().size() == 1;
    }

    private void calculate() {
        List<Vertex> vertexes = mGraph.getVerteces(); 
        List<Link> links = mGraph.getLinks();
//        if (vertexes != null && vertexes.size() == 1 &&
//                links != null && links.size() == 1)
//        {
//            Vertex vertex = vertexes.get(0);
//            if (vertex instanceof Constant && vertex.getOutgoingLink() != null &&
//                    vertex.getItem(0).getDataObject() instanceof XmlLiteralDataObject)
//            {
//
//                isXmlLiteral = true;
//            }
//        }
        //
        mPrimaryRoots = new ArrayList<Vertex>();
        mSecondryRoots = new ArrayList<Vertex>();
        mTransitLink = new ArrayList<Link>();
        //
        // Calculate roots
        mSecondryRoots = new ArrayList<Vertex>();
        mPrimaryRoots = new ArrayList<Vertex>();
        for (Vertex vertex : vertexes) {
            Link link = vertex.getOutgoingLink();
            if (link == null) {
                mSecondryRoots.add(vertex);
            } else if (link.getTarget() == mGraph) {
                mPrimaryRoots.add(vertex);
            }
        }
        //
        // Calculate links from the left to the right tree
        mTransitLink = new ArrayList<Link>();
        for (Link link : links) {
            SourcePin linkSource = link.getSource();
            TargetPin linkTarget = link.getTarget();
            //
            if (linkSource instanceof TreeSourcePin && linkTarget == mGraph) {
                mTransitLink.add(link);
            }
        }
    }
}
