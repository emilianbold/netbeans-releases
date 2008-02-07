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
package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.mapper.tree.TreeExpandedState;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperState {
    private TreeExpandedState myLeftTreeState;
    private TreeExpandedState myRightTreeState;
    private TreeExpandedState myGraphState;
    private UniqueId myEntityUid;

// TODO a    private GraphExpandedState myGraphState;

    public MapperState(UniqueId entityUid) {
        assert entityUid != null;
        myEntityUid = entityUid;
    }

    public UniqueId getEntityUid() {
        return myEntityUid;
    }

    public TreeExpandedState getGraphEXpandedState() {
        return myGraphState;
    }

    public void setGraphExpandedState(TreeExpandedState graphState) {
        myGraphState = graphState;
    }

    public TreeExpandedState getLeftTreeExpandedState() {
        return myLeftTreeState;
    }

    public void setLeftTreeExpandedState( TreeExpandedState leftTreeState) {
        myLeftTreeState = leftTreeState;
    }

    public TreeExpandedState getRightTreeExpandedState() {
        return myRightTreeState;
    }

    public void setRightTreeExpandedState( TreeExpandedState rightTreeState) {
        myRightTreeState = rightTreeState;
    }



}
