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
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public abstract class DescriptionBeanMultiple extends EnclosingBean implements DescriptionInterface {

    public DescriptionBeanMultiple(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    // methods implemented by specific s2b beans
    public void setDescription(int index, java.lang.String value){}
    public String getDescription(int index){return null;}
    public void setDescription(java.lang.String[] value){}
    //public abstract java.lang.String[] getDescription();
    public int sizeDescription(){return 0;}
    public int addDescription(java.lang.String value){return 0;}
    //public abstract int removeDescription(java.lang.String value);
    public void setDescriptionXmlLang(int index, java.lang.String value){}
    public String getDescriptionXmlLang(int index){return null;}
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        if (description==null) removeDescriptionForLocale(locale);
        else {
            int size = sizeDescription();
            boolean found=false;
            for (int i=0;i<size;i++) {
                String loc=getDescriptionXmlLang(i);
                if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                    found=true;
                    setDescription(i, description);
                    break;
                }
            }
            if (!found) {
                addDescription(description);
                if (locale!=null) setDescriptionXmlLang(size, locale.toLowerCase());
            }
        }
    }
    
    public void setDescription(String description) {
        try {
            setDescription(null,description);
        } catch (VersionNotSupportedException ex){}
    }
    
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException {
        removeAllDescriptions();
        if (descriptions!=null) {
            java.util.Iterator keys = descriptions.keySet().iterator();
            String[] newDescription = new String[descriptions.size()]; 
            int i=0;
            while (keys.hasNext()) {
                String key = (String) keys.next();
                addDescription((String)descriptions.get(key));
                setDescriptionXmlLang(i++, key);
            }
        }
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        for (int i=0;i<sizeDescription();i++) {
            String loc=getDescriptionXmlLang(i);
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                return getDescription(i);
            }
        }
        return null;
    }
    public String getDefaultDescription() {
        try {
            return getDescription(null);
        } catch (VersionNotSupportedException ex){return null;}
    }
    public java.util.Map getAllDescriptions() {
        java.util.Map map =new java.util.HashMap();
        for (int i=0;i<sizeDescription();i++) {
            String desc=getDescription(i);
            String loc=getDescriptionXmlLang(i);
            map.put(loc, desc);
        }
        return map;
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        java.util.Map map = new java.util.HashMap();
        for (int i=0;i<sizeDescription();i++) {
            String desc=getDescription(i);
            String loc=getDescriptionXmlLang(i);
            if ((locale==null && loc!=null) || (locale!=null && !locale.equalsIgnoreCase(loc)))
                map.put(loc, desc);
        }
        setAllDescriptions(map);
    }
    
    public void removeDescription() {
        try {
            removeDescriptionForLocale(null);
        } catch (VersionNotSupportedException ex){}
    }
    public void removeAllDescriptions() {
        setDescription(new String[]{});
    }
}
