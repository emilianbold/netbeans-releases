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

/**
 *  The class that perform actual merge of source files
 *
 */
public class FileBuilder {


    public FileBuilder(String newFile, String oldFile) {

    }

    /**
     *  client calls this method to indicate that text fragment representing 
     *  oldElem in the old file should be replaced by text fragment representing
     *  newElem taken from new file
     */
    public void replace(ElementDescriptor newElem, ElementDescriptor oldElem) {

    } 
   
    /**
     *  client calls this method to indicate that text fragment representing
     *  newElem in new file should be added to the old file
     */
    public void add(ElementDescriptor newElem) {

    } 
   
    /**
     *  client calls this method to indicate that text fragment representing
     *  oldElem in removed from the old file
     */
    public void remove(ElementDescriptor oldElem) {

    }

    /**
     *  client calls this method to indicate that it finished 
     *  with posting of the requests, and on return from this method 
     *  it is expected that the [old]file on disk is modified 
     *  according to all previously posted requests. 
     */
    public void completed() {

    }
		    
}
