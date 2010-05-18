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
 * The NewsService
 *
 * @since version 0.1
 *
 */
public interface NewsService {
    /**
     * retrieve and subscribe to a bulletin board.
     *
     * @param destination address of the bulletin board
     * @param listener callback object by wich new messages are provided.
     * @return a NewsChannel object materializing a connection to the
     * specified bulltin board.
     */
    public NewsChannel getNewsChannel(String destination,
                    NewsChannelListener listener) throws CollaborationException;

   /**
     * create a new bulletin board.
     *
     * @param destination address of the bulletin board
     * @param defaultAccess default privilege of new subscribers.
     * @param listener callback object by wich new messages are provided.
     * @return a NewsChannel object materializing a connection to the 
     * specified bulltin board.
     */
    public NewsChannel newNewsChannel(String destination, 
                            NewsChannelListener listener, int defaultAccess) 
                                                throws CollaborationException;

    /**
     * list all available bulletin boards
     * @return a collection of bulletin board objects.  Note that no 
     * subbscription is attached to these bulletin boards by virtue of being 
     * returned in this list.  Bulletin board subscriptions can be made 
     * by calling subscribe on elements in the collection.
     */
    public java.util.Collection listNewsChannels() 
                                                throws CollaborationException;
    
    
     /**
     * list all available bulletin boards to which the user has the given access
     * @param access  privilege user has to the bulletin board
     * @return a collection of bulletin board objects.  Note that no 
     * subbscription is attached to these bulletin boards by virtue of being 
     * returned in this list.  Bulletin board subscriptions can be made 
     * by calling subscribe on elements in the collection.
     */
    public java.util.Collection listNewsChannels(int access) 
                                                throws CollaborationException;

    /**
     * get all bulletin boards to which the user has a subsription
     * @return a collection of bulletin board objects.  
     */
    public java.util.Collection getSubscribedNewsChannels() 
                                                throws CollaborationException;
}
