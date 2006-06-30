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

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileStateInvalidException;

 
/**
 *
 * @author  Petr Zajac
 */
public class RenameTest extends NbTestCase {
        
 
    private static String[] resultFiles = {
        "src/testRename/RenamedAction.java",
        "src/testRename/layer.xml",
        "src/testRename/RenamedLoader.java",
        "manifest.mf",
        "src/META-INF/services/org.openide.loaders.UniFileLoader"                        
    };
    private static String[] goldenFiles = {
        "RenamedAction.java.pass",
        "layer.xml.pass",
        "RenamedLoader.java.pass",
        "manifest.mf.pass",
        "org.openide.loaders.UniFileLoader.pass"                               
    };
    
    private String PATH_PREFIX = "";
    
    private static TypeClass typeProxy;
    private static JavaClass jc;

    private PrintStream refPs;

    
    /** Creates a new instance of Signature1Test */
    public RenameTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RenameTest.class);
        return suite;
    }
    
    public void testRename() throws FileStateInvalidException, IOException {
            jc = (JavaClass) TestUtility.findClass("testRename.MyAction");
            typeProxy = ((JavaModelPackage) jc.refOutermostPackage()).getType();

            jc = (JavaClass) typeProxy.resolve("testRename.MyAction");
            RenameRefactoring refactoring = new RenameRefactoring(jc);
            refactoring.setNewName("RenamedAction");
            refactoring.checkParameters();
            RefactoringSession result = RefactoringSession.create("rename class");
            refactoring.prepare(result);
            result.doRefactoring(true);

            jc = (JavaClass) TestUtility.findClass("testRename.MyDataLoader");
            typeProxy = ((JavaModelPackage) jc.refOutermostPackage()).getType();

            jc = (JavaClass) typeProxy.resolve("testRename.MyDataLoader");
             refactoring = new RenameRefactoring(jc);
            refactoring.setNewName("RenamedLoader");
            refactoring.checkParameters();
            result = RefactoringSession.create("rename class"); 
            refactoring.prepare(result);
            result.doRefactoring(true);

            // check modified files
            try {
                for (int x = 0; x < resultFiles.length; x++) {
                    String fileName = PATH_PREFIX + resultFiles[x] ;

                    assertFile(TestUtility.getFile(getDataDir(),"testRename", fileName), getGoldenFile(goldenFiles[x]), getWorkDir());
                }
            } catch (FileStateInvalidException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
    } 
    public void testWhereUsed() throws Exception {
       
        File f = new File(getWorkDir(),"whereUsed.ref" );
        refPs = new PrintStream(new FileOutputStream(f)); 
        jc = (JavaClass) TestUtility.findClass("testRename.WhereUsedDataLoader");
        ref("testrename.MyDataLoader");
        WhereUsedQuery wu= new WhereUsedQuery(jc);
        wu.setSearchInComments(true);
        findClass(wu);
        wu = new WhereUsedQuery(TestUtility.findClass("testRename.WhereUsedAction"));
        ref("testrename.WhereUsedAction");
        findClass(wu);
        refPs.close();
        assertFile(f,  getGoldenFile("whereUsed.ref"));
                
    }
//        
//    
//    public static String getAsString(String file) {
//        String result;
//        try {
//            FileObject testFile = Repository.getDefault().findResource(file);
//            DataObject dob = DataObject.find(testFile);
//            
//            EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);
//            StyledDocument doc = ec.openDocument();
//            result = doc.getText(0, doc.getLength());
//        } 
//        catch (Exception e) {
//            throw new AssertionFailedErrorException(e);
//        }
//        return result;
//    }
    
    protected void findClass(WhereUsedQuery wu) {
        RefactoringSession result = RefactoringSession.create(null);
        refProblems(wu.prepare(result));
        refUsages(result);
        ref("");
    }

    /**
     * Stores problems into ref file. Problems should be sorted.
     * @return true if problem is not null and one of them is fatal
     */
    public boolean refProblems(Problem problem) {
        Problem p=problem;
        boolean ret=false;
        if (p != null) {
            ArrayList list=new ArrayList();
            while (p != null) {
                if (p.isFatal()) {
                    ret=true;
                    list.add("Problem fatal: "+p.getMessage());
                } else {
                    list.add("Problem: "+p.getMessage());
                }
                p=p.getNext();
            }
            Collections.sort(list);
            for (int i=0;i < list.size();i++) {
                ref(list.get(i));
            }
        }
        return ret;
    } 
     protected void refUsages(RefactoringSession session) {
        Collection result = session.getRefactoringElements();
        ArrayList list=new ArrayList();
        HashMap map=new HashMap();
        for (Iterator it=result.iterator();it.hasNext();) {
            Object o=it.next();
            if (o instanceof RefactoringElement) {
                RefactoringElement wue=(RefactoringElement) o;
                Element el = wue.getJavaElement();
                if (el != null && el.getResource() != null) {
                    String s;
                    s=el.getResource().getName().replace(File.separatorChar,'/');
                    list=(ArrayList)(map.get(s));
                    if (list == null) {
                        list=new ArrayList();
                        map.put(s, list);
                    }
                    list.add(getDisplayText(wue));
                } else {
                    log("refUsages without resource");
                    log(getDisplayText(wue));
                    map.put(getDisplayText(wue), "");
                }
            }
        }
        ref("Found "+String.valueOf(result.size())+" occurance(s).");
        Object[] keys=map.keySet().toArray();
        Arrays.sort(keys);
        for (int i=0;i < keys.length;i++) {
            ref("");
            if (map.get(keys[i]) instanceof ArrayList) {
                ref(keys[i]);
                list=(ArrayList)(map.get(keys[i]));
                Collections.sort(list);
                for (int j=0;j < list.size();j++) {
                    ref("      "+list.get(j));
                }
            } else {
                ref(keys[i]);
            }
        }
        ref("");
    }
   public void ref(String s) {
       refPs.println(s);
    }
    
    public void ref(Object o) {
        ref(o.toString());
    }
    
    public void ref(File file) throws Exception {
        BufferedReader br=new BufferedReader(new FileReader(file));
        String line;
        while ((line=br.readLine()) != null) {
            ref(line);
        }
        br.close();
    } 
     protected String getDisplayText(RefactoringElement elm) {
        String app="";
        if (elm.getStatus() == RefactoringElement.WARNING) {
            app=" [ warning! ]";
        } else if (elm.getStatus() == RefactoringElement.GUARDED) {
            app=" [ error: code is in guarded block ]";
        }
        return elm.getDisplayText()+app;
     }   
}