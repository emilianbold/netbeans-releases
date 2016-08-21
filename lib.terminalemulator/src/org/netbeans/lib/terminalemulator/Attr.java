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
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 */

/*
 * "Attr.java"
 * Attr.java 1.7 01/07/23
 */

package org.netbeans.lib.terminalemulator;

@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
enum Attr {
    BGCOLOR(5),
    FGCOLOR(5),
    HIDDEN(1),
    REVERSE(1),
    BLINK(1),
    UNDERSCORE(1),
    BRIGHT(1),
    DIM(1),
    ACTIVE(1),

    // Since an attr value of 0 means render using default attributes
    // We need a value that signifies that no attribute has been set.
    // Can't use the highest (sign) bit since Java has no unsigned and
    // we get complaints from the compiler.
    UNSET(30, 1);

    public final static int ALT;

    private final int width;	// ... of field
    private final int fmask;	// ... corresponding to width

    private int offset;		// ... of field from the "left"
    private int wmask;		// ... over the word. 1 where bits are set.

    static {
	// System.out.printf("Attr.static()\n");
	for (Attr a : values())
	    a.init();
	ALT = Attr.BGCOLOR.wmask | Attr.FGCOLOR.wmask | Attr.REVERSE.wmask | Attr.ACTIVE.wmask;
    }

    /*
     * Explicitly set the offset.
     */
    private Attr(int offset, int width) {
	// System.out.printf("Attr(%d, %d) -> %d\n", offset, width, ordinal());
	this.offset = offset;
	this.width = width;
	this.fmask = (1<<width)-1;
    }

    /*
     * Automatically set the offset.
     */
    private Attr(int width) {
	// System.out.printf("Attr(%d) -> %d\n", width, ordinal());
	this.offset = -1;
	this.width = width;
	this.fmask = (1<<width)-1;
    }

    private void init() {
	if (this.offset == -1) {
	    if (ordinal() == 0) {
		this.offset = 0;
	    } else {
		Attr prev = Attr.values()[ordinal()-1];
		offset = prev.offset + prev.width;
	    }
	}
	wmask = fmask << offset;
	// System.out.printf("%s\n", this);
    }

    @Override
    public String toString() {
	return String.format("%d %10s(%2d, %2d, 0x%02x %8s, 0x%08x %32s)",
		             ordinal(), name(),
			     offset, width,
			     fmask, Integer.toBinaryString(fmask),
			     wmask, Integer.toBinaryString(wmask) );
    }

    public static String toString(int attr) {
	return String.format("%32s", Integer.toBinaryString(attr)).replace(" ", "0");

    }

    public final int get(int attr) {
	return (attr >> offset) & fmask;
    }

    /*
     * Use for setting a wide field.
     * Works for 1-bit field but set(int) is more efficient.
     */
    public final int set(int attr, int value) {
	// value &= fmask;	// throw all but lowest relevant bits away
	// attr &= ~ wmask;	// 0 out existing bits
	// attr |= value << offset;// set new value

	return (attr & ~wmask) | ((value & fmask) << offset);
    }

    /*
     * Use for setting a 1 bit field
     */
    public final int set(int attr) {
	assert width == 1;
	return attr | (1 << offset);
    }

    /*
     * Use for clearing any width field.
     */
    public final int clear(int attr) {
	return attr & ~wmask;
    }


    public final boolean isSet(int attr) {
	return (attr & wmask) == wmask;
    }

    /**
     * Use this to get at the FG color value embedded in an attr.
     */
    public static int foregroundColor(int attr) {
	return FGCOLOR.get(attr);
    } 

    /**
     * Use this to get at the BG color value embedded in an attr.
     */
    public static int backgroundColor(int attr) {
	return BGCOLOR.get(attr);
    }

    /*
     * Read-modify-write utility for setting bitfields in 'attr'.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public static int setAttribute(int attr, int value) {
	switch (value) {
	    case 0:
		// Reset all attributes
		attr = 0;
		break;
	    case 5:             // slow blink
	    case 6:             // fast blink
		// Attr.BLINK
		// FALLTHRU
	    case 1:
		attr = DIM.clear(attr);
		attr = BRIGHT.set(attr);
		break;
	    case 2:
                // Faint - not implemented
		attr = BRIGHT.clear(attr);
		attr = DIM.set(attr);
		break;
            case 3:
                // Italic - not supported
                break;
	    case 4:
		attr = UNDERSCORE.set(attr);
		break;
	    case 7:
		attr = REVERSE.set(attr);
		break;
	    case 8:
		attr = HIDDEN.set(attr);
		break;

	    case 9:
		// Term specific
		attr = ACTIVE.set(attr);
		break;

	    // turn individual attributes off (dtterm specific?)
	    case 25:
		// blinking off
		// FALLTHRU
	    case 22:
		attr = DIM.clear(attr);
		attr = BRIGHT.clear(attr);
		break;
	    case 24:
		attr = UNDERSCORE.clear(attr);
		break;
	    case 27:
		attr = REVERSE.clear(attr);
		break;
	    case 28:
		attr = HIDDEN.clear(attr);
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = FGCOLOR.set(attr, value-30+1);
		break;

	    case 39:
		// revert to default (dtterm specific)
		attr = FGCOLOR.clear(attr);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = BGCOLOR.set(attr, value-40+1);
		break;

	    case 49:
		// revert to default (dtterm specific)
		attr = BGCOLOR.clear(attr);
		break;

            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:
                // custom colors
		attr = FGCOLOR.set(attr, value-50+9);
		break;

	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:
	    case 66:
	    case 67:
		// custom colors
		attr = BGCOLOR.set(attr, value-60+9);
		break;

	    case 90:
	    case 91:
	    case 92:
	    case 93:
	    case 94:
	    case 95:
	    case 96:
	    case 97:
		// bright fg
		attr = FGCOLOR.set(attr, value-90+9);
		break;

	    case 100:
	    case 101:
	    case 102:
	    case 103:
	    case 104:
	    case 105:
	    case 106:
	    case 107:
		// bright bg
		attr = BGCOLOR.set(attr, value-100+9);
		break;

	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }

    /*
     * Read-modify-write utility for unsetting bitfields in 'attr'.
     * Note: this doesn't cover the unsetting operations which
     * setAttributes does.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public static int unsetAttribute(int attr, int value) {
	switch (value) {
	    case 0:
		// Reset all attributes
		attr = 0;
		break;
	    case 5:
		// Attr.BLINK
		// FALLTHRU
	    case 1:
		attr = BRIGHT.clear(attr);
		break;
	    case 2:
		attr = DIM.clear(attr);
		break;
	    case 4:
		attr = UNDERSCORE.clear(attr);
		break;
	    case 7:
		attr = REVERSE.clear(attr);
		break;
	    case 8:
		attr = HIDDEN.clear(attr);
		break;
	    case 9:
		attr = ACTIVE.clear(attr);
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = FGCOLOR.clear(attr);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = BGCOLOR.clear(attr);
		break;

            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:
		// custom colors
		attr = FGCOLOR.clear(attr);
		break;

	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:
	    case 66:
	    case 67:
		// custom colors
		attr = BGCOLOR.clear(attr);
		break;
                
	    case 90:
	    case 91:
	    case 92:
	    case 93:
	    case 94:
	    case 95:
	    case 96:
	    case 97:
		// bright fg
		attr = FGCOLOR.clear(attr);
		break;

	    case 100:
	    case 101:
	    case 102:
	    case 103:
	    case 104:
	    case 105:
	    case 106:
	    case 107:
		// bright bg
		attr = BGCOLOR.clear(attr);
		break;
	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }
}