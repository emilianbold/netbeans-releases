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

package org.netbeans.modules.uml.codegen.java.merging;


import java.util.Iterator;
import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Operation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Parameter;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;

/** 
 */
public class ElementMatcher {


    public static enum MatchType {

	// the match to be performed using marker ID
	// thus allowing to handle renames or signature changes 
	// if ID marker is present. 
	ID_MARKER_MATCH,

	// indicates that match to be performed using name 
	// for attribute or type, and signature for method
	BASE_MATCH,
	
	// special case for operations
	SHORT_PARAM_TYPES
    }

	
    /**
     *   will return matching node if found, if several found 
     *   only first will be returned, an error will be logged
     */
    public Node findTypeMatch(IClassifier type, IClassifier scopingType, MatchType matchType) {
	return null;		
    }

    
    public INamedElement findElementMatch(INamedElement elem, 
					  IClassifier scopingType, 
					  MatchType matchType) 
    {
	List elems = null;
	if (elem instanceof IAttribute) {
	    elems = Merger.getAttributes(scopingType);
	} else if (elem instanceof IOperation) {
	    elems = Merger.getOperations(scopingType);
	} else if (elem instanceof IEnumerationLiteral) {
	    elems = Merger.getEnumLiterals(scopingType);
	} else if (elem instanceof IClassifier) {
	    elems = Merger.getSubTypes(scopingType);
	}
	return findElementMatch(elem, elems, matchType);		
    }


    public INamedElement findElementMatch(INamedElement elem, 
					  List<? extends INamedElement> elemList, 
					  MatchType matchType) 
    {
	if (elemList != null) {
	    Iterator iter = elemList.iterator();
	    while(iter.hasNext()) {
		INamedElement e = (INamedElement)iter.next();
		boolean isMatch = matchElements(elem, e, matchType);
		if (isMatch) {
		    return e;
		}
	    }
	}
	return null;		
    }


    public IAttribute findAttributeMatch(IAttribute attr, IClassifier scopingType, MatchType matchType) {
	List<IAttribute> attrs = Merger.getAttributes(scopingType);
	for(IAttribute a : attrs) {
	    boolean isMatch = matchAttributes(attr, a, matchType);
	    if (isMatch) {
		return a;
	    }
	}
	return null;		
    }


    public IOperation findOperationMatch(IOperation oper, IClassifier scopingType, MatchType matchType) {
	List<IOperation> opers = Merger.getOperations(scopingType);
	for(IOperation o : opers) {
	    boolean isMatch = matchOperations(oper, o, matchType);
	    if (isMatch) {
		return o;
	    }
	}
	return null;		
    }
    

    public boolean matchElements(INamedElement el1, INamedElement el2, MatchType matchType) 
    {       
	if (matchType == MatchType.BASE_MATCH || matchType == MatchType.SHORT_PARAM_TYPES) 
	{
	    if (el1 instanceof IOperation) {
		return matchOperations((IOperation)el1, (IOperation)el2, matchType);
	    }
	    return matchElementsByName(el1, el2);	    
	} 
	else if (matchType == MatchType.ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(el1, el2);
	}
	return false;
    }


    public boolean matchOperations(IOperation op1, IOperation op2, MatchType matchType) 
    {
	if (matchType == MatchType.BASE_MATCH || matchType == MatchType.SHORT_PARAM_TYPES) 
	{
	    boolean fqParams = true;
	    if (matchType == MatchType.SHORT_PARAM_TYPES) 
	    {
		fqParams = false;
	    }
		fqParams = false;
	    if (! matchElementsByName(op1, op2)) {
		return false;
	    }
	    List<IParameter> pars1 = Merger.getParameters(op1);
	    List<IParameter> pars2 = Merger.getParameters(op2);
	    if (pars1 == null) {
		if (pars2 == null) {
		    return true;
		} 
		return false;
	    } else {
		if (pars2 == null) {
		    return false;
		}
		if (pars1.size() != pars2.size()) {
		    return false;
		}
		Iterator<IParameter> iter1 = pars1.iterator();
		Iterator<IParameter> iter2 = pars2.iterator();
		while(iter1.hasNext()) {
		    IParameter p1 = iter1.next();
		    IParameter p2 = iter2.next();
		    if (! Merger.compareParameters(p1, p2, fqParams)) {
			return false;
		    }
		}
		return true;
	    }	    
	}
	else if (matchType == MatchType.ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(op1, op2);
	}
	return false;	
    }


    public boolean matchAttributes(IAttribute at1, IAttribute at2, MatchType matchType) 
    {       
	if (matchType == MatchType.BASE_MATCH) 
	{
	    return matchElementsByName(at1, at2);	    
	} 
	else if (matchType == MatchType.ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(at1, at2);
	}
	return false;
    }


    public boolean matchTypes(IClassifier cl1, IClassifier cl2, MatchType matchType) 
    {
	if (matchType == MatchType.BASE_MATCH) 
	{
	    return matchElementsByName(cl1, cl2);	    
	}
	else if (matchType == MatchType.ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(cl1, cl2);
	}
	return false;	
    }


    public boolean matchElementsByMarkerID(INamedElement el1, INamedElement el2) 
    {
	String id1 = getIDMarker(el1);
	String id2 = getIDMarker(el2);
	if (id1 != null && id2 != null 
	    && id1.equals(id2)) 
	{
	    return true;
	}     
	return false;
    }


    public boolean matchElementsByName(INamedElement el1, INamedElement el2) 
    { 
	String name1 = el1.getName();
	String name2 = el2.getName();	    
	if (name1 != null && name2 != null 
	    && name1.equals(name2)) 
	{
	    return true;
	}   
	return false;
    }


    public static String getIDMarker(IElement elem) 
    {
	return getIDMarker(elem.getNode());
    }


    public static String getComment(Node elemNode) 
    {
	return getMarkerValue(elemNode, "Comment");
    }


    public static String getIDMarker(Node elemNode)
    {
	return getMarkerValue(elemNode, "id");
    }


    public static boolean isMarkedByOthers(IElement elem) 
    {
        String NETBEANS_MARKER = "//GEN-BEGIN:";
        String com = getDescriptorValue(elem.getNode(), "Comment");
        if (com != null && com.contains(NETBEANS_MARKER)) 
        {
            return true;
        }
        return false;
    }

    public static boolean isMarked(IElement elem) 
    {
        return isMarked(elem.getNode());
    }

    public static boolean isMarked(Node n) 
    {
	String regen = getMarkerValue(n, "regen");
	if (regen != null 
	    && ( regen.equalsIgnoreCase("yes") 
		 || regen.equalsIgnoreCase("ok")))
	{
	    return true;
	}
	return false;
    }

    public static boolean isRegenBody(IElement elem) 
    {
	boolean res = isRegenBody(elem.getNode());
	if (elem instanceof IAttribute) {
	    return true;
	}
        return res;
    }

    public static boolean isRegenBody(Node n) 
    {
	String regen = getMarkerValue(n, "regenbody");
	if (regen != null) 
	{
	    if ( regen.equalsIgnoreCase("yes") 
		 || regen.equalsIgnoreCase("true") 
		 || regen.equalsIgnoreCase("ok"))
	    {
		return true;
	    }
	    else if ( regen.equalsIgnoreCase("false") 
		      || regen.equalsIgnoreCase("no"))
	    {
		return false;
	    }
	}
	return false;
    }

    public static String getMarkerValue(Node elemNode, String markerValueName) 
    {
        return getDescriptorValue(elemNode, "Marker-"+markerValueName);
    }
 
    public static String getDescriptorValue(Node elemNode, String valueName) 
    {
 	String query = "./TokenDescriptors/TDescriptor[@type=\""+valueName+"\"]";
	Node tdnode = XMLManip.selectSingleNode(elemNode, query);
	if (tdnode !=  null) 
	{
	    try
	    {
		return XMLManip.getAttributeValue(tdnode, "value");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	} 
	return null;
    }
 

}
