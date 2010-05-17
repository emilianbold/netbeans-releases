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
package com.sun.rave.web.ui.component.util.event;

import java.util.EventObject;

import javax.faces.component.UIComponent;


/**
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class CommandEvent extends EventObjectBase implements UIComponentHolder {

    /**
     *	<p> Constructor.</p>
     *
     *	@param	component   The <code>UIComponent</code> associated with this
     *			    <code>EventObject</code>.
     */
    public CommandEvent(UIComponent component, EventObject actionEvent) {
	super(component);
	setActionEvent(actionEvent);
    }

    /**
     *	<p> Setter for <code>actionEvent</code>.  When a
     *	    <code>CommandEvent</code> is created, there is often another event
     *	    involved.  This property contains that other event (if any).  The
     *	    type of this event is often an <code>ActionEvent</code>, hence the
     *	    name of this property.  However, this should not always be assumed
     *	    to be set or of type <code>ActionEvent</code>.</p>
     *
     *	@param	actionEvent The <code>EventObject</code> to set.
     */
    public void setActionEvent(EventObject actionEvent) {
	_actionEvent = actionEvent;
    }

    /**
     *	<p> Getter for <code>actionEvent</code>.  When a
     *	    <code>CommandEvent</code> is created, there is often another event
     *	    involved.  This property contains that other event (if any).  The
     *	    type of this event is often an <code>ActionEvent</code>, hence the
     *	    name of this property.  However, this should not always be assumed
     *	    to be set or of type <code>ActionEvent</code>.</p>
     *
     *	@return	The actionEvent <code>EventObject</code>.
     */
    public EventObject getActionEvent() {
	return _actionEvent;
    }

    private EventObject _actionEvent = null;
}
