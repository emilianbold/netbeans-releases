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
package org.netbeans.modules.javafx2.editor.completion.impl;

import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;

/**
 *
 * @author sdedic
 */
final class PropertyElementItem extends AbstractCompletionItem {
    /**
     * type in a printable form
     */
    private String  propertyType;
    
    /**
     * true, if the type is primitive - different markup
     */
    private boolean primitive;
    
    /**
     * true, if the property is static
     */
    private boolean staticProperty;
    
    private boolean attribute;
    
    private boolean inherited;
    
    private static final String RESOURCE_PROPERTY = "org/netbeans/modules/javafx2/editor/resources/property.png"; // NOI18N
    private static final String RESOURCE_STATIC_PROPERTY = "org/netbeans/modules/javafx2/editor/resources/property-static.png"; // NOI18N
    
    private ImageIcon ICON_PROPERTY;
    private ImageIcon ICON_STATIC_PROPERTY;
    
    public PropertyElementItem(CompletionContext ctx, String text, boolean attribute) {
        super(ctx, text);
        this.attribute = attribute;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    
    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public void setStaticProperty(boolean staticProperty) {
        this.staticProperty = staticProperty;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @NbBundle.Messages({
        "# {0} - property name",
        "FMT_ownProperty=<b>{0}</b>"
    })
    @Override
    protected String getLeftHtmlText() {
        if (!inherited) {
            return FMT_ownProperty(super.getLeftHtmlText());
        } else {
            return super.getLeftHtmlText();
        }
    }
    
    @Override
    protected String getSubstituteText() {
        if (attribute) {
            return super.getSubstituteText() + "=\"\" ";
        } else {
            return "<" + super.getSubstituteText() + "></" + super.getSubstituteText() + ">";
        }
    }
    
    @Override
    protected int getCaretShift() {
        // incidentally:
        if (attribute) {
            return 2 + super.getSubstituteText().length();
        } else {
            return 2 + super.getSubstituteText().length();
        }
    }

    @Override
    protected String getRightHtmlText() {
        if (propertyType == null) {
            return null;
        }
        return NbBundle.getMessage(PropertyElementItem.class, 
                primitive ? "FMT_PrimitiveType" : "FMT_DeclaredType", propertyType);
    }

    @Override
    protected ImageIcon getIcon() {
        if (staticProperty) {
            if (ICON_STATIC_PROPERTY == null) {
                ICON_STATIC_PROPERTY = ImageUtilities.loadImageIcon(RESOURCE_STATIC_PROPERTY, false);
            }
            return ICON_STATIC_PROPERTY;
        } else {
            if (ICON_PROPERTY == null) {
                ICON_PROPERTY = ImageUtilities.loadImageIcon(RESOURCE_PROPERTY, false);
            }
            return ICON_PROPERTY;
        }
    }
    
}
