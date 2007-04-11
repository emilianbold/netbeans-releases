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
 * "InterpDtTerm.java"
 * InterpDtTerm.java 1.2 01/07/23
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 */

package org.netbeans.lib.terminalemulator;


class InterpDtTerm extends InterpANSI {

    protected static class InterpTypeDtTerm extends InterpTypeANSI {
	protected final State st_esc_rb = new State("esc_rb");	// NOI18N
	protected final State st_esc_lb_q = new State("esc_lb_q");// NOI18N
	protected final State st_esc_lb_b = new State("esc_lb_b");// NOI18N
	protected final State st_wait = new State("wait");	// NOI18N

	protected final Actor act_DEC_private = new ACT_DEC_PRIVATE();
	protected final Actor act_M = new ACT_M();
	protected final Actor act_D = new ACT_D();
	protected final Actor act_done_collect = new ACT_DONE_COLLECT();
	protected final Actor act_collect = new ACT_COLLECT();
	protected final Actor act_start_collect = new ACT_START_COLLECT();


	protected InterpTypeDtTerm() {
	    st_esc.setAction(']', st_esc_rb, act_start_collect);

	    // the following two may be generic ANSI escapes
	    st_esc.setAction('D', st_base, act_D);
	    st_esc.setAction('M', st_base, act_M);

	    for (char c = 0; c < 128; c++)
		st_esc_rb.setAction(c, st_esc_rb, act_collect);
	    st_esc_rb.setAction((char) 27, st_wait, act_nop);

	    st_wait.setAction('\\', st_base, act_done_collect);

	    st_esc_lb.setAction('?', st_esc_lb_q, act_reset_number);

	    for (char c = '0'; c <= '9'; c++)
		st_esc_lb_q.setAction(c, st_esc_lb_q, act_remember_digit);
	    st_esc_lb_q.setAction('h', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('l', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('r', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('s', st_base, act_DEC_private);

	    st_esc_lb.setAction('!', st_esc_lb_b, act_reset_number);
	    st_esc_lb_b.setAction('p', st_base, new ACT_DEC_STR());
	}

	protected static final class ACT_START_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		InterpDtTerm i = (InterpDtTerm) ai;
		i.text = "";	// NOI18N
		return null;
	    }
	}

	protected static final class ACT_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		// java bug 4318526 text += c;
		InterpDtTerm i = (InterpDtTerm) ai;
		i.text = i.text + c;
		return null;
	    }
	}

	protected static final class ACT_DONE_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		/* DEBUG
		System.out.println("DtTerm emulation: got '" + text + "'");	// NOI18N
		*/
		return null;
	    }
	}

	protected static final class ACT_D implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_do(1);
		return null;
	    }
	};

	protected static final class ACT_M implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_up(1);
		return null;
	    }
	}

	protected static final class ACT_DEC_PRIVATE implements Actor {
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    return "act_DEC_private: no number";	// NOI18N
		int n = ai.numberAt(0);
		switch(c) {
		    case 'h':
			if (n == 5)
			    ai.ops.op_reverse(true);
			else if (n == 25)
			    ai.ops.op_cursor_visible(true);
			else 
			    return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
			break;
		    case 'l':
			if (n == 5)
			    ai.ops.op_reverse(false);
			else if (n == 25)
			    ai.ops.op_cursor_visible(false);
			else 
			    return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
			break;
		    case 'r':
		    case 's':
			/* DEBUG
			System.out.println("act_DEC_private " +	// NOI18N
			    numberAt(0) + " " + c);	// NOI18N
			*/
			break;
		    default:
			return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
		} 
		return null;
	    }
	}

	protected static final class ACT_DEC_STR implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_soft_reset();
		return null;
	    }
	}
    }

    private String text = null;

    private InterpTypeDtTerm type;

    public static final InterpTypeDtTerm type_singleton = new InterpTypeDtTerm();

    public InterpDtTerm(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpDtTerm(Ops ops, InterpTypeDtTerm type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    public String name() {
	return "dtterm";	// NOI18N
    } 

    public void reset() {
	super.reset();
	text = null;
    }


    private void setup() {
	state = type.st_base;
    }

}
