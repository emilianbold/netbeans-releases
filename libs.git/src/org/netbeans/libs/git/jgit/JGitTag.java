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
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.GitUser;

/**
 *
 * @author ondra
 */
public class JGitTag implements GitTag {
    private final String id;
    private final String name;
    private final String message;
    private final String taggedObject;
    private final GitUser tagger;
    private final GitObjectType type;
    private boolean lightWeight;

    public JGitTag (RevTag revTag) {
        this.id = ObjectId.toString(revTag.getId());
        this.name = revTag.getTagName();
        this.message = revTag.getFullMessage();
        this.taggedObject = ObjectId.toString(revTag.getObject().getId());
        PersonIdent personIdent = revTag.getTaggerIdent();
        if (personIdent == null) {
            personIdent = new PersonIdent("", ""); //NOI18N
        }
        this.tagger = new JGitUserInfo(personIdent);
        this.type = getType(revTag.getObject());
        this.lightWeight = false;
    }

    public JGitTag (String tagName, RevObject revObject) {
        this.id = ObjectId.toString(revObject.getId());
        this.name = tagName;
        this.message = null;
        this.taggedObject = id;
        this.tagger = null;
        this.type = getType(revObject);
        this.lightWeight = true;
    }

    public JGitTag (String tagName, GitRevisionInfo revCommit) {
        this.id = revCommit.getRevision();
        this.name = tagName;
        this.message = revCommit.getFullMessage();
        this.taggedObject = id;
        this.tagger = revCommit.getAuthor() == null ? revCommit.getCommitter() : revCommit.getAuthor();
        this.type = GitObjectType.COMMIT;
        this.lightWeight = true;
    }
    
    @Override
    public String getTagId () {
        return id;
    }

    @Override
    public String getTagName () {
        return name;
    }

    @Override
    public String getTaggedObjectId () {
        return taggedObject;
    }

    @Override
    public String getMessage () {
        return message;
    }

    @Override
    public GitUser getTagger () {
        return tagger;
    }

    @Override
    public GitObjectType getTaggedObjectType () {
        return type;
    }

    @Override
    public boolean isLightWeight () {
        return lightWeight;
    }

    private GitObjectType getType (RevObject object) {
        GitObjectType objType = GitObjectType.UNKNOWN;
        if (object != null) {
            switch (object.getType()) {
                case Constants.OBJ_COMMIT:
                    objType = GitObjectType.COMMIT;
                    break;
                case Constants.OBJ_BLOB:
                    objType = GitObjectType.BLOB;
                    break;
                case Constants.OBJ_TAG:
                    objType = GitObjectType.TAG;
                    break;
                case Constants.OBJ_TREE:
                    objType = GitObjectType.TREE;
                    break;
            }
        }
        return objType;
    }
    
}
