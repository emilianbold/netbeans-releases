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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.languages.neon;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class StateStack {

	public byte[] stack;
	private int lastIn = -1;

	/**
	 * Creates new StateStack
	 */
	public StateStack() {
		this(5);
	}

	public StateStack(int stackSize) {
		stack = new byte[stackSize];
		lastIn = -1;
	}

	public boolean isEmpty() {
		return lastIn == -1;
	}

	public int popStack() {
		int result = stack[lastIn];
		lastIn--;
		return result;
	}

	public void pushStack(int state) {
		lastIn++;
		if (lastIn == stack.length) {
			multiplySize();
        }
		stack[lastIn] = (byte) state;
	}

	private void multiplySize() {
		int length = stack.length;
		byte[] temp = new byte[length * 2];
		System.arraycopy(stack, 0, temp, 0, length);
		stack = temp;
	}

	public int clear() {
		return lastIn = -1;
	}

	public int size() {
		return lastIn + 1;
	}

	public StateStack createClone() {
		StateStack rv = new StateStack(this.size());
		rv.copyFrom(this);
		return rv;
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof StateStack)) {
			return false;
		}
		StateStack s2 = (StateStack) obj;
		if (this.lastIn != s2.lastIn) {
			return false;
		}
		for (int i = lastIn; i >= 0; i--) {
			if (this.stack[i] != s2.stack[i]) {
				return false;
			}
		}
		return true;
	}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + lastIn;
        for (int i = lastIn; i >= 0; i--) {
            hash = 31 * hash + this.stack[i];
        }
        return hash;
    }

	public void copyFrom(StateStack s) {
		while (s.lastIn >= this.stack.length) {
			this.multiplySize();
		}
		this.lastIn = s.lastIn;
		for (int i = 0; i <= s.lastIn; i++) {
			this.stack[i] = s.stack[i];
		}
	}

	public boolean contains(int state) {
		for (int i = 0; i <= lastIn; i++) {
			if (stack[i] == state) {
				return true;
			}
		}
		return false;
	}

	public int get(int index) {
		return stack[index];
	}

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		for (int i = 0; i <= lastIn; i++) {
			sb.append(" stack[").append(i).append("]= ").append(stack[i]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sb.toString();
	}

}
