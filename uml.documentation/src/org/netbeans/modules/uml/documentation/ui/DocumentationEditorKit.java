/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.documentation.ui;

import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

/**
 *
 * @author Sheryl
 */
public class DocumentationEditorKit extends HTMLEditorKit
{
    
    /** Creates a new instance of DocumentationEditorKit */
    public DocumentationEditorKit()
    {
    }
    
    public ViewFactory getViewFactory()
    {
        return new HTMLFactoryExtended();
    }
    
    public static class HTMLFactoryExtended extends HTMLFactory implements ViewFactory
    {
        public HTMLFactoryExtended()
        {
        }
        
        // override default behavior, do not display html comment
        public View create(Element elem)
        {
            Object obj = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if(obj instanceof HTML.Tag)
            {
                HTML.Tag tagType = (HTML.Tag)obj;
                if(tagType == HTML.Tag.COMMENT)
                {
                    return new ComponentView(elem);
                }
            }
            return super.create(elem);
        }
    }
    
    
}
