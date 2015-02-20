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

package org.netbeans.libs.git.remote.jgit.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitTag;
import org.netbeans.libs.git.remote.GitTag.TagContainer;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class ListTagCommand extends GitCommand {
    public static final boolean KIT = false;
    private Map<String, GitTag> allTags;
    private final ProgressMonitor monitor;
    private final boolean all;
    private final Revision revisionPlaseHolder;
    private final Revision tagNamePlaceHolder;

    public ListTagCommand (JGitRepository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.all = all;
        this.monitor = monitor;
        revisionPlaseHolder = new Revision();
        tagNamePlaceHolder = new Revision();
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    protected void runKit () throws GitException {
//        Repository repository = getRepository().getRepository();
//        Map<String, Ref> tags = repository.getTags();
//        allTags = new LinkedHashMap<String, GitTag>(tags.size());
//        RevWalk walk = new RevWalk(repository);
//        try {
//            for (Map.Entry<String, Ref> e : tags.entrySet()) {
//                GitTag tag;
//                try {
//                    tag = getClassFactory().createTag(walk.parseTag(e.getValue().getLeaf().getObjectId()));
//                } catch (IncorrectObjectTypeException ex) {
//                    tag = getClassFactory().createTag(e.getKey(),
//                            getClassFactory().createRevisionInfo(walk.parseCommit(e.getValue().getLeaf().getObjectId()), getRepository()));
//                }
//                if (all || tag.getTaggedObjectType() == GitObjectType.COMMIT) {
//                    allTags.put(tag.getTagName(), tag);
//                }
//            }
//        } catch (MissingObjectException ex) {
//            throw new GitException.MissingObjectException(ex.getObjectId().getName(), GitObjectType.TAG);
//        } catch (IOException ex) {
//            throw new GitException(ex);
//        } finally {
//            walk.release();
//        }
    }

    public Map<String, GitTag> getTags () {
        return allTags;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "tag"); //NOI18N
        addArgument(0, "-l"); //NOI18N

        addArgument(1, "show-ref"); //NOI18N
        addArgument(1, "--tags"); //NOI18N
        addArgument(1, tagNamePlaceHolder);

        addArgument(2, "show"); //NOI18N
        addArgument(2, "--raw"); //NOI18N
        addArgument(2, revisionPlaseHolder);
    }
    
    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            allTags = new LinkedHashMap<>();
            List<GitTag.TagContainer> list = new ArrayList<GitTag.TagContainer>();
            runner(canceled, 0, list, new Parser() {

                @Override
                public void outputParser(String output, List<GitTag.TagContainer> list) {
                    parseTagOutput(output, list);
                }

                @Override
                public void errorParser(String error) {
                    parseAddError(error);
                }
            });
            for (GitTag.TagContainer container : list) {
                tagNamePlaceHolder.setContent(container.name);
                runner2(canceled, 1, container, new CreateTagCommand.Parser() {

                    @Override
                    public void outputParser(String output, TagContainer container) {
                        CreateTagCommand.parseShowRef(output, container);
                    }

                    @Override
                    public void errorParser(String error) {
                        parseAddError(error);
                    }
                });
                if (container.id != null) {
                    revisionPlaseHolder.setContent(container.id);
                    runner2(canceled, 2, container, new CreateTagCommand.Parser() {

                        @Override
                        public void outputParser(String output, TagContainer container) {
                            CreateTagCommand.parseShowDetails(output, container);
                        }

                        @Override
                        public void errorParser(String error) {
                            parseAddError(error);
                        }
                    });
                    allTags.put(container.name, getClassFactory().createTag(container));
                }
            }
            //command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }
    }
    
    private void runner(ProcessUtils.Canceler canceled, int command, List<GitTag.TagContainer> list, Parser parser) {
        if(canceled.canceled()) {
            return;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output, list);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error);
        }
    }
    
    private void runner2(ProcessUtils.Canceler canceled, int command, TagContainer container, CreateTagCommand.Parser parser) {
        if(canceled.canceled()) {
            return;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output, container);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error);
        }
    }
    
    private void parseTagOutput(String output, List<GitTag.TagContainer> list) {
        //git show-ref --tags -d
        //b2eaccb05d0c3f22174824899c4fd796700e66c6 refs/tags/v2.3.0-rc2
        //15598cf41beed0d86cd2ac443e0f69c5a3b40321 refs/tags/v2.3.0-rc2^{}

        //tag-name
        //tag-name-3
        for (String line : output.split("\n")) { //NOI18N
            if (!line.isEmpty()) {
                GitTag.TagContainer tag =new GitTag.TagContainer();
                tag.name = line.trim();
                list.add(tag);
            }
        }
    }
    
    private void parseAddError(String error) {
        //The following paths are ignored by one of your .gitignore files:
        //folder2
        //Use -f if you really want to add them.
        //fatal: no files added
        processMessages(error);
    }
    
    private abstract class Parser {
        public abstract void outputParser(String output, List<GitTag.TagContainer> list);
        public void errorParser(String error){
        }
    }
}
