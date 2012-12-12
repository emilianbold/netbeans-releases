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
 *			"Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "InterpANSI.java"
 * InterpANSI.java 1.6 01/07/30
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 */

package org.netbeans.lib.terminalemulator;

public class InterpANSI extends InterpProtoANSI {

    protected static class InterpTypeANSI extends InterpTypeProtoANSI {

	protected InterpTypeANSI() {
	    st_esc.setAction('7', st_base, new ACT_SC());
	    st_esc.setAction('8', st_base, new ACT_RC());

	    st_esc_lb.setAction('i', st_base, new ACT_PRINT());
	    st_esc_lb.setAction('l', st_base, new ACT_RM());
	    st_esc_lb.setAction('t', st_base, new ACT_GLYPH());
	}

	static final class ACT_SC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_sc();
		return null;
	    }
	}
	static final class ACT_RC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_rc();
		return null;
	    }
	}

	static final class ACT_RM implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_reset_mode(1);
		else
		    ai.ops.op_reset_mode(ai.numberAt(0));
		return null;
	    }
	}

	static final class ACT_PRINT implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		// Ignored for now, except for 'dump time'
		if (ai.noNumber()) {
		    // Print screen
		} else {
		    switch (ai.numberAt(0)) {
			case 1:	// Print Line
			case 4:	// Stop Print Log
			case 5:	// Start Print Log
			    break;
			case 10:
			    ai.ops.op_time(true);
			    break;
			case 11:
			    ai.ops.op_time(false);
			    break;
		    } 
		}
		return null;
	    }
	}
        
	static final class ACT_GLYPH implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    return "ACT GLYPH: missing number";	// NOI18N
		} else {
		    int p1 = ai.numberAt(0);
		    int p2 = ai.numberAt(1);
		    int p3 = ai.numberAt(2);
		    if (p1 == 22) {
			ai.ops.op_glyph(p2, p3);
		    } else {
			return "ACT GLYPH: op othger than 22 not supported";	// NOI18N
		    } 
		} 
		return null;
	    }
	}
    }

    private InterpTypeANSI type;

    private static final InterpTypeANSI type_singleton = new InterpTypeANSI();

    public InterpANSI(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpANSI(Ops ops, InterpTypeANSI type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    @Override
    public String name() {
	return "ansi";	// NOI18N
    } 

    @Override
    public void reset() {
	super.reset();
    }

    private void setup() {
	state = type.st_base;
    }
}
