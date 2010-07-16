/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.edm.project.anttasks;

import java.io.ByteArrayInputStream;
import java.io.File;
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
public class EDMMapWriter {
	
	Map<String,Definition > wsdlMap;
	String etlmapLocation;

	public EDMMapWriter(Map<String,Definition > wsdlMap, String etlmapLocation ) {
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
		sb.append("<edmmap  xmlns:tns=\"http://com.sun.jbi/edm/edmengine\" \n");
		sb.append("\t targetNamespace=\"http://com.sun.jbi/edm/edmengine\" > \n");
		
		
		Iterator< String > iter = wsdlMap.keySet().iterator();
		while (iter.hasNext()) {
			
			String element = (String) iter.next();
			Definition def = wsdlMap.get(element);
			sb.append("\t <edm partnerLink=\"" +  def.getQName() + "_edmPartnerLink" +  "\"\n");
			sb.append("\t \t partnerLinkType=\"" +  def.getQName() + "_edmPartnerLinkType" +  "\"\n");
			sb.append("\t \t roleName=\"" +  def.getQName() + "_myrole" +  "\"\n");
			sb.append("\t \t portType=\"" +  def.getQName() + "_edmPortType" +  "\"\n");
			sb.append("\t \t operation= \"execute\" \n"  );
			sb.append("\t \t file=\"" +  def.getQName().getLocalPart() + ".xml" +  "\"\n");
			sb.append("\t \t type= \"requestReplyService\" /> \n"  );
			
			
			
		}
		
		sb.append("</edmmap>");
		
		String content = sb.toString();
		try {
			FileOutputStream fos = new FileOutputStream(etlmapLocation + File.separator + "edmmap.xml");
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
