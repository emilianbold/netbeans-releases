/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import org.netbeans.api.web.dd.common.VersionNotSupportedException;
import org.netbeans.api.web.dd.common.DescriptionInterface;

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
