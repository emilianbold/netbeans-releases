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

/*
 * DocumentTypeParser.java
 *
 * Created on January 8, 2006, 2:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever;

import java.io.File;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 *
 * @author girix
 */
public interface DocumentTypeParser {
    
    /**
     * will be called by factory to check if the mimeType is accepted by this parser.
     * @param mimeType MIME type of the current document
     */
    boolean accept(String mimeType);
    /**
     * this method will be called by the client to get all the external references found in this fileObject.
     * @param fob FileObject of the file that needs to be pased.
     */
    List<String> getAllLocationOfReferencedEntities(FileObject fob) throws Exception;
    
    List<String> getAllLocationOfReferencedEntities(File fileToBeParsed) throws Exception;
}
