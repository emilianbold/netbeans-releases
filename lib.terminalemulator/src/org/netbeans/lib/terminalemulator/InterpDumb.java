/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

package org.netbeans.lib.terminalemulator;

import java.util.Stack;

/**
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 */
public class InterpDumb extends AbstractInterp {

    protected static class InterpTypeDumb {
	public final State st_base = new State("base");	// NOI18N

	protected final Actor act_nop = new ACT_NOP();
	protected final Actor act_pause = new ACT_PAUSE();
	protected final Actor act_err = new ACT_ERR();
	protected final Actor act_regular = new ACT_REGULAR();
	protected final Actor act_cr = new ACT_CR();
	protected final Actor act_lf = new ACT_LF();
	protected final Actor act_bs = new ACT_BS();
	protected final Actor act_tab = new ACT_TAB();
	protected final Actor act_beL = new ACT_BEL();

	protected InterpTypeDumb() {
	    st_base.setRegular(st_base, act_regular);

	    for (char c = 0; c < 128; c++)
		st_base.setAction(c, st_base, act_regular);

	    st_base.setAction((char) 0, st_base, act_pause);
	    st_base.setAction('\r', st_base, act_cr);
	    st_base.setAction('\n', st_base, act_lf);
	    st_base.setAction('\b', st_base, act_bs);
	    st_base.setAction('\t', st_base, act_tab);
	    st_base.setAction((char) 7, st_base, act_beL);
	}

	protected static final class ACT_NOP implements Actor {
	    public String action(AbstractInterp ai, char c) {
		return null;
	    }
	};

	protected static final class ACT_PAUSE implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_pause();
		return null;
	    }
	};

	protected static final class ACT_ERR implements Actor {
	    public String action(AbstractInterp ai, char c) {
		return "ACT ERROR";	// NOI18N
	    }
	};

	protected static final class ACT_REGULAR implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_char(c);
		return null;
	    }
	};


	protected static final class ACT_CR implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_carriage_return();
		return null;
	    }
	};

	protected static final class ACT_LF implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_line_feed();
		return null;
	    }
	};


	protected static final class ACT_BS implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_back_space();
		return null;
	    }
	};

	protected static final class ACT_TAB implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_tab();
		return null;
	    }
	};

	protected static final class ACT_BEL implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_bel();
		return null;
	    }
	}
    }

    /*
     * A stack for State
     */
    private Stack stack = new Stack();

    protected void push_state(State s) {
	stack.push(s);
    }

    protected State pop_state() {
	return (State) stack.pop();
    }

    protected void pop_all_states() {
	while(!stack.empty())
	    stack.pop();
    }


    protected String ctl_sequence = null;

    private InterpTypeDumb type;

    public static final InterpTypeDumb type_singleton = new InterpTypeDumb();

    public InterpDumb(Ops ops) {
	super(ops);
	this.type = type_singleton;
	setup();
	ctl_sequence = null;
    } 

    protected InterpDumb(Ops ops, InterpTypeDumb type) {
	super(ops);
	this.type = type;
	setup();
	ctl_sequence = null;
    } 

    public String name() {
	return "dumb";	// NOI18N
    } 

    public void reset() {
	super.reset();
	pop_all_states();
	state = type.st_base;
	ctl_sequence = null;
    }

    private void setup() {
	state = type.st_base;
    }


    private void reset_state_bad() {
        /*
	DEBUG
	System.out.println("Unrecognized sequence in state " + state.name());	// NOI18N
	if (ctl_sequence != null) {
	    for (int sx = 0; sx < ctl_sequence.length(); sx++)
		System.out.print(String.valueOf((int)ctl_sequence.charAt(sx)) +
		    " ");	// NOI18N
	    System.out.print("\n\t");	// NOI18N
	    for (int sx = 0; sx < ctl_sequence.length(); sx++)
		System.out.print(ctl_sequence.charAt(sx) + "  ");	// NOI18N

	    System.out.println();	// NOI18N
	}
        */
	reset();
    } 

    public void processChar(char c) {

	// If we're collecting stuff into a control sequence remember the char
	if (ctl_sequence != null)
	    ctl_sequence += c;

	try {
	    State.Action a = state.getAction(c);
	    /* DEBUG
	    if (a == null) {
		System.out.println("null action in state " + state.name() +	// NOI18N
		                   " for char " + c + " = " + (int) c);	// NOI18N
	    }
	    if (a.actor == null) {
		System.out.println("null a.actor in state " + state.name() +	// NOI18N
				   " for char " + c + " = " + (int) c);	// NOI18N
	    }
	    */
	    String err_str = a.actor.action(this, c);
	    if (err_str != null) {
		/* DEBUG
		System.out.println("action error: " + err_str);	// NOI18N
		*/
		reset_state_bad();
		return;
	    }

	    if (a.new_state != null)
		state = a.new_state;
	    else
		;	// must be set by action, usually using pop_state()

	} finally {
	    if (state == type.st_base)
		ctl_sequence = null;
	}
    }

    /*
     * Actions ..............................................................
     */

}
