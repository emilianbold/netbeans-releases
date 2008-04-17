/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.editors.api;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
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
                refName = EditorUtil.getTagName((DocumentComponent)reference);
            }
            
            refName = refName == null ? "" : refName;
            
            return EditorUtil.getCorrectedHtmlRenderedString(refName);
        }
    }
}
