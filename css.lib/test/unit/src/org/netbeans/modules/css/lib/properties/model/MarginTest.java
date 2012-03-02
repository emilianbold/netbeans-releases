/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.properties.model;

import org.netbeans.modules.css.lib.api.properties.model.NodeModel;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.*;
import org.netbeans.modules.css.lib.api.properties.model.*;


/**
 *
 * @author marekfukala
 */
public class MarginTest extends CssTestBase {

    public MarginTest(String name) {
        super(name);
    }

    public void testBasic() {
//        PropertyModel model = Properties.getPropertyModel("border");
//        PropertyValue val = new PropertyValue(model, "1px solid gray");
        PropertyModel model = Properties.getPropertyModel("margin");
        ResolvedProperty val = new ResolvedProperty(model, "1px 20% 3px");

        Node root = val.getParseTree();
        dumpTree(root);

        System.out.println("-------------------");

        PropertyModel model2 = Properties.getPropertyModel("margin-left");
        ResolvedProperty val2 = new ResolvedProperty(model2, "1px");

        Node root2 = val2.getParseTree();
        dumpTree(root2);

        NodeVisitor visitor = new NodeVisitor() {

            @Override
            public void visit(Node node) {
                System.out.println("visiting " + node.name());
            }

            @Override
            public void unvisit(Node node) {
            }
        };

        System.out.println("------------");
        root2.accept(visitor);
        System.out.println("------------");

        ModelBuilderNodeVisitor<NodeModel> modelvisitor = new ModelBuilderNodeVisitor<NodeModel>(PropertyModelId.MARGIN);

        root2.accept(modelvisitor);


    }

    public void testCreateMarginModel() {
        PropertyModel model = Properties.getPropertyModel("margin");
        ResolvedProperty val = new ResolvedProperty(model, "1px 20% auto");

        Node root = val.getParseTree();
        dumpTree(root);

        ModelBuilderNodeVisitor<NodeModel> modelvisitor = new ModelBuilderNodeVisitor<NodeModel>(PropertyModelId.MARGIN);
        root.accept(modelvisitor);

        Margin margin = (Margin)modelvisitor.getModel();
        assertNotNull(margin);

        {
            MarginT mt = margin.getMarginT();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Length len = mw.getLength();
            assertNotNull(len);

            String sval = len.getLength().getValue().toString();

            assertEquals("1px", sval);
        }
        {
            MarginLr mt = margin.getMarginLr();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Text value = mw.getPercentage();
            String sval = value.getValue().toString();

            assertEquals("20%", sval);
        }
        {
            MarginB mt = margin.getMarginB();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Text value = mw.getAuto();
            String sval = value.getValue().toString();

            assertEquals("auto", sval);
            
        }

        PaddingTest.dumpBox(margin);
     
    }
    
    public void testMarginLeft() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("margin-left", "2px");
        PaddingTest.assertBox(mbox1, null, null, null, "2px");
    }
    public void testMarginRight() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("margin-right", "2px");
        PaddingTest.assertBox(mbox1, null, "2px", null, null);
    }
    public void testMarginTop() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("margin-top", "2px");
        PaddingTest.assertBox(mbox1, "2px", null, null, null);
    }
    public void testMarginBottom() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("margin-bottom", "2px");
        PaddingTest.assertBox(mbox1, null, null, "2px", null);
    }
    
    public void testMarginBox() {
        dumpMargin("20px");
        dumpMargin("30% 50%");
        dumpMargin("30% auto 20%");
        dumpMargin("1px 2px 3px 4px");
    }
    
    public void testCascade() {
        
        Box<BoxEdgeSize> mbox1 = getBoxModel("margin", "2px 3px 4px 5px");
//        PaddingTest.dumpBox(mbox1);
        
        Box<BoxEdgeSize> mbox2 = getBoxModel("margin-left", "2px");
//        PaddingTest.dumpBox(mbox2);
        
        Box<BoxEdgeSize> mbox3 = getBoxModel("margin-right", "1px");
//        PaddingTest.dumpBox(mbox3);
        
        CascadedBox<BoxEdgeSize> cbox = new CascadedBox<BoxEdgeSize> ();
        cbox.addBox(mbox1);
        cbox.addBox(mbox2);
        cbox.addBox(mbox3);
        
//        PaddingTest.dumpBox(cbox);
        
        PaddingTest.assertBox(cbox, "2px", "1px", "4px", "2px");
        
        
    }
    
    /*
     * 
     * a. margin: 2px 3px; //reset - set TBLR
     * 
     * //aT2 aB2 aL3 aR3
     * 
     * b. margin-left: 1px; //       set L
     * 
     * //aT2 aB2 bL1 aR3
     * 
     * c. margin-top: 4px;  //       set T
     * 
     * //cT4 aB2 bL1 aR3
     * 
     * 
     * 
     */
    
    public Box<BoxEdgeSize> getBoxModel(String propertyName, String text) {
        PropertyModel model = Properties.getPropertyModel(propertyName);
        ResolvedProperty val = new ResolvedProperty(model, text);

        dumpTree(val.getParseTree());
        
        ModelBuilderNodeVisitor<NodeModel> modelvisitor = new ModelBuilderNodeVisitor<NodeModel>(PropertyModelId.MARGIN);
        val.getParseTree().accept(modelvisitor);

        Box<BoxEdgeSize> mbox = (Box<BoxEdgeSize>)modelvisitor.getModel();
        
        return mbox;
    }
    
    public void dumpMargin(String value) {
        PropertyModel model = Properties.getPropertyModel("margin");
        ResolvedProperty val = new ResolvedProperty(model, value);

        ModelBuilderNodeVisitor<NodeModel> modelvisitor = new ModelBuilderNodeVisitor<NodeModel>(PropertyModelId.MARGIN);
        val.getParseTree().accept(modelvisitor);

        Margin margin = (Margin)modelvisitor.getModel();
        assertNotNull(margin);
        
        System.out.println("margin: " + value);
        System.out.println("-------------------------------");
        dumpTree(val.getParseTree());
        System.out.println("");
        PaddingTest.dumpBox(margin);
    }
    
    
}
