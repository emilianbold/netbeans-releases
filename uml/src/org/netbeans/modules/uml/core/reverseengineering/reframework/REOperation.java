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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REOperation extends REClassFeature implements IREOperation
{
    /**
     * Retrieves the operations parameters
     * @param pVal The parameters.
     */
    public ETList<IREParameter> getParameters()
    {
        REXMLCollection<IREParameter> coll =
                new REXMLCollection<IREParameter>(
                    REParameter.class,
                    "UML:Element.ownedElement/UML:Parameter");
        try
        {
            coll.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return coll;
    }

    /**
     * Specifies if the operation return type is a primitive or an object type.
     *
     * @param *pVal [out] True if primitive, False otherwise.
     */
    public boolean getIsPrimitive()
    {
        IREParameter par = getReturnParameter();
        return par != null && par.getIsPrimitive();
    }

    /**
     * Specifies if the operation is a constructor of the owner class.
     *
     * @param pVal [out] True if the operationis a constructor.
     */
    public boolean getIsConstructor()
    {
        return XMLManip.getAttributeBooleanValue(
                    getEventData(), "isConstructor");
    }
    
    /**
     * Specifies whether the operation must be defined by a descendent. 
     * True indicates that the operation must be defined by a descendent. 
     * False indicates that a descendent is not required to define the operation.
     * @param pVal [out] True if the operationis abstract.
     */
    public boolean getIsAbstract()
    {
        return XMLManip.getAttributeBooleanValue(
                    getEventData(), "isAbstract");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation#clone(java.lang.Object, java.lang.Object)
     */
    public IOperation clone(IClassifier c)
    {
        IOperation op = c.createOperation(getType(), getName());
        if (op != null)
        {
            op.removeAllParameters();
            c.addOperation(op);
            cloneParametersToOperation(op);
        }
        return op;
    }
    
    private void cloneParametersToOperation(IOperation op)
    {
        ETList<IParameter> params = op.getParameters();
        ETList<IREParameter> repars = getParameters();
        if (params != null && repars != null)
        {    
            for (int i = 0, count = repars.size(); i < count; ++i)
            {
                IREParameter repar = repars.get(i);
                IParameter p = cloneParameter(repar, op);
                if (p != null)
                    op.addParameter(p);
            }
        }
    }
    
    private IParameter cloneParameter(IREParameter rep, IOperation op)
    {
        IParameter p = op.createParameter(rep.getType(), rep.getName());
        if (p != null)
        {
            p.setDirection(rep.getKind());
            IExpression def = p.getDefault();
            if (def != null)
                def.setBody(rep.getDefaultValue());
        }
        return p;
    }

    /** 
     * Return a list of the exceptions that this operation raises
     * 
     * @param pExceptions[out] list of exceptions that this operation raises 
     */
    public IStrings getRaisedExceptions()
    {
        IStrings ret = new Strings();
        Node oen = getXMLNode("UML:Element.ownedElement");
        if (oen != null)
        {
            List nodes = XMLManip.selectNodeList(oen, "UML:Exception");
            if (nodes != null && nodes.size() > 0)
            {
                for (int i = 0, count = nodes.size(); i < count; ++i)
                {
                    Node n = (Node) nodes.get(i);
                    ret.add(XMLManip.getAttributeValue(n, "name"));
                }
            }
        }
        return ret;
    }

    /** 
     * Determines if this operation is strictfp (java only)
     */
    public boolean getIsStrictFP()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isStrictFP");
    }

    /** 
     * Determines if this operation is native
     */
    public boolean getIsNative()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isNative");
    }

    /** 
     * Determines the concurrency of this operation
     */
    public int getConcurrency()
    {
        String con = XMLManip.getAttributeValue(getEventData(), "concurrency");
        if ("guarded".equals(con))
            return BaseElement.CCK_GUARDED;
        else if ("concurrent".equals(con))
            return BaseElement.CCK_CONCURRENT;
        else
            return BaseElement.CCK_SEQUENTIAL;
    }

    /**
     * Retrieves the type of the class feature.  Example the type of a 
     * attribute or the return type of a operation.
     * @param pVal The features type.
     */
    public String getType()
    {
        IREParameter par = getReturnParameter();
        return par != null? par.getType() : null;
    }
    
    public ETList<IREMultiplicityRange> getMultiplicity()
    {
        REXMLCollection<IREMultiplicityRange> mul = 
                new REXMLCollection<IREMultiplicityRange>(
                    REMultiplicityRange.class, 
                    "UML:TypedElement.multiplicity/UML:Multiplicity" +
                    "/UML:Multiplicity.range/UML:MultiplicityRange");
        try
        {
            mul.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return mul;
    }
    
    public void setMultiplicity(ETList<IREMultiplicityRange> mul)
    {
        throw new UnsupportedOperationException("Can't set multiplicity");
    }
    
    protected IREParameter getReturnParameter()
    {
        // Since the operation type is represented as a parameter with a 
        // kind of "return", I have to search the parameters for the return type.
        ETList<IREParameter> params = getParameters();
        if (params != null)
        {
            for (int i = 0, count = params.size(); i < count; ++i)
            {
                // Now retrieve the current parameter and check if it represents
                // the return value of the operation.  If it is the return type
                // retrieve the type from the parameter.
                IREParameter par = params.get(i);
                if (par == null) continue;
                
                if (par.getKind() == IREParameter.PDK_RESULT)
                    return par;
            }
        }
        return null;
    }
}
