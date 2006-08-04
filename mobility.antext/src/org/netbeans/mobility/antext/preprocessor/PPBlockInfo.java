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

/*
 * PPBlockInfo.java
 *
 * Created on August 12, 2005, 9:40 AM
 */
package org.netbeans.mobility.antext.preprocessor;

/**
 * @author Adam Sotona
 */
public final class PPBlockInfo
{
    
    private final PPLine startLine;
    private final boolean active;
    private final boolean valid;
    private final boolean toBeCommented;
    private int endLine;
    private boolean hasFooter;
    private final PPBlockInfo parent;
    private final PPBlockInfo ifChainAncestor;
    
    PPBlockInfo(PPBlockInfo parent, PPLine startLine, boolean valid, boolean active, PPBlockInfo ifChainAncestor)
    {
        this.parent = parent;
        this.ifChainAncestor = ifChainAncestor;
        this.startLine = startLine;
        this.valid = valid && (parent == null ? true : parent.isValid()) && (ifChainAncestor == null ? true : ifChainAncestor.isValid());
        this.toBeCommented = this.valid || (parent != null && parent.isToBeCommented());
        this.active = this.valid ? (active && (ifChainAncestor == null || !ifChainAncestor.hasBeenActive()) && (parent == null ? true : parent.isActive())) : (parent == null ? active : parent.isActive());
        this.endLine = -1;
        this.hasFooter = false;
    }
    
    public int getType()
    {
        return startLine.getType();
    }
    
    public PPBlockInfo getParent()
    {
        return parent;
    }
    
    public int getStartLine()
    {
        return startLine.getLineNumber();
    }
    
    public boolean isValid()
    {
        return valid;
    }
    
    public boolean isToBeCommented()
    {
        return toBeCommented;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    public int getEndLine()
    {
        return endLine;
    }
    
    public boolean hasFooter()
    {
        return hasFooter;
    }
    
    /* package-private methods to setup PPBlockInfo during parsing */
    
    void addError(final String message)
    {
        startLine.addError(message);
    }
    
    void setEndLine(final int endLine)
    {
        this.endLine = endLine;
    }
    
    void setHasFooter(final boolean footer)
    {
        this.hasFooter = footer;
    }
    
    boolean hasBeenActive()
    {
        return active || (ifChainAncestor != null && ifChainAncestor.hasBeenActive());
    }
}
