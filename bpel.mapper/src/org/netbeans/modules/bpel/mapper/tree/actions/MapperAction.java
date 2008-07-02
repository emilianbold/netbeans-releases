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

package org.netbeans.modules.bpel.mapper.tree.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;

/**
 *
 * @author nk160297
 */
public abstract class MapperAction<SubjectType> extends AbstractAction {
    
    protected MapperTcContext mMapperTcContext;
    protected SubjectType mActionSubject;
    
    public MapperAction(MapperTcContext mapperTcContext, SubjectType actionSubject) {
        super();
        mMapperTcContext = mapperTcContext;
        mActionSubject = actionSubject;
    }
    
    protected void postInit() {
        putValue(Action.NAME, getDisplayName());
        putValue(Action.SMALL_ICON, getIcon());
    }
    
    public SubjectType getActionSubject() {
        return mActionSubject;
    }
    
    public String getDisplayName() {
        return "";
    }
    
    public Icon getIcon() {
        return null;
    }
    
    public MapperTcContext getContext(){
        return mMapperTcContext;
    }
    
    public BpelDesignContext getDesignContext() { 
        return getContext().getDesignContextController().getContext();
    }

}
