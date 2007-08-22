/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openidex.search.SearchType;
import static java.util.logging.Level.FINER;

/**
 * Action which searches files in folders, packages and projects.
 * <p>
 * This action uses two different mechanisms of enabling/disabling,
 * depending on whether the action is available in the toolbar or not:
 * <ul>
 *     <li><u>if the action is in the toolbar:</u><br />
 *         The action is updated (enabled/disabled) continuously.
 *         </li>
 *     <li><u>if the action is <em>not</em> in the toolbar</u><br />
 *         The action state is not updated but it is computed on demand,
 *         i.e. when method <code>isEnabled()</code> is called.
 *         </li>
 * </ul>
 * Moreover, the first call of method <code>isEnabled()</code> returns
 * <code>false</code>, no matter whether some projects are open or not.
 * This is made so based on the assumption that the first call of
 * <code>isEnabled()</code> is done during IDE startup as a part of menu
 * creation. It reduces startup time as it does not force projects
 * initialization.
 *
 * @author  Marian Petras
 */
public class FindInFilesAction extends CallableSystemAction
                               implements ContextAwareAction, ChangeListener {

    static final long serialVersionUID = 4554342565076372611L;
    
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.FindAction_state");            //NOI18N
    
    /**
     * name of a shared variable - is this the first call of method
     * <code>isEnabled()</code>?
     * Value of this variable is non-<code>null</code> only until method
     * {@link #isEnabled()} is called for the first time.
     */
    private static final String VAR_FIRST_ISENABLED
                                = "first call of isEnabled()";          //NOI18N
    /**
     * name of a shared variable - reference to the toolbar presenter
     */
    private static final String VAR_TOOLBAR_COMP_REF
                                = "toolbar presenter ref";              //NOI18N
    /**
     * name of a shared variable - are we listening on the set of open projects?
     * It contains <code>Boolean.TRUE</code> if we are listening,
     * and <code>null</code> if we are not listening.
     */
    private static final String VAR_LISTENING
                                = "listening";                          //NOI18N

    /**
     * name of property &quot;replacing&quot;.
     * Value of the property determines whether the action should offer
     * replacing of found matching strings (if {@code true}) or not
     * (if {@code false}). Value {@code true} thus effectively modifies
     * action &quot;find in files&quot; to &quot;replace in files&quot;.
     */
    protected static final String REPLACING = "replacing";              //NOI18N
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putProperty(VAR_FIRST_ISENABLED, Boolean.TRUE);

        putProperty(REPLACING, Boolean.FALSE, false);
    }

    public Action createContextAwareInstance(Lookup lookup) {
        if (shouldLog(LOG)) {
            log("createContextAwareInstance(lookup)");
        }
        return new LookupSensitive(this, lookup);
    }

    public Action createContextAwareInstance(Lookup lookup,
                                             boolean searchSelection) {
        if (shouldLog(LOG)) {
            log("createContextAwareInstance(lookup, " + searchSelection + ')');
        }
        Action result = new LookupSensitive(this, lookup, searchSelection);
        if (shouldLog(LOG)) {
            log(" -> " + result);
        }
        return result;
    }

    @Override
    public Component getToolbarPresenter() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("getMenuPresenter()");
        }

        Component presenter = getStoredToolbarPresenter();
        if (putProperty(VAR_LISTENING, Boolean.TRUE) == null) {
            SearchScopeRegistry.getDefault().addChangeListener(this);
            putProperty(VAR_FIRST_ISENABLED, null);
            updateState();
        }
        return presenter;
    }

    /**
     * Returns a toolbar presenter.
     * If the toolbar presenter already exists, returns the existing instance.
     * If it does not exist, creates a new toolbar presenter, stores
     * a reference to it to shared variable <code>VAR_TOOLBAR_BTN_REF</code>
     * and returns the presenter.
     *
     * @return  existing presenter; or a new presenter if it did not exist
     */
    private Component getStoredToolbarPresenter() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("getStoredToolbarPresenter()");
        }

        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
        if (refObj != null) {
            Reference ref = (Reference) refObj;
            Object presenterObj = ref.get();
            if (presenterObj != null) {
                return (Component) presenterObj;
            }
        }
        
        Component presenter = super.getToolbarPresenter();
        putProperty(VAR_TOOLBAR_COMP_REF, new WeakReference<Component>(presenter));
        return presenter;
    }
    
    /**
     * Checks whether the stored toolbar presenter exists but does not create
     * one if it does not exist.
     *
     * @return  <code>true</code> if the reference to the toolbar presenter
     *          is not <code>null</code> and has not been cleared yet;
     *          <code>false</code> otherwise
     * @see  #getStoredToolbarPresenter
     */
    private boolean checkToolbarPresenterExists() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("checkToolbarPresenterExists()");
        }

        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
        if (refObj == null) {
            return false;
        }
        return ((Reference) refObj).get() != null;
    }
    
    /**
     * This method is called if we are listening for changes on the set
     * of open projecst and some project(s) is opened/closed.
     */
    public void stateChanged(ChangeEvent e) {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("stateChanged()");
        }

        /*
         * Check whether listening on open projects is active.
         */
        if (getProperty(VAR_LISTENING) == null) {
            return;
        }
        
        if (checkToolbarPresenterExists()) {
            updateState();
        } else {
            SearchScopeRegistry.getDefault().removeChangeListener(this);
            putProperty(VAR_LISTENING, null);
            putProperty(VAR_TOOLBAR_COMP_REF, null);
        }
    }

    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("isEnabled()");
        }

        if (getProperty(VAR_LISTENING) != null) {
            log(" - isListening");
            return super.isEnabled();
        } else if (getProperty(VAR_FIRST_ISENABLED) == null) {
            log(" - checking registry");
            return SearchScopeRegistry.getDefault().hasApplicableSearchScope();
        } else {
            /* first call of this method */
            log(" - first \"isEnabled()\"");
            putProperty(VAR_FIRST_ISENABLED, null);
            return false;
        }
    }
    
    /**
     */
    private void updateState() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("updateState()");
        }
        
        final boolean enabled
                = SearchScopeRegistry.getDefault().hasApplicableSearchScope();
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                setEnabled(enabled);
            }
        });
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/find.gif";                //NOI18N
    }
    
    public String getName() {
        String key = SearchScopeRegistry.getDefault().hasProjectSearchScopes()
                     ? "LBL_Action_FindInProjects"                      //NOI18N
                     : "LBL_Action_FindInFiles";                        //NOI18N
        return NbBundle.getMessage(getClass(), key);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(FindInFilesAction.class);
    }

    /** Perform this action. */
    public void performAction() {
        performAction(SearchScopeRegistry.getDefault().getSearchScopes(),
                      null);    //no preferred search scope
    }

    private void performAction(Map<SearchScope, Boolean> searchScopes,
                               SearchScope preferredSearchScope) {
        assert EventQueue.isDispatchThread();

        String msg = Manager.getInstance().mayStartSearching();
        if (msg != null) {
            /*
             * Search cannot be started, for example because the replace
             * operation has not finished yet.
             */
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            msg,
                            NotifyDescriptor.INFORMATION_MESSAGE));
            return;
        }

        if (!isSomeEnabled(searchScopes)) {
            return;
        }

        boolean replacing = Boolean.TRUE.equals(getProperty(REPLACING));
        SearchPanel searchPanel = new SearchPanel(searchScopes,
                                                  preferredSearchScope,
                                                  replacing);
        
        searchPanel.showDialog();
        if (searchPanel.getReturnStatus() != SearchPanel.RET_OK) {
            return;
        }
        
        SearchScope searchScope = searchPanel.getSearchScope();
	BasicSearchCriteria basicSearchCriteria = searchPanel.getBasicSearchCriteria();
	List<SearchType> extraSearchTypes = searchPanel.getSearchTypes();
        
        ResultView resultView = ResultView.getInstance();
        resultView.rememberInput(searchScope,
			         basicSearchCriteria,
				 extraSearchTypes);
        resultView.open();
        resultView.requestActive();
        
        Manager.getInstance().scheduleSearchTask(
                new SearchTask(searchScope,
                               basicSearchCriteria,
			       searchPanel.getCustomizedSearchTypes()));
    }

    /**
     * Checks whether some of the {@code SearchScope}s is enabled.
     * 
     * @param  searchScopes  search scopes and their states (enabled/disabled)
     * @return  {@code true} if at least some search scope is enabled,
     *          {@code false} otherwise
     * @see  SearchScopeRegistry#getSearchScopes
     */
    private static boolean isSomeEnabled(Map<SearchScope, Boolean> searchScopes) {
	for (Boolean b : searchScopes.values()) {
            if (b) {            //auto-unboxing
                return true;
            }
	}
        return false;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static final class LookupSensitive implements Action, ChangeListener, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

        private static int counter = 0;

        private final FindInFilesAction delegate;
        private final SearchScopeRegistry searchScopeRegistry;
        private final boolean searchSelection;
        private final int id;

        /** support for listeners */
        private PropertyChangeSupport support;
        private boolean enabled;
        
        {
            id = ++counter;
        }

        LookupSensitive(FindInFilesAction delegate,
                        Lookup lookup) {
            this(delegate, lookup, false);
        }

        LookupSensitive(FindInFilesAction delegate,
                        Lookup lookup,
                        boolean searchSelection) {
            this.delegate = delegate;
            this.searchScopeRegistry = SearchScopeRegistry.getInstance(lookup, id);
            this.searchSelection = searchSelection;
            log("<init>");
        }

        private Object getLock() {
            return this;
        }

        public Object getValue(String key) {
            if (shouldLog(LOG)) {
                log("getValue(\"" + key + "\")");
            }
            return delegate.getValue(key);
        }

        public void putValue(String key, Object value) {
        }

        public void actionPerformed(ActionEvent e) {
            assert EventQueue.isDispatchThread();
            if (shouldLog(LOG)) {
                log("actionPerformed(...)");
            }

            delegate.performAction(
                    searchScopeRegistry.getSearchScopes(),
                    searchSelection
                        ? searchScopeRegistry.getNodeSelectionSearchScope()
                        : null);
        }

        public void setEnabled(boolean b) {
            if (shouldLog(LOG)) {
                log("setEnabled(" + b + ')');
            }
        }

        public boolean isEnabled() {
            assert EventQueue.isDispatchThread();
            if (shouldLog(LOG)) {
                log("isEnabled(...)");
            }

            synchronized (getLock()) {
                if (support != null) {
                    return enabled;
                }
            }

            return searchScopeRegistry.hasApplicableSearchScope();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (shouldLog(LOG)) {
                log("addPropertyChangeListener(...)");
            }

            if (listener == null) {
                return;
            }

            synchronized (getLock()) {
                if (support == null) {
                    support = new PropertyChangeSupport(this);
                    searchScopeRegistry.addChangeListener(this);
                    enabled = searchScopeRegistry.hasApplicableSearchScope();
                }
                support.addPropertyChangeListener(listener);
            }
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (shouldLog(LOG)) {
                log("removePropertyChangeListener(...)");
            }

            if (listener == null) {
                return;
            }

            synchronized (getLock()) {
                if (support == null) {
                    return;
                }

                support.removePropertyChangeListener(listener);
                boolean lastListener = !support.hasListeners(null);
                if (lastListener) {
                    searchScopeRegistry.removeChangeListener(this);
                    support = null;
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            if (shouldLog(LOG)) {
                log("stateChanged(...)");
            }

            synchronized (getLock()) {
                if (support != null) {
                    final boolean wasEnabled = enabled;
                    final boolean newEnabled = searchScopeRegistry.hasApplicableSearchScope();
                    enabled = newEnabled;
                    Mutex.EVENT.writeAccess(new Runnable() {
                        public void run() {
                            support.firePropertyChange(SystemAction.PROP_ENABLED,
                                                       wasEnabled, newEnabled);
                        }
                    });
                }
            }
        }

        public JMenuItem getMenuPresenter() {
            if (shouldLog(LOG)) {
                log("getMenuPresenter(...)");
            }
            return delegate.getMenuPresenter();
        }

        public JMenuItem getPopupPresenter() {
            if (shouldLog(LOG)) {
                log("getPopupPresenter(...)");
            }
            return delegate.getPopupPresenter();
        }

        public Component getToolbarPresenter() {
            if (shouldLog(LOG)) {
                log("getToolbarPresenter(...)");
            }
            return delegate.getToolbarPresenter();
        }

        @Override
        public String toString() {
            return shortClassName + " #" + id;
        }

        private final String shortClassName;

        {
            String clsName = getClass().getName();
            int lastDot = clsName.lastIndexOf('.');
            shortClassName = ((lastDot != -1) ? clsName.substring(lastDot + 1)
                                              : clsName)
                             .replace('$', '.');
        }

        private boolean shouldLog(Logger logger) {
            return logger.isLoggable(FINER)
                   && shortClassName.startsWith("FindInFilesAction");
        }

        private void log(String msg) {
            LOG.finer(this + ": " + msg);
        }

    }

    private final String shortClassName;

    {
        String clsName = getClass().getName();
        int lastDot = clsName.lastIndexOf('.');
        shortClassName = (lastDot != -1) ? clsName.substring(lastDot + 1)
                                         : clsName;
    }

    private boolean shouldLog(Logger logger) {
        return logger.isLoggable(FINER)
               && shortClassName.equals("FindInFilesAction");
    }

    private void log(String msg) {
        LOG.finer(shortClassName + ": " + msg);
    }


}
