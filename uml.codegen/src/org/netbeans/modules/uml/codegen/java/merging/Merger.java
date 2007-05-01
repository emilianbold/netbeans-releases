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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;


public class Merger implements IUMLParserEventsSink {

    private String newFile;
    private String oldFile;
    ArrayList<Node> classNodes;

    /**
     *
     */
    public Merger(String newFile, String oldFile) {
	this.newFile = newFile;
	this.oldFile = oldFile;
    }    

    public void merge() {
			    
	IUMLParser pParser = connectToParser();
	classNodes = new ArrayList<Node>();
	pParser.processStreamFromFile(newFile);	
	ArrayList<Node> newClassNodes =	classNodes;
	classNodes = new ArrayList<Node>();
	pParser.processStreamFromFile(oldFile);	
	ArrayList<Node> oldClassNodes = classNodes;
	merge(newClassNodes.get(0), oldClassNodes.get(0));

    }

    /**
     *
     */
    private void merge(Node newTypeNode, Node oldTypeNode) { 	
	
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
                    IUMLParser pParser = pFacility instanceof IUMLParser ? (IUMLParser) pFacility : null;
                    if (pParser != null)
                    {
                        IUMLParserEventDispatcher m_Dispatcher = pParser.getUMLParserDispatcher();
                        if (m_Dispatcher != null)
                        {
                            m_Dispatcher.registerForUMLParserEvents(this, " ");
                        }
                        return pParser;
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
    }
    
    public void onDependencyFound(IDependencyEvent data, IResultCell cell) {
    }
    
    public void onClassFound(IClassEvent data, IResultCell cell) {

        Node dataNode = null;
        
        try {

            dataNode = data.getEventData();
            
	    System.out.println("\nDefaultJavaCodegen.onClassFound \n data = "+data+"\n dataNode = "+dataNode);
	    
            if (dataNode != null){
		
		classNodes.add(dataNode);
		
		String query = ".//TDescriptor";

		List nodes = XMLManip.selectNodeList(dataNode, query);
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
		    Node curElement = (Node)iter.next();
		    System.out.println("\nDefaultJavaCodegen.onClassFound \n curElement = "+curElement);
		}
	    }
            
        } catch (Exception e) {
            e.printStackTrace(System.out);;
        }
    }
    
    public void onBeginParseFile(String fileName, IResultCell cell) {
    }
    
    public void onEndParseFile(String fileName, IResultCell cell) {
	try {
	    //new File("/tmp/out.txt").delete();
	} catch (Exception ex) {
	    ex.printStackTrace(System.out);
	}

	try {	
	    //XMLManip.save(((Node)classNodes.get(0)).getDocument(), "/tmp/out.txt");
	} catch (Exception ex) {
	    ex.printStackTrace(System.out);
	}
    }
    
    public void onError(IErrorEvent data, IResultCell cell) {
	System.out.println("\nPARSER ERROR\n");
    }

    // end of interface IUMLParserEventsSink

}

