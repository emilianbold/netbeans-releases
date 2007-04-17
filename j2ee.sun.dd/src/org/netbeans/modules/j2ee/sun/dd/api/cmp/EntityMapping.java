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

public interface EntityMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String EJB_NAME = "EjbName"; // NOI18N
    public static final String TABLE_NAME = "TableName"; // NOI18N
    public static final String CMP_FIELD_MAPPING = "CmpFieldMapping"; // NOI18N
    public static final String CMR_FIELD_MAPPING = "CmrFieldMapping"; // NOI18N
    public static final String SECONDARY_TABLE = "SecondaryTable"; // NOI18N
    public static final String CONSISTENCY = "Consistency"; // NOI18N

    public void setEjbName(String value);
    public String getEjbName();

    public void setTableName(String value);
    public String getTableName();

    public void setCmpFieldMapping(int index, CmpFieldMapping value);
    public CmpFieldMapping getCmpFieldMapping(int index);
    public int sizeCmpFieldMapping();
    public void setCmpFieldMapping(CmpFieldMapping[] value);
    public CmpFieldMapping[] getCmpFieldMapping();
    public int addCmpFieldMapping(CmpFieldMapping value);
    public int removeCmpFieldMapping(CmpFieldMapping value);
    public CmpFieldMapping newCmpFieldMapping();

    public void setCmrFieldMapping(int index, CmrFieldMapping value);
    public CmrFieldMapping getCmrFieldMapping(int index);
    public int sizeCmrFieldMapping();
    public void setCmrFieldMapping(CmrFieldMapping[] value);
    public CmrFieldMapping[] getCmrFieldMapping();
    public int addCmrFieldMapping(CmrFieldMapping value);
    public int removeCmrFieldMapping(CmrFieldMapping value);
    public CmrFieldMapping newCmrFieldMapping();

    public void setSecondaryTable(int index, SecondaryTable value);
    public SecondaryTable getSecondaryTable(int index);
    public int sizeSecondaryTable();
    public void setSecondaryTable(SecondaryTable[] value);
    public SecondaryTable[] getSecondaryTable();
    public int addSecondaryTable(SecondaryTable value);
    public int removeSecondaryTable(SecondaryTable value);
    public SecondaryTable newSecondaryTable();

    public void setConsistency(Consistency value);
    public Consistency getConsistency();
    public Consistency newConsistency();

}
