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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.reporting.dataobjects;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class AggregationData extends AssociationData
{
    private IAggregation element;
    
    /** Creates a new instance of AggregationData */
    public AggregationData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IAggregation)
            this.element = (IAggregation)e;
    }
    
    public IAggregation getElement()
    {
        return element;
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Transient,
            Property_Abstract,
            Property_Leaf,
            Property_Composite,
            Property_Reflexive
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        Boolean isComposite = new Boolean(getElement().getIsComposite());
        Boolean isReflexive = new Boolean(getElement().getIsReflexive());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf, isComposite, isReflexive};
    }
    
    protected String getEndDetails()
    {
        StringBuilder buff = new StringBuilder();
        AssociationEndData endData;
        ETList<IAssociationEnd> ends = getElement().getEnds();
        
        if (ends.size()>0)
        {
            buff.append("<!-- =========== AGGREGATION END DETAIL =========== -->\r\n");
            
            for (int i=0; i<ends.size(); i++)
            {
                IAssociationEnd end = ends.get(i);
                String name = end.getName();
                
                if (name.equals(""))
                    name = end.getParticipant().getName();
                
                if (getElement().isAggregateEnd(end))
                    buff.append(getDetailHeader("aggregation_end_detail",
                            NbBundle.getMessage(AggregationData.class, "Aggregate_End_Detail")));
                else
                    buff.append(getDetailHeader("aggregation_end_detail",
                            NbBundle.getMessage(AggregationData.class, "Part_End_Detail")));
                
                if (end instanceof INavigableEnd)
                    endData = new NavigableEndData();
                else
                    endData = new AssociationEndData();
                
                endData.setElement((IElement)end);
                
                buff.append("<A NAME=\"" + name + "\"></A><H3>" + name + "</H3>\r\n");
                buff.append(endData.getDocumentation());
                
                buff.append(endData.getProperties(false));
                buff.append(endData.getStereoTypesSummary());
                buff.append(endData.getTaggedValueSummary());
                buff.append(endData.getConstraintsSummary());
                
                if (i<ends.size()-1)
                    buff.append("<HR>\r\n\r\n");
                else
                    buff.append("\r\n");
            }
        }
        return buff.toString();
    }
}
