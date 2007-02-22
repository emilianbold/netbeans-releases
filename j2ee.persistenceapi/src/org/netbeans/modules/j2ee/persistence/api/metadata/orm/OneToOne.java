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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface OneToOne {
    
    public void setName(String value);
    
    public String getName();
    
    public void setTargetEntity(String value);
    
    public String getTargetEntity();
    
    public void setFetch(String value);
    
    public String getFetch();
    
    public void setOptional(boolean value);
    
    public boolean isOptional();
    
    public void setMappedBy(String value);
    
    public String getMappedBy();
    
    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index);
    
    public int sizePrimaryKeyJoinColumn();
    
    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value);
    
    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn();
    
    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn();
    
    public void setJoinColumn(int index, JoinColumn value);
    
    public JoinColumn getJoinColumn(int index);
    
    public int sizeJoinColumn();
    
    public void setJoinColumn(JoinColumn[] value);
    
    public JoinColumn[] getJoinColumn();
    
    public int addJoinColumn(JoinColumn value);
    
    public int removeJoinColumn(JoinColumn value);
    
    public JoinColumn newJoinColumn();
    
    public void setJoinTable(JoinTable value);
    
    public JoinTable getJoinTable();
    
    public JoinTable newJoinTable();
    
    public void setCascade(CascadeType value);
    
    public CascadeType getCascade();
    
    public CascadeType newCascadeType();
    
}
