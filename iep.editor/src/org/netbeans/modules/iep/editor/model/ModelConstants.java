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

package org.netbeans.modules.iep.editor.model;

import java.sql.Types; 
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;


// Must match library.xml
import org.netbeans.modules.iep.model.lib.TcgComponentType;
public interface ModelConstants extends TcgModelConstants, SharedConstants {
    // In both TcgModelConstants, and SharedConstants
    public static String NAME_KEY = "name";
     
    public static String COMMENT_KEY = "comment";
    
    // TcgComponentType
    public static String PROPERTY_PATH = "/Metadata/BasicModuleProgramming/Property";
    public static TcgComponentType PROPERTY_TYPE = TcgModelManager.getTcgComponentType(PROPERTY_PATH);    
    
    public static String LINK_PATH = "/IEP/Model/Link";
    public static TcgComponentType LINK_TYPE = TcgModelManager.getTcgComponentType(LINK_PATH);

    public static String PLAN_PATH = "/IEP/Model/Plan";
    
    public static String COLUMN_METADATA_PATH = "/IEP/Metadata/ColumnMetadata";
    
    public static String SCHEMA_PATH = "/IEP/Metadata/Schema";
    public static TcgComponentType SCHEMA_TYPE = TcgModelManager.getTcgComponentType(SCHEMA_PATH);

    public static String OPERATOR_PATH = "/IEP/Operator";

    public static String INPUT_PATH = "/IEP/Input";

    public static String STREAM_INPUT_PATH = "/IEP/Input/StreamInput";

    public static String OUTPUT_PATH = "/IEP/Output";

    public static String STREAM_OUTPUT_PATH = "/IEP/Output/StreamOutput";
    
    public static String SCHEMA_MARKER = "SchemaMarker";

}
    
