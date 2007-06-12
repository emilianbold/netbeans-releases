/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.container;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.FriendResolverImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * base class for hyperlink tests
 *
 * entry point:
 * - performTest (@see performTest)
 *
 * What should be configured:
 * - the dir with the same name as test class (harness init the project from the dir)
 * i.e for CsmHyperlinkProviderTestCase create
 * ${completion}/test/unit/data/org/netbeans/modules/cnd/completion/cplusplus/hyperlink/CsmHyperlinkProviderTestCase
 * and put there any C/C++ files
 *
 * @author Vladimir Voskresensky
 */
public class FriendTestCase extends TraceModelTestBase {
    
    public FriendTestCase(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        log("CndFriendTestCase.setUp started.");
        log("Test "+getName()+  " started");
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testFriend() throws Exception {
        performTest("friend.cc");
    }
    
    protected void performTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found "+testFile.getAbsolutePath(),testFile.exists());
        performModelTest(testFile, System.out, System.err);
        checkFriend();
        for(FileImpl file : getProject().getFileList()){
            file.stateChanged(true);
            file.scheduleParsing(true);
        }
        checkFriend();
        getProject().onFileRemoved(testFile);
        checkEmpty();
    }
    
    private void checkEmpty() {
        ProjectBase project = getProject();
        assertNotNull("Project must be valid", project); // NOI18N
        assertTrue("Should be 0 declarations in project", project.findDeclarationsByPrefix("").size()==0);
        assertTrue("Should be 0 declarations in global namespace", project.getGlobalNamespace().getDeclarations().size()==0);
        assertTrue("Should be 0 definitions in global namespace", project.getGlobalNamespace().getDefinitions().size()==0);
        assertTrue("Should be 0 namespaces in global namespace", project.getGlobalNamespace().getNestedNamespaces().size()==0);
    }
    
    private String getClassName(Class cls){
        String s = cls.getName();
        return s.substring(s.lastIndexOf('.')+1);
    }
    
    private void checkFriend() {
        ProjectBase project = getProject();
        assertNotNull("Project must be valid", project); // NOI18N
        CsmClass clsB = (CsmClass)project.findClassifier("B");
        assertNotNull("Class B not found", clsB); // NOI18N
        List<CsmFriend> friends = clsB.getFriends();
        assertTrue("Should be 5 friends in class B", friends.size()==5);
        CsmFriendClass friendA2 = null;
        CsmFriendFunction friendMoo2 = null;
        CsmFriendFunction friendMoo = null;
        CsmFriendFunction friendSoo = null;
        CsmFriendFunction friendSoo2= null;
        for(CsmFriend friend : friends){
            if ("A2".equals(friend.getName())){
                friendA2 = (CsmFriendClass) friend;
            } else if ("moo2".equals(friend.getName())){
                friendMoo2 =  (CsmFriendFunction) friend;
            } else if ("moo".equals(friend.getName())){
                friendMoo =  (CsmFriendFunction) friend;
            } else if ("soo".equals(friend.getName())){
                friendSoo =  (CsmFriendFunction) friend;
            } else if ("soo2".equals(friend.getName())){
                friendSoo2 =  (CsmFriendFunction) friend;
            }
        }
        assertNotNull("Friend class declaration A2 not found", friendA2); // NOI18N
        assertNotNull("Friend method declaration moo2 not found", friendMoo2); // NOI18N
        assertNotNull("Friend method declaration moo not found", friendMoo); // NOI18N
        assertNotNull("Friend method declaration soo not found", friendSoo); // NOI18N
        assertNotNull("Friend method declaration soo2 not found", friendSoo2); // NOI18N
        
        CsmClass clsA2 = friendA2.getReferencedClass();
        assertNotNull("Referenced class A2 for friend not found", clsA2); // NOI18N
        CsmFunction funMoo2 = friendMoo2.getReferencedFunction();
        assertNotNull("Referenced function moo2 for friend not found", funMoo2); // NOI18N
        CsmFunction funMoo = friendMoo.getReferencedFunction();
        assertNotNull("Referenced function moo for friend not found", funMoo); // NOI18N
        CsmFunction funSoo = friendSoo.getReferencedFunction();
        assertNotNull("Referenced function soo for friend not found", funSoo); // NOI18N
        CsmFunction funSoo2 = friendSoo2.getReferencedFunction();
        assertNotNull("Referenced function soo2 for friend not found", funSoo); // NOI18N
        
        Collection<CsmFriend> list = FriendResolverImpl.getDefault().findFriends(clsA2);
        assertTrue("Should be 1 friend declaration for class A2", list.size()==1);
        assertTrue("Friend declaration for class A2 has wrong instance", list.iterator().next()==friendA2);
        list = FriendResolverImpl.getDefault().findFriends(funMoo2);
        assertTrue("Should be 1 friend declaration for function moo2", list.size()==1);
        assertTrue("Friend declaration for function moo2 has wrong instance", list.iterator().next()==friendMoo2);
        list = FriendResolverImpl.getDefault().findFriends(funMoo);
        assertTrue("Should be 1 friend declaration for function moo", list.size()==1);
        assertTrue("Friend declaration for function moo has wrong instance", list.iterator().next()==friendMoo);
        list = FriendResolverImpl.getDefault().findFriends(funSoo);
        assertTrue("Should be 1 friend declaration for function moo", list.size()==1);
        assertTrue("Friend declaration for function soo has wrong instance", list.iterator().next()==friendSoo);
        
        Collection<CsmOffsetableDeclaration> declarations = project.findDeclarationsByPrefix("");
        Set<String> set = new HashSet<String>();
        for(CsmOffsetableDeclaration decl : declarations){
            String uName = decl.getUniqueName();
            System.out.println(uName + " \t" + getClassName(decl.getClass()));
            if ("FUNCTION:moo2(int)".equals(uName)){
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:moo(int)".equals(uName)){
                assertTrue("moo(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            }  else if ("struct:S2".equals(uName)){
                assertFalse("S2 is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:S2::soo()".equals(uName)){
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:S2::soo2()".equals(uName)){
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:S2::soo3()".equals(uName)){
                assertTrue("S2::soo3() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("class:A2".equals(uName)){
                assertTrue("A2 is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:A2::foo()".equals(uName)){
                assertTrue("A2::foo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:A2::foo()".equals(uName)){
                assertTrue("A2::foo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:ccStyle()".equals(uName)){
                assertFalse("ccStyle() is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("class:B".equals(uName)){
                assertFalse("B is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("TYPEDEF:B::xxx".equals(uName)){
                assertFalse("B::xxx is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("CLASS_FRIEND_DECLARATION:B::A2".equals(uName)){
                assertFalse("B::A2 is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:B::boo()".equals(uName)){
                assertFalse("B::boo() is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:moo2(int)".equals(uName)){
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:moo(int)".equals(uName)){
                assertTrue("moo(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:S2::soo()".equals(uName)){
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION:S2::soo2()".equals(uName)){
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else if ("FUNCTION_DEFINITION:ccStyle(int)".equals(uName)){
                assertFalse("ccStyle(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl,clsB));
            } else {
                assertTrue("Inexpected declaration "+uName, false);
            }
            assertFalse("Duplicated declaration ", set.contains(uName));
            set.add(uName);
        }
        assertTrue("Not all declaration found in project", set.size()==18);
    }
/*
int moo2(int);                          //FUNCTION:moo2(int)                    FunctionImpl
int moo(int){return 0;}                 //FUNCTION_DEFINITION:moo(int)          FunctionDDImpl
struct S2 {                             //struct:S2                             ClassImpl
    int soo();                          //FUNCTION:S2::soo()                    MethodImpl
    int soo2(){return 0;} };            //FUNCTION_DEFINITION:S2::soo2() 	MethodDDImpl
class A2{                               //class:A2                              ClassImpl
    int foo(); };                       //FUNCTION:A2::foo()                    MethodImpl
int A2::foo(){ return 0; }              //FUNCTION_DEFINITION:A2::foo() 	FunctionDefinitionImpl
int ccStyle();                          //FUNCTION:ccStyle()                    FunctionImpl
class B{                                //class:B                               ClassImpl
    typedef int xxx;                    //TYPEDEF:B::xxx                        ClassImpl$MemberTypedef
    friend class A2;                    //CLASS_FRIEND_DECLARATION:B::A2 	FriendClassImpl
    int boo();                          //FUNCTION:B::boo()                     MethodImpl
    friend int moo2(int) { return 0; }; //FUNCTION_DEFINITION:moo2(int) 	FriendFunctionDDImpl
    friend int moo(int);                //FUNCTION:moo(int)                     FriendFunctionImpl
    friend int S2::soo(){ return 0; }   //FUNCTION_DEFINITION:S2::soo() 	FriendFunctionDefinitionImpl
    friend int S2::soo2(); };           //FUNCTION:S2::soo2()                   FriendFunctionImplEx
int ccStyle(int){ return 0; }           //FUNCTION_DEFINITION:ccStyle(int) 	FunctionDDImpl
*/
}
