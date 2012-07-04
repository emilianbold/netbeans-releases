/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic.background;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.NodeUtil;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyModel;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.model.api.semantic.Color;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.Image;
import org.netbeans.modules.css.model.api.semantic.ModelFactory;
import org.netbeans.modules.css.model.api.semantic.Percentage;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundPosition;
import org.netbeans.modules.css.model.api.semantic.background.Background;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundModel;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundPosition.Position;

/**
 *
 * @author marekfukala
 */
public class BackgroundModelImplTest extends CssTestBase {

    
    public BackgroundModelImplTest(String testName) {
        super(testName);
    }

    public void testBackground_Color() {
        PropertyModel background = Properties.getPropertyModel("background-color");
        assertNotNull(background);
        
        ResolvedProperty resolved = ResolvedProperty.resolve(background, "red");
        
//        NodeUtil.dumpTree(resolved.getParseTree());
        
        BackgroundModel model = ModelFactory.getModel(BackgroundModel.class, resolved);
        assertNotNull(model);
        
        Collection<Background> backgrounds = model.getBackgrounds();
        assertNotNull(backgrounds);
        
        assertEquals(1, backgrounds.size());
        
        Background b = backgrounds.iterator().next();
        assertNotNull(b);
        
        Color c = b.getColor();
        assertNotNull(c);

        assertEquals("red", c.getValue());
        
    }
    
    public void testBackgroundImageInBackgroundProperty() {
        PropertyModel background = Properties.getPropertyModel("background");
        assertNotNull(background);
        
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarParseTreeBuilder.DEBUG = true;
        
        ResolvedProperty resolved = ResolvedProperty.resolve(background, "url(flower.png), url(ball.png)");
        
//        NodeUtil.dumpTree(resolved.getParseTree());
        
        BackgroundModel model = ModelFactory.getModel(BackgroundModel.class, resolved);
        assertNotNull(model);
        
        Collection<Background> backgrounds = model.getBackgrounds();
        assertNotNull(backgrounds);
        
        /*
          XXX BUG! the parse tree is broken in this case 
          (it contains both bg-layer and final-bg-layer for the second url):
        
        [S0|background]
            [C3|@bg-layer]
                [S4|@bg-image]
                    [S5|@image]
                        [L6|@uri]
                            url(flower.png)(URI;0-15)
            ,(COMMA;15-16)
            [C3|@bg-layer]
                [S4|@bg-image]
                    [S5|@image]
                        [L6|@uri]
                            url(ball.png)(URI;17-30)
            [C374|@final-bg-layer]
                [S375|@bg-image]
                    [S376|@image]
                        [L377|@uri]
                            url(ball.png)(URI;17-30)
         */
//        assertEquals(2, backgrounds.size()); 
        
        Iterator<Background> bgitr = backgrounds.iterator();
        Background b1 = bgitr.next();
        assertNotNull(b1);
        assertNotNull(b1.getImage());
        assertEquals("url(flower.png)", b1.getImage().getURI());
        
        Background b2 = bgitr.next();
        assertNotNull(b2);
        assertNotNull(b2.getImage());
        assertEquals("url(ball.png)", b2.getImage().getURI());
        
    }
    
    public void testBackgroundImageInBackgroundImageProperty() {
        PropertyModel background = Properties.getPropertyModel("background-image");
        assertNotNull(background);
        
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarParseTreeBuilder.DEBUG = true;
        
        ResolvedProperty resolved = ResolvedProperty.resolve(background, "url(flower.png), url(ball.png)");
        
//        NodeUtil.dumpTree(resolved.getParseTree());
        
        BackgroundModel model = ModelFactory.getModel(BackgroundModel.class, resolved);
        assertNotNull(model);
        
        Collection<Background> backgrounds = model.getBackgrounds();
        assertNotNull(backgrounds);
        
        /*
          XXX BUG! the parse tree is broken in this case 
          (it contains both bg-layer and final-bg-layer for the second url):
        
        [S0|background]
            [C3|@bg-layer]
                [S4|@bg-image]
                    [S5|@image]
                        [L6|@uri]
                            url(flower.png)(URI;0-15)
            ,(COMMA;15-16)
            [C3|@bg-layer]
                [S4|@bg-image]
                    [S5|@image]
                        [L6|@uri]
                            url(ball.png)(URI;17-30)
            [C374|@final-bg-layer]
                [S375|@bg-image]
                    [S376|@image]
                        [L377|@uri]
                            url(ball.png)(URI;17-30)
         */
//        assertEquals(2, backgrounds.size()); 
        
        Iterator<Background> bgitr = backgrounds.iterator();
        Background b1 = bgitr.next();
        assertNotNull(b1);
        assertNotNull(b1.getImage());
        assertEquals("url(flower.png)", b1.getImage().getURI());
        
        Background b2 = bgitr.next();
        assertNotNull(b2);
        assertNotNull(b2.getImage());
        assertEquals("url(ball.png)", b2.getImage().getURI());
        
    }
    
    public void testBackgroundPositionInBackgroundPositionProperty() {
        
        PropertyModel background = Properties.getPropertyModel("background-position");
        assertNotNull(background);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20px"), 
                Edge.LEFT, false, null, "20px", Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20%"), 
                Edge.LEFT, false, "20%", null, Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "left"), 
                Edge.LEFT, false, null, null, Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "bottom"), 
                Edge.LEFT, true, null, null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "top"), 
                Edge.LEFT, true, null, null, Edge.TOP, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right bottom"), 
                Edge.RIGHT, false, null, null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20% bottom"), 
                Edge.LEFT, false, "20%", null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 100px"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, null, "100px");
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20% 100px"), 
                Edge.LEFT, false, "20%", null, Edge.TOP, false, null, "100px");
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 20% top 10%"), 
                Edge.RIGHT, false, "20%", null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "left 20% bottom 10%"), 
                Edge.LEFT, false, "20%", null, Edge.BOTTOM, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right top 10%"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 20px center"), 
                Edge.RIGHT, false, null, "20px", Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right top 10%"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "center top 20px"), 
                Edge.LEFT, true, null, null, Edge.TOP, false, null, "20px");
        
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarParseTreeBuilder.DEBUG = true;
//        Node tree = resolved.getParseTree();
//        NodeUtil.dumpTree(tree);
//        
//        BackgroundModel model = ModelFactory.getModel(BackgroundModel.class, resolved);
//        assertNotNull(model);
//        
//        Collection<Background> backgrounds = model.getBackgrounds();
//        for(Background b : backgrounds) {
//            BackgroundPosition position = b.getPosition();
//            assertNotNull(position);
//            
//            System.out.println("h:" + position.getHorizontalPosition());
//            System.out.println("v:" + position.getVerticalPosition());
//            
//            
//        }
        
    }
    
    
    public void testBackgroundPositionInBackgroundProperty() {
        
        PropertyModel background = Properties.getPropertyModel("background");
        assertNotNull(background);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20px"), 
                Edge.LEFT, false, null, "20px", Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20%"), 
                Edge.LEFT, false, "20%", null, Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "left"), 
                Edge.LEFT, false, null, null, Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "bottom"), 
                Edge.LEFT, true, null, null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "top"), 
                Edge.LEFT, true, null, null, Edge.TOP, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right bottom"), 
                Edge.RIGHT, false, null, null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20% bottom"), 
                Edge.LEFT, false, "20%", null, Edge.BOTTOM, false, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 100px"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, null, "100px");
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "20% 100px"), 
                Edge.LEFT, false, "20%", null, Edge.TOP, false, null, "100px");
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 20% top 10%"), 
                Edge.RIGHT, false, "20%", null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "left 20% bottom 10%"), 
                Edge.LEFT, false, "20%", null, Edge.BOTTOM, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right top 10%"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right 20px center"), 
                Edge.RIGHT, false, null, "20px", Edge.TOP, true, null, null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "right top 10%"), 
                Edge.RIGHT, false, null, null, Edge.TOP, false, "10%", null);
        
        assertBackgroundPosition(ResolvedProperty.resolve(background, "center top 20px"), 
                Edge.LEFT, true, null, null, Edge.TOP, false, null, "20px");
    }
    
    /**
     * Asserts one background position
     */
    private void assertBackgroundPosition(ResolvedProperty resolvedProperty, 
            Edge h, boolean hcenter, String hpercentage, String hlength,
            Edge v, boolean vcenter, String vpercentage, String vlength) {
        
        BackgroundModel model = ModelFactory.getModel(BackgroundModel.class, resolvedProperty);
        assertNotNull(model);
        
        Collection<Background> backgrounds = model.getBackgrounds();
        assertNotNull(backgrounds);
        
        assertEquals(1, backgrounds.size());
        
        Background bg = backgrounds.iterator().next();
        assertNotNull(bg);
        
        BackgroundPosition pos = bg.getPosition();
        assertNotNull(pos);
        
        Position hpos = pos.getHorizontalPosition();
        assertNotNull(hpos);
        
        assertEquals(h, hpos.getRelativeTo());
        assertEquals(hcenter, hpos.isCenter());
        assertToString(hpos.getPercentage(), hpercentage);
        assertToString(hpos.getLength(), hlength);
        
        Position vpos = pos.getVerticalPosition();
        assertNotNull(vpos);
        
        assertEquals(v, vpos.getRelativeTo());
        assertEquals(vcenter, vpos.isCenter());
        assertToString(vpos.getPercentage(), vpercentage);
        assertToString(vpos.getLength(), vlength);
        
    }
    
    private void assertToString(Object object, String expectedToString) {
        if(object == null) {
            assertNull(expectedToString);
        } else if(expectedToString == null) {
            assertNull(object);
        } else {
            String asText = object.toString();
            assertEquals(expectedToString, asText);
        }
    }
    
}
