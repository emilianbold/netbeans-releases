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
