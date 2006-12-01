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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.Image;
import java.io.CharConversionException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.ImageIcon;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jan Lahoda
 */
class ElementNode extends AbstractNode {

    private String htmlDisplayName;
    private Element element;

    public ElementNode(Element element) {
        super(Children.LEAF, Lookups.singleton(element));

        this.element = element;

        htmlDisplayName = "";

        switch (element.getKind()) {
            case FIELD:
            case LOCAL_VARIABLE:
            case PARAMETER:
                VariableElement variable = (VariableElement) element;

                htmlDisplayName = getHtmlTypeName(variable.asType())  + " " + variable.getSimpleName();
                break;
            case METHOD:
                ExecutableElement method = (ExecutableElement) element;
                StringBuffer methodDisplayNameBuf = new StringBuffer();

                methodDisplayNameBuf.append(getHtmlTypeName(method.getReturnType()));
                methodDisplayNameBuf.append(' ');
                methodDisplayNameBuf.append(method.getSimpleName());
                methodDisplayNameBuf.append('(');

                boolean addCommand = false;

                for (VariableElement ve : method.getParameters()) {
                    if (addCommand)
                        methodDisplayNameBuf.append(", ");

                    methodDisplayNameBuf.append(getHtmlTypeName(ve.asType()));
                    methodDisplayNameBuf.append(' ');
                    methodDisplayNameBuf.append(ve.getSimpleName());
                    addCommand = true;
                }

                methodDisplayNameBuf.append(')');

                htmlDisplayName = methodDisplayNameBuf.toString();
                break;
        }
    }

    private String getHtmlTypeName(TypeMirror t) {
        try {
            return XMLUtil.toElementContent(Utilities.getTypeName(t, false).toString());
        } catch (CharConversionException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return "";
        }
    }

    @Override
    public Image getIcon(int type) {
        //XXX: pretty ugly cast to ImageIcon:
        return ((ImageIcon) UiUtils.getDeclarationIcon(element)).getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }


}