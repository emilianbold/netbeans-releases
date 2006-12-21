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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.spi;

import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;

/**
 * The implementor of this SPI shoudl display a dialog to user
 *
 * @param event event that contains information concerning maximum memory and memory used
 *
 * @param fatal distingwishes two modes: 
 * if it is false, user should be just warned, so that he/she can switch off code model for the some or all projects;
 * if fatal is true, this mean that code model is already switched off for all projects.
 *
 * @author Vladimir Kvashin
 */
public interface LowMemoryAlerter {
    void alert(LowMemoryEvent event, boolean fatal);
}
