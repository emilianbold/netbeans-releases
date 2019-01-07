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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class TagTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;
    protected static final boolean KIT = ListTagCommand.KIT;

    public TagTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testListTagsAll").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testCreateTag () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitTag tag = client.createTag("tag-name", commit.getRevision(), "tag message\nfor tag-name", false, false, NULL_PROGRESS_MONITOR);
        assertTag(tag, commit.getRevision(), "tag-name", "tag message\nfor tag-name", client.getUser(), GitObjectType.COMMIT, false);
    }

    public void testOverwriteTag () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitTag tag = client.createTag("tag-name", commit.getRevision(), "tag message\nfor tag-name", false, false, NULL_PROGRESS_MONITOR);
        assertTag(tag, commit.getRevision(), "tag-name", "tag message\nfor tag-name", client.getUser(), GitObjectType.COMMIT, false);
        
        write(f, "modif");
        commit = client.commit(files, "change", null, null, NULL_PROGRESS_MONITOR);
        try {
            tag = client.createTag("tag-name", commit.getRevision(), "second tag message\nfor tag-name", false, false, NULL_PROGRESS_MONITOR);
            fail("Tag already exists, should fail");
        } catch (GitException ex) {
        }
        tag = client.createTag("tag-name", commit.getRevision(), "second tag message\nfor tag-name", false, true, NULL_PROGRESS_MONITOR);
        assertTag(tag, commit.getRevision(), "tag-name", "second tag message\nfor tag-name", client.getUser(), GitObjectType.COMMIT, false);
    }

    public void testListTags () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        client.createTag("tag-name", commit.getRevision(), "tag message", false, false, NULL_PROGRESS_MONITOR);
        client.createTag("tag-name-2", commit.getRevision(), "second tag message", false, false, NULL_PROGRESS_MONITOR);
        Map<String, GitTag> tags = client.getTags(NULL_PROGRESS_MONITOR, false);
        assertEquals(2, tags.size());
        assertTag(tags.get("tag-name"), commit.getRevision(), "tag-name", "tag message", client.getUser(), GitObjectType.COMMIT, false);
        assertTag(tags.get("tag-name-2"), commit.getRevision(), "tag-name-2", "second tag message", client.getUser(), GitObjectType.COMMIT, false);
    }

    public void testListTagsAll () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitTag tag = client.createTag("tag-name", commit.getRevision(), "tag message", false, false, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR)[0];
        GitTag tag2 = client.createTag("tag-name-2", info.getTree(), "tag for tree", false, false, NULL_PROGRESS_MONITOR);
        GitTag tag3 = client.createTag("tag-name-3", tag.getTagId(), "tag for tag", false, false, NULL_PROGRESS_MONITOR);
        Map<String, GitTag> tags = client.getTags(NULL_PROGRESS_MONITOR, false);
if(false)assertEquals(1, tags.size());
else     assertEquals(3, tags.size());
        assertTag(tags.get("tag-name"), commit.getRevision(), "tag-name", "tag message", client.getUser(), GitObjectType.COMMIT, false);
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(3, tags.size());
        assertTag(tags.get("tag-name"), commit.getRevision(), "tag-name", "tag message", client.getUser(), GitObjectType.COMMIT, false);
        info = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR)[0];
        assertTag(tags.get("tag-name-2"), tag2.getTagId(), "tag-name-2", "tag for tree", client.getUser(), GitObjectType.TREE, false);
if(false)assertTag(tags.get("tag-name-3"), tag3.getTagId(), "tag-name-3", "tag for tag", client.getUser(), GitObjectType.TAG, false);
else     assertTag(tags.get("tag-name-3"), tag.getTagName(), "tag-name-3", "tag for tag", client.getUser(), GitObjectType.TAG, false);
    }

    public void testCreateLightweightTag () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitTag tag = client.createTag("tag-name", commit.getRevision(), null, false, false, NULL_PROGRESS_MONITOR);
        assertTag(tag, commit.getRevision(), "tag-name", commit.getFullMessage(), commit.getCommitter(), GitObjectType.COMMIT, true);
        Map<String, GitTag> tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(1, tags.size());
        assertTag(tags.get("tag-name"), commit.getRevision(), "tag-name", commit.getFullMessage(), commit.getCommitter(), GitObjectType.COMMIT, true);
    }

    public void testDeleteTag () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitTag tag = client.createTag("tag-name", commit.getRevision(), null, false, false, NULL_PROGRESS_MONITOR);
        Map<String, GitTag> tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(1, tags.size());

        client.deleteTag("tag-name", NULL_PROGRESS_MONITOR);
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(0, tags.size());
        
        // and what about real tag object? not a lightweight one?
        tag = client.createTag("tag-name", commit.getRevision(), "tag message", false, false, NULL_PROGRESS_MONITOR);
        String tagId = tag.getTagId();
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(1, tags.size());

        client.deleteTag("tag-name", NULL_PROGRESS_MONITOR);
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(0, tags.size());
        
        // can the same tag be created again?
        tag = client.createTag("tag-name", commit.getRevision(), "tag message", false, false, NULL_PROGRESS_MONITOR);
        assertEquals(tagId, tag.getTagId());
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(1, tags.size());

        client.deleteTag("tag-name", NULL_PROGRESS_MONITOR);
        tags = client.getTags(NULL_PROGRESS_MONITOR, true);
        assertEquals(0, tags.size());
    }

    public void testTagInvalidName () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        write(f, "init");
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo commit = client.commit(files, "init commit", null, null, NULL_PROGRESS_MONITOR);
        String name = "tag with spaces";
        try {
            client.createTag(name, commit.getRevision(), null, false, false, NULL_PROGRESS_MONITOR);
            fail("Should fail");
        } catch (GitException ex) {
            assertTrue(ex.getMessage().contains("is not a valid tag name."));
            //assertTrue(ex.getCause() != null);
            //assertTrue(ex.getCause().toString(), ex.getCause() instanceof InvalidTagNameException);
        }
    }
    
    private void assertTag (GitTag tag, String taggedObjectId, String name, String message, GitUser user, GitObjectType taggedObjectType, boolean isLightWeight) {
        //assertEquals(isLightWeight, tag.isLightWeight());
        assertEquals(taggedObjectId, tag.getTaggedObjectId());
        if (isLightWeight) {
            assertEquals(taggedObjectId, tag.getTagId());
        }
        //assertEquals(message, tag.getMessage());
        //assertEquals(user.toString(), tag.getTagger().toString());
        assertEquals(name, tag.getTagName());
        assertEquals(taggedObjectType, tag.getTaggedObjectType());
    }
}
