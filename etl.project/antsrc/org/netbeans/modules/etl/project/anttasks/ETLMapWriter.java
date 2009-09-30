/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.etl.project.anttasks;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;



/**
 * 
 */
public class ETLMapWriter {
	
	Map<String,Definition > wsdlMap;
	String etlmapLocation;

	public ETLMapWriter(Map<String,Definition > wsdlMap, String etlmapLocation ) {
		this.wsdlMap=wsdlMap;
		this.etlmapLocation=etlmapLocation;
	}

	/**
	 * 
	 * <?xml version="1.0" encoding="UTF-8"?> <etlmap
	 * xmlns:tns="http://com.sun.com/etl/etlengine"
	 * targetNamespace="http://com.sun.com/etl/etlengine" > <etl
	 * partnerLink="{http://com.sun.com/etl/etlengine}Client2ETELLink"
	 * portType="{http://com.sun.com/etl/etlengine}etlPortType"
	 * operation="execute" file="etl-engine.xml" type="requestReplyService"/>
	 * 
	 */
	
	public void writeMap(){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<etlmap  xmlns:tns=\"http://com.sun.jbi/etl/etlengine\" \n");
		sb.append("\t targetNamespace=\"http://com.sun.jbi/etl/etlengine\" > \n");
		
		
		Iterator< String > iter = wsdlMap.keySet().iterator();
		while (iter.hasNext()) {
			
			String element = (String) iter.next();
			Definition def = wsdlMap.get(element);
			sb.append("\t <etl partnerLink=\"" +  def.getQName() + "_etlPartnerLink" +  "\"\n");
			sb.append("\t \t partnerLinkType=\"" +  def.getQName() + "_etlPartnerLinkType" +  "\"\n");
			sb.append("\t \t roleName=\"" +  def.getQName() + "_myrole" +  "\"\n");
			sb.append("\t \t portType=\"" +  def.getQName() + "_etlPortType" +  "\"\n");
			sb.append("\t \t operation= \"execute\" \n"  );
			sb.append("\t \t file=\"" +  def.getQName().getLocalPart() + ".xml" +  "\"\n");
			sb.append("\t \t type= \"requestReplyService\" /> \n"  );
			
			
			
		}
		
		sb.append("</etlmap>");
		
		String content = sb.toString();
		try {
			FileOutputStream fos = new FileOutputStream(etlmapLocation+"/etlmap.xml");
			copy(content.getBytes("UTF-8"), fos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

	public static void copy(byte[] input, OutputStream output) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(input);
		copy(in, output);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[1024 * 4];
		int n = 0;
		while ((n = input.read(buf)) != -1) {
			output.write(buf, 0, n);
		}
		output.flush();
	}
	
	

}
