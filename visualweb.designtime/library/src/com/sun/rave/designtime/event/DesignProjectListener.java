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

package com.sun.rave.designtime.event;

import com.sun.rave.designtime.DesignContext;

/**
 * DesignProjectListener is the event listener interface for DesignProject.  These methods are
 * called when a DesignContext is opened or closed.
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignProjectListener extends java.util.EventListener {

    /**
     * A DesignContext has been opened in the project
     *
     * @param context the DesignContext that was opened
     */
    public void contextOpened(DesignContext context);

    /**
     * A DesignContext has been closed in the project
     *
     * @param context the DesignContext that was closed
     */
    public void contextClosed(DesignContext context);
}
