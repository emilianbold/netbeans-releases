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

/** 
 */
public class ElementMatcher {

    // indicates that match to be performed using name 
    // for attribute or type, and signature for method
    public static final int BASE_MATCH = 0;

    // the match to be performed using marker ID
    // thus allowing to handle renames or signature changes 
    // if ID marker is present. 
    public static final int ID_MARKER_MATCH = 1;

	
    /**
     *   will return matching node if found, if several found 
     *   only first will be returned, an error will be logged
     */
    public Node findTypeMatch(IClassifier type, IClassifier scopingType, int matchType) {
	return null;		
    }

    
    public INamedElement findElementMatch(INamedElement elem, 
					  IClassifier scopingType, 
					  int matchType) 
    {
	List elems = null;
	if (elem instanceof IAttribute) {
	    elems = Merger.getAttributes(scopingType);
	} else if (elem instanceof IOperation) {
	    elems = Merger.getOperations(scopingType);
	} else if (elem instanceof IClassifier) {
	    elems = Merger.getSubTypes(scopingType);
	}
	return findElementMatch(elem, elems, matchType);		
    }


    public INamedElement findElementMatch(INamedElement elem, 
					  List<? extends INamedElement> elemList, 
					  int matchType) 
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


    public IAttribute findAttributeMatch(IAttribute attr, IClassifier scopingType, int matchType) {
	List<IAttribute> attrs = Merger.getAttributes(scopingType);
	for(IAttribute a : attrs) {
	    boolean isMatch = matchAttributes(attr, a, matchType);
	    if (isMatch) {
		return a;
	    }
	}
	return null;		
    }


    public IOperation findOperationMatch(IOperation oper, IClassifier scopingType, int matchType) {
	List<IOperation> opers = Merger.getOperations(scopingType);
	for(IOperation o : opers) {
	    boolean isMatch = matchOperations(oper, o, matchType);
	    if (isMatch) {
		return o;
	    }
	}
	return null;		
    }
    

    public boolean matchElements(INamedElement el1, INamedElement el2, int matchType) 
    {       
	if (matchType == BASE_MATCH) 
	{
	    if (el1 instanceof IOperation) {
		return matchOperations((IOperation)el1, (IOperation)el2, matchType);
	    }
	    return matchElementsByName(el1, el2);	    
	} 
	else if (matchType == ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(el1, el2);
	}
	return false;
    }


    public boolean matchOperations(IOperation op1, IOperation op2, int matchType) 
    {
	if (matchType == BASE_MATCH) 
	{
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
		    if (! p1.getTypeName().equals(p2.getTypeName())) {
			return false;
		    }
		}
		return true;
	    }	    
	}
	else if (matchType == ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(op1, op2);
	}
	return false;	
    }


    public boolean matchAttributes(IAttribute at1, IAttribute at2, int matchType) 
    {       
	if (matchType == BASE_MATCH) 
	{
	    return matchElementsByName(at1, at2);	    
	} 
	else if (matchType == ID_MARKER_MATCH) 
	{
	    return matchElementsByMarkerID(at1, at2);
	}
	return false;
    }


    public boolean matchTypes(IClassifier cl1, IClassifier cl2, int matchType) 
    {
	if (matchType == BASE_MATCH) 
	{
	    return matchElementsByName(cl1, cl2);	    
	}
	else if (matchType == ID_MARKER_MATCH) 
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


    public static boolean isMarked(IElement elem) 
    {
	String regen = getMarkerValue(elem.getNode(), "regen");
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
	String regen = getMarkerValue(elem.getNode(), "regenbody");
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
 	String query = "./TokenDescriptors/TDescriptor[@type=\"Marker-"+markerValueName+"\"]";
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
