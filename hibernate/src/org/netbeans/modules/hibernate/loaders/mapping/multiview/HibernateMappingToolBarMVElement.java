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
package org.netbeans.modules.hibernate.loaders.mapping.multiview;

import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.mapping.model.Resultset;
import org.netbeans.modules.hibernate.mapping.model.Typedef;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.NbBundle;

/**
 * ToolBarMultiView for Hibernate Configuration file
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingToolBarMVElement extends ToolBarMultiViewElement {

    public static final String META_DATA = "Meta";
    public static final String IMPORT_ELEMENT="Import";
    public static final String RETURN_SCALAR= "Return Scalar";
    
    private MappingView view;
    private ToolBarDesignEditor comp;
    private HibernateMappingDataObject mappingDataObject;
    private HibernateMappingPanelFactory factory;
    private Project project;

    public HibernateMappingToolBarMVElement(HibernateMappingDataObject dObj) {
        // TODO: not supported till I can figure out conflicts between the XmlMultiViewDataObject and completion provider
        //super(dOjb);
        super(null);
        this.mappingDataObject = dObj;
        this.project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());

        comp = new ToolBarDesignEditor();
        factory = new HibernateMappingPanelFactory(comp, dObj);
        setVisualEditor(comp);

    /*repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
    public void run() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    repaintView();
    }
    });
    }
    });*/
    }

    public SectionView getSectionView() {
        return view;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();

    }

    @Override
    public void componentClosed() {
        super.componentClosed();

    }

    @Override
    public void componentShowing() {
        super.componentShowing();

        // TODO: can have more logic to handle when the view can not be displayed. See Persistence
        view = new MappingView(mappingDataObject);
        view.initialize();
        comp.setContentView(view);

        Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            // Expand the first node in session factory if there is one
            Node childrenNodes[] = view.getClassesContainerNode().getChildren().getNodes();
            if (childrenNodes.length > 0) {
                view.selectNode(childrenNodes[0]);
            }
        }

        view.checkValidity();

    }

    private class MappingView extends SectionView {

        private HibernateMappingDataObject mappingDataObject;
        
        private Node classesContainerNode;
        private SectionContainer classesCont;
        
        private Node typedefContainerNode;
        private SectionContainer typedefCont;
        
        private Node resultsetsContainerNode;
        private SectionContainer resultsetsCont;

        public SectionContainer getClassesContainer() {
            return classesCont;
        }

        public Node getClassesContainerNode() {
            return classesContainerNode;
        }

        MappingView(HibernateMappingDataObject dObj) {
            super(factory);
            mappingDataObject = dObj;
        }

        /**
         * Initialize the view
         */
        void initialize() {

            HibernateMapping mapping = mappingDataObject.getHibernateMapping();

            // Node meta
            Node metaNode = new ElementLeafNode("Mapping Meta Data"); // TODO: I18N
            SectionPanel metaSectionPanel = new SectionPanel(this, metaNode, metaNode.getDisplayName(), HibernateMappingToolBarMVElement.META_DATA, false, false);

            // Add section for typedef elements
            TypedefElementsHelper typedefHelper = new TypedefElementsHelper(this, mapping.getTypedef());
            typedefContainerNode = typedefHelper.getTypedefsContainerNode();
            typedefCont = typedefHelper.getTypedefsContainer();
            
            // Node import
            Node importNode = new ElementLeafNode("Import"); // TODO: I18N
            SectionPanel importSectionPanel = new SectionPanel(this, importNode, importNode.getDisplayName(), HibernateMappingToolBarMVElement.IMPORT_ELEMENT, false, false);

            // Add the section for the class elements
            ClassElementsHelper clsHelper = new ClassElementsHelper(this,mapping.getMyClass());
            classesContainerNode = clsHelper.getClassesContainerNode();
            classesCont = clsHelper.getClassesContainer();

            // Add the section for the resultsets
            ResultsetElementsHelper resultsetsHelper = new ResultsetElementsHelper(this,mapping.getResultset());
            resultsetsContainerNode = resultsetsHelper.getResultsetsContainerNode();
            resultsetsCont = resultsetsHelper.getResultsetsContianer();
            

            // Node for query | sql-query
            Node queryNode = new ElementLeafNode("Query");

            // Node filter-def
            Node filterDefNode = new ElementLeafNode("FilterDef");

            // Node for database-object
            Node databaseObjectNode = new ElementLeafNode("Database Object");

            // Add top level nodes to the root
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{metaNode, typedefContainerNode, importNode, classesContainerNode,
                resultsetsContainerNode, queryNode, filterDefNode, databaseObjectNode
            });
            Node root = new AbstractNode(rootChildren);

            // Add sections for the nodes
            addSection(metaSectionPanel);
            addSection(typedefCont);
            addSection(importSectionPanel);
            addSection(classesCont);
            addSection(resultsetsCont);

            setRoot(root);
        }

        @Override
        public Error validateView() {
            // TODO: valiation code here
            return null;
        }
    }
    
    public static class ElementLeafNode extends org.openide.nodes.AbstractNode {

        ElementLeafNode(String displayName) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(displayName);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HibernateMappingDataObject.HELP_ID_DESIGN_HIBERNATE_MAPPING); //NOI18N
            return null;
        }
    }

    }
