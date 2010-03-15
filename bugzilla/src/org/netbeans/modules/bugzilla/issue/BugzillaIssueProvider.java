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

package org.netbeans.modules.bugzilla.issue;

import java.beans.PropertyChangeEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiAccessor;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
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
    @ServiceProvider(service=BugzillaIssueProvider.class)
})
public final class BugzillaIssueProvider extends IssueProvider implements PropertyChangeListener {

    private static BugzillaIssueProvider instance;
    private final Object LOCK = new Object();
    private boolean initialized;
    private HashMap<String, BugzillaLazyIssue> watchedIssues = new HashMap<String, BugzillaLazyIssue>(10);
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.Bugzilla.tasklist"); //NOI18N
    private static final Level LOG_LEVEL = BugzillaUtil.isAssertEnabled() ? Level.INFO : Level.FINE;
    private final RequestProcessor rp = new RequestProcessor("BugzillaTaskListProvider", 1, false);
    private static final String KENAI_REPOSITORY_IDENT_PREFIX = "K##";  //NOI18N
    private static final String STORAGE_KENAI_VERSION = "1";                  //NOI18N
    private static final String STORAGE_COMMON_VERSION = "1";                  //NOI18N
    private final PropertyChangeSupport support;

    public static final String PROPERTY_ISSUE_REMOVED = "issue-removed"; //NOI18N

    public static synchronized BugzillaIssueProvider getInstance() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(BugzillaIssueProvider.class);
        }
        return instance;
    }

    public BugzillaIssueProvider () {
        // initialization
        support = new PropertyChangeSupport(this);
        reloadAsync();
    }

    /**
     * Schedules the given issue to be added to the tasklist
     * @param issue issue to add to the tasklist
     * @param openTaskList if set to true, the tasklist will also be asked to open
     */
    public void add (BugzillaIssue issue, boolean openTaskList) {
        URL url = getUrl(issue);
        BugzillaLazyIssue lazyIssue;
        // local store
        synchronized (LOCK) {
            if (isAdded(url)) return;
            try {
                BugzillaRepository repository = issue.getBugzillaRepository();
                repository.removePropertyChangeListener(this);
                repository.addPropertyChangeListener(this);
                // create a representation of the real issue for tasklist
                watchedIssues.put(url.toString(), lazyIssue =
                        (repository instanceof KenaiRepository) ?
                            new KenaiBugzillaLazyIssue(issue, this) :   // kenai lazy issue
                            new BugzillaLazyIssue(issue, this));        // common Bugzilla lazy issue
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
    public void remove (BugzillaIssue issue) {
        URL url = getUrl(issue);
        remove(url, true);
    }

    /**
     * Tests if given issue is added to the tasklist.
     * @param issue
     * @return true if the given issue is already added.
     */
    public boolean isAdded(BugzillaIssue issue) {
        URL url = getUrl(issue);
        return isAdded(url);
    }

    @Override
    public void removed(LazyIssue lazyIssue) {
        BugzillaLazyIssue removedIssue;
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
     * <li>{@link #PROPERTY_ISSUE_REMOVED} when an issue is removed from the tasklist in other way that with {@link #remove(org.netbeans.modules.Bugzilla.issue.BugzillaIssue),
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Repository.EVENT_ATTRIBUTES_CHANGED.equals(evt.getPropertyName())) {
            if (evt.getOldValue() != null && evt.getOldValue() instanceof Map) {
                Object oldValue = ((Map)evt.getOldValue()).get(BugzillaRepository.ATTRIBUTE_URL);
                if (oldValue != null && oldValue instanceof String) {
                    String oldRepoUrl = (String) oldValue;
                    LinkedList<BugzillaLazyIssue> issuesToRefresh = new LinkedList<BugzillaLazyIssue>();
                    synchronized (LOCK) {
                        // lookup all issues with the same repository url as the changed value
                        for (Map.Entry<String, BugzillaLazyIssue> e : watchedIssues.entrySet()) {
                            BugzillaLazyIssue issue = e.getValue();
                            Object sourceRepository = evt.getSource();
                            if (!(issue instanceof KenaiBugzillaLazyIssue) && sourceRepository != null && sourceRepository.equals(issue.getRepository())) {
                                URL oldUrl = getUrl(oldRepoUrl, issue.issueId);
                                if (issue.getUrl().toString().equals(oldUrl.toString()))  {
                                    LOG.log(Level.FINE, "propertyChange: Issue {0} with url {1} needs to be refreshed, repository's url {2} has changed", //NOI18N
                                            new String[] {issue.toString(), oldUrl.toString(), oldRepoUrl});
                                    issuesToRefresh.add(issue);
                                }
                            }
                        }
                    }
                    // refresh issues
                    for (BugzillaLazyIssue issue : issuesToRefresh) {
                        remove(issue.getUrl(), false);
                        add(issue.getName(), issue.issueId, issue.getRepository());
                    }
                    // store new issues
                    if (!issuesToRefresh.isEmpty()) {
                        saveIntern();
                    }
                }
            }
        } 
    }

    /**
     * Removes all issues from the tasklist which belong to the given repository
     * @param repository
     */
    public void removeAllFor (BugzillaRepository repository) {
        LinkedList<BugzillaLazyIssue> issuesToRemove = new LinkedList<BugzillaLazyIssue>();
                 synchronized (LOCK) {
            // lookup all issues with the same repository url as the changed value
            for (Map.Entry<String, BugzillaLazyIssue> e : watchedIssues.entrySet()) {
                BugzillaLazyIssue issue = e.getValue();
                if (!(issue instanceof KenaiBugzillaLazyIssue) && repository == issue.getRepository()) {
                    LOG.log(Level.FINE, "removeAllFor: issue {0} repository {1} has been removed", new String[]{issue.toString(), repository.toString()}); //NOI18N
                    issuesToRemove.add(issue);
                }
            }
        }
        // remove issues
        for (BugzillaLazyIssue issue : issuesToRemove) {
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
    public void notifyIssueCreated (BugzillaIssue issue) {
        URL url = getUrl(issue);
        BugzillaLazyIssue lazyIssue = null;
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

    private static URL getUrl (BugzillaIssue issue) {
        return getUrl(issue.getRepository().getUrl(), issue.getID());
    }

    private static URL getUrl(String repositoryUrl, String issueId) {
        String url = Bugzilla.getInstance().getRepositoryConnector().getTaskUrl(repositoryUrl, issueId);
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            LOG.log(LOG_LEVEL, null, ex);
        }
        try {
            return new URL(repositoryUrl + "#" + issueId);             //NOI18N
        } catch (MalformedURLException ex) {
            LOG.log(LOG_LEVEL, null, ex);
            return null;
        }
    }

    private void reloadAsync() {
        rp.post(new Runnable () {
            @Override
            public void run() {
                initializeIssues();
            }
        });
    }

    private void saveIntern() {
        BugzillaLazyIssue[] lazyIssues;
        synchronized(LOCK) {
            lazyIssues = watchedIssues.values().toArray(new BugzillaLazyIssue[watchedIssues.size()]);
        }
        final BugzillaLazyIssue[] lazyIssuesToSave = lazyIssues;
        rp.post(new Runnable () {
            @Override
            public void run() {
                initializeIssues();
                LOG.log(Level.FINE, "saveIntern: saving issues");       //NOI18N
                HashMap<String, List<String>> issues = new HashMap<String, List<String>>();
                for (BugzillaLazyIssue issue : lazyIssuesToSave) {
                    String repositoryIdent = null;
                    boolean isKenai = false;
                    if (issue instanceof KenaiBugzillaLazyIssue) {
                        BugzillaRepository repo = issue.getRepository();
                        if (repo != null && !(repo instanceof KenaiRepository)) {
                            LOG.warning("saveIntern: KenaiBugzillaIssue has no kenai repository: " + repo); //NOI18N
                        } else {
                            // kenai repository is identified by project's name, not by it's url
                            repositoryIdent = KENAI_REPOSITORY_IDENT_PREFIX + (repo == null
                                    ? ((KenaiBugzillaLazyIssue)issue).projectName
                                    : ((KenaiRepository) repo).getProductName());
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
                        issueAttributes.add(issue.issueId);            // issue id
                        issueAttributes.add(issue.getName());          // description
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
                BugzillaConfig.getInstance().setTaskListIssues(issues);
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
            Map<String, List<String>> repositoryIssues = BugzillaConfig.getInstance().getTaskListIssues();
            if (repositoryIssues.isEmpty()) {
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
        BugzillaRepository[] repositories = Bugzilla.getInstance().getRepositories();
        for (BugzillaRepository repository : repositories) {
            // all issues for this repository
            List<String> issueAttributes = repositoryIssues.get(repository.getUrl());
            if (issueAttributes != null && issueAttributes.size() > 1) {
                ListIterator<String> it = issueAttributes.listIterator();
                if (!STORAGE_COMMON_VERSION.equals(it.next())) {
                    LOG.log(Level.WARNING, "Old unsupported storage version, expecting {0}", STORAGE_COMMON_VERSION); //NOI18N
                    break;
                }
                for (; it.hasNext();) {
                    String issueId = getNextAttribute(it);
                    String issueName = getNextAttribute(it);
                    if (issueId == null || issueName == null) {
                        LOG.log(Level.WARNING, "Corrupted issue attributes: {0} {1}", new String[]{issueId, issueName}); //NOI18N
                        break;
                    }
                    add(issueName, issueId, repository);
                }
                repository.addPropertyChangeListener(this);
                // remove processed attributes
                repositoryIssues.remove(repository.getUrl());
            }
        }
    }

    private void addKenaiIssues (Map<String, List<String>> repositoryIssues) {
        // now what remains are kenai issues and non-existant repositories
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
                        String issueId = getNextAttribute(it);
                        String issueName = getNextAttribute(it);
                        String url = getNextAttribute(it);
                        if (issueId == null || issueName == null || url == null) {
                            LOG.log(Level.WARNING, "Corrupted kenai issue attributes: {0} {1} {2}", new String[]{issueId, issueName, url}); //NOI18N
                            break;
                        }
                        URL issueUrl;
                        try {
                            issueUrl = new URL(url);
                        } catch (MalformedURLException ex) {
                            LOG.log(Level.INFO, null, ex);
                            continue;
                        }
                        add(issueName, issueUrl, issueId, projectName);
                        KenaiAccessor ka = KenaiUtil.getKenaiAccessor();
                        if(ka != null) {
                            String host = issueUrl.getHost();
                            Map<String, PropertyChangeListener> kl = getKenaiListeners();
                            PropertyChangeListener l = kl.get(host);
                            if (l == null) {
                                // kenai host not registered yet
                                l = new KenaiListener(host);
                                ka.addPropertyChangeListener(l, host);
                                kl.put(host, l);
                            }
                        }
                    }
                }
            }
        }
    }

    private class KenaiListener implements PropertyChangeListener {
        private final String kenaiHost;

        public KenaiListener(String kenaiHost) {
            this.kenaiHost = kenaiHost;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (KenaiAccessor.PROP_LOGIN.equals(evt.getPropertyName())) {
                // kenai issues need instantiated repository so they can be shown in tasklist
                // but some (e.g. private kenai projects) cannot be instantiated without being logged in. So kenai issues need to be notified
                // when user loggs in so the repository can be created.
                rp.post(new Runnable() { // do not block here
                    @Override
                    public void run() {
                        notifyKenaiLogin(kenaiHost);
                    }
                });
            }
        }
    }

    private Map<String, PropertyChangeListener> kenaiListeners;
    private Map<String, PropertyChangeListener> getKenaiListeners() {
        if (kenaiListeners == null) {
            kenaiListeners = new HashMap<String, PropertyChangeListener>();
        }
        return kenaiListeners;
    }
    
    private void remove (URL url, boolean savePermanently) {
        BugzillaLazyIssue lazyIssue;
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

    private void add (String issueName, URL issueUrl, String issueId, String projectName) {
        KenaiBugzillaLazyIssue issue;
        synchronized (LOCK) {
            if (isAdded(issueUrl)) return;
            watchedIssues.put(issueUrl.toString(), issue = new KenaiBugzillaLazyIssue(issueName, issueUrl, issueId, projectName, this));
        }
        // notify tasklist
        super.add(issue);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "initializeIssues: issue added: {0}", issue); //NOI18N
        }
    }

    private void add (String issueName, String issueId, BugzillaRepository repository) {
        URL issueUrl = getUrl(repository.getUrl(), issueId);
        BugzillaLazyIssue issue;
        synchronized (LOCK) {
            if (issueUrl == null || isAdded(issueUrl)) return;
            watchedIssues.put(issueUrl.toString(), issue = new BugzillaLazyIssue(issueName, issueUrl, issueId, repository, this));
        }
        // notify tasklist
        super.add(issue);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "initializeIssues: issue added: {0}", issue); //NOI18N
        }
    }

    private static void runCancellableCommand(Runnable runnable, String progressMessage) {
        RequestProcessor.Task task = Bugzilla.getInstance().getRequestProcessor().post(runnable);
        ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage, task); //NOI18N
        handle.start();
        task.waitFinished();
        handle.finish();
    }

    private BugzillaIssue getIssue(final BugzillaRepository repository, final String issueId) {
        assert !EventQueue.isDispatchThread();
        // XXX is there a simpler way to obtain an issue?
        int status = repository.getIssueCache().getStatus(issueId);
        final BugzillaIssue[] issue = new BugzillaIssue[1];
        if (status == IssueCache.ISSUE_STATUS_UNKNOWN) { // not yet cached
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    LOG.log(Level.FINE, "getIssue: creating issue {0}", repository.getUrl() + "#" + issueId); //NOI18N
                    issue[0] = (BugzillaIssue) repository.getIssue(issueId);
                }
            };
            runCancellableCommand(runnable, NbBundle.getMessage(BugzillaIssueProvider.class, "BugzillaIssueProvider.loadingIssue"));
        } else {
            LOG.log(Level.FINER, "getIssue: getting issue {0} from the cache", repository.getUrl() + "#" + issueId); //NOI18N
            issue[0] = (BugzillaIssue) repository.getIssueCache().getIssue(issueId);
        }
        return issue[0];
    }

    private void fireIssueRemoved(BugzillaLazyIssue lazyIssue) {
        BugzillaIssue issue = lazyIssue.issueRef.get();
        if (issue != null) {
            support.firePropertyChange(PROPERTY_ISSUE_REMOVED, issue, null);
        }
    }

    /*
     * Notifies all kenai issues that user has logged on. Private kenai projects cannot be instantiated without being logged in
     * and issue tracking repository cannot be created.
     */
    private void notifyKenaiLogin (String notifiedKenaiHost) {
        assert notifiedKenaiHost != null;
        synchronized (LOCK) {
            for (BugzillaLazyIssue issue : watchedIssues.values()) {
                if (issue instanceof KenaiBugzillaLazyIssue) {
                    if(notifiedKenaiHost.equals(issue.getUrl().getHost())) {
                        ((KenaiBugzillaLazyIssue) issue).notifyKenaiLogin();
                    }
                }
            }
        }
    }

    /**
     * Common Bugzilla representation of LazyIssue
     */
    private static class BugzillaLazyIssue extends LazyIssue {
        private final String issueId;
        /**
         *  cached repository for the issue
         */
        private WeakReference<BugzillaRepository> repositoryRef;
        private final BugzillaIssueProvider provider;
        private WeakReference<BugzillaIssue> issueRef;
        private PropertyChangeListener issueListener;

        public BugzillaLazyIssue (BugzillaIssue issue, BugzillaIssueProvider provider) throws MalformedURLException {
            super(BugzillaIssueProvider.getUrl(issue), issue.getDisplayName());
            this.issueId = issue.getID();
            this.provider = provider;
            this.repositoryRef = new WeakReference<BugzillaRepository>(issue.getBugzillaRepository());
            this.issueRef = new WeakReference<BugzillaIssue>(issue);
            attachIssueListener(issue);
        }

        public BugzillaLazyIssue (String name, URL url, String issueId, BugzillaRepository repository, BugzillaIssueProvider provider) {
            super(url, name);
            this.issueId = issueId;
            this.repositoryRef = new WeakReference<BugzillaRepository>(repository);
            this.provider = provider;
            this.issueRef = new WeakReference<BugzillaIssue>(null);
        }

        @Override
        public BugzillaIssue getIssue() {
            BugzillaIssue issue = issueRef.get();
            if (issue == null) {
                BugzillaRepository repository = getRepository();
                if (repository == null) {
                    LOG.log(Level.INFO, "Repository unavailable for {0}", getUrl().toString()); //NOI18N
                    if (canBeAutoRemoved()) {
                        // no repository found for this issue and the issue can be removed automaticaly
                        provider.remove(getUrl(), true);
                    }
                } else {
                    issue = provider.getIssue(repository, issueId);
                }
                setIssueReference(issue);
            }
            return issue;
        }

        private BugzillaRepository getRepository() {
            BugzillaRepository repository = repositoryRef.get();
            return repository;
        }

        /**
         * Sets the reference to the issue and attaches an issue listener
         * @param issue if null then this only clears the reference.
         */
        private void setIssueReference (BugzillaIssue issue) {
            issueRef = new WeakReference<BugzillaIssue>(issue);
            if (issue != null) {
                applyChangesFor(issue);
                attachIssueListener(issue);
            }
        }

        private void attachIssueListener (BugzillaIssue issue) {
            if (issueListener == null) {
                issueListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        BugzillaIssue issue = issueRef.get();
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

        private void applyChangesFor (BugzillaIssue issue) {
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
            BugzillaRepository repository = repositoryRef.get();
            if (repository != null) {
                repoUrl = repository.getUrl();
            }
            return repoUrl;
        }

        @Override
        public List<? extends Action> getActions() {
            List<AbstractAction> actions = new LinkedList<AbstractAction>();
            actions.add(new AbstractAction(NbBundle.getMessage(BugzillaIssueProvider.class, "BugzillaIssueProvider.resolveAction")) { //NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            final BugzillaIssue issue = getIssue();
                            if (issue == null) {
                                LOG.fine("Resole action: null issue returned"); //NOI18N
                            } else {
                                if (!issue.isResolveAvailable()) {
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                            NbBundle.getMessage(BugzillaIssueProvider.class, "BugzillaIssueProvider.resolveAction.notPermitted"),
                                            NotifyDescriptor.INFORMATION_MESSAGE));
                                    return;
                                }
                                ResolveIssuePanel panel = new ResolveIssuePanel(issue);
                                if (panel.showDialog()) {
                                    LOG.finer("Resolve action: resolving..."); //NOI18N
                                    String pattern = NbBundle.getMessage(BugzillaIssueProvider.class, "BugzillaIssueProvider.resolveIssueMessage"); //NOI18N
                                    final String resolution = panel.getSelectedResolution();
                                    final String duplicateId = panel.getDuplicateId();
                                    final String comment = panel.getComment();
                                    runCancellableCommand(new Runnable () {
                                        @Override
                                        public void run() {
                                            if (BugzillaIssue.RESOLVE_DUPLICATE.equals(resolution)) {
                                                issue.duplicate(duplicateId);
                                            } else {
                                                issue.resolve(resolution);
                                            }
                                            if (comment.length() > 0) {
                                                issue.addComment(comment);
                                            }
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
                    BugzillaIssue issue = issueRef.get();
                    if (issue != null) {
                        allowed = issue.isResolveAvailable();
                    }
                    return allowed;
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
        protected void setRepositoryReference(BugzillaRepository repository) {
            if (repository != null) {
                repositoryRef = new WeakReference<BugzillaRepository>(repository);
            }
        }
    }

    /**
     * Specific kenai Bugzilla lazy issue.
     */
    private static final class KenaiBugzillaLazyIssue extends BugzillaLazyIssue {

        private final String projectName;
        private boolean loginStatusChanged = true;

        public KenaiBugzillaLazyIssue (BugzillaIssue issue, BugzillaIssueProvider provider) throws MalformedURLException {
            super(issue, provider);
            Repository repo = issue.getRepository();
            if (!(repo instanceof KenaiRepository)) {
                throw new IllegalStateException("Cannot instantiate with a non kenai issue: " + issue); //NOI18N
            }
            projectName = ((KenaiRepository) repo).getProductName();
        }

        public KenaiBugzillaLazyIssue (String name, URL url, String issueId, String projectName, BugzillaIssueProvider provider) {
            super(name, url, issueId, null, provider);
            this.projectName = projectName;
        }

        protected KenaiRepository lookupRepository () {
            KenaiRepository kenaiRepo = null;
            Repository repo = null;
            if (loginStatusChanged) {
                try {
                    LOG.log(Level.FINE, "KenaiBugzillaLazyIssue.lookupRepository: getting repository for: " + projectName);
                    String url = getUrl().toString();
                    repo = KenaiUtil.getRepository(url, projectName);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "KenaiBugzillaLazyIssue.lookupRepository: getting repository for " + projectName, ex);
                }
                loginStatusChanged = false;
            }
            if (repo != null && repo instanceof KenaiRepository) {
                kenaiRepo = (KenaiRepository) repo;
            } else {
                LOG.log(Level.FINE, "KenaiBugzillaLazyIssue.lookupRepository: no repository for: " + projectName);
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
