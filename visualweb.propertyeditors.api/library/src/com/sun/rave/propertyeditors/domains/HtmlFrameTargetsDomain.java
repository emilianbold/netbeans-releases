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
package com.sun.rave.propertyeditors.domains;

/**
 * Editable Domain for HTML link target frames. The reserved frames defined by HTML
 * 4.01 are provided by default, but the user may define new ones. Edits to this
 * domain are available project-wide.
 *
 */
public class HtmlFrameTargetsDomain extends EditableDomain {

    public HtmlFrameTargetsDomain() {
        super(EditableDomain.PROJECT_STORAGE, String.class);
        this.elements.add(
                new Element("_blank", bundle.getMessage("HtmlFrameTargets.label.blank"),
                bundle.getMessage("HtmlFrameTargets.desc.blank")));
        this.elements.add(
                new Element("_parent", bundle.getMessage("HtmlFrameTargets.label.parent"),
                bundle.getMessage("HtmlFrameTargets.desc.parent")));
        this.elements.add(
                new Element("_self", bundle.getMessage("HtmlFrameTargets.label.self"),
                bundle.getMessage("HtmlFrameTargets.desc.self")));
        this.elements.add(
                new Element("_top", bundle.getMessage("HtmlFrameTargets.label.top"),
                bundle.getMessage("HtmlFrameTargets.desc.top")));
    };

    public String getDisplayName() {
        return bundle.getMessage("HtmlFrameTargets.displayName");
    }

}
