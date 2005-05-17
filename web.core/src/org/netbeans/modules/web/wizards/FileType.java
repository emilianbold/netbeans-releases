/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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

} 

