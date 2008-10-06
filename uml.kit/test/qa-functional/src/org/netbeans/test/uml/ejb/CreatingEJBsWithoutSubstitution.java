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



package org.netbeans.test.uml.ejb;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.ejb.utils.EJBCreator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.exceptions.KnownBugException;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;


public class CreatingEJBsWithoutSubstitution extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static String prName= "EJB_uml";
    private static String project = prName+"|Model";
    private static boolean codeSync=false;
    private static String  lastTestCase="";
    
    
    public CreatingEJBsWithoutSubstitution(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CreatingEJBsWithoutSubstitution.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String INVOKATION_POINT = "Model|pkg";  
    private String PROJECT_NAME = "EJB_uml";  
    private String EXCEPTION_DLG = "Exception";
   
    /********************************************************/
    
    EJBCreator creator = new EJBCreator(PROJECT_NAME);    
    org.netbeans.test.uml.ejb.utils.Util util = new org.netbeans.test.uml.ejb.utils.Util();
    
    protected void setUp() {        
        ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            util.commonSetup(workdir, prName);
            //
            codeSync=true;
        }
        closeAllModal();
    }
    
    
    public void testEJB1_BMP() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_1;
        final String ejbName = EJBCreator.BMP;
        final String namespace = null;
        final boolean createDiagram = true;
        final String diagramName = "EJB1_BMP";
        final String parentPath = "Model";
        String[] elementNames = new String[]{"BMPHome", "BMPBean", "BMP"};
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            eventTool.waitNoEvent(2000);
            //verification:
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB1_BMP verification failed ");
            }            
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB1_BMP verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB1_BMP verification failed ");
            }
            

    }
    
    public void testEJB1_CMP() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_1;
        final String ejbName = EJBCreator.CMP;
        final String namespace = null;
        final boolean createDiagram = true;
        final String diagramName = "EJB1_CMP";
        String[] elementNames = new String[]{"CMPHome", "CMPBean", "CMP"};
        final String parentPath = "Model";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB1_CMP verification failed ");
            }    
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB1_CMP verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB1_CMP verification failed ");
            }
            
    }
    
    
    public void testEJB1_SSTL() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_1;
        final String ejbName = EJBCreator.SESSION_STATELESS;
        final String namespace = null;
        final boolean createDiagram = true;
        final String diagramName = "EJB1_STTL";
        String[] elementNames = new String[]{"StatelessSession", "StatelessSessionHome", "StatelessSessionBean"};
        final String parentPath = "Model";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB1_SSTL verification failed ");
            }  
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB1_SSTL verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB1_SSTL verification failed ");
            }
            
    }
    
    
    public void testEJB1_SSTF() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_1;
        final String ejbName = EJBCreator.SESSION_STATEFUL;
        final String namespace = null;
        final boolean createDiagram = true;
        final String diagramName = "EJB1_SSTF";
        String[] elementNames = new String[]{"StatefulSession", "StatefulSessionHome", "StatefulSessionBean"};
        final String parentPath = "Model";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB1_SSTL verification failed ");
            }  
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB1_SSTL verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB1_SSTL verification failed ");
            }
            
     }
    
    
    
    
    
    
    public void testEJB2_BMP() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_2;
        final String ejbName = EJBCreator.BMP;
        final String namespace = "pkg";
        final boolean createDiagram = true;
        final String diagramName = "EJB2_BMP";
        final String parentPath = "Model|pkg";
        String[] elementNames = new String[]{"BMPHome", "BMPBean", "BMP", "BMPLocal", "BMPLocalHome"};
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }

            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB2_BMP verification failed ");
            }            
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB2_BMP verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB2_BMP verification failed ");
            }
            
    }
    
    
    public void testEJB2_CMP() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_2;
        final String ejbName = EJBCreator.CMP;
        final String namespace = "pkg";
        final boolean createDiagram = true;
        final String diagramName = "EJB2_CMP";
        String[] elementNames = new String[]{"CMPHome","CMPLocalHome", "CMPBean", "CMP", "CMPLocal"};
        final String parentPath = "Model|pkg";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }

            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB2_CMP verification failed ");
            }    
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB2_CMP verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB2_CMP verification failed ");
            }
            
    }
    
    
    public void testEJB2_SSTL() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_2;
        final String ejbName = EJBCreator.SESSION_STATELESS;
        final String namespace = "pkg";
        final boolean createDiagram = true;
        final String diagramName = "EJB2_STTL";
        String[] elementNames = new String[]{"StatelessSession", "StatelessSessionHome", "StatelessSessionBean", "StatelessSessionLocal", "StatelessSessionLocalHome"};
        final String parentPath = "Model|pkg";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB2_SSTL verification failed ");
            }  
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB2_SSTL verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB2_SSTL verification failed ");
            }
            
    }
    
    
    public void testEJB2_SSTF() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_2;
        final String ejbName = EJBCreator.SESSION_STATEFUL;
        final String namespace = "pkg";
        final boolean createDiagram = true;
        final String diagramName = "EJB2_SSTF";
        String[] elementNames = new String[]{"StatefulSession", "StatefulSessionHome", "StatefulSessionBean", "StatefulSessionLocal", "StatefulSessionLocalHome"};
        final String parentPath = "Model|pkg";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB2_SSTF verification failed ");
            }  
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB2_SSTF verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB2_SSTF verification failed ");
            }
            
    }
    
    
    public void testEJB2_MDB() throws NotFoundException {        
        lastTestCase=getCurrentTestMethodName();
        final String ejbType = EJBCreator.EJB_2;
        final String ejbName = EJBCreator.MDB;
        final String namespace = "pkg";
        final boolean createDiagram = true;
        final String diagramName = "EJB2_MDB";
        String[] elementNames = new String[]{"AsyncMessageBean"};
        final String parentPath = "Model|pkg";
            //creating EJBs:
            DiagramOperator dia = null;
            try
            {
                dia=creator.create(INVOKATION_POINT, ejbType, ejbName, namespace, createDiagram, diagramName);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            if (!util.diagramHasExactElements(elementNames, dia)){
                fail("testEJB2_MDB verification failed ");
            }  
            
            if (!util.allNodesExist(PROJECT_NAME, parentPath, elementNames)){
                fail("testEJB2_MDB verification failed ");
            }
            
            if (!util.nodeExists(parentPath+"|"+diagramName, PROJECT_NAME)){
                fail("testEJB2_MDB verification failed ");
            }
            
    }
    
         
    
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        org.netbeans.test.umllib.util.Utils.saveAll();
        
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
            new JDialogOperator(EXCEPTION_DLG).close();
            if (!failedByBug){
                fail("Unexpected Exception dialog was found");
            }            
        }catch(Exception excp){
        }finally{
            if (failedByBug){
                failedByBug = false;                        
            }
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
            util.closeSaveDlg();
            closeAllModal();
        }        
    }
    
         
}

