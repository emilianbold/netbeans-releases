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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.editors.api.utils;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 *
 * @author Vitaly
 * @version 1.0
 */
public interface HtmlNameManager {
    boolean accept(NodeType nodeType, Object reference);
    String getHtmlName(NodeType nodeType, Object reference);
    
    final HtmlNameManager[] HTML_NAME_MANAGERS
            = new HtmlNameManager[] {new ShortHtmlNameManager()};
    
    class ShortHtmlNameManager implements HtmlNameManager {
        public boolean accept(NodeType nodeType, Object reference) {
            return reference instanceof Component;
        }
        
        public String getHtmlName(NodeType nodeType, Object reference) {
            if (!accept(nodeType, reference)) {
                return null;
            }
            
            String refName = null;
            if (reference instanceof Named) {
                refName = ((Named)reference).getName();
            }
            
            if (refName == null
                    && nodeType != null
                    && ! NodeType.UNKNOWN_TYPE.equals(nodeType)) {
                refName = nodeType.getDisplayName();
            }
            
            if (refName == null && reference instanceof DocumentComponent) {
                refName = org.netbeans.modules.bpel.editors.api.utils.Util.getTagName((DocumentComponent)reference);
            }
            
            refName = refName == null ? "" : refName;
            
            return org.netbeans.modules.bpel.editors.api.utils.
                                Util.getCorrectedHtmlRenderedString(refName);
        }
    }
    
    class Util {
        private Util() {
        }
    }
    
}
