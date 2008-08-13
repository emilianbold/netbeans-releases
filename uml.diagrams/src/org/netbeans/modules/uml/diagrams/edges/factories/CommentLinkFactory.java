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
package org.netbeans.modules.uml.diagrams.edges.factories;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;

/**
 *
 * @author treyspiva
 */
public class CommentLinkFactory implements RelationshipFactory
{

    public CommentLinkFactory()
    {
    }

    public IRelationship create(IElement source, IElement target)
    {
        // A nested link is not a relationship.  Therefore, it does not return
        // a relationship.  Therefore I had to implement a palette model instead.
        return null;
    }

    public void reconnectSource(IElement relationship, 
                                IElement oldSource, 
                                IElement source,
                                IElement target)
    {
        if((oldSource instanceof INamedElement) &&
           (source instanceof INamedElement) &&
           (target instanceof INamedElement))
        {
            reconnect((INamedElement)oldSource, 
                      (INamedElement)source,
                      (INamedElement)target);
        }
    }

    public void reconnectTarget(IElement relationship, 
                                IElement oldTarget, 
                                IElement target,
                                IElement source)
    {
        if((oldTarget instanceof INamedElement) &&
           (source instanceof INamedElement) &&
           (target instanceof INamedElement))
        {
            reconnect((INamedElement)oldTarget, 
                      (INamedElement)target,
                      (INamedElement)source);
        }
    }
    
    protected void reconnect(INamedElement old, INamedElement newElement, INamedElement opposite)
    {
        IComment comment = null;
        INamedElement annotation = null;
        
        if (opposite instanceof IComment)
        {
            IComment testComment = (IComment) opposite;
            
            // Since we can have a link between two different comments, we have 
            // to first find out which end is annotated.
            if(testComment.getIsAnnotatedElement(old) == true)
            {
                // If we are here than the old element was being annoated.
                testComment.removeAnnotatedElement(old);
                
                comment = testComment;
                annotation = newElement;
            }
        }
        
        if((comment == null) || (annotation == null)) 
        {
            // If we are here it means the that side that changes is the 
            // commnet end.
            if (old instanceof IComment)
            {

                // If we are here then the old element was the comment.  So, 
                // we are not going to remove any comments.
                IComment testComment = (IComment) old;
                if(testComment.getIsAnnotatedElement(opposite) == true)
                {
                    testComment.removeAnnotatedElement(opposite);
                    annotation = opposite;
                    
                    if(newElement instanceof IComment)
                    {
                        comment = (IComment)newElement;
                    }

                }
                else
                {
                    // If we are here, then we do not have a proper relationship.
                }
            }
        }
        
        if((comment != null) && (annotation != null))
        {
            comment.addAnnotatedElement(annotation);
        }

    }

    public void delete(boolean fromModel, IPresentationElement element, 
                       IElement source, IElement target)
    {
        IComment comment = null;
        INamedElement annotated = null;

        if(source instanceof IComment)
        {
            comment = (IComment)source;
            if(target instanceof INamedElement)
            {
               annotated = (INamedElement)target;
            }
        }
        else if(source instanceof INamedElement)
        {
            if(target instanceof IComment)
            {
                comment = (IComment)target;
                annotated = (INamedElement)source;
            }
        }
        
        if(fromModel == true)
        {
            comment.removeAnnotatedElement(annotated);
        }
    }
    
    public String getElementType()
    {
        return "CommentLink";
    }
    
}
