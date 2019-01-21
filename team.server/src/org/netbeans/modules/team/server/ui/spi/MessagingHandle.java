/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

package org.netbeans.modules.team.server.ui.spi;

import java.beans.PropertyChangeListener;

/**
 * Abstraction of messaging info associated with a team project.
 *
 * 
 */
public abstract class MessagingHandle {

    /**
     * The name of Integer property which is fired when the count of online members
     * has changed for this project. The property value is the new count of online members.
     */
    public static final String PROP_ONLINE_COUNT = "onlineCount"; // NOI18N
    /**
     * The name of Integer property which is fired when the count of messages
     * has changed for this project. The property value is the new count of messages.
     */
    public static final String PROP_MESSAGE_COUNT = "messageCount"; // NOI18N

    /**
     * Returns number of online project members.
     * @return if number is >= 0 it is number of online project members.<br>
     * -1 means user is offline<br>
     * -2 means chat is not available<br>
     */
    public abstract int getOnlineCount();

    /**
     *
     * @return Number of available messages or -1 if the user isn't logged in
     * or the user isn't a member of project this handle is associated with.
     */
    public abstract int getMessageCount();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
