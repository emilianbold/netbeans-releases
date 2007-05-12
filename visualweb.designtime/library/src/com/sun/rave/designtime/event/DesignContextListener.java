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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.Position;

/**
 * DesignContextListener is the event listener interface for DesignContexts.  These methods are called
 * when a DesignBean is created, deleted, changed, or moved.  This includes the DesignBeanListener
 * methods as well, so effectively a DesignContextListener is a listener to *all* DesignBeans in a
 * context.
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignContextListener extends DesignBeanListener {

    /**
     * A DesignContext has been "activated" in the project
     *
     * @param context the DesignContext that has been activated
     */
    public void contextActivated(DesignContext context);

    /**
     * A DesignContext has been "deactivated" in the project
     *
     * @param context the DesignContext that has been deactivated
     */
    public void contextDeactivated(DesignContext context);

    /**
     * Something at the context-level changed.  This is a large-grain change like a file rename or
     * something that cannot be represented by one of the smaller-grain methods.
     *
     * @param context DesignContext The DesignContext that changed
     */
    public void contextChanged(DesignContext context);

    /**
     * A new DesignBean has been created.  This corresponds to a new instance bean being dropped from
     * the palette or programmattically created via the Design-Time API.
     *
     * @param designBean DesignBean The newly created DesignBean
     */
    public void beanCreated(DesignBean designBean);

    /**
     * A DesignBean has been deleted.
     *
     * @param designBean DesignBean The DesignBean that was deleted.  At this point, it is a goner, so any
     *        manipulations done to the passed bean will be tossed out immediately after this method
     *        returns
     */
    public void beanDeleted(DesignBean designBean);

    /**
     * A DesignBean was moved either within its parent DesignBean, or to another parent DesignBean.
     *
     * @param designBean DesignBean The DesignBean that was moved
     * @param oldParent DesignBean The old parent DesignBean (my match the new parent)
     * @param pos Position The new parent DesignBean (may match the old parent)
     */
    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos);
}
