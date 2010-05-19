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

package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.Image;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedVariable extends DecoratedTMapComponentAbstract<VariableDeclarator>{

    public DecoratedVariable(VariableDeclarator orig, Variable var) {
        super(orig, var);
    }

    @Override
    public String getName() {
        Variable var = (Variable)getAlternativeReference();
        String varName = null;
        if (var != null) {
            varName = var.getName();
        }
        return varName == null ? super.getName() : varName;
    }

    @Override
    public Image getIcon() {
        return NodeType.VARIABLE.getImage();
    }

    @Override
    public String getHtmlDisplayName() {
        Variable var = (Variable)getAlternativeReference();
        Reference<Message> messRef = var == null ? null : var.getMessage();
        Message mess = messRef != null ? messRef.get() : null;
        
        String messStr = mess != null ? TMapComponentNode.WHITE_SPACE+mess.getName() : TMapComponentNode.EMPTY_STRING;
        return Util.getGrayString(getName(), messStr);
    }

    @Override
    public String getTooltip() {
        Variable var = (Variable)getAlternativeReference();
        String varName = var == null? null : var.getName();
        
        StringBuffer attributesTooltip = new StringBuffer();
        
        Reference<Message> messRef = var.getMessage();
        Message mess = messRef != null ? messRef.get() : null;
        String messStr = mess != null ? TMapComponentNode.WHITE_SPACE+mess.getName() : TMapComponentNode.EMPTY_STRING;
        
        String varTooltipHeader = Util.getGrayString(varName, messStr, TMapComponentNode.EMPTY_STRING, false);
        
        if (mess != null) {

            Collection<Part> parts = mess.getParts();
            if (parts != null) {
                for (Part part : parts) {
                    if (part == null) {
                        continue;
                    }
                    
                    NamedComponentReference<GlobalElement> partEl = part.getElement();
                    String partElStr = partEl != null ? partEl.getRefString() : TMapComponentNode.EMPTY_STRING;
                    
                    attributesTooltip.append(
                        Util.getLocalizedAttributeTemplate2(partElStr, part.getName()));
                }
            }
        }
        return NbBundle.getMessage(TMapComponentNode.class,
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", varTooltipHeader,
                attributesTooltip.toString());
    }

}
