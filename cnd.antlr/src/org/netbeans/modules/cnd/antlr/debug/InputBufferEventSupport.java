/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.debug;

import java.util.Vector;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;
import org.netbeans.modules.cnd.antlr.RecognitionException;

public class InputBufferEventSupport {
	private Object source;
	private Vector inputBufferListeners;
	private InputBufferEvent  inputBufferEvent;
	protected static final int CONSUME=0;
	protected static final int LA=1;
	protected static final int MARK=2;
	protected static final int REWIND=3;


	public InputBufferEventSupport(Object source) {
		inputBufferEvent = new InputBufferEvent(source);
		this.source = source;
	}
	public void addInputBufferListener(InputBufferListener l) {
		if (inputBufferListeners == null) inputBufferListeners = new Vector();
		inputBufferListeners.addElement(l);
	}
	public void fireConsume(char c) {
		inputBufferEvent.setValues(InputBufferEvent.CONSUME, c, 0);
		fireEvents(CONSUME, inputBufferListeners);		
	}
	public void fireEvent(int type, ListenerBase l) {
		switch(type) {
			case CONSUME: ((InputBufferListener)l).inputBufferConsume(inputBufferEvent); break;
			case LA:      ((InputBufferListener)l).inputBufferLA(inputBufferEvent); break;
			case MARK:    ((InputBufferListener)l).inputBufferMark(inputBufferEvent); break;
			case REWIND:  ((InputBufferListener)l).inputBufferRewind(inputBufferEvent); break;
			default:
				throw new IllegalArgumentException("bad type "+type+" for fireEvent()");
		}	
	}
	public void fireEvents(int type, Vector listeners) {
		Vector targets=null;
		ListenerBase l=null;
		
		synchronized (this) {
			if (listeners == null) return;
			targets = (Vector)listeners.clone();
		}
		
		if (targets != null)
			for (int i = 0; i < targets.size(); i++) {
				l = (ListenerBase)targets.elementAt(i);
				fireEvent(type, l);
			}
	}
	public void fireLA(char c, int la) {
		inputBufferEvent.setValues(InputBufferEvent.LA, c, la);
		fireEvents(LA, inputBufferListeners);
	}
	public void fireMark(int pos) {
		inputBufferEvent.setValues(InputBufferEvent.MARK, ' ', pos);
		fireEvents(MARK, inputBufferListeners);
	}
	public void fireRewind(int pos) {
		inputBufferEvent.setValues(InputBufferEvent.REWIND, ' ', pos);
		fireEvents(REWIND, inputBufferListeners);
	}
	public Vector getInputBufferListeners() {
		return inputBufferListeners;
	}
	protected void refresh(Vector listeners) {
		Vector v;
		synchronized (listeners) {
			v = (Vector)listeners.clone();
		}
		if (v != null)
			for (int i = 0; i < v.size(); i++)
				((ListenerBase)v.elementAt(i)).refresh();
	}
	public void refreshListeners() {
		refresh(inputBufferListeners);
	}
	public void removeInputBufferListener(InputBufferListener l) {
		if (inputBufferListeners != null)
			inputBufferListeners.removeElement(l);
	}
}
