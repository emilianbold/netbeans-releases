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

package org.netbeans.modules.uml.core.support.umlutils;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.modules.uml.core.support.Debug;

public class XslTransformer
{
	private static Map CACHE = new Hashtable(); // map of Templates objects
	private String xslFileName; // the filename of stylesheet and key to map
	private Transformer transformer; 

	public XslTransformer(String theXslFileName) 
		throws TransformerConfigurationException
	{
		xslFileName  = theXslFileName;
        
		TemplateWrapper stylesheet = (TemplateWrapper)CACHE.get(theXslFileName);
		if (stylesheet == null || stylesheet.isStale())
		{
			TransformerFactory factory = TransformerFactory.newInstance();
        
			Templates template = factory.newTemplates(new StreamSource(xslFileName));
        
			stylesheet = new TemplateWrapper(template, xslFileName); 
			CACHE.put(theXslFileName, stylesheet);
		}
		transformer  = stylesheet.getStylesheet().newTransformer();
	}
	public void transform(Source xmlSource, Result result)
		throws TransformerException
	{    
		if(Debug.isEnabled())
		{
//			Debug.out.println("\n\n\n\nApplying XSLT: ("+xslFileName+")");
//			Debug.logXMLDetails(new StreamSource(xslFileName));
//			Debug.out.println("\n\nto XML: [");
//			Debug.logXMLDetails(xmlSource);
//			Debug.out.println("]");
		}
		transformer.transform(xmlSource, result);
	}
   
	class TemplateWrapper
	{
		Templates stylesheet; // the compiled stylesheet
		File xslFile;         // represents the stylesheet source
		long timestamp;       // last compile time
            
		TemplateWrapper(Templates aStylesheet, String xslFileName)
		{
			stylesheet = aStylesheet;
			xslFile    = new File(xslFileName);
			timestamp  = xslFile.lastModified();
		}
            
		private boolean isStale()
		{
			return xslFile.lastModified() != timestamp;
		}
            
		Templates getStylesheet()
		{
			return stylesheet;
		}       
	}
}
