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
package org.netbeans.modules.uml.codegen.ui;

import javax.swing.Icon;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class DomainTreeNode
{
    public final static int NODE_ROOT = 0;
    public final static int NODE_FAMILY = 1;
    public final static int NODE_DOMAIN = 2;
    
    private String displayName = null;
    private String familyName = null;
    private String domainName = null;
    private boolean checked = false;
    private Icon icon = null;
    
    public DomainTreeNode(String displayName)
    {
        this(displayName, false, null, null, null);
    }
    
    public DomainTreeNode(String displayName, boolean checked)
    {
        this(displayName, checked, null, null, null);
    }
    
    public DomainTreeNode(
        String displayName,
        boolean checked,
        String familyName,
        String domainName)
    {
        this(displayName, checked, familyName, domainName, null);
    }
    
    public DomainTreeNode(
        String displayName,
        boolean checked,
        String familyName,
        String domainName,
        Icon icon)
    {
        setDisplayName(displayName);
        setChecked(checked);
        setFamilyName(familyName);
        setDomainName(domainName);
        setIcon(icon);
    }
    
    public void setDisplayName(String value)
    {
        displayName = value;
    }
    
    public String getDisplayName()
    {
        return displayName;
    }
    
    
    public void setChecked(boolean value)
    {
        checked = value;
    }
    
    public boolean isChecked()
    {
        // TODO Auto-generated method stub
        return checked;
    }
    
    public void setFamilyName(String value)
    {
        familyName = value;
    }
    
    public String getFamilyName()
    {
        return familyName;
    }
    
    public void setDomainName(String value)
    {
        domainName = value;
    }
    
    public String getDomainName()
    {
        return domainName;
    }
    
    public Icon getIcon()
    {
        return icon;
    }
    
    public void setIcon(Icon value)
    {
        icon = value;
    }
    
    public int getNodeType()
    {
        if (getDomainName() != null)
            return NODE_DOMAIN;
        
        else if (getFamilyName() != null)
            return NODE_FAMILY;
        
        else
            return NODE_ROOT;
    }
    
    public boolean isRoot()
    {
        return getNodeType() == NODE_ROOT;
    }
    
    public boolean isFamily()
    {
        return getNodeType() == NODE_FAMILY;
    }
    
    public boolean isDomain()
    {
        return getNodeType() == NODE_DOMAIN;
    }
    
    public String toString()
    {
        return getDisplayName();
    }
}