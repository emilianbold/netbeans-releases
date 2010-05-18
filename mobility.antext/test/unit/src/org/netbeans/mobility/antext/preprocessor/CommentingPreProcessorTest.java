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
 * CommentingPreProcessorTest.java
 * JUnit based test
 *
 * Created on 08 March 2006, 17:28
 */
package org.netbeans.mobility.antext.preprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.*;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Lukas Waldmann
 */
public class CommentingPreProcessorTest extends NbTestCase
{
    static final String[] CONFIG_DEFS={
        "Series60,Nokia,ScreenWidth=176,ScreenHeight=208,MMAPI,Path=\\\\",
        "Series40,Nokia,ScreenWidth=128,ScreenHeight=160,MMAPI",
        "Nokia7610,Series60,ScreenWidth=176,ScreenHeight=208,MMAPI,Bluetooth,MIDP=2.0,CLDC=1.0",
        "SE550i,ScreenWidth=176,ScreenHeight=220,MIDP=2.0,CLDC=1.1,Java3D,PIM,Bluetooth,WMA=2.0,MMAPI,WebServices,JTWI"
    };
    
    static String commands[]={"//#if ScreenHeight ==  208 \n test1 \n //#elif ScreenHeight==160 \n test2 \n//#endif \n//#endif",
                              "//#ifdef Nokia \n test3 \n //#elifdef SE550i \n test4 \n //#endif",
                              "//#ifdef Series60 \n test5 \n//-- just a comment\n//#elifndef SE550i \n test6 \n //#endif",
                              "//#mdebug debug \n//# test7 \n//#enddebug\n//#enddebug",
                              "/*#Nokia#*/\n test8 \n /*$Noka$*/\n /*$Noka$*/",
                              "//#define MIDP_NEW = MIDP",
                              "//#undefine MIDP_NEW\n",
                              "//#if Nokia"};
    
    static int blkchk[][][][]={ 
        {       //st_line,end_line,valid,footer,isActive
            {  
                {1,2,1,0,1},
                {3,5,1,1,0}        
            },
            {
                {1,2,1,0,0},
                {3,5,1,1,1}         
            },
            {
                {1,2,1,0,1},
                {3,5,1,1,0}         
            },
            {
                {1,2,1,0,0},
                {3,5,1,1,0}         
            }                    
        },
        {
            {
                {1,2,1,0,1},
                {3,5,1,1,0}        
            },
            {
                {1,2,1,0,1},
                {3,5,1,1,0}        
            },
            {
                {1,2,1,0,0},
                {3,5,1,1,0}        
            },
            {
                {1,2,1,0,0},
                {3,5,1,1,1}        
            }
        },
        {
            {
                {1,3,1,0,1},
                {4,6,1,1,0}        
            },
            {
                {1,3,1,0,0},
                {4,6,1,1,1}        
            },
            {
                {1,3,1,0,1},
                {4,6,1,1,0}        
            },
            {
                {1,3,1,0,0},
                {4,6,1,1,0}        
            }        
        },
        {
            {
                {1,3,1,1,1}
            },
            {
                {1,3,1,1,1}
            },
            {
                {1,3,1,1,1}
            },
            {
                {1,3,1,1,1}
            }
        },
        {
            {
                {1,3,1,1,1}
            },
            {
                {1,3,1,1,1}
            },
            {
                {1,3,1,1,0}
            },
            {
                {1,3,1,1,0}
            }
        },
        {{},{},{},{}},
        {{},{},{},{}},
        {
            {
                {1,0,1,0,1}
            },
            {
                {1,0,1,0,1}
            },
            {
                {1,0,1,0,0}
            },
            {
                {1,0,1,0,0}
            }
        }
    };

    static String comments[][]={
            {"//#if ScreenHeight ==  208 \n test1 \n //#elif ScreenHeight==160 \n//#  test2 \n//#endif \n//#endif",
             "//#if ScreenHeight ==  208 \n//#  test1 \n //#elif ScreenHeight==160 \n test2 \n//#endif \n//#endif",
             "//#if ScreenHeight ==  208 \n test1 \n //#elif ScreenHeight==160 \n//#  test2 \n//#endif \n//#endif",
             "//#if ScreenHeight ==  208 \n//#  test1 \n //#elif ScreenHeight==160 \n//#  test2 \n//#endif \n//#endif"},
            {"//#ifdef Nokia \n test3 \n //#elifdef SE550i \n//#  test4 \n //#endif",
             "//#ifdef Nokia \n test3 \n //#elifdef SE550i \n//#  test4 \n //#endif",
             "//#ifdef Nokia \n//#  test3 \n //#elifdef SE550i \n//#  test4 \n //#endif",
             "//#ifdef Nokia \n//#  test3 \n //#elifdef SE550i \n test4 \n //#endif"},        
            {"//#ifdef Series60 \n test5 \n just a comment\n//#elifndef SE550i \n//#  test6 \n //#endif",
             "//#ifdef Series60 \n//#  test5 \n//-- just a comment\n//#elifndef SE550i \n test6 \n //#endif",
             "//#ifdef Series60 \n test5 \n just a comment\n//#elifndef SE550i \n//#  test6 \n //#endif",
             "//#ifdef Series60 \n//#  test5 \n//-- just a comment\n//#elifndef SE550i \n//#  test6 \n //#endif"},
            {"//#mdebug debug \ntest7 \n//#enddebug\n//#enddebug",
             "//#mdebug debug \ntest7 \n//#enddebug\n//#enddebug",
             "//#mdebug debug \ntest7 \n//#enddebug\n//#enddebug",
             "//#mdebug debug \ntest7 \n//#enddebug\n//#enddebug"},        
            {"/*#Nokia#*/\n test8 \n /*$Noka$*/\n /*$Noka$*/",
             "/*#Nokia#*/\n test8 \n /*$Noka$*/\n /*$Noka$*/",
             "/*#Nokia#*/\n//#  test8 \n /*$Noka$*/\n /*$Noka$*/",
             "/*#Nokia#*/\n//#  test8 \n /*$Noka$*/\n /*$Noka$*/"},
            {"//#define MIDP_NEW = MIDP",
             "//#define MIDP_NEW = MIDP",
             "//#define MIDP_NEW = MIDP",
             "//#define MIDP_NEW = MIDP"},
            {"//#undefine MIDP_NEW\n",
             "//#undefine MIDP_NEW\n",
             "//#undefine MIDP_NEW\n",
             "//#undefine MIDP_NEW\n"},        
            {"//#if Nokia",
             "//#if Nokia",
             "//#if Nokia",
             "//#if Nokia"}        
    };
    
    static boolean results[][][]={
        {{true,false},{false,true},{true,false},{false,false}},
        {{true,false},{true,false},{false,false},{false,true}},
        {{true,true},{false,true},{true,true},{false,false}},
        {{true},{true},{true},{true}},
        {{true,false,false},{true,false,false},{false,false,false},{false,false,false}},
        {{},{},{},{}},
        {{},{},{},{}},
        {{true},{true},{false},{false}}         
    };
    
    
    public CommentingPreProcessorTest(String testName)
    {
        super(testName);
    }
    
    protected void setUp() throws Exception
    {
    }
    
    protected void tearDown() throws Exception
    {
    }
    
    
    
    static Test suite()
    {
        TestSuite suite = new TestSuite(CommentingPreProcessorTest.class);
        
        return suite;
    }
    
    
    public void testEncodeDecode()
    {
        for (int j=0;j<CONFIG_DEFS.length;j++)
        {
            final String abilities=CONFIG_DEFS[j];
            //Create simple checksum for abilities
            long  abhash=0;
            for (int i=0;i<abilities.length();i++) abhash+=abilities.charAt(i);
            
            Map     abils=CommentingPreProcessor.decodeAbilitiesMap(abilities);
            String result=CommentingPreProcessor.encodeAbilitiesMap(abils);
            
            //Create simple checksum for result
            long  reshash=0;
            for (int i=0;i<result.length();i++) reshash+=result.charAt(i);
            
            assertTrue(abhash==reshash);
            assertTrue(abilities.length()==result.length());
        }
    }
    
    public void testWithWriter()
    {
        System.out.println("WithWriter");
        
        for (int j=0;j<CONFIG_DEFS.length;j++)
        {
            final String abilities=CONFIG_DEFS[j];
        
            for (int i=0;i<commands.length;i++)
            {
                final StringWriter writer=new StringWriter();
                final String command=commands[i];

                CommentingPreProcessor preProcessor = new CommentingPreProcessor(
                        new CommentingPreProcessor.Source()
                        {
                            public Reader createReader() throws IOException
                            {
                                return new StringReader(command);
                            }
                        },
                        new CommentingPreProcessor.Destination()
                        {
                            public Writer createWriter(boolean validOutput) throws IOException
                            {
                                return writer;
                            }

                            public void doInsert(int line, String s) throws IOException
                            {
                            }

                            public void doRemove(int line, int column, int length) throws IOException
                            {
                            }
                        },
                        abilities);
                        preProcessor.run();
                        checkBlocks(preProcessor.getBlockList(),i,j);
                        checkLines(preProcessor.getLines(),i,j);
                        assertEquals(writer.getBuffer().toString(),comments[i][j]);
                        System.out.print(writer.getBuffer());
                        System.out.println("\n------------------------");
            }            
            System.out.println("pass "+j+" finished");
        }
    }
    
    
    
    public void testWithInsert()
    {
        System.out.println("WithInsert");
        
        for (int j=0;j<CONFIG_DEFS.length;j++)
        {
            final String abilities=CONFIG_DEFS[j];
        
            for (int i=0;i<commands.length;i++)
            {
                final StringWriter writer=new StringWriter();
                final String command=commands[i];

                CommentingPreProcessor preProcessor = new CommentingPreProcessor(
                        new CommentingPreProcessor.Source()
                        {
                            public Reader createReader() throws IOException
                            {
                                return new StringReader(command);
                            }
                        },
                        new CommentingPreProcessor.Destination()
                        {
                            public Writer createWriter(boolean validOutput) throws IOException
                            {
                                return null;
                            }

                            public void doInsert(int line, String s) throws IOException
                            {
                            }

                            public void doRemove(int line, int column, int length) throws IOException
                            {
                            }
                        },
                        abilities);
                        preProcessor.run();
                        
                        
                        checkLines(preProcessor.getLines(),i,j);
                        System.out.print(writer.getBuffer());
                        System.out.println("\n------------------------");
            }            
            System.out.println("pass "+j+" finished");
        }
    }
    
    public void testJustForCoverage() throws Exception
    {
        File fTest=File.createTempFile("PrepTest","test");
        FileWriter writer=new FileWriter(fTest);
        writer.write(commands[0]);
        CommentingPreProcessor.main(new String[]{fTest.getAbsolutePath()});
        writer.close();
        fTest.delete();
    }
    
    private void checkBlocks(ArrayList blocks,int i,int j)
    {   
        //Check the results
        assertEquals(blocks.size(),blkchk[i][j].length);
        for (int res=0;res<blocks.size();res++)
        {
            PPBlockInfo block=(PPBlockInfo)(blocks.get(res));
            assertEquals(block.getStartLine(),blkchk[i][j][res][0]);
            assertEquals(block.getEndLine(),blkchk[i][j][res][1]);
            assertEquals(block.isValid(),blkchk[i][j][res][2]==1);
            assertEquals(block.hasFooter(),blkchk[i][j][res][3]==1);
            assertEquals(block.isActive(),blkchk[i][j][res][4]==1);
        }
    }
    
    private void checkLines (ArrayList lines,int i, int j)
    {
        //Check the results
        int valnum=0;
        for (int res=0;res<lines.size();res++)
        {
            PPLine line=(PPLine)(lines.get(res));
            if (line.hasValue())
            try 
                {
                    assertEquals(line.getValue(),results[i][j][valnum++]);
                }
                catch (java.lang.ArrayIndexOutOfBoundsException ex)
                {
                    System.out.println(ex);
                }        }   
    }
        
}
