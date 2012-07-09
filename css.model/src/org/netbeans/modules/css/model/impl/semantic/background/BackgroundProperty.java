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
import java.util.Stack;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.NodeVisitor;
import org.netbeans.modules.css.model.api.semantic.Attachment;
import org.netbeans.modules.css.model.api.semantic.Box;
import org.netbeans.modules.css.model.api.semantic.background.Background;
import org.netbeans.modules.css.model.impl.semantic.ColorI;
import org.netbeans.modules.css.model.impl.semantic.Element;
import org.netbeans.modules.css.model.impl.semantic.ImageI;

/**
 *
 * @author marekfukala
 */
public class BackgroundProperty {
    
    private Stack<Background> MODELS = new Stack<Background>();
    
    public BackgroundProperty(Node node) {
        node.accept(new NodeVisitor.Adapter() {
            @Override
            public boolean visit(Node node) {
                switch(Element.forNode(node)) {
                    case bg_layer:
                    case final_bg_layer:
                        MODELS.push(new BackgroundI());
                        break;
                    case image:
                        MODELS.peek().setImage(new ImageI(node));
                        break;
                    case bg_pos:
                        MODELS.peek().setPosition(new BgPos(node));
                        break;
                    case background_color:
                        MODELS.peek().setColor(new ColorI(node));
                        break;
                    case repeat_style:
                        MODELS.peek().setRepeatStyle(new RepeatStyleI(node));
                        break;
                    case attachment:
                        MODELS.peek().setAttachment(new AttachmentI(node).getValue());
                        break;
                    case bg_box:
                        //If one <box> value is present then it sets both ‘background-origin’ 
                        //and ‘background-clip’ to that value. If two values are present, 
                        //then the first sets ‘background-origin’ and the second ‘background-clip’. 
                        Box box = new BgBox(node).getFirstBox();
                        Box box2 = new BgBox(node).getSecondBox();
                        
                        assert box != null;
                        //box2 is arbitrary
                        Background bg = MODELS.peek();
                        
                        if(box2 == null) {
                            bg.setClip(box);
                            bg.setOrigin(box);
                        } else {
                            bg.setOrigin(box);
                            bg.setClip(box2);
                        }
                        
                        break;
                    case bg_size:
                        MODELS.peek().setSize(new BgSizeI(node).getSize());
                        break;
                }
                
                return true;
            }
            
        });
        
    }
    
    public Collection<Background> getBackgrounds() {
        return MODELS;
    }
    
}
