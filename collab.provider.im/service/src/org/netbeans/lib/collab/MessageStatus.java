/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/**
 * This class defines message delivery status constants.
 *
 * @since version 0.1
 *
 */
public class MessageStatus {

    /**
     * message has been delivered successfully
     */
    public static final int DELIVERED = 0x0001;
    public static final int RECEIVED  = DELIVERED;

    /**
     * message has been forwarded to another domain within the
     * same messaging infrastructure (i.e. protocol).
     */
    public static final int RELAYED   = DELIVERED | 0x0002;
    public static final int FORWARDED = RELAYED;
    
    /**
     * message was delivered to a gateway to a foreign 
     * realtime messaging environment.
     */
    public static final int GATEWAYED = DELIVERED | 0x0004;
    
    /**
     * message has been read by recipient
     */
    public static final int READ      = DELIVERED | 0x0010;
    
    /**
     * recipient has replied
     */
    public static final int REPLIED   = DELIVERED | 0x0020;
    

    /**
     * delivery has failed because the user was not online.
     * The message was discarded by the server.
     */
    public static final int FAILED    = 0;
    
    /**
     * Message was not delivered.
     * Delivery was delayed because the recipient is unavailable.
     * message was stored on the server side.
     */
    public static final int DELAYED   = FAILED | 0x0002;

    /**
     * Message was not delivered.
     * message was denied access to recipient
     */
    public static final int REFUSED   = FAILED | 0x0004;
    
    /**
     * message has been emailed to an email address, as
     * the receipient is currently offline.
     */
    public static final int EMAILED = FAILED | 0x0008;
    
    /**
     * recipient has started typing
     */
    public static final int TYPING_ON = 8;
    
    /**
     * recipient has stopped typing
     */
    public static final int TYPING_OFF = 9;
}

