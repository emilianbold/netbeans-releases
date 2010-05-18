/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab;

import java.util.Properties;
import java.util.Set;

/**
 *
 * @author jerry
 */
public interface P2PAudioService extends P2PServiceBase {
    public final static int TRANSPORT_RAW_UDP = 1;

    public boolean isAudioEnabled(String userid);
    public Set getAudioFeatures();

    public void addOutgoingCodec(int id, String name, int channels, double clock, Properties props);
    public void removeOutgoingCodec(int id, String name, int channels, double clock, Properties props);
    public void addIncomingCodec(int id, String name, int channels, double clock, Properties props);
    public void removeIncomingCodec(int id, String name, int channels, double clock, Properties props);
    public void addTransport(P2PTransport transport);
    public void enable();
    public void disable();
    public void addIncomingListener(P2PIncomingAudioListener listener);
    public P2PAudioSession createSession(String who);
}
