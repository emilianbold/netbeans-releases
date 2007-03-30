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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The icon type contains small-icon and large-icon elements
 * that specify the file names for small and large GIF, JPEG,
 * or PNG icon images used to represent the parent element in a
 * GUI tool.
 *
 * The xml:lang attribute defines the language that the
 * icon file names are provided in. Its value is "en" (English)
 * by default
 *
 * @author Petr Pisl
 */

public interface Icon extends LangAttribute {
    
    public static final String SMALL_ICON = JSFConfigQNames.SMALL_ICON.getLocalName();
    public static final String LARGE_ICON = JSFConfigQNames.LARGE_ICON.getLocalName();
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the small icon
     */
    public String getSmallIcon();
    
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param smallIcon the file name
     */
    public void setSmallIcon(String smallIcon);
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the large icon
     */
    public String getLargeIcon();
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param largeIcon the path to the large icon
     */
    public void setLargeIcon(String largeIcon);
    
}
