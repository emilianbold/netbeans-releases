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

/*
 * LineParserTest.java
 * JUnit based test
 *
 * Created on November 9, 2005, 2:09 PM
 */
package org.netbeans.mobility.antext.preprocessor;

import junit.framework.*;
import java.io.*;
import java.util.*; 
import org.netbeans.junit.NbTestCase;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.AbilitiesEvaluator;

/**
 *
 * @author bohemius + luky
 */
public class LineParserTest extends NbTestCase {
            
    static final String[] EXPR_DEFS={        
        "//#condition Nokia",        
        "//#if  MIDP==\"2.0\"",
        "//#if  MIDP>=\"2.0\"",
        "//#if  CLDC!=\"1.0\"",
        "//#if  CLDC>\"1.0\"",
        "//#if ScreenWidth < \"176\"",
        "//#if \"100\" <= ScreenWidth",
        "//#if ScreenWidth == 128 ^ Series60",
        "//#if (Series60 || Series40)",
        "//#if ScreenWidth>100",
        "//#if ScreenWidth>100 && ScreenHeight>208",
        "//#if ScreenWidth > 100 && ScreenWidth < 150",
        "//#if ScreenHeight == 208",
        "//#if ScreenWidth<=150 && ScreenHeight>=160",
        "//#if !(ScreenHeight == 208 || ScreenHeight == 160)",
        "//#if ScreenHeight @ \"208 160\" ",
        "//#if \"Text1\\\\\\'\\\"\\r\\n\\t\\f\\b\\u4567\" == \"Text2\\x\"",
        "//#ifdef Nokia",
        "/*#Nokia#*/",
        "//#ifndef Nokia",
        "/*#!Nokia#*/",
    };
    
    static final String[] CONFIG_DEFS={
        "Series60,Nokia,ScreenWidth=176,ScreenHeight=208,MMAPI",
        "Series40,Nokia,ScreenWidth=128,ScreenHeight=160,MMAPI",
        "Nokia7610,Series60,ScreenWidth=176,ScreenHeight=208,MMAPI,Bluetooth,MIDP=2.0,CLDC=1.0",
        "SE550i,ScreenWidth=176,ScreenHeight=220,MIDP=2.0,CLDC=1.1,Java3D,PIM,Bluetooth,WMA=2.0,MMAPI,WebServices,JTWI"
    };
    
    // Result table for testing expression evaluation using if
    static final boolean[][] TEST_EXPR_DEFS={        
        {true,true,false,false},
        {false,false,true,true},
        {false,false,true,true},
        {true,true,false,true},
        {false,false,false,true},
        {false,true,false,false},
        {true,true,true,true},
        {true,true,true,false},
        {true,true,true,false},
        {true,true,true,true},
        {false,false,false,true},
        {false,true,false,false},
        {true,false,true,false},
        {false,true,false,false},
        {false,false,false,true},
        {true,true,true,false},        
        {false,false,false,false},
        {true,true,false,false},
        {true,true,false,false},
        {false,false,true,true},
        {false,false,true,true},
    };
    
    private Map[] configurations;
    private String src="";
    
    public LineParserTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        configurations=new HashMap[CONFIG_DEFS.length];
        for (int i=0;i<CONFIG_DEFS.length;i++) {
            configurations[i]=new HashMap();
            StringTokenizer st=new StringTokenizer(CONFIG_DEFS[i],",");
            while (st.hasMoreElements()) {
                String ab=st.nextToken();
                int idx;
                if ((idx=ab.indexOf("="))!=-1)
                    configurations[i].put(ab.substring(0,idx),ab.substring(idx+1));
                else
                    configurations[i].put(ab,null);
            }
        }
        for (int j=0;j<EXPR_DEFS.length;j++) {
            src+=EXPR_DEFS[j];
            if (j!=EXPR_DEFS.length-1)
                src+="\n";
        }
    }
    
    protected void tearDown() throws Exception {
        configurations=null;
        System.gc();
    }
    
    static Test suite() {
        TestSuite suite = new TestSuite(LineParserTest.class);
        
        return suite;
    }

    public void testErrors()
    {
        String commands[]={
            "/*$ $*/",
            "//#udnefine",
            "//#undefine",
            "//#define", 
            "//#define X=",
            "/*# #*/",
            "//#if",
            "//#elif",
            
            
            "//#if \"xxx\" + ScreenWidth",
            "//#if 100 + ",
            "//#if (100  ",
            "//#if ScreenWidth + ",
            "//#if (ScreenWidth",
            "//#if 100)",
            "//#if + 100",
            "//#if ScreenWidth)",
            "//#if + ScreenWidth",
            
            "//#elif \"xxx\" + ScreenWidth",
            "//#elif 100 + ",
            "//#elif (100  ",
            "//#elif ScreenWidth + ",
            "//#elif (ScreenWidth",
            "//#elif 100)",
            "//#elif + 100",
            "//#elif ScreenWidth)",
            "//#elif + ScreenWidth",
            
            "/*#XXX",
            "/*#if \"xxx\" + ScreenWidth*/",
            "/*#if 100 + */",
            "/*#if (100  */",
            "/*#if ScreenWidth + */",
            "/*#if (ScreenWidth*/",
            "/*#if 100)*/",
            "/*#if + 100*/",
            "/*#if ScreenWidth)*/",
            "/*#if + ScreenWidth*/",
        };
        
        for (int i=0;i<commands.length;i++)
        {
            LineParser instance = new LineParser(new StringReader(commands[i]),new MapEvaluator(configurations[0]));;
            //Dirty track to rise OutOfBoundsException
            instance.statestk=new int[2];
            instance.valstk=new PPToken[2];
            //****************************************
            
            instance.yydebug=true;
            PPLine myPpLine=instance.nextLine();
            assertTrue(myPpLine.hasErrors()||myPpLine.getErrors().size()!=0);
        }
    }
    
    public void testElIf()
    {
        String commands[]={"//#if ScreenHeight ==  208 \n //#elif ScreenHeight==160 \n //#endif",
                           "//#ifdef Nokia \n //#elifdef SE550i \n //#endif",
                           "//#ifdef Series60 \n //#elifndef SE550i \n //#endif"};
        
        boolean expResults[][][]={
            {
                {true,false},
                {false,true},
                {true,false},
                {false,false},
                },
            {
                {true,false},
                {true,false},
                {false,false},
                {false,true},
            },
            {
                {true,true},
                {false,true},
                {true,true},
                {false,false},
            }
        };
        
        for (int i=0;i<commands.length;i++)
            for (int j=0;j<configurations.length;j++)
            {
                LineParser instance = new LineParser(new StringReader(commands[i]),new MapEvaluator(configurations[j]));
                PPLine myPpLine=instance.nextLine();
                boolean result=myPpLine.getValue();
                assertEquals(result,expResults[i][j][0]);
                myPpLine=instance.nextLine();
                result=myPpLine.getValue();
                assertEquals(result,expResults[i][j][1]);
            }
    }
    
    public void testDebug()
    {
        String iLines[]={"//#debug","//#debug debug","//#debug info","//#debug warn","//#debug error","//#debug fatal"};
        String mLines[]={"//#mdebug","//#mdebug debug","//#mdebug info","//#mdebug warn","//#mdebug error","//#mdebug fatal"};
        boolean expResults[][]={
            {true,true,true,true,true,true},
            {true,true,true,true,true,true},
            {false,false,true,true,true,true},
            {false,false,false,true,true,true},
            {false,false,false,false,true,true},
            {false,false,false,false,false,true},
        };
        
        for (int i=0;i<6;i++)
        {   
            //Set the correct debug level
            String level=iLines[i].length()<9?"\n":"//#define DebugLevel=\"" + iLines[i].substring(9)+"\"\n";
            for (int j=0;j<iLines.length;j++)
            {
                //Check #debug
                LineParser instance = new LineParser(new StringReader(level+iLines[j]),new MapEvaluator(configurations[0]));
                PPLine myPpLine=instance.nextLine();
                myPpLine=instance.nextLine();
                boolean result=myPpLine.getValue();
                assertEquals(result,expResults[i][j]);
                
                //Check #mdebug
                instance = new LineParser(new StringReader(level+mLines[j]+"\n//#enddebug"),new MapEvaluator(configurations[0]));
                myPpLine=instance.nextLine();
                myPpLine=instance.nextLine();
                result=myPpLine.getValue();
                assertEquals(result,expResults[i][j]);
                myPpLine=instance.nextLine();
            }
        }
    }
    
    
    public void testDefine()
    {
        String iLine="//#ifdef TEST";
        LineParser instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        PPLine myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        boolean result=myPpLine.getValue();
        assertFalse(result);

        iLine="//#define TEST \n //#ifdef TEST \n //#undefine TEST \n //#ifdef TEST";
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        //Check first ifdef
        assertTrue(result);
        
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        //Check second ifdef
        assertFalse(result);
        
        //Define boolean true
        iLine="//#define TEST=true \n //#ifdef TEST";
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
        
        //Define boolean false (undefined variable is equal to false)
        iLine="//#if TEST==false";
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
        
        //Define number
        iLine="//#define TEST=100 \n //#if TEST != 200 ";
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        //Check first ifdef
        
        //Define String
        iLine="//#define TEST=\"test\" \n //#if TEST == \"test\" ";
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
        
        //Define with variable assignment
        iLine="//#define TEST=ScreenWidth \n //#if TEST == 176" ;
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
        
        
        //Define oldstyle
        iLine="/*# Nokia #*/ \n /*$ Nokia $*/" ;
        instance = new LineParser(new StringReader(iLine),new MapEvaluator(configurations[0]));
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
        myPpLine=instance.nextLine();
        assertFalse(myPpLine.hasErrors());
        result=myPpLine.getValue();
        assertTrue(result);
    }
    
    public void testExpressionEvaluation() {
        for (int i=0;i<CONFIG_DEFS.length;i++) {
            LineParser instance = new LineParser(new StringReader(src),new MapEvaluator(configurations[i]));
            int j=0;
            while (instance.hasMoreLines()) {
                String expr="";
                PPLine myPpLine=instance.nextLine();
                assertFalse(myPpLine.hasErrors());
                boolean result=myPpLine.getValue();
                    
                for (Iterator it=myPpLine.getTokens().iterator();it.hasNext();)
                    expr+=((PPToken)it.next()).getText()+" ";
                
                if (!myPpLine.getErrors().isEmpty()) {
                    for (Iterator it=myPpLine.getErrors().iterator();it.hasNext();)
                        System.out.println("Error: "+it.next().toString());
                }
                System.out.println("Result: "+TEST_EXPR_DEFS[j][i]+"=="+result);
                assertEquals(TEST_EXPR_DEFS[j][i],result); 
                j++;
            }
        }      
    }
    
    private class MapEvaluator extends HashMap implements AbilitiesEvaluator {

        public MapEvaluator() {
            super();
        }
        
        MapEvaluator(Map m) {
            super(m);
        }
        
        public boolean isAbilityDefined(String abilityName) {
            return containsKey(abilityName);
        }

        public String getAbilityValue(String abilityName) {
            return (String)get(abilityName);
        }

        public void requestDefineAbility(String abilityName, String value) {
            if (!containsKey(abilityName)) put(abilityName, value);
        }
        
        public void requestUndefineAbility(String abilityName) {
            remove(abilityName);
        }

    }
            
}
