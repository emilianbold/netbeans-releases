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

import java.io.Serializable;
import java.util.List;

/**
 * JBI Extension element.
 *
 * @author jqian
 */
public class JbiExtensionElement implements Serializable {

    /**
     * Name of the element.
     */
    private String name;

    /**
     * Display name of the element.
     */
    private String displayName;
    
    /**
     * A list of child elements.
     */
    private List<JbiExtensionElement> elements;
    
    /**
     * A list of attributes.
     */
    private List<JbiExtensionAttribute> attributes;
    
    /**
     * Description of the element.
     */
    private String description;

    /**
     * 
     */
    public JbiExtensionElement(String name, 
            String displayName,
            List<JbiExtensionElement> subElements,
            List<JbiExtensionAttribute> attributes,
            String description) {
        this.name = name;
        this.displayName = displayName;
        this.elements = subElements;
        this.attributes = attributes;
        this.description = description;
    }
    
    /**
     * Gets the name of the element.
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * Gets the display name of the element.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the list of child elements.
     */
    public List<JbiExtensionElement> getElements() {
        return elements;        
    }
    
    /**
     * Gets the list of attributes.
     */
    public List<JbiExtensionAttribute> getAttributes() {
        return attributes;
    }
    
    /**
     * Gets the description of the element.
     */
    public String getDescription() {
        return description;
    }
      
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JbiExtensionElement:");
        
        String newLine = System.getProperty("line.separator");
        
        if (attributes != null) {
            for (JbiExtensionAttribute attr : attributes) {
                sb.append(newLine);
                sb.append("  ");
                sb.append(attr.toString());
            }
        } 
        
        if (elements != null) {
            for (JbiExtensionElement childElement : elements) {
                sb.append(newLine);
                sb.append("  ");
                sb.append(childElement.toString());
            }
        }        
        
        return sb.toString();
    }
}
