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

package com.sun.rave.designtime;

/**
 * <p>A DesignEvent represents a single event listener method (and possibly handler) on a single
 * instance of a DesignBean at design-time.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignEvent {

    /**
     * Returns the EventDescriptor that defines the meta-data for this DesignEvent
     *
     * @return The EventDescriptor that defines teh meta-data for this DesignEvent
     */
    public EventDescriptor getEventDescriptor();

    /**
     * Returns the DesignBean that owns this DesignEvent
     *
     * @return the DesignBean that owns this DesignEvent
     */
    public DesignBean getDesignBean();

    /**
     * Returns the default event handler method name.  For example on a Button component's 'click'
     * event, the default handler name might be "button1_click".
     *
     * @return the default event handler method name, same as setHandlerName would use if passed null
     */
    public String getDefaultHandlerName();

    /**
     * Sets the method name for this DesignEvent.  If the event is not currently 'hooked', this will
     * 'hook' it and add the required wiring to direct the event handler code to this method name.
     * If 'null' is passed as the handlerName, then the default event handler method name will be
     * used.
     *
     * @param handlerMethodName The desired event handler method name - may be null to use default
     *        event handler method name
     * @return <code>true</code> if the event was successfully 'hooked', and the specified name was unique
     */
    public boolean setHandlerName(String handlerMethodName);

    /**
     * Returns the current event method handler name, or null if the event is currently not 'hooked'
     *
     * @return the current event method handler name, or null if the event is currently not 'hooked'
     */
    public String getHandlerName();

    /**
     * Returns <code>true</code> if this DesignEvent is currently 'hooked', or <code>false</code>
     * if it is not.
     *
     * @return <code>true</code> if this DesignEvent is currently 'hooked', or <code>false</code>
     *         if it is not
     */
    public boolean isHandled();

    /**
     * Removes and unwires an event handler method from this DesignEvent, if one exists.  Returns
     * <code>true</code> if successful, <code>false</code> if not.
     *
     * @return <code>true</code> if successful, <code>false</code> if not
     */
    public boolean removeHandler();

    /**
     * Sets the Java source for the method body of the handler method.  This is expected to be valid
     * Java source to be injected into the body of this event handler method.  If it is not, an
     * IllegalArgumentException is thrown.
     *
     * @param methodBody The Java source for the method body of this event handler method
     * @throws IllegalArgumentException thrown if the Java source is invalid
     */
    public void setHandlerMethodSource(String methodBody) throws IllegalArgumentException;

    /**
     * Returns the Java source code from the body of the handler method
     *
     * @return the Java source code from the body of the handler method
     */
    public String getHandlerMethodSource();
}
