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
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;


/**
 *
 * @author Peter Williams
 */
public abstract class SRMBaseTableModel extends AbstractTableModel {
    
    /** Model synchronizer
     */
    private final XmlMultiViewDataSynchronizer synchronizer;
    
    /** Reference to security-role-mapping instance
     */
    protected final SecurityRoleMapping mapping;
    
    public SRMBaseTableModel(XmlMultiViewDataSynchronizer s, SecurityRoleMapping m) {
        assert m != null;
        
        synchronizer = s;
        mapping = m;
    }

    protected void modelUpdatedFromUI() {
        if (synchronizer != null) {
            synchronizer.requestUpdateData();
        }
    }

    /** Model manipulation
     */
	public abstract void removeElements(int[] indices);
    
    
}
