/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli;

import org.netbeans.modules.git.remote.cli.jgit.Utils;

/**
 * When there is a merge conflict in a file from a repository, the file's status 
 * provides instance of this class and you can get more information about the conflict.
 * Can be acquired with {@link GitStatus#getConflictDescriptor() } method.
 * 
 */
public final class GitConflictDescriptor {
    private final Type type;

    GitConflictDescriptor (Type type) {
        this.type = type;
    }
    
    public static enum Type {
        /**
         * Deleted in both branches.
         */
        BOTH_DELETED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_DELETED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by both"; //NOI18N
            }
        },
        /**
         * Added by us
         */
        ADDED_BY_US {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_ADDED_BY_US.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by us"; //NOI18N
            }
        },
        /**
         * Modified but deleted in other branch
         */
        DELETED_BY_THEM {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_DELETED_BY_THEM.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by them"; //NOI18N
            }
        },
        /**
         * Added by them
         */
        ADDED_BY_THEM {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_ADDED_BY_THEM.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by them"; //NOI18N
            }
        },
        /**
         * Deleted and modified in other branch
         */
        DELETED_BY_US {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_DELETED_BY_US.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by us"; //NOI18N
            }
        },
        /**
         * Added in two branches simultaneously
         */
        BOTH_ADDED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_ADDED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by both"; //NOI18N
            }
        },
        /**
         * Modified in two branches simultaneously
         */
        BOTH_MODIFIED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_MODIFIED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Modified by both"; //NOI18N
            }
        };

        public abstract String getDescription ();
    }

    /**
     * @return type of the merge conflict
     */
    public Type getType () {
        return type;
    }
}
