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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.model;

import java.util.List;

import org.netbeans.api.lexer.TokenSequence;


/**
 * This is base class for all PHP source elements.
 *  
 * @author ads
 *
 */
public interface SourceElement extends Acceptor {

    /**
     * Return lexer token sequence that correspond to this source element.
     * Note that this sequence can be out of sync with real content of
     * Document. Because model is like snapshot of Document , not 
     * one-to-one object representation.
     * 
     * Model correspond last call of sync() method.
     * So if you want real token sequence that correspond to curent 
     * state of Document you need to sync() model and perform access to 
     * token sequence inside <b>locked document</b>.
     *   
     * @return  token sequence for this source element.
     */
    TokenSequence getTokenSequence();
    
    /**
     * Getter for text that this element represent.
     * @return
     */
    String getText(); 
    
    /**
     * @return offset from document beginning for this element
     */
    int getOffset();
    
    /**
     * @return end offset in document for this element.
     */
    int getEndOffset();
    
    /**
     * @return parent compound source element
     */
    SourceElement getParent();
    
    List<SourceElement> getChildren();
    
    <T extends SourceElement> List<T> getChildren( Class<T> clazz );
    
    PhpModel getModel();
    
    Class<? extends SourceElement> getElementType();
}
