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
package org.netbeans.modules.visualweb.propertyeditors;

import org.netbeans.modules.visualweb.propertyeditors.UrlFileFilter;
import org.netbeans.modules.visualweb.propertyeditors.UrlPropertyEditor;
import java.util.ResourceBundle;

/**
 * An editor for properties that take URLs for which the resource should be an
 * image file.
 *
 * @author gjmurphy
 */
public class ImageUrlPropertyEditor extends UrlPropertyEditor implements
        com.sun.rave.propertyeditors.ImageUrlPropertyEditor {

    static UrlFileFilter fileFilter = new UrlFileFilter(
        ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString(
            "ImageUrlPropertyEditor.imageFileFilter.label"), //NOI18N
            ".*\\.(jpg|JPG|jpeg|JPEG|tif|TIF|tiff|TIFF|gif|GIF|png|PNG)$" //NOI18N
        );

    public UrlFileFilter getFileFilter() {
        return fileFilter;
    }

    protected String getPropertyHelpId() {
        return "projrave_ui_elements_propeditors_imageurl_prop_ed";
    }

}
