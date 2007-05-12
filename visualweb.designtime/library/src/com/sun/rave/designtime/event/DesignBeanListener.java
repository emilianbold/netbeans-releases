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

import java.util.EventListener;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;

/**
 * DesignBeanListener is the event listener interface for DesignBeans.  These methods are called
 * when a DesignBean is changed, a DesignProperty is changed (on a DesignBean), or a DesignEvent is
 * changed (on a DesignBean).
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignBean#addDesignBeanListener(DesignBeanListener)
 */
public interface DesignBeanListener extends EventListener {

    /**
     * The specified DesignBean's DesignContext has been "activated" in the project
     *
     * @param designBean the DesignBean who's DesignContext that has been activated
     */
    public void beanContextActivated(DesignBean designBean);

    /**
     * The specified DesignBean's DesignContext has been "deactivated" in the project
     *
     * @param designBean the DesignBean who's DesignContext that has been deactivated
     */
    public void beanContextDeactivated(DesignBean designBean);

    /**
     * The specified DesignBean's instance name was changed.  This is the source-code instance name
     * of the bean component.
     *
     * @param designBean The DesignBean that has a new instance name
     * @param oldInstanceName The old instance name
     */
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName);

    /**
     * The specified DesignBean has changed.  This represents a larger-scale change than a single
     * property - this may be the instance name has changed or some other more-than-just-a-property
     * aspect of the DesignBean has changed.
     *
     * @param designBean The DesignBean that has changed
     */
    public void beanChanged(DesignBean designBean);

    /**
     * The specified DesignProperty has changed.  This could mean that a new value was set, or the
     * property was 'unset', or anything that results in the DesignProperty being different.  The
     * oldValue will be passed in if applicable and possible.
     *
     * @param prop The DesignProperty that has changed
     * @param oldValue The prior value of the property (may be null)
     */
    public void propertyChanged(DesignProperty prop, Object oldValue);

    /**
     * The specified DesignEvent has changed.  This could mean that the event was hooked, unhooked,
     * or the handler method name was changed, or anything that results in the DesignEvent being
     * different.
     *
     * @param event The DesignEvent that has changed
     */
    public void eventChanged(DesignEvent event);
}
