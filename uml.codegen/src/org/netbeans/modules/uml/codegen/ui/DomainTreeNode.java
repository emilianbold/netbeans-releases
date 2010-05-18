/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
