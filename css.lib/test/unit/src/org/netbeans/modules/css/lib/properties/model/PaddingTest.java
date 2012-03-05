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
public class PaddingTest extends CssTestBase {

    public PaddingTest(String name) {
        super(name);
    }

    public void testBasic() {
//        PropertyModel model = Properties.getPropertyModel("border");
//        PropertyValue val = new PropertyValue(model, "1px solid gray");
        PropertyModel model = Properties.getPropertyModel("padding");
        ResolvedProperty val = new ResolvedProperty(model, "1px 20% 3px");

//        Node root = val.getParseTree();
//        dumpTree(root);

//        System.out.println("-------------------");

        PropertyModel model2 = Properties.getPropertyModel("padding-left");
        ResolvedProperty val2 = new ResolvedProperty(model2, "1px");

        Node root2 = val2.getParseTree();
//        dumpTree(root2);

        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.PADDING);

        root2.accept(modelvisitor);


    }

    public void testCreatePaddingModel() {
        PropertyModel model = Properties.getPropertyModel("padding");
        ResolvedProperty val = new ResolvedProperty(model, "1px 20% auto");

        Node root = val.getParseTree();
        dumpTree(root);

        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.PADDING);
        root.accept(modelvisitor);

        Padding padding = (Padding)modelvisitor.getModel();
        assertNotNull(padding);

        {
            PaddingT mt = padding.getPaddingT();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Length len = mw.getLength();
            assertNotNull(len);

            String sval = len.getLength().getValue().toString();

            assertEquals("1px", sval);
        }
        {
            PaddingLr mt = padding.getPaddingLr();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Text value = mw.getPercentage();
            String sval = value.getValue().toString();

            assertEquals("20%", sval);
        }
        {
            PaddingB mt = padding.getPaddingB();
            assertNotNull(mt);

            BoxEdgeSize mw = mt.getBoxEdgeSize();

            assertNotNull(mw);

            Text value = mw.getAuto();
            String sval = value.getValue().toString();

            assertEquals("auto", sval);
            
        }

        dumpBox(padding);
     
    }
    
    public void testPaddingLeft() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("padding-left", "2px");
        assertBox(mbox1, null, null, null, "2px");
    }
    public void testPaddingRight() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("padding-right", "2px");
        assertBox(mbox1, null, "2px", null, null);
    }
    public void testPaddingTop() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("padding-top", "2px");
        assertBox(mbox1, "2px", null, null, null);
    }
    public void testPaddingBottom() {
        Box<BoxEdgeSize> mbox1 = getBoxModel("padding-bottom", "2px");
        assertBox(mbox1, null, null, "2px", null);
    }
    
    public void testPaddingBox() {
        dumpPadding("20px");
        dumpPadding("30% 50%");
        dumpPadding("30% auto 20%");
        dumpPadding("1px 2px 3px 4px");
    }
    
    public void testCascade() {
        
        Box<BoxEdgeSize> mbox1 = getBoxModel("padding", "2px 3px 4px 5px");
//        dumpBox(mbox1);
        
        Box<BoxEdgeSize> mbox2 = getBoxModel("padding-left", "2px");
//        dumpBox(mbox2);
        
        Box<BoxEdgeSize> mbox3 = getBoxModel("padding-right", "1px");
//        dumpBox(mbox3);
        
        CascadedBox<BoxEdgeSize> cbox = new CascadedBox<BoxEdgeSize> ();
        cbox.addBox(mbox1);
        cbox.addBox(mbox2);
        cbox.addBox(mbox3);
        
//        dumpBox(cbox);
        
        assertBox(cbox, "2px", "1px", "4px", "2px");
        
        
    }
    
    /*
     * 
     * a. padding: 2px 3px; //reset - set TBLR
     * 
     * //aT2 aB2 aL3 aR3
     * 
     * b. padding-left: 1px; //       set L
     * 
     * //aT2 aB2 bL1 aR3
     * 
     * c. padding-top: 4px;  //       set T
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
        
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.PADDING);
        val.getParseTree().accept(modelvisitor);

        Box<BoxEdgeSize> mbox = (Box<BoxEdgeSize>)modelvisitor.getModel();
        
        return mbox;
    }
    
    public void dumpPadding(String value) {
        PropertyModel model = Properties.getPropertyModel("padding");
        ResolvedProperty val = new ResolvedProperty(model, value);

        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.PADDING);
        val.getParseTree().accept(modelvisitor);

        Padding padding = (Padding)modelvisitor.getModel();
        assertNotNull(padding);
        
        System.out.println("padding: " + value);
        System.out.println("-------------------------------");
        dumpTree(val.getParseTree());
        System.out.println("");
        dumpBox(padding);
    }
    
    static void dumpBox(Box<BoxEdgeSize> box) {
        Utils.dumpBox(box);
    }
    
    static void assertBox(Box<BoxEdgeSize> box, String top, String right, String bottom, String left) {
        BoxEdgeSize e = box.getEdge(Edge.TOP);
        assertEquals(top, e == null ? null : e.getTextRepresentation());

        e = box.getEdge(Edge.RIGHT);
        assertEquals(right, e == null ? null : e.getTextRepresentation());
        
        e = box.getEdge(Edge.BOTTOM);
        assertEquals(bottom, e == null ? null : e.getTextRepresentation());
        
        e = box.getEdge(Edge.LEFT);
        assertEquals(left, e == null ? null : e.getTextRepresentation());
        
    }

    
}
