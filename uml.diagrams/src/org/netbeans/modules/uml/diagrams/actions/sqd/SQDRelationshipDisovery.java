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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.event.ActionEvent;
import org.netbeans.modules.uml.drawingarea.actions.DiscoverRelationshipAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 * message edges are specific and can't be handled by just attach edge on scene 
 * (because of location, additional objects cretion like execution specification)
 * @author sp153251
 */
public class SQDRelationshipDisovery extends DiscoverRelationshipAction{
    
    private DesignerScene scene;
   
    public SQDRelationshipDisovery(DesignerScene scene)
    {
        super(scene);
        this.scene=scene;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent event)
    {
//        ETArrayList<IElement> elements = new ETArrayList<IElement>();
//        HashSet<IPresentationElement> edges = new HashSet<IPresentationElement>();
//
//        Collection<IPresentationElement> nodes = scene.getNodes();
//        if (scene.getSelectedObjects().size() > 0)
//        {
//            nodes = (Set<IPresentationElement>) scene.getSelectedObjects();
//        }
//
//
//        for (IPresentationElement pe : nodes)
//        {
//            elements.add(pe.getFirstSubject());
//            edges.addAll(scene.findNodeEdges(pe, true, true));
//        }
//
//        ArrayList<IRelationProxy> proxies = RelationshipDiscovery.discoverRelations(elements);
//
//        for (IRelationProxy proxy : proxies)
//        {
//            boolean edgeExist = false;
//            IElement connection = proxy.getConnection();
//            for (IPresentationElement pe : edges)
//            {
//                if (pe.getFirstSubject().equals(connection))
//                {
//                    edgeExist = true;
//                    break;
//                }
//            }
//
//            // avoid creating duplicates
//            if (edgeExist)
//            {
//                continue;
//            }
//
//            IElement from = proxy.getFrom();
//            IElement to = proxy.getTo();
//
//            IPresentationElement edge = RelationshipDiscovery.createEdgePresentationElement(connection);
//            Widget w = scene.addEdge(edge);
//
//            IPresentationElement sourceElement = null;
//            IPresentationElement targetElement = null;
//
//            for (IPresentationElement pe : nodes)
//            {
//                if (pe.getFirstSubject().equals(from))
//                {
//                    sourceElement = pe;
//                    break;
//                }
//            }
//            for (IPresentationElement pe : nodes)
//            {
//                if (pe.getFirstSubject().equals(to))
//                {
//                    targetElement = pe;
//                    break;
//                }
//            }
//
//            scene.setEdgeSource(edge, sourceElement);
//            scene.setEdgeTarget(edge, targetElement);
//
//            Lookup lookup = w.getLookup();
//            if (lookup != null)
//            {
//                LabelManager manager = lookup.lookup(LabelManager.class);
//                if (manager != null)
//                {
//                    manager.createInitialLabels();
//                }
//            }
//
//            scene.validate();
//        }
    }
}
