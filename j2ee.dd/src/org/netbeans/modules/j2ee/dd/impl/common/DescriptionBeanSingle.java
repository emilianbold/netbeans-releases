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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * Superclass that implements DescriptionInterface for Ejb2.0 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public abstract class DescriptionBeanSingle extends EnclosingBean implements DescriptionInterface {

    public DescriptionBeanSingle(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    // methods implemented by specific s2b beans
    public String getDescription() {return null;}
    public void setDescription(String description){}
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        if (locale==null) setDescription(description);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        if (locale==null) return getDescription();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getDefaultDescription() {
        return getDescription();
    }
    public java.util.Map getAllDescriptions() {
        java.util.Map map = new java.util.HashMap();
        map.put(null, getDescription());
        return map;
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        if (locale==null) setDescription(null);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeDescription() {
        setDescription(null);
    }
    public void removeAllDescriptions() {
        setDescription(null);
    }
}
