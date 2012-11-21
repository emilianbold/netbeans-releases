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
package org.netbeans.test.java.editor.folding.JavaFoldsTest;

// import section fold
import javax.swing.JApplet;
import javax.swing.JButton;


/**
 * Outer Class Javadoc Fold
 * Demonstration of Code Folding functionality file.
 * @author Martin Roskanin
 */
public class testJavaFolds {
    
    /** One Line Field Javadoc Fold*/
    JButton button;
    
    /** 
     *  Multi-line Field Favadoc Fold
     */
    JApplet applet;
    
    /** One-line Constructor Javadoc Fold */
    public testJavaFolds() { } //One-line Constructor Fold
    
    /** 
     *  Multi-line Constructor Javadoc Fold
     */
    public testJavaFolds(String s) { //Multi-line Constructor Fold
        button = new JButton();
        applet = new JApplet();
    }
    
    
    /** One-line Method Javadoc Fold */
    public void methodOne(){ } // One-line Method Fold
    
    /**
     *  Multi-line Method Javadoc Fold 
     */
    public void methodTwo(){ // Multi-line Method Fold
        System.out.println(""); //NOI18N
    } 
 
    public void firstMethod(){ } public void secondMethod(){ } public void thirdMethod(){ }
    
    /** One-line InnerClass Javadoc Fold */
    public static class InnerClassOne{ }
    
    /** 
     *  Multi-line InnerClass Javadoc Fold 
     */
    public static class InnerClassTwo{ //Multi-line InnerClass Fold
    }
    
    public static class InnerClassThree{
        /** One Line InnerClass Field Javadoc Fold*/
        JButton button;

        /** 
         *  Multi-line InnerClass Field Favadoc Fold
         */
        JApplet applet;

        /** One-line InnerClass Constructor Javadoc Fold */
        public InnerClassThree() { } //One-line InnerClass Constructor Fold

        /** 
         *  Multi-line InnerClass Constructor Javadoc Fold
         */
        public InnerClassThree(String s) { //Multi-line InnerClass Constructor Fold
            button = new JButton();
            applet = new JApplet();
        }


        /** One-line InnerClass Method Javadoc Fold */
        public void methodOne(){ } // One-line InnerClass Method Fold

        /** 
         *  Multi-line InnerClass Method Javadoc Fold 
         */
        public void methodTwo(){ // Multi-line InnerClass Method Fold
            System.out.println(""); //NOI18N
        } 
        
        public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }
    }

    public static class InnerClassFour{
        public InnerClassFour(){
        }
        
        /** One-line InnerClassInInnerClass Javadoc Fold */
        public static class InnerClassInInnerClassOne{ } //One-line InnerClassInInnerClass Fold
        
        /**
         *   Multi-line InnerClassInInnerClass Javadoc Fold
         *
         */
        public static class InnerClassInInnerClassTwo{ //Multi-line InnerClassInInnerClass Fold
            /** One Line InnerClassInInnerClass Field Javadoc Fold*/
            JButton button;

            /** 
             *  Multi-line InnerClassInInnerClass Field Favadoc Fold
             */
            JApplet applet;
            
            /** One-line InnerClassInInnerClass Constructor Javadoc Fold */
            public InnerClassInInnerClassTwo() { } //One-line InnerClassInInnerClassTwo Constructor Fold

            /** 
             *  Multi-line InnerClassInInnerClassTwo Constructor Javadoc Fold
             */
            public InnerClassInInnerClassTwo(String s) { //Multi-line InnerClassInInnerClass Constructor Fold
                button = new JButton();
                applet = new JApplet();
            }
            
            /** One-line InnerClassInInnerClass Method Javadoc Fold */
            public void methodOne(){ } // One-line InnerClassInInnerClass Method Fold

            /** 
             *  Multi-line InnerClassInInnerClass Method Javadoc Fold 
             */
            public void methodTwo(){ // Multi-line InnerClassInInnerClass Method Fold
                System.out.println(""); //NOI18N
            } 
            
            public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }
            
        } 
       
    }
    
}
