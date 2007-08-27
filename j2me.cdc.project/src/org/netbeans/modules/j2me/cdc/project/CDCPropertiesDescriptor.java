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

package org.netbeans.modules.j2me.cdc.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
public class CDCPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    public static final String APPLICATION_ARGS = "application.args"; //NOI18N
    public static final String APPLICATION_DESCRIPTION_DETAIL = "application.description.detail"; //NOI18N
    public static final String APPLICATION_DESCRIPTION = "application.description"; //NOI18N
    public static final String APPLICATION_NAME = "application.name"; //NOI18N
    public static final String APPLICATION_VENDOR = "application.vendor"; //NOI18N
    public static final String MAIN_CLASS_CLASS = "main.class.class"; //NOI18N
    public static final String MAIN_CLASS = "main.class"; //NOI18N
    public static final String MANIFEST_FILE = "manifest.file"; //NOI18N
    public static final String RESOURCES_DIR = "resources.dir"; //NOI18N
    public static final String RUN_JVMARGS = "run.jvmargs"; //NOI18N
    public static final String PLATFORM_FAT_JAR = "platform.fat.jar"; //NOI18N

        
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference<Set<PropertyDescriptor>>(null);
    
    /** Creates a new instance of CDCPropertiesDescriptor */
    public CDCPropertiesDescriptor() {
    }
    
    public Set<PropertyDescriptor> getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            String EMPTY = ""; //NOI18N
            set = new HashSet<PropertyDescriptor>();
            set.add(new PropertyDescriptor(APPLICATION_ARGS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_DESCRIPTION_DETAIL, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_DESCRIPTION, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_NAME, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(APPLICATION_VENDOR, true, DefaultPropertyParsers.STRING_PARSER,  "Vendor")); //NOI18N
            set.add(new PropertyDescriptor(MAIN_CLASS_CLASS, true, DefaultPropertyParsers.STRING_PARSER,  "applet")); //NOI18N
            set.add(new PropertyDescriptor(MAIN_CLASS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_FILE, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER,  "manifest.mf")); //NOI18N
            set.add(new PropertyDescriptor(RESOURCES_DIR, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER,  "resources")); //NOI18N
            set.add(new PropertyDescriptor(RUN_JVMARGS, true, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(PLATFORM_FAT_JAR, true, DefaultPropertyParsers.BOOLEAN_PARSER, "true")); //NOI18N
            ref = new WeakReference(set);
        }
        return set;
    }

}
