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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample application used for testing the evaluator algorithm.
 * Testing is done in two parts. First we stop in main() method and all static
 * methods are tested. Then we stop in instanceMethod() and all instance methods
 * are tested.<p>
 * 
 * All methods starting with "test" are invoked automatically. It's expected that
 * they contain just a return statement, which follows an expression that is
 * to be tested. That expression is automatically extracted and it's evaluation
 * is compared to the actual returned value.
 * When a method has &lt;method_name&gt;_undo counterpart, it is called
 * both after the invocation of the expression and invocation of the method.
 * 
 * @author Martin Entlicher
 */
public class EvaluatorApp {

    private static int      ix = 74;
    private static float    fx = 10.0f;
    private static double   dx = 10.0;
    private static boolean  bx = true;
    private static short    sx = 10;
    private static char     cx = 'a';
    private static byte     btx = 127;
    
    public EvaluatorApp() {
    }

    /* **************************************************************************************************
        The following code must stay where it is, on same line numbers, else all unit tests will fail.
    ************************************************************************************************** */
    public static void main(String[] args) {
        EvaluatorApp app = new EvaluatorApp(); // BRKP 1
        app.instanceMethod();
    }

    private void instanceMethod() {
        int instance = 0; // BRKP 2
    }
    
    // TEST METHODS
    
    /* Literals:
     */
    
    public static boolean testBoolean1() {
        return true;
    }
    
    public static boolean testBoolean2() {
        return false;
    }
    
    public static byte testByte() {
        return (byte) 127;
    }
    
    public static char testChar1() {
        return 'a';
    }
    
    public static char testChar2() {
        return '\n';
    }
    
    public static char testChar3() {
        return '\r';
    }
    
    public static char testChar4() {
        return '\b';
    }
    
    public static char testChar5() {
        return '\t';
    }
    
    public static char testChar6() {
        return '\f';
    }
    
    public static char testChar7() {
        return '\\';
    }
    
    public static char testChar8() {
        return '\'';
    }
    
    public static char testChar9() {
        return '"';
    }
    
    public static char testChar10() {
        return '\u03a9';
    }
    
    public static char testChar11() {
        return '\uFFFF';
    }
    
    public static char testChar12() {
        return '\177';
    }
    
    public static short testShort() {
        return (short) 2345;
    }
        
    public static int testInt1() {
        return 3342345;
    }
        
    public static int testInt2() {
        return -334234230;
    }
        
    public static int testInt3() {
        return 0347;
    }
        
    public static int testInt4() {
        return 0xaef;
    }
        
    public static int testInt5() {
        return -0123;
    }
        
    public static int testInt6() {
        return -0x4AEFFF1;
    }
        
    public static int testInt7() {
        return -2147483648;
    }
        
    public static long testLong1() {
        return 3342345l;
    }
        
    public static long testLong2() {
        return 3674898763456478329l;
    }
        
    public static long testLong3() {
        return 3674898763456478329L;
    }
        
    public static long testLong4() {
        return 01233211234435456722112L;
    }
        
    public static long testLong5() {
        return 0x456ad56765el;
    }
        
    public static long testLong6() {
        return 0xF456AD56765EL;
    }
        
    public static long testLong7() {
        return 0xFFFFFFFFFFFFFFFFL;
    }
        
    public static float testFloat1() {
        return 1f;
    }
        
    public static float testFloat2() {
        return 1.321243554323456345676543f;
    }
        
    public static float testFloat3() {
        return -1123e-22f;
    }
        
    public static float testFloat4() {
        return 1344e30f;
    }
        
    public static float testFloat5() {
        return 132E30F;
    }
        
    public static float testFloat6() {
        return .234E-10f;
    }
        
    public static float testFloat7() {
        return 0x234Ep1f;
    }
        
    public static float testFloat8() {
        return 0xA34P-9f;
    }
        
    public static float testFloat9() {
        return 0xA.BP0f;
    }
        
    public static double testDouble1() {
        return 1d;
    }
        
    public static double testDouble2() {
        return 23.44444444432345432456453234586547483927364567382763456784392d;
    }
        
    public static double testDouble3() {
        return -1D;
    }
        
    public static double testDouble4() {
        return 123E303D;
    }
        
    public static double testDouble5() {
        return -.1e-307d;
    }
        
    public static double testDouble6() {
        return 0xAP1D;
    }
        
    public static double testDouble7() {
        return 0xFFFFFFFFFFFFFFFFFFFEEEEEEEEEEEEEEFP1;
    }
        
    public static double testDouble8() {
        return 0xA.BP1;
    }
        
    public static double testDouble9() {
        return 0xABCDEF.FEDCBAP0D;
    }
        
    public static double testDouble10() {
        return -0xABCDEF.FEDCBAP0D;
    }
        
    public static String testString1() {
        return "asd";
    }
    
    public static String testString2() {
        return "\"";
    }
    
    public static String testString3() {
        return "\n";
    }
    
    public static String testString4() {
        return "\2345\n\r\t\\ \b\f\'\'\u5678\uffff\uFFFF";
    }
    
    public static Object testNull() {
        return null;
    }
    
    
    /* Operators. All Java operators are:
        >    <    !       ~       ?       :
        ==      <=   >=   !=      &&      ||      ++      --
        +       -       *       /       &   |       ^       %       <<        >>        >>>
        +=      -=      *=      /=      &=  |=      ^=      %=      <<=       >>=       >>>=
     */
    
    public static boolean testOp1() {
        return ix > fx;
    }

    public static boolean testOp2() {
        return 10 < 11;
    }

    public static boolean testOp3() {
        return !bx;
    }

    public static int testOp4() {
        return ~10;
    }

    public static int testOp5a() {
        return bx ? 1 : 0;
    }

    public static int testOp5b() {
        return !bx ? 1 : 0;
    }

    public static boolean testOp6a() {
        return 10 == ix;
    }

    public static boolean testOp6b() {
        return 10 == fx;
    }

    public static boolean testOp6c() {
        return "10" == "10";
    }

    public static boolean testOp6d() {
        return "10" == "11";
    }

    public static boolean testOp6e() {
        return System.in == System.in;
    }

    public static boolean testOp6f() {
        return System.err == System.out;
    }

    public static boolean testOp7() {
        return 10 >= ix;
    }

    public static boolean testOp8() {
        return 10 <= ix;
    }

    public static boolean testOp9a() {
        return 10 != ix;
    }

    public static boolean testOp9b() {
        return 10 != fx;
    }

    public static boolean testOp9c() {
        return "10" != "10";
    }

    public static boolean testOp9d() {
        return "10" != "11";
    }

    public static boolean testOp9e() {
        return System.in != System.in;
    }

    public static boolean testOp9f() {
        return System.err != System.out;
    }

    public static boolean testOp10a() {
        return bx && true;
    }

    public static boolean testOp10b() {
        return bx && false;
    }

    public static boolean testOp11a() {
        return bx || true;
    }

    public static boolean testOp11b() {
        return bx || false;
    }

    public static boolean testOp11c() {
        return !bx || false;
    }

    public static int testOp12a() {
        return ix++;
    }

    public static int testOp12a_undo() {
        return ix--;
    }

    public static int testOp12b() {
        return ++ix;
    }

    public static int testOp12b_undo() {
        return --ix;
    }

    public static int testOp13a() {
        return ix--;
    }

    public static int testOp13a_undo() {
        return ix++;
    }

    public static int testOp13b() {
        return --ix;
    }

    public static int testOp13b_undo() {
        return ++ix;
    }

    public static int testOp14a() {
        return 5+6;
    }

    public static long testOp14b() {
        return 503211234500000000l+6043213l;
    }

    public static float testOp14c() {
        return 50.234f+6.043e5f;
    }

    public static double testOp14d() {
        return -50.234e148+6.043e150;
    }

    public static short testOp14e() {
        return (short) -50 + (short) 6043;
    }

    public static String testOp14f() {
        return "He" + "llo";
    }

    public static int testOp15a() {
        return 5-6;
    }

    public static double testOp15b() {
        return -5e200-6e200;
    }

    public static int testOp16a() {
        return 5*6;
    }

    public static int testOp16b() {
        return 0xF*0xABCD;
    }

    public static double testOp16c() {
        return 0xFP10*0xABCD.FFAP-1;
    }

    public static int testOp17a() {
        return 5/6;
    }

    public static int testOp17b() {
        return 6/5;
    }

    public static int testOp17c() {
        return -6/5;
    }

    public static int testOp17d() {
        return -5/6;
    }

    public static double testOp17e() {
        return 5./6;
    }

    public static double testOp17f() {
        return 5.111111111111111111/(-6e2);
    }
    
    public static int testOp18a() {
        return 1234 & 54321;
    }
    
    public static int testOp18b() {
        return 0x50F23 & 0x51111;
    }
    
    public static boolean testOp18c() {
        return true & false;
    }
    
    public static long testOp19() {
        return 12345432346L | 95432354654321l;
    }
    
    public static long testOp20() {
        return 12345432346L ^ 95432354654321l;
    }
    
    public static long testOp21() {
        return 12345432343642234L % 2345l;
    }
    
    public static long testOp22() {
        return 12345432343642234L >> 10;
    }
    
    public static long testOp23() {
        return 1234543234364223400L << 18;
    }
    
    public static long testOp24() {
        return 12345432343642231L >>> 24;
    }
    
    //    +=      -=      *=      /=      &=  |=      ^=      %=      <<=       >>=       >>>=
    
    public static long testOp25() {
        return ix += 10;
    }
    
    public static long testOp25_undo() {
        return ix -= 10;
    }
    
    public static long testOp26() {
        return ix -= 10;
    }
    
    public static long testOp26_undo() {
        return ix += 10;
    }
    
    public static long testOp27() {
        return ix *= 10;
    }
    
    public static long testOp27_undo() {
        return ix /= 10;
    }
    
    
    
    // Test operand priorities

}
