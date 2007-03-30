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

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * This group keeps the usage of the contained description related
 * elements consistent across Java EE deployment descriptors.
 *
 * All elements may occur multiple times with different languages,
 * to support localization of the content.
 *
 * @author Petr Pisl
 */
public interface DescriptionGroup {
    
    public static final String DESCRIPTION = JSFConfigQNames.DESCRIPTION.getLocalName();
    public static final String DISPLAY_NAME = JSFConfigQNames.DISPLAY_NAME.getLocalName();
    public static final String ICON = JSFConfigQNames.ICON.getLocalName();
    
    /**
     *
     * @return
     */
    List<Description> getDescriptions();
    
    /**
     *
     * @param description
     */
    void addDescription(Description description);
    /**
     *
     * @param index
     * @param description
     */
    void addDescription(int index, Description description);
    
    /**
     *
     * @param description
     */
    void removeDescription(Description description);
    
    /**
     *
     * @return
     */
    List<DisplayName> getDisplayNames();
    
    /**
     *
     * @param displayName
     */
    void addDisplayName(DisplayName displayName);
    
    /**
     *
     * @param index
     * @param displayName
     */
    void addDisplayName(int index, DisplayName displayName);
    /**
     *
     * @param displayName
     */
    void removeDisplayName(DisplayName displayName);
    
    /**
     *
     * @return
     */
    List<Icon> getIcons();
    
    /**
     *
     * @param icon
     */
    void addIcon(Icon icon);
    void addIcon(int index, Icon icon);
    void removeIcon(Icon icon);
}
