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
 */



/*
 * Created on Apr 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.reintegration;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.util.RequestProcessor;
/**
 * @author avaneeshj
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class REOperationTestCase extends AbstractUMLTestCase
{
        
	HashMap m_Values = new HashMap();
    
    public void testREOperation()
    {
        String fileString = null;
        try 
        {
        	InputStream stream =  
                this.getClass().getResourceAsStream("REOperation.etd");
            byte[] b= new byte[stream.available()];
            stream.read(b);
            fileString = new String(b);
        }
        catch(Exception e)
        {
            e.printStackTrace(); 
        }
        
        Document document = XMLManip.loadXML(fileString);
        List fileList = document.selectNodes("//UML:Operation/UML:Element.ownedElement/UML:Interaction");
        ETSystem.out.println("  Number Of Interaction   :"+fileList.size());
        
        ETList<INamedElement> els  = project.getOwnedElementsByName("SQDmap");
        IClass clazz  = (IClass)els.get(0);
        
        ETList<IElement> elements = new ETArrayList<IElement>(); 
        Element projectEl = (Element) project.getNode();
        
        for (int i = 0; i < fileList.size(); i++) 
        {
            Element el = (Element)fileList.get(i);
            String attName =el.attributeValue("name") ;
            if(attName.equals("New Diagram"))
                continue;

            ETList<IOperation> ops = clazz.getOperationsByName(attName);
            IOperation op = ops != null && ops.size() > 0? ops.get(0) : null;
            if (op == null || op.getIsConstructor())
            	continue;
		
            integrator.reverseEngineerOperation(project, op);
       
            Element modelElement = (Element) 
                projectEl.selectSingleNode(
                        "//UML:Interaction[@name=\"" + attName+"\"]");
            boolean elementEqual = false; 
            if((el != null) && (modelElement != null))
                elementEqual = areElementsEqual(el,modelElement);
            else
            {
                 ETSystem.out.println("Element 1::"+el);   
                 ETSystem.out.println("Element 2::"+modelElement);
            }
            ETSystem.out.println("Status Value::"+elementEqual);
            assertTrue("Interaction comparison failed for '" + attName + "'", elementEqual);
		}
        
        
    }
        
    public boolean areElementsEqual(Element a, Element b)
    {
        try
        {
            if((a.getName().equals(b.getName())))
            {
                if(a.attributeCount() == b.attributeCount())
                {
                	Iterator iter1 = a.attributeIterator();
                    while (iter1.hasNext())
                    {
                    	Attribute atribute1 = (Attribute)iter1.next();
                     	Attribute atribute2 = b.attribute(atribute1.getName());
                        boolean flag1 = isAttributeEqual(atribute1,atribute2);
                        if(flag1 == false)
                        {
                            return false;
                        }    
                    }
                    List list1 = a.elements();
                    List list2 = b.elements();
                    if(list1.size() == list2.size())
                    {
                    	for (int i = 0; i < list1.size(); i++) 
                        {
                            boolean status = 
                            areElementsEqual( (Element)list1.get(i),(Element) list2.get(i)) ;
                            
                            if(status == false)
                                return false;
						}
                        return true;
                    }
                }
            }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            return false;
        }
        return false;
    }
    
    public boolean isAttributeEqual(Attribute a, Attribute b )
    {
        String attributeValue1 = a.getValue();
        String attributeValue2 = b.getValue();
        
        
        if(attributeValue1.startsWith("DCE.") && attributeValue2
                .startsWith("DCE."))
        {   
            
        	if(!m_Values.containsKey(attributeValue1))
            {    
        		m_Values.put(attributeValue1,attributeValue2);
                return true;
            }
            
            return attributeValue2.equals(m_Values.get(attributeValue1));
        }
        
        return attributeValue1.equals(attributeValue2);
    }
        
    private IStrings getFilesForTest() {
        writeFile("A.java", "public class A {\r\n" + 
                "   \r\n" + 
                "   private int value;\r\n" + 
                "   B myVar = new B();\r\n" + 
                "           \r\n" + 
                "   public A() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public A(int a) {\r\n" + 
                "       this.value = a; \r\n" + 
                "   }   \r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   public void methodA() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "    public void overrideMe() {\r\n" + 
                "       \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    public B methodA1() {\r\n" + 
                "        B b = new B();\r\n" + 
                "   return b;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "}\r\n" + 
                "");
        
        writeFile("B.java", "public class B {\r\n" + 
                "   public B() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void methodB() {\r\n" + 
                "       ETSystem.out.println(\"Hello from B\");\r\n" + 
                "   }\r\n" + 
                "}\r\n" + 
                "");
        
        
        writeFile("C.java","public class C {\r\n" + 
                "   public C() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void opC1() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void opC2() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void opC3() {\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "}");
        
        writeFile("SQDmap.java", "\r\n" + 
                "import java.io.IOException;\r\n" + 
                "import java.net.Socket;\r\n" + 
                "import java.math.*; //currently not used\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "public class SQDmap extends A {\r\n" + 
                "   \r\n" + 
                "   private A obj1;\r\n" + 
                "   private A obj2;\r\n" + 
                "       \r\n" + 
                "   private int x;int count;int count2;\r\n" + 
                "        static int classCount;\r\n" + 
                "   static int classCount2;\r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   void whileLoop() {\r\n" + 
                "        int counter = 0;A s = new A();\r\n" + 
                "        \r\n" + 
                "        while (counter < 4) {counter++;s.methodA();}\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void doWhileLoop() {\r\n" + 
                "        int counter = 0;\r\n" + 
                "        A s = new A();\r\n" + 
                "        \r\n" + 
                "        do {\r\n" + 
                "            s.methodA();\r\n" + 
                "            counter++; \r\n" + 
                "           \r\n" + 
                "        } while (counter < 4);     \r\n" + 
                "       \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    void simpleIf() {\r\n" + 
                "       \r\n" + 
                "       int num = 0;\r\n" + 
                "       A s = new A();\r\n" + 
                "       B t = new B();\r\n" + 
                "       \r\n" + 
                "        if (num < 100) {   \r\n" + 
                "            num = num + 2;     \r\n" + 
                "           s.methodA();\r\n" + 
                "           t.methodB();  \r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void simpleIfElse() {\r\n" + 
                "        A s = new A();\r\n" + 
                "        B t = new B();\r\n" + 
                "        String text = getPassword();\r\n" + 
                "           \r\n" + 
                "        if (text.equals(\"joe\")) {\r\n" + 
                "            s.methodA();\r\n" + 
                "            text = \"\";   \r\n" + 
                "        }\r\n" + 
                "        else {\r\n" + 
                "            t.methodB();\r\n" + 
                "            text = \"\";   \r\n" + 
                "        }      \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    public String getPassword() {\r\n" + 
                "       return null;\r\n" + 
                "    }  \r\n" + 
                "\r\n" + 
                "\r\n" + 
                "   public SQDmap() {\r\n" + 
                "       super(4);\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public SQDmap(int x) {\r\n" + 
                "       this(); \r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void createObject1() {\r\n" + 
                "       A obj1;\r\n" + 
                "       obj1 = new A();\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void createObject2() {\r\n" + 
                "        B obj1 = new B();  \r\n" + 
                "    } \r\n" + 
                "\r\n" + 
                " \r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   /**\r\n" + 
                "    * public void objDestruction() {\r\n" + 
                "    * AirConditioner air = null;\r\n" + 
                "    * AirConditioner air2 = null;\r\n" + 
                "    * \r\n" + 
                "    * air.turnOn();\r\n" + 
                "    * air2 = air;\r\n" + 
                "    * air = null;\r\n" + 
                "    * air2.turnOff();\r\n" + 
                "    * }\r\n" + 
                "    * //obj1:A member variable\r\n" + 
                "    * \r\n" + 
                "    * \r\n" + 
                "    *\r\n" + 
                "    **/\r\n" + 
                "   \r\n" + 
                "   public void simpleMethodCall() {\r\n" + 
                "       obj1.methodA();\r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    //obj1:A local variable\r\n" + 
                "    public void simpleMethodCall2() {\r\n" + 
                "        A obj1 = null;\r\n" + 
                "   // do something\r\n" + 
                "   // \r\n" + 
                "        obj1 = new A();\r\n" + 
                "        obj1.methodA();\r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    //obj2:A member variable\r\n" + 
                "    //obj3:B local variable\r\n" + 
                "    void simpleSequence1() {\r\n" + 
                "        B obj3 = new B();\r\n" + 
                "        obj2.methodA();\r\n" + 
                "        obj3.methodB();    \r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    //obj2:A local variable\r\n" + 
                "    //obj3:B local variable\r\n" + 
                "    void simpleSequence2() {\r\n" + 
                "        A obj2 = new A();\r\n" + 
                "        B obj3 = new B();\r\n" + 
                "        \r\n" + 
                "        obj2.methodA();\r\n" + 
                "        obj3.methodB();    \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void methodC() {\r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    public void methodD() {\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void simpleSequence3() {\r\n" + 
                "        methodC();\r\n" + 
                "        methodD(); \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void hide() {\r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    public void show() {\r\n" + 
                "    }\r\n" + 
                "    \r\n" + 
                "    void cmdButton4_Click() {\r\n" + 
                "        this.hide();\r\n" + 
                "        this.show();   \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    void forLoop() {\r\n" + 
                "        A s = new A();\r\n" + 
                "\r\n" + 
                "   for (int intCtr = 0; intCtr < 10; intCtr++) \r\n" + 
                "            s.methodA();           \r\n" + 
                "   \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void nestedforLoop() {\r\n" + 
                "       // FileSystemObject fso = new FileSystemObject();\r\n" + 
                "       C c = new C();\r\n" + 
                "        \r\n" + 
                "        int x = 0;\r\n" + 
                "        int y = 0;\r\n" + 
                "        int z = 0;\r\n" + 
                "        \r\n" + 
                "        for (x = 0; x < 2; x++) {\r\n" + 
                "            c.opC1();\r\n" + 
                "            for (y = 0; y < 3; y++)\r\n" + 
                "                c.opC2();\r\n" + 
                "                for (z = 0; z < 4; z++)\r\n" + 
                "                    c.opC3();     \r\n" + 
                "       \r\n" + 
                "        }      \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "   void nestedforLoop2() {\r\n" + 
                "       C c = new C();\r\n" + 
                "       int x = 0, y = 0, z = 0;\r\n" + 
                "\r\n" + 
                "       for (x = 0; x < 2; x++) {\r\n" + 
                "           c.opC1();\r\n" + 
                "      for (y = 0; y < 2; y++) {\r\n" + 
                "          c.opC2();\r\n" + 
                "          for (z = 0; z < 2; z++)\r\n" + 
                "                   c.opC3();              \r\n" + 
                "           }\r\n" + 
                "       }\r\n" + 
                "   }\r\n" + 
                "\r\n" + 
                "    \r\n" + 
                "    void whileLoop() {\r\n" + 
                "        int counter = 0;\r\n" + 
                "        A s = new A();\r\n" + 
                "        \r\n" + 
                "        while (counter < 4) {\r\n" + 
                "            counter++;\r\n" + 
                "            s.methodA();   \r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void doWhileLoop() {\r\n" + 
                "        int counter = 0;\r\n" + 
                "        A s = new A();\r\n" + 
                "        \r\n" + 
                "        do {\r\n" + 
                "            s.methodA();\r\n" + 
                "            counter++; \r\n" + 
                "           \r\n" + 
                "        } while (counter < 4);     \r\n" + 
                "       \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    void simpleIf() {\r\n" + 
                "       \r\n" + 
                "       int num = 0;\r\n" + 
                "       A s = new A();\r\n" + 
                "       B t = new B();\r\n" + 
                "       \r\n" + 
                "        if (num < 100) {   \r\n" + 
                "            num = num + 2;     \r\n" + 
                "           s.methodA();\r\n" + 
                "           t.methodB();  \r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void simpleIfElse() {\r\n" + 
                "        A s = new A();\r\n" + 
                "        B t = new B();\r\n" + 
                "        String text = getPassword();\r\n" + 
                "           \r\n" + 
                "        if (text.equals(\"joe\")) {\r\n" + 
                "            s.methodA();\r\n" + 
                "            text = \"\";   \r\n" + 
                "        }\r\n" + 
                "        else {\r\n" + 
                "            t.methodB();\r\n" + 
                "            text = \"\";   \r\n" + 
                "        }      \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    public String getPassword() {\r\n" + 
                "       return null;\r\n" + 
                "    }  \r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void nestedIf() {\r\n" + 
                "        A s = new A();\r\n" + 
                "        B t = new B();\r\n" + 
                "        C u = new C();\r\n" + 
                "        int intHours = 0;\r\n" + 
                "        int curOverTime = 0;\r\n" + 
                "        int sngRate = 2;\r\n" + 
                "        \r\n" + 
                "        if (intHours <= 40) {\r\n" + 
                "            curOverTime = 0;\r\n" + 
                "            s.methodA();\r\n" + 
                "        } else if (intHours <= 50) {\r\n" + 
                "              curOverTime = (intHours - 40) * 2 * sngRate;\r\n" + 
                "              t.methodB(); \r\n" + 
                "                   \r\n" + 
                "          }  else\r\n" + 
                "              u.opC1();            \r\n" + 
                "         \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    void simpleSelectCase() {\r\n" + 
                "        A s = new A();\r\n" + 
                "        B t = new B();\r\n" + 
                "        C u = new C(); \r\n" + 
                "        int number = 0;\r\n" + 
                "        \r\n" + 
                "        switch (number) {\r\n" + 
                "        \r\n" + 
                "            case 1:\r\n" + 
                "                ETSystem.out.println(\"1\");\r\n" + 
                "                s.methodA();\r\n" + 
                "                break;\r\n" + 
                "                \r\n" + 
                "            case 2: \r\n" + 
                "                ETSystem.out.println(\"2\");\r\n" + 
                "                t.methodB();\r\n" + 
                "                break;\r\n" + 
                "                \r\n" + 
                "            case 3:\r\n" + 
                "                ETSystem.out.println(\"3\"); \r\n" + 
                "                u.opC1();\r\n" + 
                "                break;\r\n" + 
                "                \r\n" + 
                "            default:\r\n" + 
                "                ETSystem.out.println(\"invalid\");\r\n" + 
                "                break;             \r\n" + 
                "                   \r\n" + 
                "        }\r\n" + 
                "   }\r\n" + 
                "\r\n" + 
                "   void throwAnException() throws IOException { \r\n" + 
                "\r\n" + 
                "      throw new IOException();\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   public void overrideMe() {\r\n" + 
                "       super.overrideMe(); \r\n" + 
                "   }\r\n" + 
                "\r\n" + 
                "    \r\n" + 
                "    void exceptHandling() {\r\n" + 
                "        try {\r\n" + 
                "            Socket s = new Socket(\"localhost\",1234);\r\n" + 
                "            ETSystem.out.println(\"Connected!\");\r\n" + 
                "        }\r\n" + 
                "        catch (IOException e) {\r\n" + 
                "            ETSystem.out.println(\"IOException: could not connect.\");\r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    void myInfiniteLoop()\r\n" + 
                "    {\r\n" + 
                "      for (;;)\r\n" + 
                "      {\r\n" + 
                "         ETSystem.out.println(\"Looping\");\r\n" + 
                "      }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    \r\n" + 
                "    void myBreakStatement()\r\n" + 
                "    {\r\n" + 
                "      int myCounter = 0;\r\n" + 
                "      while (myCounter < 10)\r\n" + 
                "      {\r\n" + 
                "        ETSystem.out.println(\"Looping\");\r\n" + 
                "        myCounter++;\r\n" + 
                "        if (myCounter == 5)\r\n" + 
                "        {\r\n" + 
                "            break;\r\n" + 
                "   }\r\n" + 
                "        else\r\n" + 
                "        {\r\n" + 
                "                  \r\n" + 
                "        }\r\n" + 
                "      }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    void myContinueStatement()\r\n" + 
                "    {\r\n" + 
                "      for ( int x = 0 ; x < 10 ; x++)\r\n" + 
                "      {\r\n" + 
                "         if(x == 5)\r\n" + 
                "         {\r\n" + 
                "            continue; \r\n" + 
                "         }\r\n" + 
                "            ETSystem.out.println(\"Looping\");\r\n" + 
                "      }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void myStaticCall()\r\n" + 
                "    {\r\n" + 
                "\r\n" + 
                "      int x = 16;\r\n" + 
                "      int y = 0;\r\n" + 
                "      int z;\r\n" + 
                "      z = Math.max(x,y);\r\n" + 
                "      \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void myTernaryOperator(boolean x) {\r\n" + 
                "        int b = 10;\r\n" + 
                "        int c = 5;\r\n" + 
                "               \r\n" + 
                "        int bigValue = x ? b : c;  \r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "   public A getObj1() {\r\n" + 
                "       return obj1;\r\n" + 
                "   }\r\n" + 
                "   public void setObj1(A val) {\r\n" + 
                "       this.obj1 = val;\r\n" + 
                "   }\r\n" + 
                "   public A getObj2() {\r\n" + 
                "       return obj2;\r\n" + 
                "   }\r\n" + 
                "   public void setObj2(A val) {\r\n" + 
                "       this.obj2 = val;\r\n" + 
                "   }\r\n" + 
                "\r\n" + 
                "       //member variable: int count\r\n" + 
                "       void criticalSection1()\r\n" + 
                "       {\r\n" + 
                "           synchronized (this)\r\n" + 
                "           {\r\n" + 
                "             count++;\r\n" + 
                "           }\r\n" + 
                "       }\r\n" + 
                "       //member variable: int classCount\r\n" + 
                "       static void criticalSection2()\r\n" + 
                "       {\r\n" + 
                "          try\r\n" + 
                "          {\r\n" + 
                "             synchronized (Class.forName(\"BumpTest\"))\r\n" + 
                "             {\r\n" + 
                "                classCount++;\r\n" + 
                "             }\r\n" + 
                "          }\r\n" + 
                "          catch (ClassNotFoundException e) \r\n" + 
                "          {\r\n" + 
                "          }\r\n" + 
                "       }\r\n" + 
                "\r\n" + 
                "       //Member variable: int count2\r\n" + 
                "       synchronized void opSynchronized1()\r\n" + 
                "       {\r\n" + 
                "          count2++;\r\n" + 
                "       }   \r\n" + 
                "\r\n" + 
                "       //Member variable: static int classCount2\r\n" + 
                "       static synchronized void classBump()\r\n" + 
                "       {\r\n" + 
                "          classCount2++;\r\n" + 
                "       }\r\n" + 
                "      \r\n" + 
                "       //Member variable: A obj1\r\n" + 
                "       public void nestedMethodInvocation1() {\r\n" + 
                "           obj1 = new A();\r\n" + 
                "           obj1.methodA1().methodB();\r\n" + 
                "       }\r\n" + 
                "       \r\n" + 
                "       //Member variable: A obj1\r\n" + 
                "       public void nestedMethodInvocation2() {\r\n" + 
                "           obj1 = new A();\r\n" + 
                "      obj1.myVar.methodB();\r\n" + 
                "       }\r\n" + 
                "        \r\n" + 
                "   public static void main(String[] arguments) {\r\n" + 
                "            SQDmap a = new SQDmap();\r\n" + 
                "       a.nestedMethodInvocation2();\r\n" + 
                "\r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "        public void sayHello() {\r\n" + 
                "            ETSystem.out.println(\"Hello\");   \r\n" + 
                "   }\r\n" + 
                "   \r\n" + 
                "   \r\n" + 
                "   /*   void handleException() {\r\n" + 
                "       try {\r\n" + 
                "           throwAnException(); \r\n" + 
                "       } catch (IOException e) {}\r\n" + 
                "    } */\r\n" + 
                "\r\n" + 
                "}\r\n");
        
        IStrings s = new Strings();
        s.add("A.java");
        s.add("B.java");
        s.add("C.java");
        s.add("SQDmap.java");
        
        return s;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REOperationTestCase.class);
    }
    
    private IUMLParsingIntegrator integrator;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
//        integrator = new UMLParsingIntegrator();
        IStrings s = getFilesForTest();

// conover - RE is a n NB task now, so can't call it directly anymore
//        integrator.setFiles(s);
//        integrator.reverseEngineer(project, false, false, false, false);

        ReverseEngineerTask reTask = new ReverseEngineerTask(
            project, s, false, false, false, true, null);

        reTask.run();
        
//        RequestProcessor processor = 
//            new RequestProcessor("uml/ReverseEngineer"); // NOI18N
//
//        processor.post(reTask);
    }
}
