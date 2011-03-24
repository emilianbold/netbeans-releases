/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

import junit.framework.TestCase;

/**
 *
 * @author marekfukala
 */
public class ELPreprocessorTest extends TestCase {
    
    public ELPreprocessorTest(String name) {
        super(name);
    }

    public void testUnusualInputs() {
        String source = "";
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(preprocessed, source);
    }
    
    public void testNothingToPreprocess() {
        String source = "#{myBean.property}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(source, preprocessed);
    }
    
    public void testAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
    public void testAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        String result = "#{myBean.property && myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
    public void testOffsetsConversionAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        //               01234567890123456789012345678901234567890123456789
        //               0         1         2         3         4
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getPreprocessedOffset(0));
        assertEquals(1, elp.getPreprocessedOffset(1));
        assertEquals(18, elp.getPreprocessedOffset(18));
        
        //inside the pattern
        assertEquals(18, elp.getPreprocessedOffset(19));
        assertEquals(18, elp.getPreprocessedOffset(20));
        assertEquals(18, elp.getPreprocessedOffset(21));
        assertEquals(18, elp.getPreprocessedOffset(22));
        
        //after the pattern
        assertEquals(19, elp.getPreprocessedOffset(23));
        assertEquals(20, elp.getPreprocessedOffset(24));
        assertEquals(26, elp.getPreprocessedOffset(30));
        assertEquals(41, elp.getPreprocessedOffset(45));
        
        
    }
    
    public void testOffsetsConversionAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.property && myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getPreprocessedOffset(0));
        assertEquals(1, elp.getPreprocessedOffset(1));
        assertEquals(18, elp.getPreprocessedOffset(18));
        
        //inside the pattern
        assertEquals(18, elp.getPreprocessedOffset(19));
        assertEquals(18, elp.getPreprocessedOffset(20));
        assertEquals(18, elp.getPreprocessedOffset(21));
        assertEquals(18, elp.getPreprocessedOffset(22));
        
        //between the patterns
        assertEquals(19, elp.getPreprocessedOffset(23));
        
        //inside the second pattern
        assertEquals(19, elp.getPreprocessedOffset(24));
        assertEquals(19, elp.getPreprocessedOffset(25));
        assertEquals(19, elp.getPreprocessedOffset(26));
        assertEquals(19, elp.getPreprocessedOffset(27));
        
        //after the second pattern
        assertEquals(20, elp.getPreprocessedOffset(28));
        assertEquals(21, elp.getPreprocessedOffset(29));
        assertEquals(22, elp.getPreprocessedOffset(30));
        assertEquals(37, elp.getPreprocessedOffset(45));
        assertEquals(43, elp.getPreprocessedOffset(51));
        
    }
    
     public void testBackwardOffsetsConversionAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        //               01234567890123456789012345678901234567890123456789
        //               0         1         2         3         4
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getOriginalOffset(0));
        assertEquals(1, elp.getOriginalOffset(1));
        assertEquals(18, elp.getOriginalOffset(18));
        
        //after the pattern
        assertEquals(23, elp.getOriginalOffset(19));
        assertEquals(24, elp.getOriginalOffset(20));
        assertEquals(30, elp.getOriginalOffset(26));
        assertEquals(45, elp.getOriginalOffset(41));
        
        
    }
     
    public void testBackwardOffsetsConversionAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.property && myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getOriginalOffset(0));
        assertEquals(1, elp.getOriginalOffset(1));
        assertEquals(18, elp.getOriginalOffset(18));
        
        //between the patterns
        assertEquals(23, elp.getOriginalOffset(19));
        
        //after the second pattern
        assertEquals(28, elp.getOriginalOffset(20));
        assertEquals(29, elp.getOriginalOffset(21));
        assertEquals(51, elp.getOriginalOffset(43));
        
        
    }
    
}
