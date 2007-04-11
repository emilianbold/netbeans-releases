/*
 *	The contents of this file are subject to the terms of the Common Development
 *	and Distribution License (the License). You may not use this file except in
 *	compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 *	or http://www.netbeans.org/cddl.txt.
 *	
 *	When distributing Covered Code, include this CDDL Header Notice in each file
 *	and include the License file at http://www.netbeans.org/cddl.txt.
 *	If applicable, add the following below the CDDL Header, with the fields
 *	enclosed by brackets [] replaced by your own identifying information:
 *	"Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is Terminal Emulator.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc..
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.
 * All Rights Reserved.
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

    protected static class InterpTypeANSI extends InterpTypeDumb {
	protected final State st_esc = new State("esc");	// NOI18N
	protected final State st_esc_lb = new State("esc_lb");	// NOI18N

	protected final Actor act_reset_number = new ACT_RESET_NUMBER();
	protected final Actor act_remember_digit = new ACT_REMEMBER_DIGIT();

	protected InterpTypeANSI() {
	    st_base.setAction((char) 27, st_esc, new ACT_TO_ESC());

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

	protected static final class ACT_TO_ESC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		InterpDumb i = (InterpDumb) ai;
		i.ctl_sequence = "";	// NOI18N
		return null;
	    }
	}

	protected static final class ACT_SC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_sc();
		return null;
	    }
	}
	protected static final class ACT_RC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_rc();
		return null;
	    }
	}
	protected static final class ACT_FULL_RESET implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_full_reset();
		return null;
	    }
	}

	protected static class ACT_RESET_NUMBER implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.resetNumber();
		return null;
	    }
	};

	protected static class ACT_REMEMBER_DIGIT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.remember_digit(c);
		return null;
	    }
	};

	protected static final class ACT_PUSH_NUMBER implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (!ai.pushNumber())
		    return "ACT PUSH_NUMBER";	// NOI18N
		return null;
	    }
	}
	protected static final class ACT_UP implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_up(1);
		else
		    ai.ops.op_up(ai.numberAt(0));
		return null;
	    }
	}
	protected static final class ACT_DO implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_do(1);
		else
		    ai.ops.op_do(ai.numberAt(0));
		return null;
	    }
	}
	protected static final class ACT_ND implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_nd(1);
		else
		    ai.ops.op_nd(ai.numberAt(0));
		return null;
	    }
	}
	protected static final class ACT_BC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_bc(1);
		else
		    ai.ops.op_bc(ai.numberAt(0));
		return null;
	    }
	}
	protected static final class ACT_MARGIN implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_margin(0, 0);
		else
		    ai.ops.op_margin(ai.numberAt(0), ai.numberAt(1));
		return null;
	    }
	}
	protected static final class ACT_DC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_dc(1);
		else
		    ai.ops.op_dc(ai.numberAt(0));
		return null;
	    }
	}

	protected static final class ACT_SM implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_set_mode(1);
		else
		    ai.ops.op_set_mode(ai.numberAt(0));
		return null;
	    }
	}

	protected static final class ACT_RM implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_reset_mode(1);
		else
		    ai.ops.op_reset_mode(ai.numberAt(0));
		return null;
	    }
	}

	protected static final class ACT_IC implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    ai.ops.op_ic(1);
		else
		    ai.ops.op_ic(ai.numberAt(0));
		return null;
	    }
	}
	protected static final class ACT_DL implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_dl(1);
		} else {
		    ai.ops.op_dl(ai.numberAt(0));
		} 
		return null;
	    }
	}
	protected static final class ACT_HO implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_ho();
		} else {
		    ai.ops.op_cm(ai.numberAt(0), ai.numberAt(1));// row, col
		} 
		return null;
	    }
	}
	protected static final class ACT_PRINT implements Actor {
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
	protected static final class ACT_J implements Actor {
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
	protected static final class ACT_K implements Actor {
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
	protected static final class ACT_AL implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_al(1);
		} else {
		    ai.ops.op_al(ai.numberAt(0));
		} 
		return null;
	    }
	}

	protected static final class ACT_ATTR implements Actor {
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

	protected static final class ACT_DSR implements Actor {
	    // Device Status Report
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    ai.ops.op_status_report(5);	// reset everything
		} else {
		    ai.ops.op_status_report(ai.numberAt(0));
		}
		return null;
	    }
	}

	protected static final class ACT_GLYPH implements Actor {
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

    public String name() {
	return "ansi";	// NOI18N
    } 

    public void reset() {
	super.reset();
    }

    private void setup() {
	state = type.st_base;
    }
}
