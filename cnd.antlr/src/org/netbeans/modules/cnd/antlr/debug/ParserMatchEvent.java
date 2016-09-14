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

public class ParserMatchEvent extends GuessingEvent {
	// NOTE: for a mismatch on type STRING, the "text" is used as the lookahead
	//       value.  Normally "value" is this
	public static int TOKEN=0;
	public static int BITSET=1;
	public static int CHAR=2;
	public static int CHAR_BITSET=3;
	public static int STRING=4;
	public static int CHAR_RANGE=5;
	private boolean inverse;
	private boolean matched;
	private Object target;
	private int value;
	private String text;


	public ParserMatchEvent(Object source) {
		super(source);
	}
	public ParserMatchEvent(Object source, int type,
	                        int value, Object target, String text, int guessing,
	                        boolean inverse, boolean matched) {
		super(source);
		setValues(type,value,target,text,guessing,inverse,matched);
	}
	public Object getTarget() {
		return target;
	}
	public String getText() {
		return text;
	}
	public int getValue() {
		return value;
	}
	public boolean isInverse() {
		return inverse;
	}
	public boolean isMatched() {
		return matched;
	}
	void setInverse(boolean inverse) {
		this.inverse = inverse;
	}
	void setMatched(boolean matched) {
		this.matched = matched;
	}
	void setTarget(Object target) {
		this.target = target;
	}
	void setText(String text) {
		this.text = text;
	}
	void setValue(int value) {
		this.value = value;
	}
	/** This should NOT be called from anyone other than ParserEventSupport! */
	void setValues(int type, int value, Object target, String text, int guessing, boolean inverse, boolean matched) {
		super.setValues(type, guessing);
		setValue(value);
		setTarget(target);
		setInverse(inverse);
		setMatched(matched);
		setText(text);
	}
	public String toString() {
		return "ParserMatchEvent [" + 
		       (isMatched()?"ok,":"bad,") +
		       (isInverse()?"NOT ":"") +
		       (getType()==TOKEN?"token,":"bitset,") +
		       getValue() + "," + getTarget() + "," + getGuessing() + "]";
	}
}
