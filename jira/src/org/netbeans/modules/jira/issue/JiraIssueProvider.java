/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.issue;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.jira.repository.*;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Ondra Vrabec
 */
@ServiceProviders({
    @ServiceProvider(service=org.netbeans.modules.bugtracking.spi.IssueProvider.class),
    @ServiceProvider(service=JiraIssueProvider.class)
})
public final class JiraIssueProvider extends IssueProvider implements PropertyChangeListener {

    private final Object LOCK = new Object();
    private boolean initialized;
    private HashMap<String, JiraLazyIssue> watchedIssues = new HashMap<String, JiraLazyIssue>(10);
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.jira.tasklist"); //NOI18N
    private static final Level LOG_LEVEL = JiraUtils.isAssertEnabled() ? Level.INFO : Level.FINE;
    private final RequestProcessor rp = new RequestProcessor("JiraTaskListProvider", 1, false);
    private static final String KENAI_REPOSITORY_IDENT_PREFIX = "K##";  //NOI18N
    private static final String STORAGE_KENAI_VERSION = "1";                  //NOI18N
    private static final String STORAGE_COMMON_VERSION = "1";                  //NOI18N
    private final PropertyChangeSupport support;

    public static final String PROPERTY_ISSUE_REMOVED = "issue-removed"; //NOI18N

    public static JiraIssueProvider getInstance() {
        JiraIssueProvider provider = Lookup.getDefault().lookup(JiraIssueProvider.class);
        return provider;
    }

    public JiraIssueProvider () {
        // initialization
        support = new PropertyChangeSupport(this);
        reloadAsync();
    }

    /**
     * Schedules the given issue to be added to the tasklist
     * @param issue issue to add to the tasklist
     * @param openTaskList if set to true, the tasklist will also be asked to open
     */
    public void add (NbJiraIssue issue, boolean openTaskList) {
        URL url = getUrl(issue);
        JiraLazyIssue lazyIssue;
        // local store
        synchronized (LOCK) {
            if (isAdded(url)) return;
            try {
                JiraRepository repository = issue.getRepository();
                repository.removePropertyChangeListener(this);
                repository.addPropertyChangeListener(this);
                // create a representation of the real issue for tasklist
                watchedIssues.put(url.toString(), lazyIssue =
                        (repository instanceof KenaiRepository) ?
                            new KenaiJiraLazyIssue(issue, this) :   // kenai lazy issue
                            new JiraLazyIssue(issue, this));        // common jira lazy issue
            } catch (MalformedURLException e) {
                return;
            }
        }
        saveIntern();

        // schedule the addition to tasklist
        super.add(openTaskList, lazyIssue);
    }

    /**
     * Schedule given issue to be removed from the tasklist
     * @param issue
     */
    public void remove (NbJiraIssue issue) {
        URL url = getUrl(issue);
        remove(url, true);
    }

    /**
     * Tests if given issue is added to the tasklist.
     * @param issue
     * @return true if the given issue is already added.
     */
    public boolean isAdded(NbJiraIssue issue) {
        URL url = getUrl(issue);
        return isAdded(url);
    }

    @Override
    public void removed(LazyIssue lazyIssue) {
        JiraLazyIssue removedIssue;
        synchronized (LOCK) {
            if (!isAdded(lazyIssue.getUrl())) return;
            removedIssue = watchedIssues.remove(lazyIssue.getUrl().toString());
        }
        saveIntern();
        fireIssueRemoved(removedIssue);
    }

    /**
     * These properties are fired:
     * <ul>
     * <li>{@link #PROPERTY_ISSUE_REMOVED} when an issue is removed from the tasklist in other way that with {@link #remove(org.netbeans.modules.jira.issue.NbJiraIssue),
     * e.g. with a Remove action from a popup menu in the tasklist.</li>
     * </ul>
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (Repository.EVENT_ATTRIBUTES_CHANGED.equals(evt.getPropertyName())) {
            if (evt.getOldValue() != null && evt.getOldValue() instanceof Map) {
                Object oldValue = ((Map)evt.getOldValue()).get(JiraRepository.ATTRIBUTE_URL);
                if (oldValue != null && oldValue instanceof String) {
                    String oldRepoUrl = (String) oldValue;
                    LinkedList<JiraLazyIssue> issuesToRefresh = new LinkedList<JiraLazyIssue>();
                    synchronized (LOCK) {
                        // lookup all issues with the same repository url as the changed value
                        for (Map.Entry<String, JiraLazyIssue> e : watchedIssues.entrySet()) {
                            JiraLazyIssue issue = e.getValue();
                            Object sourceRepository = evt.getSource();
                            if (!(issue instanceof KenaiJiraLazyIssue) && sourceRepository != null && sourceRepository.equals(issue.getRepository())) {
                                URL oldUrl = getUrl(oldRepoUrl, issue.issueKey);
                                if (issue.getUrl().toString().equals(oldUrl.toString()))  {
                                    LOG.log(Level.FINE, "propertyChange: Issue {0} with url {1} needs to be refreshed, repository's url {2} has changed", //NOI18N
                                            new String[] {issue.toString(), oldUrl.toString(), oldRepoUrl});
                                    issuesToRefresh.add(issue);
                                }
                            }
                        }
                    }
                    // refresh issues
                    for (JiraLazyIssue issue : issuesToRefresh) {
                        remove(issue.getUrl(), false);
                        add(issue.getName(), issue.issueKey, issue.getRepository());
                    }
                    // store new issues
                    if (!issuesToRefresh.isEmpty()) {
                        saveIntern();
                    }
                }
            }
        } else if (Kenai.PROP_LOGIN.equals(evt.getPropertyName())) {
            // kenai issues need instantiated repository so they can be shown in tasklist
            // but some (e.g. private kenai projects) cannot be instantiated without being logged in. So kenai issues need to be notified
            // when user loggs in so the repository can be created.
            rp.post(new Runnable() { // do not block here
                public void run() {
                    notifyKenaiLogin();
                }
            });
        }
    }

    /**
     * Removes all issues from the tasklist which belong to the given repository
     * @param repository
     */
    public void removeAllFor (JiraRepository repository) {
        LinkedList<JiraLazyIssue> issuesToRemove = new LinkedList<JiraLazyIssue>();
                 synchronized (LOCK) {
            // lookup all issues with the same repository url as the changed value
            for (Map.Entry<String, JiraLazyIssue> e : watchedIssues.entrySet()) {
                JiraLazyIssue issue = e.getValue();
                if (!(issue instanceof KenaiJiraLazyIssue) && repository == issue.getRepository()) {
                    LOG.log(Level.FINE, "removeAllFor: issue {0} repository {1} has been removed", new String[]{issue.toString(), repository.toString()}); //NOI18N
                    issuesToRemove.add(issue);
                }
            }
        }
        // remove issues
        for (JiraLazyIssue issue : issuesToRemove) {
            remove(issue.getUrl(), false);
        }
        // store issues
        if (!issuesToRemove.isEmpty()) {
            saveIntern();
        }
    }

    /**
     * Call when an issue is loaded for the first time.
     * @param issue cannot be null
     */
    public void notifyIssueCreated (NbJiraIssue issue) {
        URL url = getUrl(issue);
        JiraLazyIssue lazyIssue = null;
        synchronized (LOCK) {
            lazyIssue = watchedIssues.get(url.toString());
        }
        if (lazyIssue != null) {
            lazyIssue.setIssueReference(issue);
        }
    }

    // **** private methods ***** //
    private boolean isAdded(URL url) {
        initializeIssues();
        if (url == null) {
            return false;
        }
        synchronized (LOCK) {
            return watchedIssues.containsKey(url.toString());
        }
    }

    private static URL getUrl (NbJiraIssue issue) {
        return getUrl(issue.getRepository().getUrl(), issue.getID());
    }

    private static URL getUrl(String repositoryUrl, String issueKey) {
        String url = JiraRepositoryConnector.getTaskUrlFromKey(repositoryUrl, issueKey);
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            LOG.log(LOG_LEVEL, null, ex);
        }
        try {
            return new URL(repositoryUrl + "#" + issueKey);             //NOI18N
        } catch (MalformedURLException ex) {
            LOG.log(LOG_LEVEL, null, ex);
            return null;
        }
    }

    private void reloadAsync() {
        rp.post(new Runnable () {
            public void run() {
                initializeIssues();
            }
        });
    }

    private void saveIntern() {
        JiraLazyIssue[] lazyIssues;
        synchronized(LOCK) {
            lazyIssues = watchedIssues.values().toArray(new JiraLazyIssue[watchedIssues.size()]);
        }
        final JiraLazyIssue[] lazyIssuesToSave = lazyIssues;
        rp.post(new Runnable () {
            public void run() {
                initializeIssues();
                LOG.log(Level.FINE, "saveIntern: saving issues");       //NOI18N
                HashMap<String, List<String>> issues = new HashMap<String, List<String>>();
                for (JiraLazyIssue issue : lazyIssuesToSave) {
                    String repositoryIdent = null;
                    boolean isKenai = false;
                    if (issue instanceof KenaiJiraLazyIssue) {
                        JiraRepository repo = issue.getRepository();
                        if (repo != null && !(repo instanceof KenaiRepository)) {
                            LOG.warning("saveIntern: KenaiJiraIssue has no kenai repository: " + repo); //NOI18N
                        } else {
                            // kenai repository is identified by project's name, not by it's url
                            repositoryIdent = KENAI_REPOSITORY_IDENT_PREFIX + (repo == null
                                    ? ((KenaiJiraLazyIssue)issue).projectName
                                    : ((KenaiRepository) repo).getDisplayName());
                            isKenai = true;
                        }
                    } else {
                        repositoryIdent = issue.getRepositoryUrl();
                    }
                    if (repositoryIdent != null) {
                        List<String> issueAttributes = issues.get(repositoryIdent);
                        if (issueAttributes == null) {
                            issueAttributes = new LinkedList<String>();
                            issueAttributes.add(isKenai ? STORAGE_KENAI_VERSION : STORAGE_COMMON_VERSION);
                        }
                        issueAttributes.add(issue.issueKey);            // key
                        issueAttributes.add(issue.getName());           // description
                        if (isKenai) {
                            issueAttributes.add(issue.getUrl().toString()); // url needed only for kenai repos
                        }
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("saveIntern: saving " + issueAttributes + " for repo: " + repositoryIdent); //NOI18N
                        }
                        issues.put(repositoryIdent, issueAttributes);
                    }
                }
                // save permanently
                JiraStorageManager.getInstance().setTaskListIssues(issues);
            }
        });
    }

    private void initializeIssues () {
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            initialized = true;
            LOG.finer("initializeIssues: reloading saved issues");      //NOI18N
            // load from storage
            Map<String, List<String>> repositoryIssues = JiraStorageManager.getInstance().getTaskListIssues();
            if (repositoryIssues.size() == 0) {
                LOG.fine("initializeIssues: no saved issues");          //NOI18N
                return;
            }
            addCommonIssues(repositoryIssues);
            addKenaiIssues(repositoryIssues);
        }
    }

    private String getNextAttribute (ListIterator<String> it) {
        String attr = null;
        if (it.hasNext()) {
            attr = it.next();
        }
        return attr;
    }

    private void addCommonIssues (Map<String, List<String>> repositoryIssues) {
        JiraRepository[] repositories = Jira.getInstance().getRepositories();
            for (JiraRepository repository : repositories) {
                // all issues for this repository
                List<String> issueAttributes = repositoryIssues.get(repository.getUrl());
                if (issueAttributes != null && issueAttributes.size() > 1) {
                    ListIterator<String> it = issueAttributes.listIterator();
                    if (!STORAGE_COMMON_VERSION.equals(it.next())) {
                        LOG.log(Level.WARNING, "Old unsupported storage version, expecting {0}", STORAGE_COMMON_VERSION); //NOI18N
                        break;
                    }
                    for (; it.hasNext(); ) {
                        String issueKey = getNextAttribute(it);
                        String issueName = getNextAttribute(it);
                        if (issueKey == null || issueName == null) {
                            LOG.log(Level.WARNING, "Corrupted issue attributes: {0} {1}", new String[]{issueKey, issueName}); //NOI18N
                            break;
                        }
                        add(issueName, issueKey, repository);
                    }
                    repository.addPropertyChangeListener(this);
                    // remove processed attributes
                    repositoryIssues.remove(repository.getUrl());
                }
            }
    }

    private void addKenaiIssues (Map<String, List<String>> repositoryIssues) {
        // now what remains are kenai issues and non-existant repositories
        boolean kenaiIssueAdded = false;
        for (Map.Entry<String, List<String>> e : repositoryIssues.entrySet()) {
            String projectName = e.getKey();
            if (projectName.startsWith(KENAI_REPOSITORY_IDENT_PREFIX)) { // is kenai
                projectName = projectName.substring(KENAI_REPOSITORY_IDENT_PREFIX.length());
                List<String> issueAttributes = e.getValue();
                if (issueAttributes != null && issueAttributes.size() > 1) {
                    ListIterator<String> it = issueAttributes.listIterator();
                    if (!STORAGE_KENAI_VERSION.equals(it.next())) {
                        LOG.log(Level.WARNING, "Old unsupported storage version for kenai issues, expecting {0}", STORAGE_KENAI_VERSION); //NOI18N
                        break;
                    }
                    for (; it.hasNext(); ) {
                        String issueKey = getNextAttribute(it);
                        String issueName = getNextAttribute(it);
                        String url = getNextAttribute(it);
                        if (issueKey == null || issueName == null || url == null) {
                            LOG.log(Level.WARNING, "Corrupted kenai issue attributes: {0} {1} {2}", new String[]{issueKey, issueName, url}); //NOI18N
                            break;
                        }
                        URL issueUrl;
                        try {
                            issueUrl = new URL(url);
                        } catch (MalformedURLException ex) {
                            LOG.log(Level.INFO, null, ex);
                            continue;
                        }
                        add(issueName, issueUrl, issueKey, projectName);
                        kenaiIssueAdded = true;
                    }
                }
            }
        }
        if (kenaiIssueAdded) {
            Kenai.getDefault().removePropertyChangeListener(this);
            Kenai.getDefault().addPropertyChangeListener(this);
        }
    }

    private void remove (URL url, boolean savePermanently) {
        JiraLazyIssue lazyIssue;
        synchronized (LOCK) {
            if (!isAdded(url)) return;
            lazyIssue = watchedIssues.remove(url.toString());
        }
        if (savePermanently) {
            saveIntern();
        }
        // notify tasklist
        super.remove(lazyIssue);
    }

    private void add (String issueName, URL issueUrl, String issueKey, String projectName) {
        KenaiJiraLazyIssue issue;
        synchronized (LOCK) {
            if (isAdded(issueUrl)) return;
            watchedIssues.put(issueUrl.toString(), issue = new KenaiJiraLazyIssue(issueName, issueUrl, issueKey, projectName, this));
        }
        // notify tasklist
        super.add(issue);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "initializeIssues: issue added: {0}", issue); //NOI18N
        }
    }

    private void add (String issueName, String issueKey, JiraRepository repository) {
        URL issueUrl = getUrl(repository.getUrl(), issueKey);
        JiraLazyIssue issue;
        synchronized (LOCK) {
            if (issueUrl == null || isAdded(issueUrl)) return;
            watchedIssues.put(issueUrl.toString(), issue = new JiraLazyIssue(issueName, issueUrl, issueKey, repository, this));
        }
        // notify tasklist
        super.add(issue);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "initializeIssues: issue added: {0}", issue); //NOI18N
        }
    }

    private static void runCancellableCommand(Runnable runnable, String progressMessage) {
        RequestProcessor.Task task = Jira.getInstance().getRequestProcessor().post(runnable);
        ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage, task); //NOI18N
        handle.start();
        task.waitFinished();
        handle.finish();
    }

    private NbJiraIssue getIssue(final JiraRepository repository, final String issueKey) {
        assert !EventQueue.isDispatchThread();
        // XXX is there a simpler way to obtain an issue?
        int status = repository.getIssueCache().getStatus(issueKey);
        final NbJiraIssue[] issue = new NbJiraIssue[1];
        if (status == IssueCache.ISSUE_STATUS_UNKNOWN) { // not yet cached
            Runnable runnable = new Runnable() {
                public void run() {
                    LOG.log(Level.FINE, "getIssue: creating issue {0}", repository.getUrl() + "#" + issueKey); //NOI18N
                    issue[0] = (NbJiraIssue) repository.getIssue(issueKey);
                }
            };
            runCancellableCommand(runnable, NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.loadingIssue"));
        } else {
            LOG.log(Level.FINER, "getIssue: getting issue {0} from the cache", repository.getUrl() + "#" + issueKey); //NOI18N
            issue[0] = (NbJiraIssue) repository.getIssueCache().getIssue(issueKey);
        }
        return issue[0];
    }

    private void fireIssueRemoved(JiraLazyIssue lazyIssue) {
        NbJiraIssue issue = lazyIssue.issueRef.get();
        if (issue != null) {
            support.firePropertyChange(PROPERTY_ISSUE_REMOVED, issue, null);
        }
    }

    /*
     * Notifies all kenai issues that user has logged on. Private kenai projects cannot be instantiated without being logged in
     * and issue tracking repository cannot be created.
     */
    private void notifyKenaiLogin () {
        synchronized (LOCK) {
            for (JiraLazyIssue issue : watchedIssues.values()) {
                if (issue instanceof KenaiJiraLazyIssue) {
                    ((KenaiJiraLazyIssue) issue).notifyKenaiLogin();
                }
            }
        }
    }

    /**
     * Common Jira representation of LazyIssue
     */
    private static class JiraLazyIssue extends LazyIssue {
        private final String issueKey;
        /**
         *  cached repository for the issue
         */
        private WeakReference<JiraRepository> repositoryRef;
        private final JiraIssueProvider provider;
        private WeakReference<NbJiraIssue> issueRef;
        private PropertyChangeListener issueListener;

        public JiraLazyIssue (NbJiraIssue issue, JiraIssueProvider provider) throws MalformedURLException {
            super(JiraIssueProvider.getUrl(issue), issue.getDisplayName());
            this.issueKey = issue.getID();
            this.provider = provider;
            this.repositoryRef = new WeakReference<JiraRepository>(issue.getRepository());
            this.issueRef = new WeakReference<NbJiraIssue>(issue);
            attachIssueListener(issue);
        }

        public JiraLazyIssue (String name, URL url, String issueKey, JiraRepository repository, JiraIssueProvider provider) {
            super(url, name);
            this.issueKey = issueKey;
            this.repositoryRef = new WeakReference<JiraRepository>(repository);
            this.provider = provider;
            this.issueRef = new WeakReference<NbJiraIssue>(null);
        }

        @Override
        public NbJiraIssue getIssue() {
            NbJiraIssue issue = issueRef.get();
            if (issue == null) {
                JiraRepository repository = getRepository();
                if (repository == null) {
                    LOG.log(Level.INFO, "Repository unavailable for {0}", getUrl().toString()); //NOI18N
                    if (canBeAutoRemoved()) {
                        // no repository found for this issue and the issue can be removed automaticaly
                        provider.remove(getUrl(), true);
                    }
                } else {
                    issue = provider.getIssue(repository, issueKey);
                }
                setIssueReference(issue);
            }
            return issue;
        }

        private JiraRepository getRepository() {
            JiraRepository repository = repositoryRef.get();
            return repository;
        }

        /**
         * Sets the reference to the issue and attaches an issue listener
         * @param issue if null then this only clears the reference.
         */
        private void setIssueReference (NbJiraIssue issue) {
            issueRef = new WeakReference<NbJiraIssue>(issue);
            if (issue != null) {
                applyChangesFor(issue);
                attachIssueListener(issue);
            }
        }

        private void attachIssueListener (NbJiraIssue issue) {
            if (issueListener == null) {
                issueListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        NbJiraIssue issue = issueRef.get();
                        if (Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName()) && issue != null) {
                            // issue has somehow changed, checks for its changes and apply them in the tasklist
                            applyChangesFor(issue);
                        }
                    }
                };
            }
            LOG.log(Level.FINE, "attachIssueListener: on issue {0}", issue.toString());
            issue.addPropertyChangeListener(WeakListeners.propertyChange(issueListener, issue));
        }

        private void applyChangesFor (NbJiraIssue issue) {
            boolean requiresSave = false;
            if (!getName().equals(issue.getDisplayName())) {
                setName(issue.getDisplayName());
                requiresSave = true;
            }
            if (requiresSave) {
                provider.saveIntern();
            }
        }

        @Override
        public String getRepositoryUrl() {
            String repoUrl = null;
            JiraRepository repository = repositoryRef.get();
            if (repository != null) {
                repoUrl = repository.getUrl();
            }
            return repoUrl;
        }

        @Override
        public List<? extends Action> getActions() {
            List<AbstractAction> actions = new LinkedList<AbstractAction>();
            actions.add(new AbstractAction(NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.resolveAction")) { //NOI18N
                public void actionPerformed(ActionEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            final NbJiraIssue issue = getIssue();
                            if (issue == null) {
                                LOG.fine("Resole action: null issue returned"); //NOI18N
                            } else {
                                if (!issue.isResolveAllowed()) {
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                            NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.resolveAction.notPermitted"),
                                            NotifyDescriptor.INFORMATION_MESSAGE));
                                    return;
                                }
                                ResolveIssuePanel panel = new ResolveIssuePanel(issue);
                                String title = NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.resolveIssueButton.text"); //NOI18N
                                if (BugtrackingUtil.show(panel, title, title)) {
                                    LOG.finer("Resole action: resolving..."); //NOI18N
                                    String pattern = NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.resolveIssueMessage"); //NOI18N
                                    final Resolution resolution = panel.getSelectedResolution();
                                    final String comment = panel.getComment();
                                    runCancellableCommand(new Runnable () {
                                        public void run() {
                                            issue.resolve(resolution, comment);
                                            if (issue.submitAndRefresh()) {
                                                issue.open();
                                            }
                                        }
                                    }, MessageFormat.format(pattern, issue.getID()));
                                }
                            }
                        }
                    });
                }

                @Override
                public boolean isEnabled() {
                    // try to disable the action for cached closed issues
                    boolean allowed = true;
                    NbJiraIssue issue = issueRef.get();
                    if (issue != null) {
                        allowed = issue.isResolveAllowed();
                    }
                    return allowed;
                }
            });
            actions.add(new AbstractAction(NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.logWorkDoneAction")) { //NOI18N
                public void actionPerformed(ActionEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            final NbJiraIssue issue = getIssue();
                            if (issue == null) {
                                LOG.fine("Log Work done action: null issue returned"); //NOI18N
                            } else {
                                final WorkLogPanel panel = new WorkLogPanel(issue);
                                if (panel.showDialog()) {
                                    LOG.finer("Log Work done action: logging..."); //NOI18N
                                    String pattern = NbBundle.getMessage(JiraIssueProvider.class, "JiraIssueProvider.logWorkDoneMessage"); // NOI18N
                                    String message = MessageFormat.format(pattern, issue.getID());
                                    runCancellableCommand(new Runnable() {
                                        public void run() {
                                            issue.addWorkLog(panel.getStartDate(), panel.getTimeSpent(), panel.getDescription());
                                            int remainingEstimate = panel.getRemainingEstimate();
                                            if (remainingEstimate != -1) { // -1 means auto-adjust
                                                issue.setFieldValue(NbJiraIssue.IssueField.ESTIMATE, (remainingEstimate + panel.getTimeSpent()) + ""); // NOI18N
                                            }
                                            if (issue.submitAndRefresh()) {
                                                issue.open();
                                            }
                                        }
                                    }, message);
                                }
                            }
                        }
                    });
                }
            });
            return actions;
        }

        /**
         * Returns true if the issue can be automatically removed, which should not be met for kenai issues
         * @return
         */
        protected boolean canBeAutoRemoved() {
            return true;
        }

        /**
         * Stores a reference to the repository for quick access
         * @param repository
         */
        protected void setRepositoryReference(JiraRepository repository) {
            if (repository != null) {
                repositoryRef = new WeakReference<JiraRepository>(repository);
            }
        }
    }

    /**
     * Specific kenai jira lazy issue.
     */
    private static final class KenaiJiraLazyIssue extends JiraLazyIssue {

        private final String projectName;
        private boolean loginStatusChanged = true;

        public KenaiJiraLazyIssue (NbJiraIssue issue, JiraIssueProvider provider) throws MalformedURLException {
            super(issue, provider);
            Repository repo = issue.getRepository();
            if (!(repo instanceof KenaiRepository)) {
                throw new IllegalStateException("Cannot instantiate with a non kenai issue: " + issue); //NOI18N
            }
            projectName = ((KenaiRepository) repo).getDisplayName();
        }

        public KenaiJiraLazyIssue (String name, URL url, String issueKey, String projectName, JiraIssueProvider provider) {
            super(name, url, issueKey, null, provider);
            this.projectName = projectName;
        }

        protected KenaiRepository lookupRepository () {
            KenaiRepository kenaiRepo = null;
            Repository repo = null;
            if (loginStatusChanged) {
                try {
                    LOG.log(Level.FINE, "KenaiJiraLazyIssue.lookupRepository: getting repository for: " + projectName);
                    repo = KenaiUtil.getKenaiBugtrackingRepository(projectName);
                } catch (KenaiException ex) {
                    LOG.log(Level.INFO, "KenaiJiraLazyIssue.lookupRepository: getting repository for " + projectName, ex);
                }
                loginStatusChanged = false;
            }
            if (repo != null && repo instanceof KenaiRepository) {
                kenaiRepo = (KenaiRepository) repo;
            } else {
                LOG.log(Level.FINE, "KenaiJiraLazyIssue.lookupRepository: no repository for: " + projectName);
            }
            return kenaiRepo;
        }

        @Override
        protected boolean canBeAutoRemoved() {
            return false;
        }

        @Override
        /**
         * Stored Kenai issues have no repository url, but rather a project name as a repository identifier,
         * so the repository must be looked up.
         */
        public String getRepositoryUrl() {
            String repoUrl = super.getRepositoryUrl();
            if (repoUrl == null) {
                KenaiRepository repo = lookupRepository();
                if (repo != null) {
                    setRepositoryReference(repo);
                    repoUrl = repo.getUrl();
                }
            }
            return repoUrl;
        }

        private void notifyKenaiLogin () {
            loginStatusChanged = true;
            setValid(false);
        }
    }
}
