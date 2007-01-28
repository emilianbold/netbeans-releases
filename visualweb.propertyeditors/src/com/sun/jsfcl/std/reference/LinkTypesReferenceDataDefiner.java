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
package com.sun.jsfcl.std.reference;

import java.util.List;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LinkTypesReferenceDataDefiner extends ReferenceDataDefiner {

    public void addBaseItems(List list) {

        super.addBaseItems(list);
        list.add(newItem(
            "", //NOI18N
            null,
            true,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("alt"), //NOI18N
            "Alternate", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("styleSh"), //NOI18N
            "Stylesheet", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("start"), //NOI18N
            "Start", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("next"), //NOI18N
            "Next", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("prev"), //NOI18N
            "Prev", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("contents"), //NOI18N
            "Contents", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("index"), //NOI18N
            "Index", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("gloss"), //NOI18N
            "Glossary", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("copyr"), //NOI18N
            "Copyright", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("chap"), //NOI18N
            "Chapter", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("sect"), //NOI18N
            "Section", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("subsect"), //NOI18N
            "Subsection", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("appdx"), //NOI18N
            "Appendix", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("help"), //NOI18N
            "Help", //NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("bookmark"), //NOI18N
            "Bookmark", //NOI18N
            false,
            false));
    }

    /* (non-Javadoc)
     * @see com.sun.jsfcl.std.reference.ReferenceDataDefiner#canAddRemoveItems()
     */
    public boolean canAddRemoveItems() {

        return true;
    }

    public boolean isValueAString() {

        return true;
    }

}
