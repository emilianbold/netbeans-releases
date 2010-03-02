/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.schema.completion;

import javax.swing.ImageIcon;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.ElementPaintComponent;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementResultItem extends CompletionResultItem {
    private int caretPosition = 0;
    private String replacingText;
    
    /**
     * Creates a new instance of ElementResultItem
     */
    public ElementResultItem(AbstractElement element, CompletionContext context) {
        this(element, context, null);
    }

    public ElementResultItem(AbstractElement element, CompletionContext context,
        TokenSequence tokenSequence) {
        super(element, context, tokenSequence);
        itemText = element.getName();
        icon = new ImageIcon(CompletionResultItem.class.getResource(
            ICON_LOCATION + ICON_ELEMENT));
    }
    
    /**
     * Creates a new instance of ElementResultItem
     */
    public ElementResultItem(AbstractElement element, String prefix, CompletionContext context) {
        super(element, context);        
        itemText = prefix + ":" + element.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ELEMENT));
    }

    @Override
    public String getDisplayText() {
        AbstractElement element = (AbstractElement)axiComponent;
        String cardinality = null;
        if(axiComponent.supportsCardinality() &&
           element.getMinOccurs() != null &&
           element.getMaxOccurs() != null) {
            cardinality = "["+element.getMinOccurs()+".."+element.getMaxOccurs()+"]";
        }
        String displayText = itemText;
        if(cardinality != null)
            displayText = displayText + " " + cardinality;
        
        return displayText;
    }
    
    /**
     * Overwrites getReplacementText of base class.
     * Add mandatory attributes. See issue: 108720
     */
    @Override
    public String getReplacementText() {
        replacingText = null;

        AbstractElement element = (AbstractElement)axiComponent;
        StringBuffer buffer = new StringBuffer();
        boolean firstAttr = false;
        for (AbstractAttribute aa : element.getAttributes()) {
            if (aa instanceof AnyAttribute) continue;
            
            Attribute a = (Attribute)aa;
            if (a.getUse() == Use.REQUIRED) {
                if (buffer.length() == 0)
                    firstAttr = true;
                buffer.append(" " + a.getName() +
                    AttributeResultItem.ATTRIBUTE_EQUALS_AND_VALUE_STRING);
                if (firstAttr) {
                    caretPosition = buffer.length() - 1;
                    firstAttr = false;
                }                
            }
        }
        replacingText = 
            (CompletionUtil.TAG_FIRST_CHAR +
            itemText + buffer.toString() +
            CompletionUtil.TAG_LAST_CHAR);
        return replacingText;
    }
        
    @Override
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new ElementPaintComponent(this);
        }
        return component;
    }

    
    /**
     * For elements, the caret should go inside the double quotes of
     * the first mandatory attribute.
     */
    @Override
    public int getCaretPosition() {
        return ((replacingText == null ? 0 : replacingText.length()) + caretPosition);
    }    
}
