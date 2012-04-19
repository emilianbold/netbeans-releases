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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasks.ui.dashboard;

import java.awt.Component;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.tasks.ui.LinkButton;
import org.netbeans.modules.tasks.ui.actions.CreateCategoryAction;
import org.netbeans.modules.tasks.ui.actions.CreateRepositoryAction;
import org.netbeans.modules.tasks.ui.cache.CategoryEntry;
import org.netbeans.modules.tasks.ui.cache.DashboardStorage;
import org.netbeans.modules.tasks.ui.cache.TaskEntry;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.filter.DashboardFilter;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.treelist.ColorManager;
import org.netbeans.modules.tasks.ui.treelist.TreeList;
import org.netbeans.modules.tasks.ui.treelist.TreeListModel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public final class DashboardViewer {

    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {

            return null;
        }
    };
    private RequestProcessor requestProcessor = new RequestProcessor("Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    public final JScrollPane dashboardComponent;
    private boolean opened = false;
    private final TitleNode titleCategoryNode;
    private final TitleNode titleRepositoryNode;
    private final Object LOCK = new Object();
    private Map<Category, CategoryNode> mapCategoryToNode;
    private List<CategoryNode> categoryNodes;
    private List<RepositoryNode> repositoryNodes;
    private AppliedFilters<Issue> appliedTaskFilters;
    private AppliedFilters<CategoryNode> appliedCategoryFilters;
    private AppliedFilters<RepositoryNode> appliedRepositoryFilters;
    private int taskHits;
    private Set<TreeListNode> expandedNodes;
    private boolean persistExpanded = true;
    private TreeListNode activeTaskNode;

    private DashboardViewer() {
        expandedNodes = new HashSet<TreeListNode>();
        dashboardComponent = new JScrollPane() {
            @Override
            public void requestFocus() {
                Component view = getViewport().getView();
                if (view != null) {
                    view.requestFocus();
                } else {
                    super.requestFocus();
                }
            }

            @Override
            public boolean requestFocusInWindow() {
                Component view = getViewport().getView();
                return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
            }

            @Override
            public void addNotify() {
                super.addNotify();
                DashboardViewer.this.loadData();
            }
        };
        dashboardComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        mapCategoryToNode = new HashMap<Category, CategoryNode>();
        categoryNodes = new ArrayList<CategoryNode>();
        repositoryNodes = new ArrayList<RepositoryNode>();

        LinkButton btnAddCategory = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_category.png", true), new CreateCategoryAction()); //NOI18N
        btnAddCategory.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_CreateCategory")); // NOI18N
        titleCategoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Categories"), btnAddCategory); // NOI18N
        LinkButton btnAddRepo = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_repo.png", true), new CreateRepositoryAction()); //NOI18N
        btnAddRepo.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_AddRepo")); // NOI18N
        titleRepositoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Repositories"), btnAddRepo); // NOI18N
        model.addRoot(-1, titleCategoryNode);
        model.addRoot(-1, titleRepositoryNode);

        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardViewer.class, "A11Y_TeamProjects"); //NOI18N
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        appliedTaskFilters = new AppliedFilters<Issue>();
        appliedCategoryFilters = new AppliedFilters<CategoryNode>();
        appliedRepositoryFilters = new AppliedFilters<RepositoryNode>();
        taskHits = 0;
        treeList.setModel(model);
        dashboardComponent.setViewportView(treeList);
        dashboardComponent.invalidate();
        dashboardComponent.revalidate();
        dashboardComponent.repaint();
    }

    /**
     * currently visible dashboard instance
     *
     * @return
     */
    public static DashboardViewer getInstance() {
        return Holder.theInstance;
    }

    private static class Holder {

        private static final DashboardViewer theInstance = new DashboardViewer();
    }

    public void setActiveTaskNode(TreeListNode activeTaskNode) {
        this.activeTaskNode = activeTaskNode;
    }

    public boolean containsActiveTask(TreeListNode parent) {
        if (activeTaskNode == null) {
            return false;
        }
        TreeListNode activeParent = activeTaskNode.getParent();
        while (activeParent != null) {
            if (parent.equals(activeParent)) {
                return true;
            }
            activeParent = activeParent.getParent();
        }
        return false;
    }

    public boolean isTaskNodeActive(TaskNode taskNode) {
        return taskNode.equals(activeTaskNode);
    }

    boolean isOpened() {
        return opened;
    }

    public void close() {
        synchronized (LOCK) {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
            opened = false;
        }
    }

    public JComponent getComponent() {
        opened = true;
        return dashboardComponent;
    }

    public void addTaskToCategory(TaskNode taskNode, Category category) {
        TaskNode categorizedTaskNode = getCategorizedTask(taskNode);
        //task is already categorized (task exists within categories)
        if (categorizedTaskNode != null) {
            //task is already in this category, do nothing
            if (category.equals(categorizedTaskNode.getCategory())) {
                return;
            }
            //task is already in another category, dont add new taskNode but move existing one
            taskNode = categorizedTaskNode;
        }
        CategoryNode destCategoryNode = mapCategoryToNode.get(category);
        TaskNode toAdd = new TaskNode(taskNode.getTask(), destCategoryNode);
        if (destCategoryNode.addTaskNode(toAdd, appliedTaskFilters.isInFilter(toAdd.getTask()))) {
            //remove from old category
            if (taskNode.isCategorized()) {
                removeTask(taskNode);
            }
            //set new category
            toAdd.setCategory(category);
            if (DashboardViewer.getInstance().isTaskNodeActive(taskNode)) {
                DashboardViewer.getInstance().setActiveTaskNode(toAdd);
            }
            model.contentChanged(destCategoryNode);
            destCategoryNode.updateContent();
        }
        storeCategory(category);
    }

    public void removeTask(TaskNode taskNode) {
        CategoryNode categoryNode = mapCategoryToNode.get(taskNode.getCategory());
        taskNode.setCategory(null);
        categoryNode.removeTaskNode(taskNode);
        model.contentChanged(categoryNode);
        //TODO only remove that child, dont updateContent all
        categoryNode.updateContent();
        storeCategory(categoryNode.getCategory());
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<Category>(categoryNodes.size());
        for (CategoryNode CategoryNode : categoryNodes) {
            list.add(CategoryNode.getCategory());
        }
        return list;
    }

    public boolean isCategoryNameUnique(String categoryName) {
        for (CategoryNode node : categoryNodes) {
            if (node.getCategory().getName().equalsIgnoreCase(categoryName)) {
                return false;
            }
        }
        return true;
    }

    public void renameCategory(Category category, final String newName) {
        CategoryNode node = mapCategoryToNode.get(category);
        final String oldName = category.getName();
        category.setName(newName);
        model.contentChanged(node);
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().renameCategory(oldName, newName);
            }
        });
    }

    public void addCategory(Category category) {
        //add category to the model - sorted
        CategoryNode newCategoryNode = new CategoryNode(category, true);
        categoryNodes.add(newCategoryNode);
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        mapCategoryToNode.put(category, newCategoryNode);
        addCategoryToModel(index, newCategoryNode);
        storeCategory(category);
    }

    public void deleteCategory(final Category category) {
        //TODO lock categNodes
        CategoryNode categoryNode = mapCategoryToNode.remove(category);
        model.removeRoot(categoryNode);
        categoryNodes.remove(categoryNode);
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().deleteCategory(category.getName());
            }
        });
    }

    public void setCategoryOpened(CategoryNode categoryNode, boolean opened) {
        categoryNode.setOpened(opened);
        if (!isCategoryInFilter(categoryNode)) {
            model.removeRoot(categoryNode);
        }
        storeClosedCategories();
    }

    private void addCategoryToModel(int index, CategoryNode categoryNode) {
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof CategoryNode) {
                CategoryNode displNode = (CategoryNode) node;
                if (categoryNode.compareTo(displNode) < 0) {
                    model.addRoot(model.getRootNodes().indexOf(node), categoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                model.addRoot(model.getRootNodes().indexOf(node), categoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            model.addRoot(-1, categoryNode);
        }
    }

    private void storeCategory(final Category category) {
        final List<TaskEntry> taskEntries = new ArrayList<TaskEntry>(category.getTasks().size());
        for (Issue issue : category.getTasks()) {
            taskEntries.add(new TaskEntry(issue.getID(), issue.getRepository().getId()));
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().storeCategory(category.getName(), taskEntries);
            }
        });
    }

    private void storeClosedCategories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<CategoryNode> closed = getClosedCategoryNodes();
        final List<String> names = new ArrayList<String>(closed.size());
        for (CategoryNode categoryNode : closed) {
            names.add(categoryNode.getCategory().getName());
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedCategories(names);
            }
        });
    }

    private List<CategoryNode> getClosedCategoryNodes() {
        //TODO lock categNodes
        List<CategoryNode> closed = new ArrayList<CategoryNode>(categoryNodes.size());
        for (CategoryNode categoryNode : categoryNodes) {
            if (!categoryNode.isOpened()) {
                closed.add(categoryNode);
            }
        }
        return closed;
    }

    public void addRepository(Repository repository) {
        //add repository to the model - sorted
        RepositoryNode repositoryNode = new RepositoryNode(repository);
        repositoryNodes.add(repositoryNode);
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        addRepositoryToModel(index, repositoryNode);
    }

    public void removeRepository(final RepositoryNode repositoryNode) {
        repositoryNodes.remove((RepositoryNode) repositoryNode);
        model.removeRoot(repositoryNode);

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                repositoryNode.getRepository().remove();
            }
        });
    }

    public void setRepositoryOpened(RepositoryNode repositoryNode, boolean opened) {
        repositoryNode.setOpened(opened);
        if (!isRepositoryInFilter(repositoryNode)) {
            model.removeRoot(repositoryNode);
        }
        storeClosedRepositories();
    }

    private void addRepositoryToModel(int index, RepositoryNode repositoryNode) {
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof RepositoryNode) {
                RepositoryNode displNode = (RepositoryNode) node;
                if (repositoryNode.compareTo(displNode) < 0) {
                    model.addRoot(model.getRootNodes().indexOf(node), repositoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                model.addRoot(model.getRootNodes().indexOf(node), repositoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            model.addRoot(-1, repositoryNode);
        }
    }

    private void storeClosedRepositories() {
        final DashboardStorage storage = DashboardStorage.getInstance();
        List<RepositoryNode> closed = getClosedRepositoryNodes();
        final List<String> ids = new ArrayList<String>(closed.size());
        for (RepositoryNode repositoryNode : closed) {
            ids.add(repositoryNode.getRepository().getId());
        }

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedRepositories(ids);
            }
        });
    }

    private List<RepositoryNode> getClosedRepositoryNodes() {
        List<RepositoryNode> closed = new ArrayList<RepositoryNode>(repositoryNodes.size());
        for (RepositoryNode repositoryNode : repositoryNodes) {
            if (!repositoryNode.isOpened()) {
                closed.add(repositoryNode);
            }
        }
        return closed;
    }

    public AppliedFilters getAppliedTaskFilters() {
        return appliedTaskFilters;
    }

    public int updateTaskFilter(DashboardFilter<Issue> oldFilter, DashboardFilter<Issue> newFilter) {
        if (oldFilter != null) {
            appliedTaskFilters.removeFilter(oldFilter);
        }
        return applyTaskFilter(newFilter, true);
    }

    public int applyTaskFilter(DashboardFilter<Issue> taskFilter, boolean refresh) {
        appliedTaskFilters.addFilter(taskFilter);
        return manageApplyFilter(refresh);
    }

    public int removeTaskFilter(DashboardFilter<Issue> taskFilter, boolean refresh) {
        appliedTaskFilters.removeFilter(taskFilter);
        return manageRemoveFilter(refresh, !taskFilter.expandNodes());
    }

    public int applyCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.addFilter(categoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeCategoryFilter(DashboardFilter<CategoryNode> categoryFilter, boolean refresh) {
        appliedCategoryFilters.removeFilter(categoryFilter);
        return manageRemoveFilter(refresh, !categoryFilter.expandNodes());
    }

    public int applyRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.addFilter(repositoryFilter);
        return manageApplyFilter(refresh);
    }

    public int removeRepositoryFilter(DashboardFilter<RepositoryNode> repositoryFilter, boolean refresh) {
        appliedRepositoryFilters.removeFilter(repositoryFilter);
        return manageRemoveFilter(refresh, !repositoryFilter.expandNodes());
    }

    private int manageRemoveFilter(boolean refresh, boolean wasForceExpand) {
        if (refresh) {
            taskHits = 0;
            persistExpanded = !wasForceExpand;
            refreshContent();
            persistExpanded = true;
            return taskHits;
        } else {
            return -1;
        }
    }

    private int manageApplyFilter(boolean refresh) {
        if (refresh) {
            taskHits = 0;
            refreshContent();
            return taskHits;
        } else {
            return -1;
        }
    }

    public boolean expandNodes() {
        return appliedTaskFilters.expandNodes() || appliedCategoryFilters.expandNodes() || appliedRepositoryFilters.expandNodes();
    }

    public boolean showHitCount() {
        return appliedTaskFilters.showHitCount() || appliedCategoryFilters.showHitCount() || appliedRepositoryFilters.showHitCount();
    }

    public boolean isNodeExpanded(TreeListNode node) {
        if (expandNodes()) {
            return true;
        }
        return expandedNodes.contains(node);
    }

    private void loadData() {
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                titleRepositoryNode.setProgressVisible(true);
                titleCategoryNode.setProgressVisible(true);
                loadRepositories();
                titleRepositoryNode.setProgressVisible(false);
                loadCategories();
                titleCategoryNode.setProgressVisible(false);
            }
        });
    }

    private void loadCategories() {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<CategoryEntry> categoryEntries = storage.readCategories();
        List<String> names = storage.readClosedCategories();

        final List<CategoryNode> catNodes = new ArrayList<CategoryNode>(categoryEntries.size());
        for (CategoryEntry categoryEntry : categoryEntries) {
            List<Issue> tasks = loadTasks(categoryEntry.getTaskEntries());
            // was category closed
            if (names.contains(categoryEntry.getCategoryName())) {
                catNodes.add(new CategoryNode(new Category(categoryEntry.getCategoryName(), tasks), false));
            } else {
                catNodes.add(new CategoryNode(new Category(categoryEntry.getCategoryName(), tasks)));
            }
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setCategories(catNodes);
                }
            });
        }
    }

    private List<Issue> loadTasks(List<TaskEntry> taskEntries) {
        List<Issue> tasks = new ArrayList<Issue>(taskEntries.size());
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        for (TaskEntry taskEntry : taskEntries) {
            List<String> l = m.get(taskEntry.getRepositoryId());
            if(l == null) {
                l = new LinkedList<String>();
                m.put(taskEntry.getRepositoryId(), l);
            }
            l.add(taskEntry.getIssueId());
        }
        for (Entry<String, List<String>> e : m.entrySet()) {
            Repository repository = getRepository(e.getKey());
            if (repository != null) {
                List<String> l = e.getValue();
                Issue[] issues = repository.getIssues(l.toArray(new String[l.size()]));
                if (issues != null) {
                    tasks.addAll(Arrays.asList(issues));
                }
            }        
        }
        return tasks;
    }

    private Repository getRepository(String repositoryId) {
        List<Repository> repositories = new ArrayList<Repository>(RepositoryManager.getInstance().getRepositories());
        for (Repository repository : repositories) {
            if (repository.getId().equals(repositoryId)) {
                return repository;
            }
        }
        return null;
    }

    private void loadRepositories() {
        List<Repository> allRepositories = new ArrayList<Repository>(RepositoryManager.getInstance().getRepositories());
        List<String> ids = DashboardStorage.getInstance().readClosedRepositories();
        final List<RepositoryNode> repoNodes = new ArrayList<RepositoryNode>(allRepositories.size());

        for (Repository repository : allRepositories) {
            // was repository closed
            RepositoryNode repositoryNode;
            if (ids.contains(repository.getId())) {
                repositoryNode = new RepositoryNode(repository, false, false);
            } else {
                repositoryNode = new RepositoryNode(repository, false, true);
                //TODO uncomment when query updateContent is fixed
                //refreshQueries(repository.getQueries());
            }
            repoNodes.add(repositoryNode);
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setRepositories(repoNodes);
                }
            });
        }
    }

    private void refreshQueries(Collection<Query> queries) {
        for (Query query : queries) {
            query.refresh(true);
        }
    }

    private TaskNode getCategorizedTask(TaskNode taskNode) {
        //TODO lock categNodes
        for (CategoryNode categoryNode : categoryNodes) {
            int index = categoryNode.indexOf(taskNode.getTask());
            if (index != -1) {
                return categoryNode.getTaskNodes().get(index);
            }
        }
        return null;
    }

    private void addRootToModel(int index, TreeListNode node) {
        if (expandNodes() || expandedNodes.remove(node)) {
            node.setExpanded(true);
        }
        model.addRoot(index, node);
    }

    private void removeRootFromModel(TreeListNode node) {
        if (persistExpanded) {
            expandedNodes.remove(node);
            if (node.isExpanded()) {
                expandedNodes.add(node);
            }
        }
        model.removeRoot(node);
    }

    private void refreshContent() {
        //TODO update only filtered (opened if the filter is on)
        //update filtered nodes
        for (CategoryNode categoryNode : categoryNodes) {
            categoryNode.updateContent();
        }
        for (RepositoryNode repositoryNode : repositoryNodes) {
            repositoryNode.updateContent();
        }

        setRepositories(repositoryNodes);
        setCategories(categoryNodes);
    }

    private void setCategories(List<CategoryNode> catNodes) {
        synchronized (LOCK) {
            removeNodesFromModel(CategoryNode.class);
            categoryNodes = catNodes;
            mapCategoryToNode.clear();
            Collections.sort(categoryNodes);
            int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
            for (CategoryNode categoryNode : categoryNodes) {
                if (isCategoryInFilter(categoryNode)) {
                    taskHits += categoryNode.getTotalCount();
                    mapCategoryToNode.put(categoryNode.getCategory(), categoryNode);
                    addRootToModel(index++, categoryNode);
                }
            }
        }
    }

    private void setRepositories(List<RepositoryNode> repoNodes) {
        synchronized (LOCK) {
            removeNodesFromModel(RepositoryNode.class);
            repositoryNodes = repoNodes;
            Collections.sort(this.repositoryNodes);
            int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (isRepositoryInFilter(repositoryNode)) {
                    taskHits += repositoryNode.getFilterHits();
                    addRootToModel(index++, repositoryNode);
                }
            }
        }
    }

    private boolean isCategoryInFilter(CategoryNode categoryNode) {
        return (appliedTaskFilters.isEmpty() || !categoryNode.getFilteredTaskNodes().isEmpty()) && appliedCategoryFilters.isInFilter(categoryNode);
    }

    private boolean isRepositoryInFilter(RepositoryNode repositoryNode) {
        return (appliedTaskFilters.isEmpty() || !repositoryNode.getFilteredQueryNodes().isEmpty()) && appliedRepositoryFilters.isInFilter(repositoryNode);
    }

    private void removeNodesFromModel(Class nodeClass) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>();
        for (TreeListNode root : model.getRootNodes()) {
            if (root != null && nodeClass.isAssignableFrom(root.getClass())) {
                nodesToRemove.add(root);
            }
        }
        for (TreeListNode node : nodesToRemove) {
            removeRootFromModel(node);
        }
    }
}
