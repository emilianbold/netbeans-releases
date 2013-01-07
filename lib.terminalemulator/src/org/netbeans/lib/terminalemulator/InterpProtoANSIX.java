/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.lib.terminalemulator;

import org.netbeans.lib.terminalemulator.AbstractInterp.Actor;

/**
 * Stuff common to InterpANSI, InterpDtTerm and InterpXTerm but 
 * @author ivan
 */
public class InterpProtoANSIX extends InterpProtoANSI {

    protected static class InterpTypeProtoANSIX extends InterpTypeProtoANSI {
	protected final State st_wait = new State("wait");	// NOI18N

	protected final State st_esc_rb = new State("esc_rb");	// NOI18N
	protected final State st_esc_rb_N = new State("esc_rb_N");// NOI18N
	protected final State st_esc_lb_q = new State("esc_lb_q");// NOI18N
	protected final State st_esc_lb_b = new State("esc_lb_b");// NOI18N

	protected final Actor act_D = new ACT_D();

	protected final Actor act_start_collect = new ACT_START_COLLECT();
	protected final Actor act_collect = new ACT_COLLECT();
	protected final Actor act_done_collect_bel = new ACT_DONE_COLLECT_BEL();
	protected final Actor act_DEC_private = new ACT_DEC_PRIVATE();

	protected InterpTypeProtoANSIX() {
	    st_esc.setAction('D', st_base, act_D);

            // \ESC]%d;%s\BEL
	    st_esc.setAction(']', st_esc_rb, act_start_collect);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_rb.setAction(c, st_esc_rb_N, act_collect);
	    for (char c = 0; c < 128; c++)
		st_esc_rb_N.setAction(c, st_esc_rb_N, act_collect);
	    st_esc_rb_N.setAction((char) 7, st_base, act_done_collect_bel);// BEL

            // \ESC[?%dh
            // \ESC[?%dl
            // \ESC[?%dr
            // \ESC[?%ds
	    st_esc_lb.setAction('?', st_esc_lb_q, act_reset_number);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_lb_q.setAction(c, st_esc_lb_q, act_remember_digit);
	    st_esc_lb_q.setAction('h', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('l', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('r', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('s', st_base, act_DEC_private);

            // \ESC[!p
	    st_esc_lb.setAction('!', st_esc_lb_b, act_reset_number);
	    st_esc_lb_b.setAction('p', st_base, new ACT_DEC_STR());
        }

	static final class ACT_D implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_do(1);
		return null;
	    }
	};

	static final class ACT_START_COLLECT implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
		i.text = "";	// NOI18N
		return null;
	    }
	}

	static final class ACT_COLLECT implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		// java bug 4318526 text += c;
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
		i.text = i.text + c;
		return null;
	    }
	}

	static final class ACT_DONE_COLLECT_BEL implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
                int semix = i.text.indexOf(';');
                if (semix == -1)
                    return null;
                String p1 = i.text.substring(0, semix);
                String p2 = i.text.substring(semix+1);
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
                        // cwd is a dttermism.
                        // This will be inherited by InterpXTerm but it's really
                        // not supported by xterm.
                        ai.ops.op_cwd(p2);
                        break;

                    case 10: {
                        // This is specific to nbterm!
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
            
            @Override
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
            @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_soft_reset();
		return null;
	    }
	}
    }

    protected String text = null;

    private InterpTypeProtoANSIX type;

    private static final InterpTypeProtoANSIX type_singleton = new InterpTypeProtoANSIX();

    public InterpProtoANSIX(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpProtoANSIX(Ops ops, InterpTypeProtoANSIX type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    @Override
    public String name() {
	return "proto-ansi-x";	// NOI18N
    } 

    @Override
    public void reset() {
	super.reset();
	text = null;
    }

    private void setup() {
    }
}
