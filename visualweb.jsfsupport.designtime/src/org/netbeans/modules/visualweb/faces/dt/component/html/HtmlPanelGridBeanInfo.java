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
package org.netbeans.modules.visualweb.faces.dt.component.html;

import com.sun.rave.designtime.faces.FacetDescriptor;

/**
 * The HtmlPanelGridBeanInfo class provides design-time meta data for
 * the HtmlPanelGrid component for use in a visual design tool.
 */
public class HtmlPanelGridBeanInfo extends HtmlPanelGridBeanInfoBase {

    /**
     * <p>FIXME - workaround for problem that returning any facets causes
     * the palette entry for this component to be messed up.</p>
     */
    public FacetDescriptor[] getFacetDescriptors() {
        return new FacetDescriptor[0];
    }

}
