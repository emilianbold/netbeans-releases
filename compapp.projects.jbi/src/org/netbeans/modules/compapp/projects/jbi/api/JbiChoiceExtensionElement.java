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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.ArrayList;
import java.util.List;

/**
 * JBI extension element for choices.
 * <p>
 * In the following example, "on-failure" is the choice extension element.
 * <pre>
 * &lt;folder name="on-failure">
 *      &lt;attr name="choice" stringvalue="true"/>
 *      &lt;folder name="error-endpoint"> ... &lt;/folder>
 *      &lt;folder name="delete"/>
 *      &lt;folder name="suspend"/>
 *  &lt;/folder>
 * </pre>
 * 
 * @author jqian
 */
public class JbiChoiceExtensionElement extends JbiExtensionElement {
    
    private String defaultChoice;
    
    public JbiChoiceExtensionElement(String name, 
            String displayName,
            List<JbiExtensionElement> subElements,
            List<JbiExtensionAttribute> attributes,
            String description,
            String defaultChoice) {
        super(name, displayName, subElements, attributes, description);
        
        this.defaultChoice = defaultChoice;
    }
    
    public List<String> getChoices() {
        List<String> ret = new ArrayList<String>();
        
        for (JbiExtensionElement child : getElements()) {
            ret.add(child.getName());
        }
        
        return ret;
    }
    
    public String getDefaultChoice() {
        return defaultChoice;
    }
}
