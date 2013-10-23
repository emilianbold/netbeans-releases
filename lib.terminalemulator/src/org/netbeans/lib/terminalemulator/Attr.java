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
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "Attr.java"
 * Attr.java 1.7 01/07/23
 */

package org.netbeans.lib.terminalemulator;

class Attr {
    // 'f' suffix for oFfset
    // 'w' suffix for Width
    // 'm' suffix for mask
    private final static int BGCOLORf = 0;
    private final static int BGCOLORw = 5;
    private final static int BGCOLORm = 0xf;
    @SuppressWarnings("PointlessBitwiseExpression")
    public final static int BGCOLOR = BGCOLORm << BGCOLORf;

    private final static int FGCOLORf = BGCOLORf + BGCOLORw;
    private final static int FGCOLORw = 5;
    private final static int FGCOLORm = 0xf;
    public final static int FGCOLOR = FGCOLORm << FGCOLORf;

    private final static int HIDDENf = FGCOLORf + FGCOLORw;
    private final static int HIDDENw = 1;
    public final static int HIDDEN = 0x1 << HIDDENf;

    private final static int REVERSEf = HIDDENf + HIDDENw;
    private final static int REVERSEw = 1;
    public final static int REVERSE = 0x1 << REVERSEf;

    private final static int BLINKf = REVERSEf + REVERSEw;
    private final static int BLINKw = 1;
    public final static int BLINK = 0x1 << BLINKf;

    private final static int UNDERSCOREf = BLINKf + BLINKw;
    private final static int UNDERSCOREw = 1;
    public final static int UNDERSCORE = 0x1 << UNDERSCOREf;

    private final static int BRIGHTf = UNDERSCOREf + UNDERSCOREw;
    private final static int BRIGHTw = 1;
    public final static int BRIGHT = 0x1 << BRIGHTf;

    private final static int DIMf = BRIGHTf + BRIGHTw;
    private final static int DIMw = 1;
    public final static int DIM = 0x1 << DIMf;

    private final static int ACTIVEf = DIMf + DIMw;
    private final static int ACTIVEw = 1;
    public final static int ACTIVE = 0x1 << ACTIVEf;

    // Since an attr value of 0 means render using default attributes
    // We need a value that signifies that no attribute has been set.
    // Can't use the highest (sign) bit since Java has no unsigned and
    // we get complaints from the compiler.
    public final static int UNSET = 0x40000000;



    /**
     * attr = Attr.setBackgroundColor(attr, 7);
     */

    @SuppressWarnings("PointlessBitwiseExpression")
    public static int setBackgroundColor(int attr, int code) {
	code &= BGCOLORm;	// throw all but lowest relevant bits away
	attr &= ~ BGCOLOR;	// 0 out existing bits
	attr |= code << BGCOLORf;
	return attr;
    } 


    /**
     * attr = Attr.setForegroundColor(attr, 7);
     */

    public static int setForegroundColor(int attr, int code) {
	code &= FGCOLORm;	// throw all but lowest relevant bits away
	attr &= ~ FGCOLOR;	// 0 out existing bits
	attr |= code << FGCOLORf;
	return attr;
    }

    /**
     * Use this to get at the FG color value embedded in an attr.
     */
    public static int foregroundColor(int attr) {
	return (attr >> FGCOLORf) & FGCOLORm;
    } 

    /**
     * Use this to get at the BG color value embedded in an attr.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public static int backgroundColor(int attr) {
	return (attr >> BGCOLORf) & BGCOLORm;
    }

    /*
     * Read-modify-write utility for setting bitfields in 'attr'.
     */
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
		attr &= ~ Attr.DIM;
		attr |= Attr.BRIGHT;
		break;
	    case 2:
		attr &= ~ Attr.BRIGHT;
		attr |= Attr.DIM;
		break;
            case 3:
                // Italic - not supported
                break;
	    case 4:
		attr |= Attr.UNDERSCORE;
		break;
	    case 7:
		attr |= Attr.REVERSE;
		break;
	    case 8:
		attr |= Attr.HIDDEN;
		break;

	    case 9:
		// Term specific
		attr |= Attr.ACTIVE;
		break;

	    // turn individual attributes off (dtterm specific?)
	    case 25:
		// blinking off
		// FALLTHRU
	    case 22:
		attr &= ~ Attr.DIM;
		attr &= ~ Attr.BRIGHT;
		break;
	    case 24:
		attr &= ~ Attr.UNDERSCORE;
		break;
	    case 27:
		attr &= ~ Attr.REVERSE;
		break;
	    case 28:
		attr &= ~ Attr.HIDDEN;
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = Attr.setForegroundColor(attr, value-30+1);
		break;

	    case 39:
		// revert to default (dtterm specific)
		attr = Attr.setForegroundColor(attr, 0);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = Attr.setBackgroundColor(attr, value-40+1);
		break;

	    case 49:
		// revert to default (dtterm specific)
		attr = Attr.setBackgroundColor(attr, 0);
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
		attr = Attr.setForegroundColor(attr, value-50+9);
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
		attr = Attr.setBackgroundColor(attr, value-60+9);
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
		attr &= ~ Attr.BRIGHT;
		break;
	    case 2:
		attr &= ~ Attr.DIM;
		break;
	    case 4:
		attr &= ~ Attr.UNDERSCORE;
		break;
	    case 7:
		attr &= ~ Attr.REVERSE;
		break;
	    case 8:
		attr &= ~ Attr.HIDDEN;
		break;
	    case 9:
		attr &= ~ Attr.ACTIVE;
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = Attr.setForegroundColor(attr, 0);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = Attr.setBackgroundColor(attr, 0);
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
		attr = Attr.setForegroundColor(attr, 0);
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
		attr = Attr.setBackgroundColor(attr, 0);
		break;
                
	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }
}
