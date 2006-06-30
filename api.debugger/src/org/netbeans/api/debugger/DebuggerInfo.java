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

package org.netbeans.api.debugger;

import java.util.List;


/**
 * Contains information needed to start new debugging. Process of starting of
 * debugger can create one or more {@link Session} and one or more
 * {@link DebuggerEngine} and register them to {@link DebuggerManager}. For
 * more information about debugger start process see:
 * {@link DebuggerManager#startDebugging}.
 *
 * @author   Jan Jancura
 */
public final class DebuggerInfo {

    private Lookup lookup;


    /**
     * Creates a new instance of DebuggerInfo.
     *
     * @param typeID identification of DebuggerInfo type. Is used for 
     *      registration of external services.
     * @param services you can register additional services for this 
     *      DebuggerInfo here
     * @return returns a new instance of DebuggerInfo
     */
    public static DebuggerInfo create (
        String typeID,
        Object[] services
    ) {
        return new DebuggerInfo (
            typeID, 
            services
        );
    }
    
    private DebuggerInfo (
        String typeID,
        Object[] services
    ) {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 0, services.length);
        s [s.length - 1] = this;
        lookup = new Lookup.Compound (
            new Lookup.Instance (s),
            new Lookup.MetaInf (typeID)
        );
    }

    /**
     * Returns type identification of this session. This parameter is used for
     * registration of additional services in Meta-inf/debugger.
     *
     * @return type identification of this session
     */
//    public String getTypeID () {
//        return typeID;
//    }
    
//    /**
//     * Returns list of services of given type.
//     *
//     * @param service a type of service to look for
//     * @return list of services of given type
//     */
//    public List lookup (Class service) {
//        return lookup.lookup (null, service);
//    }
//    
//    /**
//     * Returns one service of given type.
//     *
//     * @param service a type of service to look for
//     * @return ne service of given type
//     */
//    public Object lookupFirst (Class service) {
//        return lookup.lookupFirst (null, service);
//    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public List lookup (String folder, Class service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public Object lookupFirst (String folder, Class service) {
        return lookup.lookupFirst (folder, service);
    }
    
    Lookup getLookup () {
        return lookup;
    }
}

