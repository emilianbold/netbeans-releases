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

import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TChangeVariables;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TInit;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.worklist.editor.mapper.AbstractWlmModelUpdater;
import org.netbeans.modules.worklist.editor.mapper.MapperTcContext;
import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;

/**
 * Looks on the current state of the BPEL Mapper and modifies 
 * the BPEL model correspondingly.  
 * 
 * @author nk160297
 */
public class WlmModelUpdater extends AbstractWlmModelUpdater {

    public WlmModelUpdater(MapperTcContext mapperTcContext) {
        super(mapperTcContext);
    }

    /**
     * Implements Callable interface
     * @return
     * @throws java.lang.Exception
     */
    public Object updateOnChanges(TreePath treePath) throws Exception {
        //
        // TODO m
        WLMComponent wlmComponent = getDesignContext().getContextEntity();
        //
        Class<? extends WLMComponent> elementType = wlmComponent.getElementType();
        if (elementType == WLMComponent.class) {
            TTask task = wlmComponent.getModel().getTask();
            if (task != null) {
                updateTask(treePath, task);
            }
        } else if (elementType == TAction.class) {
            TAction action = (TAction)wlmComponent;
            if (action != null) {
                updateAction(treePath, action);
            }
        }
        //
        return null; // TODO: return some result flag
    }
    
    //==========================================================================

    protected void updateFrom(Graph graph, MapperLsmTree lsmTree,
            TFrom from, TreePath rightTreePath) throws Exception
    {
        assert from != null;
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        //
        if (graphInfo.onlyOneTransitLink()) {
            // Only one link from the left to the right tree
            // 
            // Take the first link, which is the only.
            Link link = graphInfo.getTransitLinks().get(0); 
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, lsmTree);
            //
            
            XPathModel xPathModel = WlmXPathModelFactory.create(from);
            populateFrom(from, xPathModel, tpInfo);
        } else {
            boolean processed = false;
            // 
            if (!processed) {
                populateContentHolder(from, graphInfo, lsmTree);
            }
        }
    }

    private void updateCopy(TreePath rightTreePath, TCopy copy) throws Exception {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        //=====================================================================
        //
        // Remove copy if there is not any content in the graph 
        //
        if (graph.isEmpty()) {
            // Remove copy from the BPEL model
            WLMComponent copyOwner = copy.getParent();
            if (copyOwner != null) {
                if (copyOwner instanceof TChangeVariables) {
                    ((TChangeVariables) copyOwner).removeCopy(copy);
                }
            }
            getMapperModel().deleteGraph(rightTreePath); // Remove empty graph !!!
            return; // NOTHING TO DO FURTHER
        }
        //
        //
        //=====================================================================
        // Populate FROM
        //
        // Recreate the From as a whole
        TFrom from = copy.getFrom();
        if (from != null) {
            copy.removeFrom(from);
        }
        WLMModel wlmModel = copy.getModel();
        from = wlmModel.getFactory().createFrom(wlmModel);
        copy.setFrom(from);


//        if (from == null) {
//            BpelModel bpelModel = copy.getBpelModel();
//            from = bpelModel.getBuilder().createFrom();
//            copy.setFrom(from);
//        }
        //
//        LsmProcessor.deleteAllLsm(from);
        //
        MapperLsmTree lsmTree = new MapperLsmTree();
        updateFrom(graph, lsmTree, from, rightTreePath);
        //
//        getMapperLsmProcessor().registerAll(from, lsmTree, true);
        //
        //=====================================================================
        // Populate TO
        //
        // Recreate the To as a whole
        TTo to = copy.getTo();
        if (to != null) {
            copy.removeTo(to);
        }
        wlmModel = copy.getModel();
        to = wlmModel.getFactory().createTo(wlmModel);
        copy.setTo(to);


//        if (to == null) {
//            bpelModel = copy.getBpelModel();
//            to = bpelModel.getBuilder().createTo();
//            copy.setTo(to);
//        }
//        //
//        LsmProcessor.deleteAllLsm(to);
        //
        lsmTree = new MapperLsmTree();
        TreePathInfo tpInfo = collectTreeInfo(rightTreePath, lsmTree);
        //
        XPathModel xPathModel = WlmXPathModelFactory.create(to);
        populateTo(to, xPathModel, tpInfo);
//        getMapperLsmProcessor().registerAll(to, lsmTree, false);
    }
    
    private void updateTask(TreePath rightTreePath, TTask task) throws Exception {
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        Object dataObject = graph.getDataObject();
        if (dataObject instanceof TCopy) {
            // Process a copy
            updateCopy(rightTreePath, (TCopy)dataObject);
        } else if (dataObject == null) {
            // absence of data object means that it is necessary to create a new Copy
            WLMModel wlmModel = task.getModel();
            TInit init = task.getInit();
            if (init == null) {
                init = wlmModel.getFactory().createInit(wlmModel);
                task.setInit(init);
            }
            //
            VariableInit varInit = init.getVariableInit();
            if (varInit == null) {
                varInit = wlmModel.getFactory().createVariableInit(wlmModel);
                init.setVariableInit(varInit);
            }
            //
            TCopy newCopy = wlmModel.getFactory().createCopy(wlmModel);
            varInit.addCopy(newCopy);
            //
            updateCopy(rightTreePath, newCopy);
            //
            graph.setDataObject(newCopy);
        }
    }
    
    private void updateAction(TreePath rightTreePath, TAction action) throws Exception {
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        Object dataObject = graph.getDataObject();
        if (dataObject instanceof TCopy) {
            // Process a copy
            updateCopy(rightTreePath, (TCopy)dataObject);
        } else if (dataObject == null) {
            // absence of data object means that it is necessary to create a new Copy
            WLMModel wlmModel = action.getModel();
            TChangeVariables chVar = action.getChangeVariables();
            if (chVar == null) {
                chVar = wlmModel.getFactory().createChangeVariables(wlmModel);
                action.setChangeVariables(chVar);
            }
            //
            TCopy newCopy = wlmModel.getFactory().createCopy(wlmModel);
            chVar.addCopy(newCopy);
            //
            updateCopy(rightTreePath, newCopy);
            //
            graph.setDataObject(newCopy);
        }
    }

    //==========================================================================

    private TFrom populateFrom(TFrom from, XPathModel xPathModel, TreePathInfo tpInfo) {
        XPathExpression xPathExpr = createVariableXPath(xPathModel, tpInfo);
        if (xPathExpr != null) {
            from.setContent(xPathExpr.getExpressionString());
        }
        //
        return from;
    }

    private TTo populateTo(TTo to, XPathModel xPathModel, TreePathInfo tpInfo) {
        XPathExpression xPathExpr = createVariableXPath(xPathModel, tpInfo);
        to.setContent(xPathExpr.getExpressionString());
        return to;
    }

    //==========================================================================

   private static final String XSI_PREFIX = "xsi"; // NOI18N
   private static final String XSI_SLASH = "/@";  // NOI18N
}
    
