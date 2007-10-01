/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.query;

import org.netbeans.api.java.source.Comment;

/**
 * The set of comments associated with a single tree node.
 */
public interface CommentSet {
    /**
     * Add the specified comment to the list of preceding comments.
     */
    void addPrecedingComment(Comment c);

    /**
     * Add the specified comment string to the list of preceding comments.
     */
    void addPrecedingComment(java.lang.String s);

    /**
     * Add a list of comments to the list of preceding comments.
     */
    void addPrecedingComments(java.util.List<Comment> comments);

    /**
     * Add the specified comment to the list of trailing comments.
     */
    void addTrailingComment(Comment c);

    /**
     * Add the specified comment string to the list of trailing comments.
     */
    void addTrailingComment(java.lang.String s);

    /**
     * Add a list of comments to the list of preceding comments.
     */
    void addTrailingComments(java.util.List<Comment> comments);

    java.util.List<Comment> getPrecedingComments();

    java.util.List<Comment> getTrailingComments();

    boolean hasChanges();

    boolean hasComments();

    /**
     * 
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or NOPOS if there are no comments.
     * 
     * @see org.netbeans.modules.java.source.query.Query#NOPOS
     */
    int pos();
    
}
