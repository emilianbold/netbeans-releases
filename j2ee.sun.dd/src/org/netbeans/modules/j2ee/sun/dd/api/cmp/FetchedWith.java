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

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface FetchedWith extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String DEFAULT = "Default"; // NOI18N
    public static final String LEVEL = "Level"; // NOI18N
    public static final String NAMED_GROUP = "NamedGroup"; // NOI18N
    public static final String NONE = "None"; // NOI18N

    public void setDefault(boolean value) throws VersionNotSupportedException;
    public boolean isDefault() throws VersionNotSupportedException;

    public void setLevel(String value);
    public String getLevel();

    public void setNamedGroup(String value);
    public String getNamedGroup();

    public void setNone(boolean value);
    public boolean isNone();

}
