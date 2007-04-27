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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.j2me.cdc.project.nsicom;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author suchys
 */
public class NSIcomPropertiesDescriptor implements ProjectPropertiesDescriptor {

    public static String PROP_MONITOR_HOST = "nsicom.application.monitorhost"; //NOI18N
    public static String PROP_VERBOSE      = "nsicom.application.runverbose";  //NOI18N
    public static String PROP_RUN_REMOTE   = "nsicom.application.runremote";  //NOI18N
    public static String PROP_REMOTE_VM    = "nsicom.remotevm.location";      //NOI18N
    public static String PROP_REMOTE_APP   = "nsicom.remoteapp.location";     //NOI18N

    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);

    public NSIcomPropertiesDescriptor() {
    }

    public Set getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            set = new HashSet();
            set.add(new PropertyDescriptor(PROP_MONITOR_HOST, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_VERBOSE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_RUN_REMOTE, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_REMOTE_VM, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            set.add(new PropertyDescriptor(PROP_REMOTE_APP, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            ref = new WeakReference(set);
        }
        return set;
    }

}
