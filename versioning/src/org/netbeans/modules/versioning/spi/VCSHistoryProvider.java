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
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import javax.swing.event.ChangeListener;


/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSHistoryProvider {
    
    public abstract void addChangeListener(ChangeListener l);
    
    public abstract void removeChangeListener(ChangeListener l);
    
    public abstract HistoryEntry[] getHistory(File[] files, Date fromDate);
    
    public abstract Action createShowHistoryAction(File[] files);
    
    public static final class HistoryEntry {
        private Date dateTime;
        private String message;
        private File[] files;
        private String usernameShort;
        private String username;
        private String revisionShort;
        private String revision;
        private Action[] actions;
        private RevisionProvider rp;
        private MessageEditProvider mep;
        
        public HistoryEntry(
                File[] files, 
                Date dateTime, 
                String message, 
                String username, 
                String usernameShort, 
                String revision, 
                String revisionShort, 
                Action[] actions, 
                RevisionProvider rp) 
        {
            assert files != null && files.length > 0 : "a history entry must have at least one file"; // NOI18N
            assert revision != null && revision != null : "a history entry must have a revision";     // NOI18N
            assert dateTime != null : "a history entry must have a date";                                 // NOI18N
            
            this.files = files;
            this.dateTime = dateTime;
            this.message = message;
            this.username = username;
            this.usernameShort = usernameShort;
            this.revision = revision;
            this.revisionShort = revisionShort;
            this.actions = actions;
            this.rp = rp;
        }
        
        public HistoryEntry(
                File[] files, 
                Date dateTime, 
                String message, 
                String username, 
                String usernameShort, 
                String revision, 
                String revisionShort, 
                Action[] actions, 
                RevisionProvider rp,
                MessageEditProvider mep) 
        {
            this(files, dateTime, message, username, usernameShort, revision, revisionShort, actions, rp);
            this.mep = mep;
        }
        
        public boolean canEdit() {
            return mep != null;
        }
        public Date getDateTime() {
            return dateTime;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) throws IOException {
            if(!canEdit()) throw new IllegalStateException("This entry is read-only");
            mep.setMessage(message);
            this.message = message;
        }
        public File[] getFiles() {
            return files;
        }
        public String getUsername() {
            return username;
        }
        public String getUsernameShort() {
            return usernameShort;
        }
        public String getRevision() {
            return revision;
        }
        public String getRevisionShort() {
            return revisionShort;
        }
        public Action[] getActions() {
            return actions;
        }
        public void getRevisionFile(File originalFile, File revisionFile) {
            rp.getRevisionFile(originalFile, revisionFile);
        }
        RevisionProvider getRevisionProvier() {
            return rp;
        }
        MessageEditProvider getMessageEditProvider() {
            return mep;
        }
    }
    
    public interface RevisionProvider {
        void getRevisionFile(File originalFile, File revisionFile);
    }
    
    public interface MessageEditProvider {
        void setMessage(String message) throws IOException;
    }
}
