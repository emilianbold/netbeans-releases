/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model;

import java.util.EventListener;

/**
 * Gets notification on model events
 * for now, project opening and closing
 *
 * @author Vladimir Kvashin
 */
public interface CsmModelListener extends EventListener {

    /** Is called upon project opening */
    void projectOpened(CsmProject project);

    /** 
     * Is called upon project closing.
     * At the moment of this call the project isn't really closed;
     * (this is more convenient to clients)
     * TODO: consider renaming to projectClosing
     */
    void projectClosed(CsmProject project);

    /**
     * Is called when model is changed
     * (except for changes made at initial scanning)
     */
    void modelChanged(CsmChangeEvent e);
}
