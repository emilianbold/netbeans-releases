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
package org.netbeans.modules.uml.drawingarea.palette;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;

/**
 *
 * @author treyspiva
 */
public interface RelationshipFactory
{
    IElement create(IElement source, IElement target);
    
    /**
     * Reconnects a new source to the relationship.  Since some relationships 
     * are not true relationships, but instead represent properties on one of 
     * the nodes, the old source and the target are also passed into the method.
     * 
     * To make it easy for true relationships, which are the majority of 
     * relationships, this implementation calls the abstract reconnectSource
     * that only takes the relationship and the source element.
     * 
     * @param relationship The model element associated with the edge.
     * @param oldSource The old source end of the edge.
     * @param source The new source end of the edge.
     * @param target The target end of the edge.
     * @param see #reconnectSource(IElement, IElement)
     */
    public void reconnectSource(IElement relationship, 
                                IElement oldSource, 
                                IElement source,
                                IElement target);
    
    /**
     * Reconnects a new target to the relationship.  Since some relationships 
     * are not true relationships, but instead represent properties on one of 
     * the nodes, the old target and the source are also passed into the method.
     * 
     * To make it easy for true relationships, which are the majority of 
     * relationships, this implementation calls the abstract reconnectSource
     * that only takes the relationship and the target element.
     * 
     * @param relationship The model element associated with the edge.
     * @param oldTarget The old target end of the edge.
     * @param target The new target end of the edge.
     * @param source The source end of the edge.
     * @param see #reconnectTarget(IElement, IElement)
     */
    public void reconnectTarget(IElement relationship, 
                                IElement oldTarget, 
                                IElement target,
                                IElement source);
    
    /**
     * The name of the relationship.
     * 
     * @return the name.
     */
    public String getElementType();
    
    /**
     * Deletes a relationship.  If the fromModel parameter is true then the 
     * model element will be deleted as well as the model elements presentation
     * information.  If the fromModel is false, only the presentation 
     * information is deleted.
     * 
     * Since soem relationships are not true relationships, but instead 
     * represents propeties on one of the ends, the source and element elements
     * are also supplied.
     * 
     * @param fromModel if true remove from the model as well.
     * @param element The model element.
     * @param source The source end of the relationship.
     * @param target The target end of the relationship.
     */
    public void delete(boolean fromModel, IPresentationElement element, 
                       IElement source, IElement target);
     
}
