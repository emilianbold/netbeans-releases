/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package test;
import javax.swing.text.BadLocationException;
public class ExitPoints {
    
    public ExitPoints() {
    }
    
    public void test() {
        if (true)
            return ;
        
        if(true)
            throw new NullPointerException();
        
        return;
    }
    
    public int test(int a) {
        if (true)
            return 0;
        
        if(true)
            throw new NullPointerException();
        
        return 0;
    }
    
    public Object test(Object a) {
        if (true)
            return null;
        
        if(true)
            throw new NullPointerException();
        
        return null;
    }
    
    public void test(String s) throws NullPointerException, javax.swing.text.BadLocationException {
        if(true)
            return ;
        
        throwNPE();
        throwBLE();
        
        try{
            throwNPE();
            throwBLE();
        } catch (NullPointerException e) {}
        
        try{
            throwNPE();
            throwBLE();
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            throwNPE();
            throwBLE();
        } catch (Exception e) {}
        
        try{
            try{
                throwNPE();
            } catch (NullPointerException e) {}
            throwBLE();
        } catch (NullPointerException e) {}

        try{
            try{
                throwNPE();
                throwBLE();
            } catch (NullPointerException e) {}
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            try{
                throwNPE();
            } catch (NullPointerException e) {}
            throwBLE();
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            throwBLE();
            try{
                throwNPE();
            } catch (NullPointerException e) {}
        } catch (NullPointerException e) {}
    }
    
    public void test(double x) throws NullPointerException, javax.swing.text.BadLocationException {
        new ConstructorThrows();
    }
    
    private void throwNPE() throws NullPointerException {
        
    }
    
    private void throwBLE() throws javax.swing.text.BadLocationException {
        
    }
    
    private java.util.List<String> testListString() {
        return null;
    }
    
    private String[] testArray() {
        return new String[]{new String()};
    }

    public String method() {
        class H {
            public void run() {
                if(true) return;
            }
        }
        new Runnable() {
            public void run() {
                if(true) return;
            }
        };
        return "";
    }

}

class ConstructorThrows {

    public ConstructorThrows() throws NullPointerException, BadLocationException {
    }

    public ConstructorThrows(int a) throws NullPointerException, BadLocationException {
        this();
    }

}

class Foo extends ConstructorThrows {

    public Foo() throws BadLocationException {
        super(1);
    }

}
