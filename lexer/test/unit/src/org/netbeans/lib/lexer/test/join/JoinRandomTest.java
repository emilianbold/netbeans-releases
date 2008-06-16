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
 */

package org.netbeans.lib.lexer.test.join;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinSectionsTopTokenId;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test join updating algorithm TokenListUpdater.updateJoined() by random document modifications.
 *
 * @author mmetelka
 */
public class JoinRandomTest extends NbTestCase {

    public JoinRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    public PrintStream getLog() {
        return System.out;
//        return super.getLog();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();;
    }

    public void testRandom() throws Exception {
        test(1212495582649L);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed, this.getClass());
        randomModify.setLanguage(TestJoinSectionsTopTokenId.language());

//        randomModify.setDebugOperation(true);
//        randomModify.setDebugDocumentText(true);
//        randomModify.setDebugHierarchy(true);

        // Certain explicit cases that caused errors to be found follow:
        
        randomModify.insertText(0, "{x}<a>y");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "{x}<a>y"
        randomModify.insertText(3, "u");
        randomModify.clearDocument();


        randomModify.insertText(0, "E{r}Ia{x}>k}A}}e{}BIX{<<OBx}<}><c<}><{<}>n<<arz>i<xs" +
            "BMKhim{<gK>n><<<{s}F{Sx>}{{>{{DU{x}<a<a>zV{B}}u<a><k><><>jWFn<>}<iy}QlSt" + 
            "}{OOz<a<{Rh>v{<{}{{{pV}fH}<<g{{>}>r><a>><}<XG{<u<a>>{QT<<}Y><>r}x{p}>}Q{Lx}}" + 
            "n{QMn><<<<>>}yyj<<Enj}<a>u}rO>z{}>{}{{a>o<<a}yT{x}<>cwRQf{PF<zm>}>>}{}{{<TTo" +
            "<<}<Gw<>CaZ<>mBG>{K{<<a><X<}v<Kni}H><{}<>}}>>s}K}}LP>yxeGC>}}<BQ<><W}y{{}GUK" +
            "<fY>>X<lg>>}{vx}u}e{x}<>oB{r>{{<>>>{<BcL{x}>v<{y>k>{{W<<aVe{{i>w<>>S}{ey}}U}" +
            "{QVYBG<ta{{x}Y>Ms<>gQ<{}Yp{K}VD<a<{x}N}t<a<a>a>{rF{{S>PCfx}N}<>>n{<aj{TP}{hr" + 
            "><<{<gp<>{ib}<mMTs{x}}<a>{x}}{x{x<>i<<T>A<{A<lx>emo{}opO>Y}J><G><{QLIIa{Lto<" + 
            "YFsb}t<a>z}<><a><P<<KJYx}<}a>{x}<i<Q><w{{><>>}}<tQf<cr<{g>}{p{>}<<faMKT}<a>{" +
            "t{}>i<a><>OloX}}>Um<{x}a>bx{}}>K<}><>><}<{<a>R>>>{g>z{xhYs{}}ikbFV{ND<w{}}>a" +
            "i><{{x}Esx>}}Q>}{}>Fv>>{x}<<>}r}n<e<a><wmF>Y<>>BUQ}}jbG>lacFn<l}>}"
        );
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(612, ">");
        randomModify.clearDocument();


        randomModify.insertText(0, "Zl>J{O{c}<>}}TK>>}}n}jUs<>{x}}>>l<<a>psM{F<FN}E}vd{}}>>{{{>{{<}" +
                ">V}e>A<>y{Ns}I<T>}{{lT}vJ{oOD{lK{}OrDKb}i}<XS>vfJhtx}{r{{x}x}}f<C}g{}VuRQ<<a>}K{AdGQ<{}<ZYVS<}>vdHD<}");
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(22, "{");
        randomModify.clearDocument();


        randomModify.insertText(0, "a{<a>lm<xyz>c<u>d<>x}<");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "a{<a>lm<xyz>c<u>d<>x}<"
        randomModify.removeText(20, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}<}a>S<{x}JD<><a>{k>D>>}}ZxF}<}no>Q{}>z");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "<a>}<}a>S<{x}JD<><a>{k>D>>}}ZxF}<}no>Q{}>z"
        randomModify.insertText(8, "j");
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}s<}<NGFxT>a>jN{BN<<oHTS{x}JD<><a<a>B>xF}<}n<a>o<>x}Q{}>z");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "<a>}s<}<NGFxT>a>jN{BN<<oHTS{x}JD<><a<a>B>xF}<}n<a>o<>x}Q{}>z"
        randomModify.insertText(52, ">}enPM");
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}s<>sU}<TTG}<NGFxT>a>jS<{x}JD<><a>xF}<}n<a>o>{x}Q{}>z");
        //             00000000001111111111222222222233333333334444444444
        //             01234567890123456789012345678901234567890123456789
        //     text = "<a>}s<>sU}<TTG}<NGFxT>a>jS<{x}JD<><a>xF}<}n<a>o>{x}Q{}>z"
        randomModify.removeText(10, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, "a{b<{}<x>y{c}");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{b<{}<x>y{c}";
        randomModify.insertText(1, "<");
        randomModify.clearDocument();


        randomModify.insertText(0, ">F{WGCha<{}<E>J>R{a}K{g}{x}<}Y");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(1, "<");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "a{b<{}<x>y{c}");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{b<{}<x>y{c}";
        randomModify.insertText(3, "}");
        randomModify.clearDocument();


        randomModify.insertText(0, "ab{<x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(0, "c{><n>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "ab{<x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(0, "c{><n>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "a<b>c");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<b>c";
        randomModify.removeText(3, 1);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<bc";
        randomModify.clearDocument();


        // Attempt insert after cleared document
        randomModify.insertText(0, "<b>c");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<b>c";
        randomModify.removeText(0, 1);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "b>c";


        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("<a>", 0.2),
            FixedTextDescriptor.create("{x}", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.chars(new char[] { '<', '>', '{', '}'}, 0.3),
        };

        RandomTextProvider textProvider = new RandomTextProvider(regularChars, fixedTexts);
        
        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(20000, textProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0), // snapshots create/destroy
            }
        );

    }
    
}
