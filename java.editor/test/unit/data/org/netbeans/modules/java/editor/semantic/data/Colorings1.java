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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;

public class Colorings1 {
    
    private List list1;
    private static List list2;
    private static final List list3 = null;
    
    /**@deprecated*/
    private List list4;
    
    @Deprecated
    public List list5;
    
           List list6;
    
    protected List list7;
    
    public static void main(String[] args) {
        List list8 = null;
        
        list2.add("");
    }
    
    private void test1() {
        Test1 test1;
        Test2 test2;
        Test3 test3;
        Test4 test4;
        Test5 test5;
    }

    private static void test2() {
        
    }
    
    /**@deprecated*/
    public final void test3() {
        
    }
    
    @Deprecated
    private static void test4() {
        
    }
    
    private static class Test1 {
        
    }

    /**@deprecated*/
    private static final class Test2 {
        
    }

    static class Test3 {
        
    }

    class Test4 {
        
    }

    @Deprecated
    public class Test5 {
        
    }
    
}
