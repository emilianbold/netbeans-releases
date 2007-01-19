/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2;

/** Mostly used acceptors
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AcceptorFactory {

    public static final Acceptor TRUE = new Fixed(true);

    public static final Acceptor FALSE = new Fixed(false);

    public static final Acceptor NL = new Char('\n');

    public static final Acceptor SPACE_NL = new TwoChar(' ', '\n');

    public static final Acceptor WHITESPACE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isWhitespace(ch);
          }
      };

    public static final Acceptor LETTER_DIGIT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isLetterOrDigit(ch);
          }
      };

    public static final Acceptor JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isJavaIdentifierPart(ch);
          }
      };

    public static final Acceptor NON_JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch);
          }
      };

    private static final class Fixed implements Acceptor {
	private boolean state;
	
	public Fixed(boolean state) {
	    this.state = state;
	}
        
	public final boolean accept(char ch) {
              return state;
	}
    }

    private static final class Char implements Acceptor {
	private char hit;
	
	public Char(char hit) {
	    this.hit = hit;
	}
        
	public final boolean accept(char ch) {
              return ch == hit;
	}
    }

    private static final class TwoChar implements Acceptor {
	private char hit1, hit2;
	
	public TwoChar(char hit1, char hit2) {
	    this.hit1 = hit1;
	    this.hit2 = hit2;
	}
        
	public final boolean accept(char ch) {
              return ch == hit1 || ch == hit2;
	}
    }
}
