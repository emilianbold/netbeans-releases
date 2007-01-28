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
public class TFramesReferenceDataDefiner extends ReferenceDataDefiner {

    public void addBaseItems(List list) {

        list.add(newItem(
            "", //NOI18N
            null,
            true,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("void"), //NOI18N
            "void", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("above"), //NOI18N
            "above", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("below"), //NOI18N
            "below", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("hsides"), //NOI18N
            "hsides", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("lhs"), //NOI18N
            "lhs", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("rhs"), //NOI18N
            "rhs", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("vsides"), //NOI18N
            "vsides", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("box"), //NOI18N
            "box", // NOI18N
            false,
            false));
        list.add(newItem(
            BundleHolder.bundle.getMessage("border"), //NOI18N
            "border", // NOI18N
            false,
            false));
    }

    public boolean canAddRemoveItems() {

        return false;
    }

    public boolean isValueAString() {

        return true;
    }

}
