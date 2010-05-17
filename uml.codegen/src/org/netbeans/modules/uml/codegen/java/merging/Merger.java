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


import java.lang.ref.WeakReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Attribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Interface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Operation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Parameter;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.constructs.EnumerationLiteral;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.Class;
import org.netbeans.modules.uml.core.metamodel.core.constructs.Enumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.UMLParser;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

public class Merger implements IUMLParserEventsSink {

    public static final String REGENERATE_MARKER_STRING = "generated";
    public static final String MARKER_SIGN = "#";

    private Properties props;
    private String newFile;
    private String oldFile;
    private String targetFile;
    private FileBuilder fileBuilder;
    private ElementMatcher matcher;
    private ArrayList<IClassifier> classNodes;
    private ArrayList<Import> imports;
    private ArrayList<Node> packageNodes;
    private ParsedInfo parsedNew;
    private ParsedInfo parsedOld;    
    private HashSet<IElement> matchedNew = new HashSet<IElement>();
    private HashSet<IElement> matchedOld = new HashSet<IElement>();
    private static WeakHashMap<Node, WeakReference<IElement>> cache 
	= new WeakHashMap<Node, WeakReference<IElement>>();
    private IUMLParser pParser; 
    private boolean errorHappened = false;

    public Merger(Properties props) {
	this.props = props;
    }    


    public void merge(ParsedInfo parsedNew, 
		      String newFile, 
		      ParsedInfo parsedOld,
		      String oldFile, 
		      String targetFile) 
	throws IOException
    {
	this.newFile = newFile;
	this.oldFile = oldFile;
	this.targetFile = targetFile;
	this.parsedOld = parsedOld;
	this.parsedNew = parsedNew;

	fileBuilder = new FileBuilder(newFile, oldFile, targetFile);
			    	
	// TBD using of Java Model, ie. Info* classes.
	// At this moment the sequence of tranforms  are  
	// JavaSource-> [ UML XML model->UML core model ] -> JavaSource
	// whereis codegen adds Java specific model in between
	// UML core model -> Java[Info* classes] -> JavaSource
	// The above reverse engineering for merging in generic case 
	// should be done into and logical merging analysis should be performed at 
	// the level of platform specific (in this case would be Java) model  

	// using stateless matching for now, though more 
	// powerfull statefull matching may be an option later
	matcher = new ElementMatcher();

	Node stImportNode = null;
	if (parsedOld.getImports().size() > 0) {
	    Collections.sort(parsedOld.getImports(), new Comparator<Import>() {
		public int compare(Import im1, Import im2) {
		    return (int)(im1.getStartPos() - im2.getStartPos());
		}
	    });
	    stImportNode = parsedOld.getImports().get(0).getNode();
	}

	Node stClassNode = null;
	ArrayList<IClassifier> oldClasses = parsedOld.getClasses();
	if (oldClasses.size() > 0) {
	    sortElementsByPosition(oldClasses);
	    stClassNode = oldClasses.get(0).getNode();
	}	
	
	if ( ! parsedNew.getPackageName().equals(parsedOld.getPackageName()) )
	{
	    mergePackages(parsedNew.getPack(), parsedOld.getPack(), 
			  stImportNode == null ? stClassNode : stImportNode);
	}

	mergeImports(parsedNew.getImports(), parsedOld.getImports(), oldClasses.get(0));

	//mergeTop(newClass, oldClass);
	merge(null, null, parsedNew.getClasses(), oldClasses); 

	fileBuilder.completed();
    }


    public ParsedInfo parse(String fileName, String charset) 
    {
	pParser = connectToParser();
	errorHappened = false;
	classNodes = new ArrayList<IClassifier>();
	imports = new ArrayList<Import>();
	packageNodes = new ArrayList<Node>();
        if (charset != null && java.nio.charset.Charset.isSupported(charset)
            && pParser instanceof UMLParser) 
        {
            ((UMLParser)pParser).processStreamFromFile(fileName, charset);
        }
        else 
        {
            pParser.processStreamFromFile(fileName);
        }
	if (errorHappened || classNodes.size() == 0) 
	{
	    return null;
	}
	return new ParsedInfo(packageNodes, imports, classNodes, fileName);
	//establishIDs(classNodes.get(0));
    }


    /**
     */
    private void mergePackages(Node newPack, 
			       Node oldPack, 
			       Node insPoint) 
    { 
	if (newPack == null ) {
	    if (oldPack != null) {
		fileBuilder.remove(new ElementDescriptor(oldPack));
	    } 
	} else {
	    if (oldPack != null) {
		
		fileBuilder.replace(new ElementDescriptor(newPack), 
				    new ElementDescriptor(oldPack));
	    } else {
		fileBuilder.insert(new ElementDescriptor(newPack), 
				   new ElementDescriptor(insPoint),
				   false, 
				   -1);		
	    }
	}	
   }


    /**
     *  merge all new imports into old ones if 
     *  not present already
     */
    private void mergeImports(List<Import> newImports, 
			      List<Import> oldImports, 
			      IClassifier oldClass) 
    { 
	
	Hashtable newByName = fillHashtable(newImports, true);
	Hashtable oldByName= fillHashtable(oldImports, true);
	//Hashtable newByPosition = fillHashtable(newImports, false);
	Hashtable oldByPosition = fillHashtable(oldImports, false);
	
	ArrayList newNames = new ArrayList(newByName.keySet());
	ArrayList oldStartPositions = new ArrayList(oldByPosition.keySet());

	Collections.sort(newNames);
	Collections.sort(oldStartPositions);	

	Iterator newIter = newNames.iterator();
	while(newIter.hasNext()) {
	    String name = (String)newIter.next();
	    if (oldByName != null && oldByName.get(name) != null) {
		continue;
	    }
	    int i = name.lastIndexOf(".");
	    if (i > -1 && i < (name.length() - 1)) {
		String pack = name.substring(0, i); 
		pack += ".*";
		if (oldByName != null && oldByName.get(pack) != null) {
		    continue;
		}
	    }
	    // nor class, nor it's package ".*" is imported
	    Iterator oldIter = oldStartPositions.iterator();
	    boolean inserted = false;
	    while(oldIter.hasNext()) {
		Import imp = (Import)oldByPosition.get(oldIter.next());
		if (imp != null && imp.getName().compareTo(name) < 0 ) {
		    fileBuilder.insert((Import)newByName.get(name), 
					(Import)imp, 
					true);
		    inserted = true;
		    break;
		}
	    }
	    if (!inserted) {
		fileBuilder.insert((Import)newByName.get(name), 
				   new ElementDescriptor(oldClass.getNode()), 
				   false);		
	    }	    
	}	
    }


    /**
     *  ID Marker based match takes precedence
     *  TBD with an element moved from one owner to another
     *  
     */
    private void mergeTop(IClassifier newClass, IClassifier oldClass) { 
	// match names of passed in top level nodes
	// if no match - undecided TBD

	String oldName = oldClass.getName();
	String newName = newClass.getName();
	if (ElementMatcher.isMarked(oldClass) || isOverwriteProp()
	    || (! newName.equals(oldName))) 
	{
	    fileBuilder.replace(new ElementDescriptor(newClass.getNode()), 
				new ElementDescriptor(oldClass.getNode()),
				ElementMatcher.isRegenBody(oldClass) 
				    ? FileBuilder.HEADER_AND_BODY
		                    : FileBuilder.HEADER_ONLY);
	}

	merge(newClass, oldClass);
	
    }

    /**
     *  ID Marker based match takes precedence
     *  TBD with an element moved from one owner to another
     *  
     */
    private void merge(IClassifier newClass, IClassifier oldClass) 
    { 

	List<IEnumerationLiteral> newLits = getEnumLiterals(newClass);
	List<IEnumerationLiteral> oldLits = getEnumLiterals(oldClass);
	mergeLiterals(newClass, oldClass, newLits, oldLits);
	mergeLiteralSectionTerminators(newClass, oldClass);

	List<IAttribute> newAttrs = getAttributes(newClass);
	List<IAttribute> oldAttrs = getAttributes(oldClass);
	merge(newClass, oldClass, newAttrs, oldAttrs);

	List<IOperation> newOps = getOperations(newClass);
	List<IOperation> oldOps = getOperations(oldClass);
	merge(newClass, oldClass, newOps, oldOps);

	// and subtypes with recursion embedded
	List<IClassifier> newSubTypes = getSubTypes(newClass);
	List<IClassifier> oldSubTypes = getSubTypes(oldClass);
	merge(newClass, oldClass, newSubTypes, oldSubTypes);
	
    }


    private void merge(IClassifier newClass, 
		       IClassifier oldClass, 
		       List<? extends INamedElement> newElems, 
		       List<? extends INamedElement> oldElems) 
    { 

	for(ElementMatcher.MatchType mt : ElementMatcher.MatchType.values()) 
	{
	    for(INamedElement newElem : newElems) 
	    {
		if (mt == ElementMatcher.MatchType.SHORT_PARAM_TYPES 
		    && ! (newElem instanceof IOperation))
		{
		    continue;
		}
		if (matchedNew.contains(newElem)) {
		    // has been already matched using ID marker
		    continue;
		}
		INamedElement elem = matcher.findElementMatch(newElem, oldElems, mt);
		if (elem != null) 
		{
		    if (! matchedOld.contains(elem)) 
		    {
			if ((ElementMatcher.isMarked(elem) || isOverwriteProp()) 
                            && ! ElementMatcher.isMarkedByOthers(elem))
			{
			    fileBuilder.replace(new ElementDescriptor(newElem.getNode()), 
						new ElementDescriptor(elem.getNode()),
						ElementMatcher.isRegenBody(elem)
					            ? FileBuilder.HEADER_AND_BODY
					            : FileBuilder.HEADER_ONLY);
			}
			addToMatched(matchedNew, newElem);
			addToMatched(matchedOld, elem);
			// and recursion for nested types
			if (newElem instanceof IClassifier) 
			{
			    // TBD if not full body replacement
			    merge((IClassifier)newElem, (IClassifier)elem);
			}
		    } 
		    else 
		    {		    
			// TBD we've already matched that element
			// need to at least log the error
		    }
		    continue;	    
		}
	    }
	}

	// adding all un-matched new elements
	for(INamedElement newElem : newElems) {
	    if (matchedNew.contains(newElem)) {
		// has been already matched using ID marker
		continue;
	    }
	    if (oldClass != null) {
		fileBuilder.add(new ElementDescriptor(newElem.getNode()),
				new ElementDescriptor(oldClass.getNode()));
	    } else {
		ArrayList<IClassifier> oldClasses = parsedOld.getClasses();
		sortElementsByPosition(oldClasses);
		fileBuilder.insert
		    (new ElementDescriptor(newElem.getNode()),
		     new ElementDescriptor(oldClasses.get(oldClasses.size() - 1).getNode()),
		     true);		
	    }
	}
	    
	// removing all un-matched regenerateable old elements
	for(INamedElement oldElem : oldElems) {
	    if (matchedOld.contains(oldElem)) {
		// has been already matched using ID marker
		continue;
	    }
	    if ((ElementMatcher.isMarked(oldElem) || isOverwriteProp())
                && ! ElementMatcher.isMarkedByOthers(oldElem))
	    {
		// the element is regenerateable, 
		// ie. not having been matched means to be deleted
		fileBuilder.remove(new ElementDescriptor(oldElem.getNode()));
	    }
	}
    }



    class Mapping {

	INamedElement ne;
	INamedElement oe;
	int pr = 0;

	Mapping(INamedElement ne, INamedElement oe) {
	    this.ne = ne;
	    this.oe = oe;
	}

    }


    /**
     *  
     *  
     */
    private void mergeLiterals(IClassifier newClass,
			       IClassifier oldClass,
			       List<IEnumerationLiteral> newElems, 
			       List<IEnumerationLiteral> oldElems) 
    { 
	
	Mapping map[] = new Mapping[newElems.size()];
	int i = 0;
	// marker ID based matching	
	for(INamedElement newElem : newElems) 
	{
	    INamedElement elem = matcher.findElementMatch(newElem, oldElems, ElementMatcher.MatchType.ID_MARKER_MATCH);
	    if (elem != null)
	    {
		if (! matchedOld.contains(elem)) 
		{
		    map[i] = new Mapping(newElem, elem);
		    addToMatched(matchedNew, newElem);
		    addToMatched(matchedOld, elem);		    
		} 
		else 
		{		    
		    // TBD we've already matched that element
		    // need to at least log the error
		}
	    }
	    i++;
	}
	
	i = 0;
	// base matching, ie. name (signature for operations) based
	for(INamedElement newElem : newElems) 
	{
	    if (! matchedNew.contains(newElem)) 
	    {
		INamedElement elem = matcher.findElementMatch(newElem, oldElems, ElementMatcher.MatchType.BASE_MATCH);
		if (elem != null)
		{
		    if (! matchedOld.contains(elem)) 
		    { 
			map[i] = new Mapping(newElem, elem);
			addToMatched(matchedNew, newElem);
			addToMatched(matchedOld, elem);		    
		    }		    
		    else 
		    {
			// TBD we've already matched that element
			// need to at least log the error
		    }
		
		}
	    }
	    i++;
	}

	int pnt = 0;
	INamedElement lastProcessed = null;
	for(i = 0; i < map.length; i++) 
	{
	    boolean processed = false;
	    if (map[i] != null) 
	    {
		Mapping m = map[i];
		if (lastProcessed == null || isOrdered(lastProcessed, m.oe)) 
		{
		    for( int j = pnt; j < i; j++) 
		    {
			if (map[j] != null) 
			{
			    if ((ElementMatcher.isMarked(map[j].oe) || isOverwriteProp())
                                && ! ElementMatcher.isMarkedByOthers(map[j].oe))
			    {
				fileBuilder.remove(new ElementDescriptor(map[j].oe.getNode()));
			    } 
			    else 
			    {
				continue;
			    }
			}				
			fileBuilder.insert(new ElementDescriptor(newElems.get(j).getNode()),
					   new ElementDescriptor(m.oe.getNode()),
					   false,
					   j - i);
		    }
		    if ((ElementMatcher.isMarked(m.oe) || isOverwriteProp()) 
                        && ! ElementMatcher.isMarkedByOthers(m.oe))
                    {
			fileBuilder.replace(new ElementDescriptor(m.ne.getNode()), 
					    new ElementDescriptor(m.oe.getNode()),
					    ElementMatcher.isRegenBody(m.oe) 
					        ? FileBuilder.HEADER_AND_BODY
					        : FileBuilder.HEADER_ONLY);
		    }
		    lastProcessed = m.oe;
		    pnt = i + 1;
		    processed = true;
		}
	    }
	    if (! processed && i == map.length - 1) 
	    {
		INamedElement anchor = null;
		if (oldElems != null && oldElems.size() > 0) 
		{ 
		    sortElementsByPosition(oldElems);
		    anchor = oldElems.get(oldElems.size() - 1);
		} 
		for( int j = pnt; j < map.length; j++) 
		{
		    if (map[j] != null) 
		    {
			if ((ElementMatcher.isMarked(map[j].oe) || isOverwriteProp()) 
                            && ! ElementMatcher.isMarkedByOthers(map[j].oe))
			{
			    fileBuilder.remove(new ElementDescriptor(map[j].oe.getNode()));
			} 
			else 
			{
			    continue;			    
			}
		    }	
		    if (anchor != null) 
		    {
			fileBuilder.insert(new ElementDescriptor(newElems.get(j).getNode()),
					   new ElementDescriptor(anchor.getNode()),
					   true,
					   j - map.length);
		    } 
		    else 
		    {
			fileBuilder.add(new ElementDescriptor(newElems.get(j).getNode()),
					new ElementDescriptor(oldClass.getNode()),
					j - map.length);
		    }
		}
	    }
	}
       
	    
	// removing all un-matched regenerateable old elements
	for(INamedElement oldElem : oldElems) {
	    if (matchedOld.contains(oldElem)) {
		// has been already matched using ID marker
		continue;
	    }
	    if ((ElementMatcher.isMarked(oldElem) || isOverwriteProp()) 
                && ! ElementMatcher.isMarkedByOthers(oldElem))
	    {
		// the element is regenerateable, 
		// ie. not having been matched means to be deleted
		fileBuilder.remove(new ElementDescriptor(oldElem.getNode()));
	    }
	}
	
    }


    private void mergeLiteralSectionTerminators(IClassifier newClass, IClassifier oldClass) 
    { 
	if (newClass == null || oldClass == null) 
	{
	    return;
	}
	ElementDescriptor nd = new ElementDescriptor(newClass.getNode());
	ElementDescriptor od = new ElementDescriptor(oldClass.getNode());

	if (ElementMatcher.isMarked(oldClass)) 
	{		 
	    if ("Enumeration".equals(nd.getModelElemType())) 
	    {
		if (od.getPosition("Literal Section Terminator") < 0) 
		{
		    fileBuilder.insertLiteralSectionTerminator(nd, od);
		}
	    }
	    else 
	    {
		if (od.getPosition("Literal Section Terminator") > -1) 
		{
		    fileBuilder.removeLiteralSectionTerminator(od);
		}
	    }
	}
    }

    private boolean addToMatched(HashSet<IElement> list, IElement elem) 
    {
	if (list.contains(elem)) 
	{
	    // TBD we've already matched that attribute
	    // need to at least log the error
	    return false;
	} 
	else 
	{
	    list.add(elem);
	    return true;
	}	
    } 
	
    
    private void establishIDs(Node node) 
    {
	establishXMIID(node, ElementMatcher.getIDMarker(node));	

	String query = ".//UML:Attribute";
	List ats = XMLManip.selectNodeList(node, query);
	if (ats != null) 
	{
	    Iterator iter = ats.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		establishXMIID(n, ElementMatcher.getIDMarker(n));
	    }
	}
	
	query = ".//UML:Operation";
	List ops = XMLManip.selectNodeList(node, query);
	if (ops != null)
	{
	    Iterator iter = ops.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		establishXMIID(n, ElementMatcher.getIDMarker(n));
	    }
	}

 	query = ".//UML:Class";
	List cs = XMLManip.selectNodeList(node, query);
	if (cs != null) 
	{
	    Iterator iter = ops.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		//establishIDs(n);
	    }
	}
   }


    protected static List<IOperation> getOperations(IClassifier cl) {
	ArrayList<IOperation> res = new ArrayList<IOperation>();
	String query = "./UML:Element.ownedElement/UML:Operation";
	List ops = XMLManip.selectNodeList(cl.getNode(), query);
	if (ops != null)
	{
	    Iterator iter = ops.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IOperation o = (IOperation)retrieveElement(n);
		if (o == null) {
		    o = new Operation();
		    o.setNode(n);
		    cacheElement(o);
		}
		res.add(o);
	    }
	}
	return res;
    }


    protected static List<IEnumerationLiteral> getEnumLiterals(IClassifier cl) {
	ArrayList<IEnumerationLiteral> res = new ArrayList<IEnumerationLiteral>();
	String query = "./UML:Enumeration.literal/UML:EnumerationLiteral";
	List lits = XMLManip.selectNodeList(cl.getNode(), query);
	if (lits != null)
	{
	    Iterator iter = lits.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IEnumerationLiteral l = (IEnumerationLiteral)retrieveElement(n);
		if (l == null) {
		    l = new EnumerationLiteral();
		    l.setNode(n);
		    cacheElement(l);
		}
		res.add(l);
	    }
	}
	return res;
    }


    protected static List<IParameter> getParameters(IOperation op) {
	ArrayList<IParameter> res = new ArrayList<IParameter>();
	String query = "./UML:Element.ownedElement/UML:Parameter";
	List pars = XMLManip.selectNodeList(op.getNode(), query);
	if (pars != null)
	{
	    Iterator iter = pars.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		String direction = XMLManip.getAttributeValue(n, "direction");
		if (direction == null || ! direction.equals("result")) 
		{ 
		    IParameter p = (IParameter)retrieveElement(n);
		    if (p == null) 
		    {
			p = new Parameter();
			p.setNode(n);
			cacheElement(p);
		    }
		    res.add(p);
		}
	    }
	}
	return res;
    }


    protected static List<IAttribute> getAttributes(IClassifier cl) {
	ArrayList<IAttribute> res = new ArrayList<IAttribute>();
	String query = "./UML:Element.ownedElement/UML:Attribute";
	List ats = XMLManip.selectNodeList(cl.getNode(), query);
	if (ats != null)
	{
	    Iterator iter = ats.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IAttribute a = (IAttribute)retrieveElement(n);
		if (a == null) {
		    a = new Attribute();
		    a.setNode(n);
		    cacheElement(a);
		}
		res.add(a);
	    }
	}
	return res;
    }


    protected static List<IClassifier> getSubTypes(IClassifier cl) {
	ArrayList<IClassifier> res = new ArrayList<IClassifier>();
	String query = "./UML:Element.ownedElement/UML:Class";
	List subnodes = XMLManip.selectNodeList(cl.getNode(), query);
	if (subnodes != null)
	{
	    Iterator iter = subnodes.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IClass s = (IClass)retrieveElement(n);
		if (s == null) {
		    s = new Class();
		    s.setNode(n);
		    cacheElement(s);
		}
		res.add(s);
	    }
	}
	query = "./UML:Element.ownedElement/UML:Interface";
	subnodes = XMLManip.selectNodeList(cl.getNode(), query);
	if (subnodes != null)
	{
	    Iterator iter = subnodes.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IInterface s = (IInterface)retrieveElement(n);
		if (s == null) {
		    s = new Interface();
		    s.setNode(n);
		    cacheElement(s);
		}
		res.add(s);
	    }
	}
	query = "./UML:Element.ownedElement/UML:Enumeration";
	subnodes = XMLManip.selectNodeList(cl.getNode(), query);
	if (subnodes != null)
	{
	    Iterator iter = subnodes.iterator();
	    while(iter.hasNext()) 
	    { 
		Node n = (Node)iter.next();
		IEnumeration s = (IEnumeration)retrieveElement(n);
		if (s == null) {
		    s = new Enumeration();
		    s.setNode(n);
		    cacheElement(s);
		}
		res.add(s);
	    }
	}
	return res;
    }


    public static boolean compareNodeLists(List l1, List l2, NodeComparator nodeCpr) {
	if (l1 == null) {
	    if (l2 != null) { 
		return false;
	    }
	} else {
	    if (l2 == null) { 
		return false;
	    }
	    if (l1.size() != l2.size()) {
		return false;		
	    }
	    Iterator iter1 = l1.iterator();
	    Iterator iter2 = l2.iterator();
	    while(iter1.hasNext()) 
	    { 
		if (! nodeCpr.compare((Node)iter1.next(), (Node)iter2.next())) {
		    return false;
		}		
	    }
	}
	return true;
    }
    

    public static interface NodeComparator {
	
	public boolean compare(Node n1, Node n2);

    }

    public static interface StringPreProcessor {
	
	public String preprocess(String val);

    }
     
    public static class BySpecificAttributeNodeComparator implements NodeComparator {

	String attrName;
	StringPreProcessor pp;

	public BySpecificAttributeNodeComparator(String attrName) {
	    this.attrName = attrName;
	}

	public BySpecificAttributeNodeComparator(String attrName, StringPreProcessor pp) {
	    this.attrName = attrName;
	    this.pp = pp;
	}

	public boolean compare(Node n1, Node n2) {
	    if ( (n1 == null) != (n2 == null) ) {
		return false;
	    } else if (n1 != null) {	    
		String v1 = XMLManip.getAttributeValue(n1, attrName);
		String v2 = XMLManip.getAttributeValue(n2, attrName);	
		if (pp != null) 
		{
		    v1 = pp.preprocess(v1);
		    v2 = pp.preprocess(v2);
		}
		if (!compareStringValues(v1, v2)) {
		    return false;
		}
	    }
	    return true;
	}
    
    }

    public static class PackageFilter implements StringPreProcessor{
	
	public String preprocess(String fqName) 
	{
	    if (fqName != null) 
	    {
		int li = fqName.lastIndexOf("::");
		if (li > -1) 
		{
		    if (li < fqName.length() - 2) 
		    {
			return fqName.substring(li + 2);
		    }
		    else 
		    {
			return "";
		    }
		}
		return fqName;
	    }
	    return null;
	}

    }

    public static boolean compareStringValues(String s1, String s2) {
	if ( (s1 == null) != (s2 == null)) {
	    return false;
	} else if ((s1 != null) && (! s1.equals(s2))) {
	    return false;
	} 
	return true;
    }


    public static boolean compareParameters(IParameter par1, IParameter par2, boolean fq) {
	
	Node pn1 = par1.getNode();
	Node pn2 = par2.getNode();

	StringPreProcessor pp = null;
	if (! fq) 
	{
	    pp = new PackageFilter();
	}
	if (! new BySpecificAttributeNodeComparator("type", pp).compare(pn1, pn2)) {
	    return false;
	}	
	
	String query = "./UML:TypedElement.multiplicity/UML:Multiplicity/UML:Multiplicity.range/UML:MultiplicityRange";
	List mrs1 = XMLManip.selectNodeList(pn1, query);
	List mrs2 = XMLManip.selectNodeList(pn2, query);
	
	if (! compareNodeLists(mrs1, mrs2,
			       new BySpecificAttributeNodeComparator("collectionType", pp))) 
	{
	    return false;
	}
	
	query = ".//TDerivation";
	mrs1 = XMLManip.selectNodeList(pn1, query);
	mrs2 = XMLManip.selectNodeList(pn2, query);
	if (! compareNodeLists(mrs1, mrs2,
			       new BySpecificAttributeNodeComparator("name", pp))) 
	{
	    return false;
	}

	query = ".//DerivationParameter";
	mrs1 = XMLManip.selectNodeList(pn1, query);
	mrs2 = XMLManip.selectNodeList(pn2, query);
	if (! compareNodeLists(mrs1, mrs2,
			       new BySpecificAttributeNodeComparator("value", pp))) 
	{
	    return false;
	}	

	return true;

    }


    public boolean isOverwriteProp() {
	boolean overwrite = false;
	if (props != null) 
	{
	    String val = props.getProperty("addMarkers", "false"); // NOI18N
	    if (val != null) 
	    {
		overwrite = Boolean.valueOf(val).booleanValue(); 
	    }
	}
	return overwrite;
    }


    protected static IElement retrieveElement(Node n) 
    {
	if (cache.get(n) != null) {
	    return cache.get(n).get();
	}
	return null;
    }


    protected static void cacheElement(IElement e) 
    {
	if (e != null && e.getNode() != null) 
	{
	    cache.put(e.getNode(), new WeakReference(e));
	}
    }
    

    private void establishXMIID(Node node, String markerIDValue)
    {
        try
        {
	    String curID = XMLManip.getAttributeValue(node, "xmi.id");
            if (curID == null || curID.length() == 0)
	    {
		String id;
		if (markerIDValue == null || markerIDValue.length() == 0) 
		{
		    id = XMLManip.retrieveDCEID();
		}
		else 
		{
		    id = markerIDValue;
		}
		XMLManip.setAttributeValue(node, "xmi.id", id);                
            }
        }
        catch (Exception e)
	{
            //
        }
    }
    

    private IUMLParser connectToParser()
    {
        try {
            IFacilityManager pManager = null;
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            ProductRetriever retriever;
            if (pProduct != null)
            {
                pManager = pProduct.getFacilityManager();
                if (pManager != null)
                {
                    IFacility pFacility = pManager.retrieveFacility("Parsing.UMLParser");
                    IUMLParser pParserRet = pFacility instanceof IUMLParser ? (IUMLParser) pFacility : null;
                    if (pParserRet != null)
                    {
                        IUMLParserEventDispatcher m_Dispatcher = pParserRet.getUMLParserDispatcher();
                        if (m_Dispatcher != null)
                        {
                            m_Dispatcher.revokeUMLParserSink(this);
                            m_Dispatcher.registerForUMLParserEvents(this, " ");
                        }
                        return pParserRet;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        return null;
    }


    // interface IUMLParserEventsSink

    
    public void onPackageFound(IPackageEvent data, IResultCell cell) {
        Node dataNode = null;        
        try {
            dataNode = data.getEventData();
            if (dataNode != null){		
		packageNodes.add(dataNode);
	    }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    

    public void onDependencyFound(IDependencyEvent data, IResultCell cell) {
        Node dataNode = null;        
        try {
            dataNode = data.getEventData();
            if (dataNode != null){		
		imports.add(new Import(dataNode));		
	    }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    
    public void onClassFound(IClassEvent data, IResultCell cell) {

        Node dataNode = null;
        
        try {

            dataNode = data.getEventData();
            if (dataNode != null){
		
		IClassifier cls = new Classifier();
		cls.setNode(dataNode);
		classNodes.add(cls);
	    }
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void onBeginParseFile(String fileName, IResultCell cell) {
    }
    
    public void onEndParseFile(String fileName, IResultCell cell) {
    }
    
    public void onError(IErrorEvent data, IResultCell cell) {
	errorHappened = true;
    }

    // end of interface IUMLParserEventsSink


    public static class Import extends ElementDescriptor {
	
	public Import(Node n) {
	    super(n);
	}
	
	public String getName() {
	    String query = "TokenDescriptors/TDescriptor[@type=\"Name\"]";
	    Node n = XMLManip.selectSingleNode(getNode(), query);
	    String name = XMLManip.getAttributeValue(n, "value");
	    return name;
	}

    }


    private Hashtable fillHashtable(List<Import> imports, boolean byName) {
	Hashtable res = new Hashtable();
	if (imports != null) {
	    for(Import imp : imports) {
		Object key;
		if (byName) {
		    key = imp.getName();
		} else {
		    key = new Long(imp.getStartPos());
		}
		res.put(key, imp);
	    }
	}
	return res;
    }

    public void sortElementsByPosition(List<? extends IElement> list) {
	Collections.sort(list, new Comparator<IElement>() {
	    public int compare(IElement c1, IElement c2) {
		return (int) (new ElementDescriptor(c1.getNode()).getStartPos() 
			      - new ElementDescriptor(c2.getNode()).getStartPos());
	    }
	});
    }


    public boolean isOrdered(IElement c1, IElement c2) 
    {
	return (new ElementDescriptor(c1.getNode()).getStartPos() 
		- new ElementDescriptor(c2.getNode()).getStartPos()) < 0 ;
    }

  
    public static class ParsedInfo {
	ArrayList<Node> pack;
	ArrayList<Import> imports;
	ArrayList<IClassifier> classes;
	String filePath = null;

	public ParsedInfo(ArrayList<Node> pack,
			  ArrayList<Import> imports,
			  ArrayList<IClassifier> classes,
			  String filePath) 
	{
	    this.pack = pack;
	    this.imports = imports;
	    this.classes = classes;	 
	    this.filePath = filePath;
	}

	public Node getPack() {
	    if (pack != null && pack.size() > 0) {
		return pack.get(0);
	    }
	    return null;
	}

	public String getPackageName() {
	    String res = "";
	    Node n = getPack();
	    if (n != null) {
		ElementDescriptor d = new ElementDescriptor(n);
		res = d.getModelElemAttribute("name");
	    }
	    return res;
	}

	public ArrayList<Import> getImports() {
	    return imports;

	}

	public ArrayList<IClassifier> getClasses() {
	    return classes;
	}

	public String getFilePath() {
	    return filePath;
	}
	
	public List<String> getDefinitiveClassIds() {
	    ArrayList<String> res = new ArrayList<String>();
	    if (classes == null) {
		return res;
	    }
	    for(IClassifier cls : classes) 
	    {
		String id = null;
		Node n = cls.getNode();
		if (n != null) 
		{
		    id = ElementMatcher.getIDMarker(n);
		    String vis = new ElementDescriptor(n).getModelElemAttribute("visibility");
		    if (vis != null && vis.equals("public"))  
		    {
			if (id != null) 
			{
			    res = new ArrayList<String>();
			    res.add(id);
			    return res;
			}
			else 
			{
			    return null;
			}
		    }					
		}
		if (id != null) 
		{
		    res.add(id);
		}
	    }
	    return res;
	}

    }

}

