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

package org.netbeans.modules.bpel.editors.api.nodes;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author nk160297
 */
public enum NodeType {
    UNKNOWN_TYPE, // Special element which means that the value isn't known.
    PROCESS,
    SCOPE,
    SEQUENCE,
    FLOW,
    IF,
    ELSE_IF,
    ELSE,
    WHILE,
    EMPTY,
    INVOKE,
    RECEIVE,
    REPLY,
    PICK,
    ASSIGN,
    WAIT,
    THROW,
    RETHROW,
    THEN,
    EXIT,
    COMPENSATE,
    COMPENSATE_SCOPE,
    POOL,
    PARTNER_LINK,
    PARTNER_LINK_TYPE,
    DATA_OBJECT,
    ANNOTATION,
    CATCH,
    CATCH_ALL,
    //
    ALARM_HANDLER,
    ALARM_EVENT_HANDLER,
    EVENT_HANDLERS,
    COMPENSATION_HANDLER,
    TERMINATION_HANDLER,
    FAULT_HANDLERS,
    DIAGRAM,
    EXCEPTION_HANDLER,
    DEFAULT_EXCEPTION_HANDLER,
    MESSAGE_HANDLER,
    ON_EVENT,
    SEQUENCE_CONNECTOR,
    FLOW_LINK_CONNECTOR,
    DEFAULT_FLOW_LINK_CONNECTOR,
    FLOW_CONNECTOR_LINK_EVENT,
    FLOW_JOIN_CONDITION_GATEWAY,
    PARTNER_MESSAGE_CONNECTOR,
    //
    ALL_TYPES_CONTAINER,
    //
    MESSAGE_TYPE,
    BPEL_GLOBAL_CATALOG,
    PRIMITIVE_TYPE,
    GLOBAL_SIMPLE_TYPE,
    GLOBAL_COMPLEX_TYPE,
    GLOBAL_ELEMENT,
    //
    WSDL_FILE,
    SCHEMA_FILE,
    //
    VARIABLE_SCOPE,
    VARIABLE_CONTAINER,
    VARIABLE,
    CORRELATION,
    CORRELATION_P,
    CORRELATION_SET_CONTAINER,
    CORRELATION_SET,
    CORRELATION_PROPERTY,
    CORRELATION_PROPERTY_ALIAS,
    MESSAGE_PART,
    //
    COPY,
    //
    SCHEMA_ELEMENT,
    IMPORT,
    IMPORT_WSDL,
    IMPORT_SCHEMA,
    IMPORT_CONTAINER,
    //
    MESSAGE_EXCHANGE,
    MESSAGE_EXCHANGE_CONTAINER,
    REPEAT_UNTIL,
    FOR_EACH,
    FROM_PART,
    TO_PART,
    FROM,
    TO,
    //
    STANDARD_FAULTS_FOLDER,
    WSDL_FILES_FOLDER,
    BPEL_FAULTS_FOLDER,
    FAULT,
    //
    EMBEDDED_SCHEMAS_FOLDER,
    EMBEDDED_SCHEMA,
    //
    PARTNER_ROLE,
    QUERY,
    BOOLEAN_EXPR,
    COMPLETION_CONDITION,
    DEFAULT_BPEL_ENTITY_NODE,
    //
    FOLDER
    ;
    
    public static final String FOLDER_MODIFICATOR = "FOLDER";
    private static final String BADGE_MODIFICATOR = "BADGE";
    private static final String IMAGE_FOLDER_NAME =
            "org/netbeans/modules/bpel/editors/api/nodes/images/"; // NOI18N
    private static final String HELP_ID_PREFIX = 
            "org.netbeans.modules.bpel.editors.api.nodes.NodeType"; // NOI18N
    
    private AtomicReference<String> myDisplayName = new AtomicReference<String>();
    private AtomicReference<Image> myDefaultImage = new AtomicReference<Image>();
    private Map<Object, Image> myImageMap;
    private String myHelpId;
    
    /**
     * This image is used as the default for types which hasn't icon provided.
     * It is public to be able to check if the image is provided.
     */
    public static final Image UNKNOWN_IMAGE = getImageImpl(UNKNOWN_TYPE, null);
    
    public String getDisplayName() {
        if (myDisplayName.get() == null) {
            try {
                myDisplayName.compareAndSet(null,
                        NbBundle.getMessage(NodeType.class, this.toString()));
            } catch(Exception ex) {
                myDisplayName.compareAndSet(null, name());
            }
        }
        return myDisplayName.get();
    }
    
    public String getHelpId() {
        if (myHelpId == null) {
            myHelpId = HELP_ID_PREFIX + "." + this.toString(); // NOI18N
        }
        return myHelpId;
    }
    
    public String getDisplayName(Object modificator) {
        String displayName = null;
        try {
            String key = this.toString() + "_" + modificator.toString();
            displayName = NbBundle.getMessage(NodeType.class, key);
        } catch (Exception ex) {
            // do nothing
        }
        // can return null intentionally!
        return displayName;
    }
    
    public Icon getIcon() {
        return new ImageIcon(getImage());
    }

    public Image getImage() {
        if (myDefaultImage.get() == null) {
            Image image = getImageImpl(this, null);
            if (image == null) {
                image = UNKNOWN_IMAGE;
            }
            //
            myDefaultImage.compareAndSet(null, image);
        }
        return myDefaultImage.get();
    }
    
    public Image getImage(Object modificator) {
        if (modificator == null) {
            return getImage();
        } else {
            synchronized (this) {
                Image image = getImageMap().get(modificator);
                if (image == null) {
                    image = getImageImpl(this, modificator);
                    if (image == null) {
                        image = UNKNOWN_IMAGE;
                    }
                    //
                    getImageMap().put(modificator, image);
                }
                return image;
            }
        }
    }
    
    public Image getBadgeImage() {
        return getImage(BADGE_MODIFICATOR);
    }
    
    private Map<Object, Image> getImageMap() {
        if (myImageMap == null) {
            myImageMap = new HashMap<Object, Image>();
        }
        return myImageMap;
    }
    
    /**
     * Modificator allows having more then one icon associated with a Node Type
     */
    private static Image getImageImpl(Object name, Object modificator) {
        String fileName = null;
        if (modificator == null) {
            fileName = IMAGE_FOLDER_NAME + name + ".png"; // NOI18N
        } else {
            fileName = IMAGE_FOLDER_NAME + name + "_" + modificator + ".png"; // NOI18N
        }
        return ImageUtilities.loadImage(fileName);
    }
    
    public static boolean isValidImage(Image img) {
        return img != null && !NodeType.UNKNOWN_IMAGE.equals(img);
    }
    
}
