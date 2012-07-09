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
package org.netbeans.modules.css.model.impl.semantic;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.css.lib.api.properties.Node;

/**
 *
 * @author marekfukala
 */
public enum Element {
    
    //properties
    background("background"),
    background_position("background-position"),
    background_color("background-color"),
    background_image("background-image"),
    background_repeat("background-repeat"),
    background_attchment("background-attachment"),
    background_clip("background-clip"),
    background_origin("background-origin"),
    background_size("background-size"),
    
    color("color"),

    //inner (named) elements
    single("single"),
    pair("pair"),
    
    
    //elements
    bg_size("@bg-size"),
    bg_box("@bg-box"),
    attachment("@attachment"),
    repeat_style("@repeat-style"),
    bg_pos("@bg-pos"),
    
    bg_pos_1("@bg-pos-1"),
    
    bg_pos_2("@bg-pos-2"),
    bg_pos_2_horizontal("@bg-pos-2-horizontal"),
    bg_pos_2_vertical("@bg-pos-2-vertical"),
    
    bg_pos_34("@bg-pos-34"),
    bg_pos_34_1("@bg-pos-34-1"),
    bg_pos_34_2("@bg-pos-34-2"),
    bg_pos_34_left_right_pair("@bg-pos-34-left-right-pair"),
    bg_pos_34_top_botoom_pair("@bg-pos-34-top-bottom-pair"),
    
    bg_layer("@bg-layer"),
    final_bg_layer("@final-bg-layer"),
    bg_image("@bg-image"),
    
    //unit elements
    percentage("@percentage"),
    length("@length"),
    image("@image"),
    uri("@uri"),
    
    //element for those who aren't in the enum - temporary, to be removed.
    DEFAULT("default!!!");
    
    private String nodeName;
    private static final Map<String, Element> NAMES_MAP = new HashMap<String, Element>();

    static {
        for(Element e : values()) {
            NAMES_MAP.put(e.getNodeName(), e);
        }
    }
    
    private Element(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    public static Element forNode(Node node) {
        Element element = NAMES_MAP.get(node.name());
        if(element == null) {
            return DEFAULT;
//            throw new IllegalArgumentException(String.format("No Element enum member for node name '%s'", node.name()));
        }
        return element;
    }
    
    
}
