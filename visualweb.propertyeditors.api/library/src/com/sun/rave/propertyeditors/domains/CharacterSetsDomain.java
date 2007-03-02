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
 * Editable domain of character set names, as specified by the IANA. Only the more
 * commonly used character sets are provided by default, but the user may add
 * more. No explicit support is provided for charset aliases, but these may be
 * added as new elements. The default names are given in the IANA preferred form.
 * Edits of this domain are available Project-wide.
 */
// TODO - When DesignContext.getProject().getGlobalData() fixed, make this domain IDE-scoped
public class CharacterSetsDomain extends EditableDomain {

    public CharacterSetsDomain() {
        super(EditableDomain.PROJECT_STORAGE, String.class);
        this.elements.add( new Element("ISO-8859-1"));
        this.elements.add( new Element("US-ASCII"));
        this.elements.add( new Element("UTF-8"));
        this.elements.add( new Element("SHIFT_JIS"));
        this.elements.add( new Element("EUC-JP"));
        this.elements.add( new Element("EUC-KR"));
        this.elements.add( new Element("GB2312"));
        this.elements.add( new Element("Big5"));
    }

    public String getDisplayName() {
        return bundle.getMessage("CharacterSets.displayName");
    }

}
