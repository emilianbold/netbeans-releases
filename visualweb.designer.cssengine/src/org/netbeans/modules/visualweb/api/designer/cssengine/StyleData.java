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


package org.netbeans.modules.visualweb.api.designer.cssengine;


/**
 * XXX Copy of Rave class in modified batik/StyleSetting, to shield the batik stuff.
 *
 * A value object for communicating a desired setting or clearing of a CSS property
 *
 * @author Tor Norbye
 */
public class StyleData {

    private final int index;
    private final String value;

    /** Create a style setting only specifying a property; should only
     * be used for removals */
    public StyleData(int index) {
        this(index, null);
    }

    /** Construct a StyleSetting with the given index and value */
    public StyleData(int index, String value) {
        this.index = index;
        this.value = value;
    }


    /** Return the CSS property index */
    public int getIndex() {
        return index;
    }

    /** Return the value, if any, for this CSS setting */
    public String getValue() {
        return value;
    }
}
