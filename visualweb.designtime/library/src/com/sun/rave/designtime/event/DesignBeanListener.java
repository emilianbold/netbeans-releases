/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
