/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

public class InterpANSI extends InterpDumb {

    private static class Ascii {
        public static final char ESC = 27;
        public static final char CTRL_N = 14;   // ASCII SO/ShiftOut
        public static final char CTRL_O = 15;   // ASCII SI/ShiftIn
    }
    
    protected static class InterpTypeANSI extends InterpTypeDumb {
	protected final State st_esc = new State("esc");	// NOI18N
	protected final State st_esc_lb = new State("esc_lb");	// NOI18N

	protected final Actor act_reset_number = new ACT_RESET_NUMBER();
	protected final Actor act_remember_digit = new ACT_REMEMBER_DIGIT();

	protected InterpTypeANSI() {
	    st_base.setAction((char) 27, st_esc, new ACT_TO_ESC());
	    st_base.setAction(Ascii.CTRL_N, st_base, new ACT_AS());
	    st_base.setAction(Ascii.CTRL_O, st_base, new ACT_AE());

	    st_esc.setRegular(st_esc, act_regular);
	    st_esc.setAction('7', st_base, new ACT_SC());
	    st_esc.setAction('8', st_base, new ACT_RC());
	    st_esc.setAction('c', st_base, new ACT_FULL_RESET());
	    st_esc.setAction('[', st_esc_lb, act_reset_number);


	    st_esc_lb.setRegular(st_esc_lb, act_regular);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_lb.setAction(c, st_esc_lb, act_remember_digit);
            
	    st_esc_lb.setAction(';', st_esc_lb, new ACT_PUSH_NUMBER());
	    st_esc_lb.setAction('A', st_base, new ACT_UP());
	    st_esc_lb.setAction('B', st_base, new ACT_DO());
	    st_esc_lb.setAction('C', st_base, new ACT_ND());
	    st_esc_lb.setAction('D', st_base, new ACT_BC());
	    st_esc_lb.setAction('H', st_base, new ACT_HO());
	    st_esc_lb.setAction('i', st_base, new ACT_PRINT());
	    st_esc_lb.setAction('J', st_base, new ACT_J());
	    st_esc_lb.setAction('K', st_base, new ACT_K());
	    st_esc_lb.setAction('L', st_base, new ACT_AL());
	    st_esc_lb.setAction('M', st_base, new ACT_DL());
	    st_esc_lb.setAction('m', st_base, new ACT_ATTR());
	    st_esc_lb.setAction('n', st_base, new ACT_DSR());
	    st_esc_lb.setAction('P', st_base, new ACT_DC());
	    st_esc_lb.setAction('h', st_base, new ACT_SM());
	    st_esc_lb.setAction('l', st_base, new ACT_RM());
	    st_esc_lb.setAction('r', st_base, new ACT_MARGIN());
	    st_esc_lb.setAction('t', st_base, new ACT_GLYPH());
	    st_esc_lb.setAction('@', st_base, new ACT_IC());
	}

	static final class ACT_TO_ESC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		InterpDumb i = (InterpDumb) ai;
		i.ctl_sequence = "";	// NOI18N
		return null;
	    }
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
	static final class ACT_FULL_RESET implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_full_reset();
		return null;
	    }
	}

	static class ACT_RESET_NUMBER implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.resetNumber();
		return null;
	    }
	};

	static final class ACT_REMEMBER_DIGIT implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.remember_digit(c);
		return null;
	    }
	};

	static final class ACT_PUSH_NUMBER implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (!ai.pushNumber())
		    return "ACT PUSH_NUMBER";	// NOI18N
		return null;
	    }
	}
	static final class ACT_UP implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_up(1);
		else
		    ai.ops.op_up(ai.numberAt(0));
		return null;
	    }
	}
	static final class ACT_DO implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_do(1);
		else
		    ai.ops.op_do(ai.numberAt(0));
		return null;
	    }
	}
	static final class ACT_ND implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_nd(1);
		else
		    ai.ops.op_nd(ai.numberAt(0));
		return null;
	    }
	}
	static final class ACT_BC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_bc(1);
		else
		    ai.ops.op_bc(ai.numberAt(0));
		return null;
	    }
	}
	static final class ACT_MARGIN implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_margin(0, 0);
		else
		    ai.ops.op_margin(ai.numberAt(0), ai.numberAt(1));
		return null;
	    }
	}
	static final class ACT_DC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_dc(1);
		else
		    ai.ops.op_dc(ai.numberAt(0));
		return null;
	    }
	}

	static final class ACT_SM implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_set_mode(1);
		else
		    ai.ops.op_set_mode(ai.numberAt(0));
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

	static final class ACT_IC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_ic(1);
		else
		    ai.ops.op_ic(ai.numberAt(0));
		return null;
	    }
	}
	static final class ACT_DL implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_dl(1);
		} else {
		    ai.ops.op_dl(ai.numberAt(0));
		} 
		return null;
	    }
	}
	static final class ACT_HO implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_ho();
		} else {
		    ai.ops.op_cm(ai.numberAt(0), ai.numberAt(1));// row, col
		} 
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
	static final class ACT_J implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_cd();
		} else {
		    int count = ai.numberAt(0);
		    if (count == 1) {
			return "ACT J: count of 1 not supported";	// NOI18N
		    } else if (count == 2) {
			ai.ops.op_cl();
		    } 
		} 
		return null;
	    }
	}
	static final class ACT_K implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_ce();
		} else {
		    int count = ai.numberAt(0);
		    if (count == 1) {
			return "ACT K: count of 1 not supported";	// NOI18N
		    } else if (count == 2) {
			return "ACT K: count of 2 not supported";	// NOI18N
		    } 
		} 
		return null;
	    }
	}
        
	static final class ACT_AL implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_al(1);
		} else {
		    ai.ops.op_al(ai.numberAt(0));
		} 
		return null;
	    }
	}
        
        static final class ACT_AS implements Actor {
	    @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_as();
                return null;
            }
        }
        
        static final class ACT_AE implements Actor {
	    @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_ae();
                return null;
            }
        }

	static final class ACT_ATTR implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		// set graphics modes (bold, reverse video etc)
		if (ai.noNumber()) {
		    ai.ops.op_attr(0);	// reset everything
		} else {
		    for (int n = 0; n <= ai.nNumbers(); n++)
			ai.ops.op_attr(ai.numberAt(n));
		}
		return null;
	    }
	}

	static final class ACT_DSR implements Actor {
	    // Device Status Report
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_status_report(5);	// reset everything
		} else {
		    ai.ops.op_status_report(ai.numberAt(0));
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

    public static final InterpTypeANSI type_singleton = new InterpTypeANSI();

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
