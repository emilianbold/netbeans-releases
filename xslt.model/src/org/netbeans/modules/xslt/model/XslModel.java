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
package org.netbeans.modules.xslt.model;

import org.netbeans.modules.xml.xam.dom.DocumentModel;



/**
 * This interface represents an instance of a xsl model. A schema model is
 * bound to a single file.
 * @author ads
 *
 */
public interface XslModel extends DocumentModel<XslComponent> {

    /**
     * @return the stylesheet represented by this model. The returned stylesheet
     * instance will be valid and well formed, thus attempting to update 
     * from a document which is not well formed will not result in any changes
     * to the stylesheet model. 
     */
    Stylesheet getStylesheet();
    
    /**
     * @return common xsl element factory valid for this instance
     */
    XslComponentFactory getFactory();
}
