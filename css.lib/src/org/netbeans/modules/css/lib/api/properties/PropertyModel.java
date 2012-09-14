/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.lib.api.properties;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.css.lib.properties.GrammarParser;

/**
 * This class is almost the same as {@link PropertyDefinition} except merging 
 * duplicit PropertyDefinition-s into one SET group (which should be probably fixed
 * in the providers).
 *
 * @author mfukala@netbeans.org
 */
public class PropertyModel {
    
    private GroupGrammarElement values;
    private Collection<PropertyDefinition> properties;
    private String grammar;
    private String propertyName;
    
    public PropertyModel(String propertyName, Collection<PropertyDefinition> properties) {
        assert !properties.isEmpty();
        this.propertyName = propertyName;
        this.properties = properties;
    }
    
    public Collection<PropertyDefinition> getProperties() {
        return properties;
    }
    
    public PropertyDefinition getProperty() {
        return properties.iterator().next();
    }
        
    /**
     * Returns the root element of the property grammar.
     * 
     * @return a non null value.
     */
    public synchronized GroupGrammarElement getGrammarElement() {
        if(values == null) {
            values = GrammarParser.parse(getGrammar(), propertyName);
        } 
        return values;
    }    
    
    public synchronized String getGrammar() {
        if(grammar == null) {
            if(properties.size() == 1) {
                return getProperty().getValueGrammar(); //nothing to merge
            }
            StringBuilder sb = new StringBuilder();
            //the resulting grammar is a set of all property grammars
            for(Iterator<PropertyDefinition> i = getProperties().iterator(); i.hasNext();) {
                PropertyDefinition p = i.next();
                sb.append(" [ ");
                sb.append(p.getValueGrammar());
                sb.append(" ] ");
                if(i.hasNext()) {
                    sb.append(" | "); //NOI18N
                }
            }
            grammar = sb.toString();
        }
        return grammar;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
}
