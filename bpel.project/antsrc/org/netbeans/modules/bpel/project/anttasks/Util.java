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

package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileFilter;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Util {
	public static final String WSDL_FILE_EXTENSION = "wsdl";
	
	public static final String XSD_FILE_EXTENSION = "xsd";
	
	public static final String BPEL_FILE_EXTENSION = "bpel";
	
	static class ProjectFileFilter implements FileFilter {
    	
    	public boolean accept(File pathname) {
    		boolean result = false;
//    		if(pathname.isDirectory()) {
//    			return true;
//    		}
    		
    		String fileName = pathname.getName();
    		String fileExtension = null;
    		int dotIndex = fileName.lastIndexOf('.');
    		if(dotIndex != -1) {
    			fileExtension = fileName.substring(dotIndex +1);
    		}
    		
    		if(fileExtension != null 
    		   && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION) 
    		   	   || fileExtension.equalsIgnoreCase(XSD_FILE_EXTENSION))) {
    			result = true;
    		}
    		
    		return result;
		}
	 }
	 
	 static class BpelFileFilter implements FileFilter {
    	
    	public boolean accept(File pathname) {
    		boolean result = false;
    		if(pathname.isDirectory()) {
    			return true;
    		}
    		
    		String fileName = pathname.getName();
    		String fileExtension = null;
    		int dotIndex = fileName.lastIndexOf('.');
    		if(dotIndex != -1) {
    			fileExtension = fileName.substring(dotIndex +1);
    		}
    		
    		if(fileExtension != null 
    		   && (fileExtension.equalsIgnoreCase(BPEL_FILE_EXTENSION))) {
    			result = true;
    		}
    		
    		return result;
		}
	 }
	 
	 
	 static class WsdlFileFilter implements FileFilter {
    	
    	public boolean accept(File pathname) {
    		boolean result = false;
//    		if(pathname.isDirectory()) {
//    			return true;
//    		}
    		
    		String fileName = pathname.getName();
    		String fileExtension = null;
    		int dotIndex = fileName.lastIndexOf('.');
    		if(dotIndex != -1) {
    			fileExtension = fileName.substring(dotIndex +1);
    		}
    		
    		if(fileExtension != null 
    		   && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION))) {
    			result = true;
    		}
    		
    		return result;
		}
	 }
}
