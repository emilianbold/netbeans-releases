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
 * Superclass that implements DisplayNameInterface and IconInterface for Ejb2.0 beans.
 *
 * @author  Milan Kuchtiak
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.api.web.dd.common.IconInterface;
import org.netbeans.api.web.dd.common.VersionNotSupportedException;
import org.netbeans.api.web.dd.common.DisplayNameInterface;

public abstract class ComponentBeanSingle extends DescriptionBeanSingle implements DisplayNameInterface, IconInterface {
    
    public ComponentBeanSingle(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    
    // methods implemented in specific BaseBeans e.g. Servlet
    public org.netbeans.api.web.dd.Icon getIcon(){return null;}
    public void setIcon(org.netbeans.api.web.dd.Icon icon){}
    public abstract String getDisplayName();
    public abstract void setDisplayName(String displayName);
    
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        if (locale==null) setDisplayName(displayName);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public void setAllDisplayNames(java.util.Map displayNames) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        if (locale==null) return getDisplayName();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getDefaultDisplayName() {
        return getDisplayName();
    }
    public java.util.Map getAllDisplayNames() {
        java.util.Map map = new java.util.HashMap();
        map.put(null, getDisplayName());
        return map;
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        if (locale==null) setDisplayName(null);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeDisplayName() {
        setDisplayName(null);
    }
    public void removeAllDisplayNames() {
        setDisplayName(null);
    }

    // setters
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        if (locale==null) setSmallIcon(icon);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void setSmallIcon(String icon) {
        setIcon(icon, true);
    }
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        if (locale==null) setLargeIcon(icon);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void setLargeIcon(String icon) {
        setIcon(icon, false);
    }
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    // getters
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) return getSmallIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getSmallIcon() {
        org.netbeans.api.web.dd.Icon icon = getIcon();
        if (icon==null) return null;
        else return icon.getSmallIcon();
    }
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) return getLargeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getLargeIcon() {
        org.netbeans.api.web.dd.Icon icon = getIcon();
        if (icon==null) return null;
        else return icon.getLargeIcon();
    }
    public org.netbeans.api.web.dd.Icon getDefaultIcon() {
        return getIcon();
    }
    public java.util.Map getAllIcons() {
        java.util.Map map = new java.util.HashMap();
        org.netbeans.api.web.dd.Icon icon = getIcon();
        if (icon!=null) {
            String[] icons = new String[]{icon.getSmallIcon(),icon.getLargeIcon()};
            map.put(null, icons);
        }
        return map;
    }
    // removers
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeSmallIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeLargeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeIcon(String locale) throws VersionNotSupportedException {
        if (locale==null) removeIcon();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeSmallIcon() {
        org.netbeans.api.web.dd.Icon icon = getIcon();
        if (icon!=null) {
            icon.setSmallIcon(null);
            if (icon.getLargeIcon()==null) setIcon(null);
        }
    }
    public void removeLargeIcon() {
        org.netbeans.api.web.dd.Icon icon = getIcon();
        if (icon!=null) {
            icon.setLargeIcon(null);
            if (icon.getSmallIcon()==null) setIcon(null);
        }
    }
    public void removeIcon() {
        setIcon(null);
    }
    public void removeAllIcons() {
        setIcon(null);
    }
    // universal method for setting icon
    private void setIcon(String icon, boolean isSmall) {
        org.netbeans.api.web.dd.Icon oldIcon = getIcon();
        if (oldIcon==null) {
            if (icon!=null) {
                try {
                    org.netbeans.api.web.dd.Icon newIcon = (org.netbeans.api.web.dd.Icon) createBean("Icon");
                    if (isSmall) newIcon.setSmallIcon(icon);
                    else newIcon.setLargeIcon(icon);
                    setIcon(newIcon);
                } catch(ClassNotFoundException ex){}
            }
        } else {
            if (icon==null) {
                if (isSmall) {
                    oldIcon.setSmallIcon(null);
                    if (oldIcon.getLargeIcon()==null) setIcon(null);
                } else {
                    oldIcon.setLargeIcon(null);
                    if (oldIcon.getSmallIcon()==null) setIcon(null);
                }
            } else {
                if (isSmall) oldIcon.setSmallIcon(icon);
                else oldIcon.setLargeIcon(icon);
            }
        }        
    }
}
