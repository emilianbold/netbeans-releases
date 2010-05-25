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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.encoder.coco.ui.wizard;

/**
 * Constants used as the keys for identifying wizard properties.
 * 
 * @author Jun Xu
 */
public interface PropertyKey {
    
    /*************************
     * Systematic Properties *
     *************************/
    /**
     * Property key for identifying the Current Project property. The value
     * is an instance of <code>Project</code>.
     */
    static final String CURRENT_PROJECT = "current_project";
    
    /**
     * Property key for identifying the Current Folder property. The value
     * is an instance of <code>FileObject</code>.
     */
    static final String CURRENT_FOLDER = "current_folder";
    
    /**
     * Property key for identifying the Current File Name property. The value
     * is an instance of <code>java.lang.String</code>.  It is only a name
     * of the file, not including any path.
     */
    static final String CURRENT_FILE_NAME = "current_file_name";

    
    /*****************************
     * Properties for the Panels *
     *****************************/
    static final String SOURCE_TYPE = "source_type";
    
    static final String SOURCE_LOCATION = "source_location";
    
    static final String TARGET_FOLDER = "target_folder";
    
    static final String OVERWRITE_EXIST = "overwrite_exist";
    
    static final String COPYBOOK_CODEPAGE = "copybook_codepage";

    static final String DISPLAY_CODEPAGE = "display_codepage";
    
    static final String DISPLAY1_CODEPAGE = "display1_codepage";
    
    static final String PREDECODE_CODING = "predecode_coding";
    
    static final String POSTENCODE_CODING = "postencode_coding";
    
    static final String TARGET_NAMESPACE = "target_namespace";
    
    static final String IGNORE_72_COL_BEYOND = "ignore_72_col_beyond";
    
    static final String CHECK_RESERVED_WORDS = "check_reserved_words";
}
