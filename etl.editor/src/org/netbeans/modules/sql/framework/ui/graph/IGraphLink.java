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
package org.netbeans.modules.sql.framework.ui.graph;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;

/**
 * @author radval
 */
public interface IGraphLink {

    /**
     * get from port of this link
     * 
     * @return from port
     */
    public IGraphPort getFromGraphPort();

    /**
     * get to port of this link
     * 
     * @return to port
     */
    public IGraphPort getToGraphPort();

    /**
     * get the data object associated with link
     * 
     * @return data object
     */
    Object getDataObject();

    /**
     * set data object in this link
     * 
     * @param obj data object
     */
    void setDataObject(Object obj);

    /**
     * start highlighting this link
     */
    void startHighlighting();

    /**
     * stop highlighting this link
     */
    void stopHighlighting();

    /**
     * set the link pen
     * 
     * @param pen pen
     */
    void setGraphLinkPen(JGoPen pen);

    /**
     * set the link brush
     * 
     * @param brush brush
     */
    void setGraphLinkBrush(JGoBrush brush);
}

