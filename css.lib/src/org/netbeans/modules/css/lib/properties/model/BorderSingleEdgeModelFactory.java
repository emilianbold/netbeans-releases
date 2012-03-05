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
package org.netbeans.modules.css.lib.properties.model;

import java.util.*;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.model.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = CustomModelFactory.class)
public class BorderSingleEdgeModelFactory implements CustomModelFactory {

    private static final String BORDER_PREFIX = "border-"; //NOI18N
    
    private Map<String, Edge> EDGE_NAMES = new HashMap<String, Edge>();

    public BorderSingleEdgeModelFactory() {
        for (Edge e : Edge.values()) {
            EDGE_NAMES.put(e.name().toLowerCase(), e);
        }
    }

    @Override
    public NodeModel createModel(Node node) {
        String nodeName = node.name().toLowerCase(Locale.ENGLISH);

        if (nodeName.startsWith(BORDER_PREFIX)) {
            int secondDashIndex = nodeName.lastIndexOf('-'); //e.g. in border-top"-"color
            if (secondDashIndex == -1) {
                return null;
            }

            String edgeName = nodeName.substring(BORDER_PREFIX.length(), secondDashIndex);
            Edge edge = EDGE_NAMES.get(edgeName);
            if (edge == null) {
                return null;
            }

            String typeName = nodeName.substring(secondDashIndex + 1);
            if("color".equals(typeName)) {
                return new BorderSingleEdgeColor(edge, node);
            } else if("style".equals(typeName)) {
                
            } else if("width".equals(typeName)) {
                
            }



        }

        return null;
    }
}
