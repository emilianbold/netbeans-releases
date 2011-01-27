/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitTransportUpdate;

/**
 *
 * @author ondra
 */
public class JGitTransportUpdate implements GitTransportUpdate {
    private final String localName;
    private final String remoteName;
    private final String oldObjectId;
    private final String newObjectId;
    private final GitRefUpdateResult result;
    private final String uri;
    private final Type type;

    public JGitTransportUpdate (URIish uri, TrackingRefUpdate update) {
        this.localName = stripRefs(update.getLocalName());
        this.remoteName = stripRefs(update.getRemoteName());
        this.oldObjectId = update.getOldObjectId() == null || ObjectId.zeroId().equals(update.getOldObjectId()) ? null : update.getOldObjectId().getName();
        this.newObjectId = update.getNewObjectId() == null || ObjectId.zeroId().equals(update.getNewObjectId()) ? null : update.getNewObjectId().getName();
        this.result = GitRefUpdateResult.valueOf(update.getResult().name());
        this.uri = uri.toString();
        this.type = getType(update);
    }

    @Override
    public String getRemoteUri () {
        return uri;
    }

    @Override
    public String getLocalName () {
        return localName;
    }

    @Override
    public String getRemoteName () {
        return remoteName;
    }

    @Override
    public String getOldObjectId () {
        return oldObjectId;
    }

    @Override
    public String getNewObjectId () {
        return newObjectId;
    }

    @Override
    public GitRefUpdateResult getResult () {
        return result;
    }

    @Override
    public Type getType () {
        return type;
    }

    private static String stripRefs (String refName) {
        if (refName.startsWith(Constants.R_HEADS)) {
            refName = refName.substring(Constants.R_HEADS.length());
        } else if (refName.startsWith(Constants.R_TAGS)) {
            refName = refName.substring(Constants.R_TAGS.length());
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            refName = refName.substring(Constants.R_REMOTES.length());
        } else if (refName.startsWith(Constants.R_REFS)) {
            refName = refName.substring(Constants.R_REFS.length());
        } else {
            throw new IllegalArgumentException("Unknown refName: " + refName);
        }
        return refName;
    }

    private Type getType (TrackingRefUpdate update) {
        String refName = update.getLocalName();
        Type retval;
        if (refName.startsWith(Constants.R_TAGS)) {
            retval = Type.TAG;
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            retval = Type.BRANCH;
        } else {
            throw new IllegalArgumentException("Unknown type for: " + refName);
        }
        return retval;
    }

}
