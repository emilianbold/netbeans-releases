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
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001-2004.
 * All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

package org.netbeans.lib.terminalemulator;

public abstract class AbstractInterp implements Interp {

    protected interface Actor {
	public String action(AbstractInterp interp, char c);
    }

    protected static class State {

	// some generic actors
	Actor act_error = new Actor() {
	    public String action(AbstractInterp ai, char c) {
		return "generic error";	// NOI18N
	    } 
	};

	public String name() {
	    return name;
	} 
	private String name;


	class Action {
	    public State new_state = null;
	    public Actor actor = act_error;
	};

	private Action action[] = new Action[128];
	private Action action_regular = new Action();

	public State(String name) {
	    this.name = name;
	    for (int i = 0; i < action.length; i++)
		action[i] = new Action();
	    action_regular.actor = null;
	    action_regular.new_state = null;
	}

	/*
	 * Specify the state action_regular will transition to.
	 */
	public void setRegular(State new_state, Actor actor) {
	    action_regular.actor = actor;
	    action_regular.new_state = new_state;
	} 

	public void setAction(char c, State new_state, Actor actor) {
	    if ((int) c > 127)
		return;
	    action[c].actor = actor;
	    action[c].new_state = new_state;
	}

	public Action getAction(char c) {
	    if ((int) c > 127)
		return action_regular;
	    return action[c];
	} 
    };

    // Why make these be public and not protected?
    // Someone might inherit from us in a package other than org.netbeans
    // and while the inherited Interp will see these if they are protected, 
    // the corresponding InterpType won't.

    /*
    */
    public Ops ops;
    public State state;	// current state

    /*
    protected Ops ops;
    protected State state;	// current state
    */

    protected AbstractInterp(Ops ops) {
	this.ops = ops;
    } 

    public void reset() {
	;
    } 

    /*
     * Management of number parsing
     */

    private static final int max_numbers = 5;
    private int numberx = 0;
    private String number[] = new String[max_numbers];

    protected void resetNumber() {
	for (int x = 0; x < max_numbers; x++) {
	    number[x] = "";
	}
	numberx = 0;
    }
    protected void remember_digit(char c) {
	number[numberx] += c;
    }
    protected boolean pushNumber() {
	numberx++;
	return (numberx < max_numbers);
    }
    protected boolean noNumber() {
	return number[0].equals("");	// NOI18N
    }
    protected int numberAt(int position) {
	if (position > numberx) {
	    return 1;
	}
	return Integer.parseInt(number[position]);
    }
    protected int nNumbers() {
	return numberx;
    }


} 
