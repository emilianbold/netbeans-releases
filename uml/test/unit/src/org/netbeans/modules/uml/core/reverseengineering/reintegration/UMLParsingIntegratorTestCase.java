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


package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import java.io.File;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.util.ITaskFinishListener;
import org.openide.util.RequestProcessor;
/**
 */
public class UMLParsingIntegratorTestCase extends AbstractUMLTestCase
    implements ITaskFinishListener
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UMLParsingIntegratorTestCase.class);
    }
    
    private IUMLParsingIntegrator integrator;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        integrator = new UMLParsingIntegrator();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AbstractUMLTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // Now save, delete and reload the project
        product.save();
        
        product.getApplication().closeAllProjects(false);
        workspace.close(false);

        establishNamespaces();
    }
    
    public void testReverseEngineer()
    {
        IStrings files = getFilesForRE();
// conover - RE is a NB task now, so can't call it directly anymore
//        integrator.setFiles(s);
//        integrator.reverseEngineer(project, false, false, false, false);

        ReverseEngineerTask reTask = new ReverseEngineerTask(
            project, files, false, false, false, true, null);
        
//        System.out.println("files: " + files.toString());
//        System.out.println("project: " + project.toString());
  
        reTask.run();
        
        assertEquals(0, project.getOwnedElementsByName("lll").size());
        assertEquals(0, project.getOwnedElementsByName("mmm").size());
        assertEquals(0, project.getOwnedElementsByName("menu").size());
        
//        System.out.println("project element count: " + project.getElementCount());
//        System.out.println("project elements: " + project.getElements().toString());
//        System.out.println("project filename: " + project.getFileName());
//        System.out.println("project's owned element count: " + project.getOwnedElementCount());
//        System.out.println("project's owned elements: " + project.getOwnedElements().toString());
//        System.out.println("com's owned elements: " + project.getOwnedElementsByName("com").toString());

    	IPackage pkg = (IPackage)project.getOwnedElementsByName("com").get(0);
    	pkg = (IPackage)pkg.getOwnedElementsByName("xyz").get(0);
	pkg = (IPackage)pkg.getOwnedElementsByName("zee").get(0);
		
        ETList<INamedElement> els = pkg.getOwnedElementsByName("Xyz");
        assertEquals(1, els.size());
	assertEquals("Javadoc of sublime purity.", 
            els.get(0).getDocumentation().trim());

        IClass c = (IClass) els.get(0);
        assertEquals("Xyz", c.getName());
        
        els = pkg.getOwnedElementsByName("KingKongVsGodzilla");
        IClass other = (IClass) els.get(0);
        assertEquals("KingKongVsGodzilla", other.getName());
        
        ETList<IOperation> ops = c.getOperations();
        assertEquals(2, ops.size());
        assertEquals("test", ops.get(0).getName());
        assertEquals("getChar", ops.get(1).getName());

        els = pkg.getOwnedElementsByName("KingKongVsGodzilla");
        assertEquals(1, els.size());
        
        // [PR 494] Verify that inner classes have gone to the right places
        assertEquals(1, pkg.getOwnedElementsByName("Llama").size());
        INamedElement nel = pkg.getOwnedElementsByName("Llama").get(0);
        assertTrue(nel instanceof IClass);
        assertEquals(1, ((IClass) nel).getOwnedElementsByName("Lame").size());
    }
    
    public void testRETypes()
    {
        IStrings s = getFilesForTypeTest();
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

        IClass c = (IClass) project.getOwnedElementsByName("ClassA").get(0);
        IAttribute a = c.getAttributeByName("x");
        Node attrNode = a.getNode();
        assertFalse("int".equals( ((Element) attrNode).attributeValue("type") ));
        
        IOperation op = c.getOperationsByName("test").get(0);
        IParameter par = op.getReturnType();
        assertFalse("int".equals( 
                        ((Element) par.getNode()).attributeValue("type") ));
        
//        IOperation op = c.getOperationsByName("myTest").get(0);
//        
//        ETSystem.out.println("Operation XML: " + op.getNode().asXML());
    }

//    public void testCanOperationBeREed()
//    {
//        ETList<INamedElement> els = project.getOwnedElementsByName("Xyz");
//        IClass c = (IClass) els.get(0);
//        ETList<IOperation> ops = c.getOperations();
//        
//        ops.get(0).addSourceFile("Xyz.java");
//        ops.get(1).addSourceFile("Xyz.java");
//        assertTrue( integrator.canOperationBeREed(ops.get(0)) );
//        assertTrue( integrator.canOperationBeREed(ops.get(1)) );
//    }
//    
//    public void testReverseEngineerOperations()
//    {
//        ETList<INamedElement> els = project.getOwnedElementsByName("Xyz");
//        IClass c = (IClass) els.get(0);
//        ETList<IOperation> ops = c.getOperations();
//        
//        File xyz = new File("Xyz.java").getAbsoluteFile();
//        ops.get(0).addSourceFile(xyz.toString());
//        ops.get(1).addSourceFile(xyz.toString());
//        
//        els = project.getOwnedElementsByName("KingKongVsGodzilla");
//        IClass other = (IClass) els.get(0);
//        assertEquals("KingKongVsGodzilla", other.getName());
//        
//        ETList<IElement> opels = CollectionTranslator.translate(ops);
//        integrator.reverseEngineerOperations(other, opels);
//        
//        // This assertion fails (of course). Have no real idea what the code is
//        // supposed to do, so we'll stay with this for now.
//        // assertEquals(2, other.getOperations().size());
//    }
        
    /**
     * @return
     */
    private IStrings getFilesForRE() {
        writeFile("com/xyz/zee/Xyz.java", "package com.xyz.zee;\n" +
				"/** Javadoc of sublime purity. */ " +
				"public class Xyz { int x = 0, y = 1; " +
                "// \u968e\u5c64\u578b\u30ec\n" +
                "Llama.Lame x = new Llama.Lame();" +
                "int z = x + y;  \n/////\n" +
                "int[] zigzag = new int[30]; " +
                "char tantalum = getChar(); " +
                "public void test() { }  " +
                "public char getChar() { return 'a'; } " +
                "} " +
        "class KingKongVsGodzilla { } // Nightmare");
    
        writeFile("com/xyz/zee/Llama.java", "package com.xyz.zee;\n" +
                "// - false for boolean\r\n" + 
                "        // - zero for numeric types\r\n" + 
                "        // - zero for types octet, char, and wchar\r\n" + 
                "        // - the empty string for string and wstring\r\n" + 
                "        // - nil for object references\r\n" + 
                "        // - a type code with a TCKind value of tk_null for type codes\r\n" + 
                "        // - for Any values, an Any containing a type code with a TCKind value of tk_null\n" +
                "public class Llama { " +
                "   public static class Lame { }" +
                "}"
        );
        
        writeFile("com/xyz/zee/lll/Yyy.java", "package com.xyz.zee.lll;\n" +
                "public class Yyy { }"
        );
        writeFile("com/xyz/zee/lll/Zzz.java", "package com.xyz.zee.lll;\n" +
                "public class Zzz { }"
        );
        
        writeFile("com/xyz/zee/lll/IMalagant.java", "package com.xyz.zee.lll;\n" +
                "public interface IMalagant { }"
        );

        writeFile("com/xyz/zee/lll/IPod.java", "package com.xyz.zee.lll;\n" +
                "public interface IPod { }"
        );
        
        writeFile("com/xyz/zee/mmm/Aeon.java", "package com.xyz.zee.mmm;\n" +
                "public class Aeon { }"
        );
        writeFile("com/xyz/zee/mmm/Parasite.java", "package com.xyz.zee.mmm;\n" +
                "public class Parasite { }"
        );
        
        writeFile("com/xyz/zee/mmm/IParasite.java", "package com.xyz.zee.mmm;\n" +
                "public interface IParasite { }"
        );

        writeFile("com/xyz/zee/mmm/ISequoia.java", "package com.xyz.zee.mmm;\n" +
                "public interface ISequoia { }"
        );
        
        writeFile("com/embarcadero/integration/menu/GDDynamicMenuInvokerAction.java", "/*\r\n" + 
                " * GDDynamicMenuInvoker.java\r\n" + 
                " *\r\n" + 
                " * Created on May 7, 2001, 5:20 PM\r\n" + 
                " */\r\n" + 
                "\r\n" + 
                "package com.embarcadero.integration.menu;\r\n" + 
                "\r\n" + 
                "import java.awt.event.ActionEvent;\r\n" + 
                "import java.util.ArrayList;\r\n" + 
                "\r\n" + 
                "import javax.swing.AbstractAction;\r\n" + 
                "import javax.swing.Action;\r\n" + 
                "import javax.swing.JFrame;\r\n" + 
                "\r\n" + 
                "import com.embarcadero.integration.GDProSupport;\r\n" + 
                "import com.embarcadero.integration.Log;\r\n" + 
                "import com.embarcadero.integration.actions.GDAbstractAction;\r\n" + 
                "import org.netbeans.modules.uml.core.addinframework.IAddInButton;\r\n" + 
                "import org.netbeans.modules.uml.core.addinframework.IAddInButtonSupport;\r\n" + 
                "import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;\r\n" + 
                "import org.netbeans.modules.uml.core.support.umlutils.ETList;\r\n" + 
                "import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;\r\n" + 
                "import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;\r\n" + 
                "\r\n" + 
                "/**\r\n" + 
                " *\r\n" + 
                " * @author  tspiva\r\n" + 
                " * @version\r\n" + 
                " */\r\n" + 
                "public class GDDynamicMenuInvokerAction\r\n" + 
                "    extends AbstractAction {\r\n" + 
                "    private IAddInButton mExecutor = null;\r\n" + 
                "    private IAddInButtonSupport buttonSupport = null;\r\n" + 
                "    private JFrame mMainWnd = null;\r\n" + 
                "\r\n" + 
                "    public GDDynamicMenuInvokerAction(JFrame mainWindow, IAddInButton menu,\r\n" + 
                "                                      IAddInButtonSupport buttonSupport) {\r\n" + 
                "        super(menu.getName());\r\n" + 
                "        mMainWnd = mainWindow;\r\n" + 
                "        mExecutor = menu;\r\n" + 
                "        this.buttonSupport = buttonSupport;\r\n" + 
                "        try {\r\n" + 
                "            setName(menu.getName());\r\n" + 
                "        }\r\n" + 
                "        catch (Exception E) {\r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public GDDynamicMenuInvokerAction(IAddInButton menu) {\r\n" + 
                "        this(menu, \"\");\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public GDDynamicMenuInvokerAction(IAddInButton menu, String name) {\r\n" + 
                "        this(null, menu, \"\");\r\n" + 
                "\r\n" + 
                "        try {\r\n" + 
                "            setName(menu.getName());\r\n" + 
                "        }\r\n" + 
                "        catch (Exception E) {\r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public GDDynamicMenuInvokerAction(JFrame mainWindow, IAddInButton menu,\r\n" + 
                "                                      String name) {\r\n" + 
                "        setName(name);\r\n" + 
                "        setExecutingMenu(menu);\r\n" + 
                "        setMainWindow(mainWindow);\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void setMainWindow(JFrame mainWnd) {\r\n" + 
                "        mMainWnd = mainWnd;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public JFrame getMainWindow() {\r\n" + 
                "        return mMainWnd;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void setName(String name) {\r\n" + 
                "        String actionName = setMnemonic(name);\r\n" + 
                "        putValue(Action.NAME, removeAccelerator(actionName));\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public String getName() {\r\n" + 
                "        return (String) getValue(Action.NAME);\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public void setExecutingMenu(IAddInButton menu) {\r\n" + 
                "        mExecutor = menu;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public IAddInButton getExecutingMenu() {\r\n" + 
                "        return mExecutor;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public boolean isEnabled() {\r\n" + 
                "    /*\r\n" + 
                "         IAddInButton button = getExecutingMenu();\r\n" + 
                "         return button.getSensitive();\r\n" + 
                "         */\r\n" + 
                "\r\n" + 
                "        buttonSupport.update(mExecutor, getHWND(getMainWindow()));\r\n" + 
                "         boolean en = mExecutor.getSensitive();\r\n" + 
                "         if (en != super.isEnabled())\r\n" + 
                "             super.setEnabled(en);\r\n" + 
                "\r\n" + 
                "         return en;\r\n" + 
                "     }\r\n" + 
                "\r\n" + 
                "    public void actionPerformed(ActionEvent actionEvent) {\r\n" + 
                "//    if(mExecutor.getName().equalsIgnoreCase(\"&Web Report...\"))\r\n" + 
                "        String str = mExecutor.getName();\r\n" + 
                "        if (stripAmperstand(str).equalsIgnoreCase(\r\n" + 
                "                       GDProSupport.getString(\"AddIn.WebReport\"))) {\r\n" + 
                "            executeForWebReport();\r\n" + 
                "        }\r\n" + 
                "        else\r\n" + 
                "            buttonSupport.execute(mExecutor, getHWND(getMainWindow()));\r\n" + 
                "\r\n" + 
                "        Log.out(\"Name of the addin button: \" + str);\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected String stripAmperstand(String name) {\r\n" + 
                "        String retVal = name;\r\n" + 
                "        int pos = name.indexOf(\'&\');\r\n" + 
                "        if (pos >= 0) {\r\n" + 
                "            StringBuffer buf = new StringBuffer(name);\r\n" + 
                "            buf.deleteCharAt(pos);\r\n" + 
                "\r\n" + 
                "            retVal = buf.toString();\r\n" + 
                "        }\r\n" + 
                "        Log.out(\"After stripAmpersand, value = \" + retVal);\r\n" + 
                "        return retVal;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    private void executeForWebReport() {\r\n" + 
                "        Log.out(\"Executing Web report part 1\");\r\n" + 
                "        final ArrayList diagrams = new ArrayList();\r\n" + 
                "        GDProSupport gdpro = GDProSupport.getGDProSupport();\r\n" + 
                "        IProductDiagramManager man = gdpro.getDiagramManager();\r\n" + 
                "\r\n" + 
                "        ProxyDiagramManager diaMan = ProxyDiagramManager.instance();\r\n" + 
                "        if (diaMan != null && GDProSupport.getCurrentProject() != null) {\r\n" + 
                "            ETList<IProxyDiagram> dias = diaMan.getDiagramsInProject(\r\n" + 
                "                                GDProSupport.getCurrentProject());\r\n" + 
                "            if (dias != null) {\r\n" + 
                "                Log.out(\"No. of diagrams in the workspace = \" + dias.getCount());\r\n" + 
                "                for (int i = 0; i < dias.getCount(); i++) {\r\n" + 
                "                    IProxyDiagram dia = dias.item(i);\r\n" + 
                "                    if (!dia.isOpen()) {\r\n" + 
                "                        diagrams.add(dia);\r\n" + 
                "                        man.openDiagram2(dia, true, null);\r\n" + 
                "                    }\r\n" + 
                "                }\r\n" + 
                "            }\r\n" + 
                "\r\n" + 
                "            Runnable r = new Runnable() {\r\n" + 
                "                public void run() {\r\n" + 
                "                    Log.out(\"executeForWebReport: Executing addin \" +\r\n" + 
                "                            mExecutor.getName());\r\n" + 
                "                    buttonSupport.execute(mExecutor, getHWND(getMainWindow()));\r\n" + 
                "                    executeForWebReport2(diagrams);\r\n" + 
                "                }\r\n" + 
                "            };\r\n" + 
                "            GDProSupport.getGDProSupport().getEventQueue().queueRunnable(r);\r\n" + 
                "        }\r\n" + 
                "\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    private void executeForWebReport2(ArrayList openedDiags) {\r\n" + 
                "        try {\r\n" + 
                "            Log.out(\"Executing Web report part 2\");\r\n" + 
                "            GDProSupport gdpro = GDProSupport.getGDProSupport();\r\n" + 
                "            IProductDiagramManager man = gdpro.getDiagramManager();\r\n" + 
                "            if (man != null) {\r\n" + 
                "                if (openedDiags != null && !openedDiags.isEmpty()) {\r\n" + 
                "                    int nowOpen = openedDiags.size();\r\n" + 
                "                    Log.out(\"No of open diagrams = \" + nowOpen);\r\n" + 
                "                    //close all the diagrams which are open\r\n" + 
                "                    for (int k = 0; k < nowOpen; k++) {\r\n" + 
                "                        IProxyDiagram dia = (IProxyDiagram)openedDiags.get(k);\r\n" + 
                "                        Log.out(\"Going to close the diagram = \" +\r\n" + 
                "                                dia.getName());\r\n" + 
                "                        dia.getDiagram().save();\r\n" + 
                "                        man.closeDiagram3(dia);\r\n" + 
                "                    }\r\n" + 
                "                }\r\n" + 
                "            }\r\n" + 
                "        }\r\n" + 
                "        catch (Exception exp4) {\r\n" + 
                "            Log.stackTrace(exp4);\r\n" + 
                "        }\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected String setMnemonic(String name) {\r\n" + 
                "        String retVal = name;\r\n" + 
                "        int pos = retVal.indexOf(\'&\');\r\n" + 
                "        if (pos >= 0) {\r\n" + 
                "            retVal = setMnemonic(retVal, pos);\r\n" + 
                "        }\r\n" + 
                "\r\n" + 
                "        return retVal;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected String setMnemonic(String name, int pos) {\r\n" + 
                "        char mnemonic = name.charAt(pos + 1);\r\n" + 
                "\r\n" + 
                "        String retVal = name.substring(0, pos) + name.substring(pos + 1);\r\n" + 
                "        putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));\r\n" + 
                "\r\n" + 
                "        // JBuilder-specific code\r\n" + 
                "        putValue(GDAbstractAction.JB_MNEMONIC, new Character(mnemonic));\r\n" + 
                "\r\n" + 
                "        return retVal;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected String removeAccelerator(String name) {\r\n" + 
                "        String retVal = name;\r\n" + 
                "        int pos = retVal.indexOf(\'\\t\');\r\n" + 
                "        if (pos >= 0) {\r\n" + 
                "            retVal = removeAccelerator(retVal, pos);\r\n" + 
                "        }\r\n" + 
                "\r\n" + 
                "        return retVal;\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected String removeAccelerator(String name, int pos) {\r\n" + 
                "        return name.substring(0, pos);\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    protected int getHWND(Object source) {\r\n" + 
                "        return GDProSupport.getGDProSupport().getTopWindowHandle();\r\n" + 
                "    }\r\n" + 
                "\r\n" + 
                "    public String toString() {\r\n" + 
                "        return getName();\r\n" + 
                "    }\r\n" + 
                "}\r\n" + 
        "");
        
        writeFile("com/embarcadero/integration/menu/GDMenuInvokerAction.java", "/*\r\n" + 
                " * GDMenuInvokerAction.java\r\n" + 
                " *\r\n" + 
                " * Created on January 14, 2001, 2:13 PM\r\n" + 
                " */\r\n" + 
                "\r\n" + 
                "package com.embarcadero.integration.menu;\r\n" + 
                "\r\n" + 
                "import java.awt.event.*;\r\n" + 
                "import javax.swing.*;\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "/**\r\n" + 
                " *\r\n" + 
                " * @author  tspiva\r\n" + 
                " * @version\r\n" + 
                " */\r\n" + 
                "public class GDMenuInvokerAction extends AbstractAction\r\n" + 
                "{\r\n" + 
                "  //IGDMenu mExecutor = null;\r\n" + 
                "  String  mCommand  = \"\";\r\n" + 
                "\r\n" + 
                "  public void setCommand(String cmd)\r\n" + 
                "  {\r\n" + 
                "    mCommand = cmd;\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "  public String getCommand()\r\n" + 
                "  {\r\n" + 
                "    return mCommand;\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "  public void setName(String name)\r\n" + 
                "  {\r\n" + 
                "    putValue(Action.NAME, name);\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "  public void actionPerformed(ActionEvent actionEvent)\r\n" + 
                "  {\r\n" + 
                "    /*\r\n" + 
                "    if(getExecutingMenu() != null)\r\n" + 
                "    {\r\n" + 
                "      //getExecutingMenu().executeSubMenuItem(getCommand());\r\n" + 
                "    }\r\n" + 
                "    */\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "  /*\r\n" + 
                "  protected String setMnemonic(String name, int pos)\r\n" + 
                "  {\r\n" + 
                "    char mnemonic = name.charAt(pos + 1);\r\n" + 
                "\r\n" + 
                "    String retVal = name.substring(0, pos) + name.substring(pos + 1);\r\n" + 
                "    this.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));\r\n" + 
                "\r\n" + 
                "    return retVal;\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "  protected String setAccelerator(String name, int pos)\r\n" + 
                "  {\r\n" + 
                "    String accelerator = name.substring(pos + 1);\r\n" + 
                "\r\n" + 
                "    //accelerator = accelerator.replace(\'+\', \' \');\r\n" + 
                "    //putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(accelerator));\r\n" + 
                "    return name.substring(0, pos);\r\n" + 
                "  }\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "  protected KeyStroke parseAccelerator(String accel)\r\n" + 
                "  {\r\n" + 
                "   // int       modifiers = 0;\r\n" + 
                "   // Character key       = null;\r\n" + 
                "   // KeyStoke  retVal    = null;\r\n" + 
                "\r\n" + 
                "   // StringTokenizer tokenizer = new StringTokenizer(accel, \"+\");\r\n" + 
                "   // while(tokenizer.hasMoreTokens() == true)\r\n" + 
                "   // {\r\n" + 
                "   //   String token = tokenizer.nextToken();\r\n" + 
                "   //   if(isModifier(token) == true)\r\n" + 
                "   //     setModifier(token, modifiers);\r\n" + 
                "   //   if(isCharacter(token) == true)\r\n" + 
                "   //     setCharacter(token, key);\r\n" + 
                "   // }\r\n" + 
                "\r\n" + 
                "   // if(key != null)\r\n" + 
                "   //   retVal = KeyStroke.getKeyStroke(key, modifiers);\r\n" + 
                "\r\n" + 
                "   // return retVal;\r\n" + 
                "\r\n" + 
                "\r\n" + 
                "    return null;\r\n" + 
                "  }\r\n" + 
                "   */\r\n" + 
                "}\r\n" + 
        "");
        
        IStrings s = new Strings();
	s.add("com/xyz/zee/Llama.java");
        s.add("com/xyz/zee/Xyz.java");
        s.add("com/xyz/zee/mmm/ISequoia.java");
        s.add("com/xyz/zee/mmm/IParasite.java");
        s.add("com/xyz/zee/lll/IPod.java");
        s.add("com/xyz/zee/lll/IMalagant.java");
        s.add("com/xyz/zee/lll/Yyy.java");
        s.add("com/xyz/zee/lll/Zzz.java");
        s.add("com/xyz/zee/mmm/Aeon.java");
        s.add("com/xyz/zee/mmm/Parasite.java");
        s.add("com/embarcadero/integration/menu/GDDynamicMenuInvokerAction.java");
        s.add("com/embarcadero/integration/menu/GDMenuInvokerAction.java");
        
        return s;
    }
    
    /**
     * @return
     */
    private IStrings getFilesForTypeTest()
    {
        writeFile("ClassA.java",
                "public class ClassA\r\n" + 
                "{ private int x; " +
                " public int test() { return 0; } " +
                "}");
        
        IStrings s = new Strings();
        s.add("ClassA.java");
        return s;
    }
    
    private void findJavaFiles(File dir, IStrings s)
    {
        if (dir == null) return;
        
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i)
        {
        	File f = files[i];
            if (f.isDirectory())
                findJavaFiles(f, s);
			else if (f.getName().endsWith(".java"))
                s.add(f.toString());
        }
    }

    private boolean waiting = false;
    
    public void taskFinished()
    {
        waiting = false;
    }
}
