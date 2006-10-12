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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

class FileType {
    private String name, suffix;

    private FileType(String name, String suffix) {
	this.name = name;
	this.suffix = suffix;
    }

    public String toString() { return name; }

    public String getSuffix() { return suffix; }

    public static final FileType SERVLET =
	new FileType("servlet", "java");

    public static final FileType FILTER =
	new FileType("filter", "java");

    public static final FileType LISTENER =
	new FileType("listener", "java");

    public static final FileType JSP = 
	new FileType("jsp", "jsp"); 

    public static final FileType JSPDOC = 
	new FileType("jspdoc", "jspx"); 

    public static final FileType JSPF = 
	new FileType("jspf", "jspf"); 

    public static final FileType TAG = 
	new FileType("tag_file", "tag");
    
    public static final FileType TAGLIBRARY = 
	new FileType("tag_library", "tld");

    public static final FileType TAG_HANDLER = 
	new FileType("tag_handler", "java");
    
    public static final FileType HTML = 
	new FileType("html", "html"); 
    
    public static final FileType XHTML = 
	new FileType("xhtml", "xhtml"); 

} 

