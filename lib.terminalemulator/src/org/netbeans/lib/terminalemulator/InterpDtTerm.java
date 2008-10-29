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
 * "InterpDtTerm.java"
 * InterpDtTerm.java 1.2 01/07/23
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 * 
 * See
 * http://h30097.www3.hp.com/docs/base_doc/DOCUMENTATION/V51_HTML/MAN/MAN5/0200____.HTM
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
	protected final Actor act_done_collect2 = new ACT_DONE_COLLECT2();
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

	    st_esc_rb.setAction((char) 7, st_base, act_done_collect2);	// BEL
            
	    st_esc_rb.setAction((char) 27, st_wait, act_nop);		// ESC
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

	static final class ACT_START_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		InterpDtTerm i = (InterpDtTerm) ai;
		i.text = "";	// NOI18N
		return null;
	    }
	}

	static final class ACT_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		// java bug 4318526 text += c;
		InterpDtTerm i = (InterpDtTerm) ai;
		i.text = i.text + c;
		return null;
	    }
	}

	static final class ACT_DONE_COLLECT implements Actor {
	    public String action(AbstractInterp ai, char c) {
		/* DEBUG
		System.out.println("DtTerm emulation: got '" + text + "'");	// NOI18N
		*/
		return null;
	    }
	}

	static final class ACT_DONE_COLLECT2 implements Actor {
	    public String action(AbstractInterp ai, char c) {
		InterpDtTerm i = (InterpDtTerm) ai;
		// OLD i.text = "";	// NOI18N
                int semix = i.text.indexOf(';');
                if (semix == -1)
                    return null;
                String p1 = i.text.substring(0, semix);
                String p2 = i.text.substring(semix+1);
		/* DEBUG
		System.out.println("DtTerm emulation done_collect2: got '" + p1 + "' '" + p2 + "'");	// NOI18N
		*/
                int code = Integer.parseInt(p1);
                switch (code) {
                    case 0:
                        ai.ops.op_icon_name(p2);
                        ai.ops.op_win_title(p2);
                        break;
                    case 1:
                        ai.ops.op_icon_name(p2);
                        break;
                    case 2:
                        ai.ops.op_win_title(p2);
                        break;
                    case 3:
                        ai.ops.op_cwd(p2);
                        break;

                    case 10: {
                        int semix2 = p2.indexOf(';');
                        if (semix == -1)
                            return null;
                        String p3 = p2.substring(semix2+1);
                        p2 = p2.substring(0, semix2);
                        ai.ops.op_hyperlink(p2, p3);
                    }
                }
		return null;
	    }
	}

        
	static final class ACT_D implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_do(1);
		return null;
	    }
	};

	static final class ACT_M implements Actor {
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_up(1);
		return null;
	    }
	}

	static final class ACT_DEC_PRIVATE implements Actor {
            
            // xterm Sequences to turn mouse reporting on and off are to be
            // implemeted here.
            // See http://www.xfree86.org/current/ctlseqs.html#Mouse%20Tracking
            
            private static String decPrivateSet(AbstractInterp ai, char c, int n) {
			if (n == 5)
			    ai.ops.op_reverse(true);
			else if (n == 25)
			    ai.ops.op_cursor_visible(true);
			else 
			    return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
                return null;
            }
            
            private static String decPrivateReset(AbstractInterp ai, char c, int n) {
			if (n == 5)
			    ai.ops.op_reverse(false);
			else if (n == 25)
			    ai.ops.op_cursor_visible(false);
			else 
			    return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
                return null;
            }
            
            private static String decPrivateSave(AbstractInterp ai, char c, int n) {
			return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
		} 
            
            
            private static String decPrivateRestore(AbstractInterp ai, char c, int n) {
                
                return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
            }
            
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    return "act_DEC_private: no number";	// NOI18N
		int n = ai.numberAt(0);
		switch(c) {
		    case 'h': return decPrivateSet(ai, c, n);
		    case 'l': return decPrivateReset(ai, c, n);
		    case 'r': return decPrivateRestore(ai, c, n);
		    case 's': return decPrivateSave(ai, c, n);
		    default:  return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
		} 
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
