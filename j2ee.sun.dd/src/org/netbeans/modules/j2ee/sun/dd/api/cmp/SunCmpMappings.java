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
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

public interface SunCmpMappings extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {

    public static final String VERSION_1_0 = "1.0"; //NOI18N
    public static final String VERSION_1_1 = "1.1"; //NOI18N
    public static final String VERSION_1_2 = "1.2"; //NOI18N

    public static final String SUN_CMP_MAPPING = "SunCmpMapping"; // NOI18N

    public void setSunCmpMapping(int index, SunCmpMapping value);
    public SunCmpMapping getSunCmpMapping(int index);
    public int sizeSunCmpMapping();
    public void setSunCmpMapping(SunCmpMapping[] value);
    public SunCmpMapping[] getSunCmpMapping();
    public int addSunCmpMapping(SunCmpMapping value);
    public int removeSunCmpMapping(SunCmpMapping value);
    public SunCmpMapping newSunCmpMapping();

}
