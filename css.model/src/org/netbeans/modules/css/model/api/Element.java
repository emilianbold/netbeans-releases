/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.api;

//import java.util.List;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.css.model.api.semantic.PModel;

/**
 *
 * @author marekfukala
 */
public interface Element {
    
    public void accept(ModelVisitor modelVisitor);
    
    public Element getParent();

    public void setParent(Element e);
    
    public int addElement(Element e);

    public Element removeElement(int index);
    
    public boolean removeElement(Element e);
    
    public void insertElement(int index, Element element);

    public int getElementsCount();
    
    public int getElementIndex(Element e);

    public Element getElementAt(int index);
    
    public Element setElementAt(int index, Element e);
    
    public Iterator<Element> childrenIterator();

    public void addElementListener(ElementListener listener);
    
    public void removeElementListener(ElementListener listener);
 
    //XXX what should happen to the element offsets when the model is changed
    //by adding/removing some element. Clearly the original offsets become invalid then.
    
    /**
     * @return offset of the element start in the source code. 
     * May return -1 if the element has been added to the model.
     */
    public int getStartOffset();
    
    /**
     * @return offset of the element end in the source code. 
     * May return -1 if the element has been added to the model.
     */
    public int getEndOffset();
    
    /**
     * Return a collection of {@link SemanticModel} based on the <b>current model
     * state</b>. Once the model is changed (by adding/removing/updating elements
     * the semantic model becomes obsolete and cannot be used anymore.
     * 
     * <b>Implementation notices:</b>
     * There are several ways how the behavior can look like:
     * 1) <b>CURRENT</b> the semantic model (SEM) becomes obsolete once the underlying model (UM) has changed.
     * 2) #1 + the SEM throws some exception on access.
     * 3) SEM can update itself based on the underlying data change
     * 
     */
    public Collection<? extends PModel> getSemanticModels();
    
    /**
     * Notice: No semantic checks beyond parsing are done with respect to the returned value.
     * 
     * @return true if there's no parsing error in the element, false otherwise
     */
    public boolean isValid();
    
}
