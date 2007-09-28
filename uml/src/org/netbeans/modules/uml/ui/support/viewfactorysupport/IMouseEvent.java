/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;


public interface IMouseEvent
{
//	/**
//	 * Initialize this object
//	*/
//	//public long initialize( TSGraphDisplay pDisp, int deviceX, int deviceY, int logicalX, int logicalY, int modKeys, int newTSMouseEventType );
//
//	/**
//	 * The TS TSGraphDisplay object
//	*/
//	//public TSGraphDisplay getGraphDisplay();
//
//	/**
//	 * The TS TSGraphDisplay object
//	*/
//	//public void setGraphDisplay( TSGraphDisplay value );
//
	/**
	 * The device x for this event, set via Initializ()
	*/
	public int getDeviceX();

	/**
	 * The device y for this event, set via Initializ()
	*/
	public int getDeviceY();

	/**
	 * The logical x for this event, set via Initializ()
	*/
	public int getLogicalX();

	/**
	 * The logical y for this event, set via Initializ()
	*/
	public int getLogicalY();

//	/**
//	 * The modkeys for this event
//	*/
//	public int getModKeys();
//
//	/**
//	 * The modkeys for this event
//	*/
//	public void setModKeys( int value );
//
//	/**
//	 * The TS MouseEventType enum
//	*/
//	public int getMouseEventType();
//
//	/**
//	 * The TS MouseEventType enum
//	*/
//	public void setMouseEventType( int value );
//
//	/**
//	 * The client x for this event (relative to node)
//	*/
//	public int getClientX();
//
	/**
	 * The client x for this event (relative to node)
	*/
	public void setClientX( int value );

//	/**
//	 * The client y for this event (relative to node)
//	*/
//	public int getClientY();

	/**
	 * The client y for this event (relative to node)
	*/
	public void setClientY( int value );

//	/**
//	 * The device rectangle during the mouse event
//	*/
//	public IETRect getDeviceRect();
//
//	/**
//	 * The device rectangle during the mouse event
//	*/
//	public void setDeviceRect( IETRect value );

}
