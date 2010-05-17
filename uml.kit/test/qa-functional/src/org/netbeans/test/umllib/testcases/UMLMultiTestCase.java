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


package org.netbeans.test.umllib.testcases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ported from qa.jse.MutiTestCase
 * @author sp153251
 */
public abstract class UMLMultiTestCase extends UMLTestCase{
    
    private int _instanceCount = 1;
    private Throwable err = null;
    
    /**
     * Creates a new instance of UMLMultiTestCase
     * @param name 
     */
    public UMLMultiTestCase(String name) {
        super(name);
    }
    
    public UMLMultiTestCase() {
        super(null);
        setName(shortName(this.getClass().getName()));
    }
    
    /**
     * 
     * @throws java.lang.Throwable 
     */
    protected void runTest() throws Throwable {
        System.out.println("MultiTestCase:runTest "+getName());
        execute();
    }

    /**
     * 
     * @throws java.lang.Throwable 
     */
    public void runBare() throws Throwable {
        if(err != null){
            throw(err);
        }
        else{
            super.runBare();
        }
    }
//Safe preparation for testing
    /**
     * 
     * @return 
     */
    final boolean isPrepared(){
        boolean result = false;
        try{
            prepare();
            result = true;
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while preparing for test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    final void cleanit(){
        try{
            cleanup();
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while cleaning after test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @return 
     */
    final boolean gotFailed(){
        return err != null;
    }
    
    /**
     * 
     * @return 
     */
    final UMLMultiTestCase internalCreate(){
        UMLMultiTestCase testcase = null;
        try{
            testcase = create();
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while creating MultiTestCase: "+e.toString());
            e.printStackTrace();
        }
        return testcase;
    }
    
//Methods    
    public void prepare(){
    }
    
    public void cleanup(){
        
    }
    
    public abstract void execute();
    
//Default implementation of MultiTestCreator interface
    /**
     * 
     * @return 
     */
    public UMLMultiTestCase create(){
        if(_instanceCount>0){
            _instanceCount--;
            return this;
        }
        return null;
    }
    
//Utils    
    /**
     * 
     * @param longName 
     * @return 
     */
    static String shortName(String longName){
        String shortName = longName;
        char[] delims = {'.','$'};
        int pos;
        for(char ch: delims){
            pos = shortName.lastIndexOf(ch);
            if(pos >= 0)
                shortName = shortName.substring(pos+1);
        }
        return shortName;
    }
}
