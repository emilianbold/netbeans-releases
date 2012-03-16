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

import org.netbeans.modules.tasks.ui.treelist.ColorManager;
import org.netbeans.modules.tasks.ui.treelist.TreeList;
import org.netbeans.modules.tasks.ui.treelist.TreeListModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.tasks.ui.LinkButton;
import org.netbeans.modules.tasks.ui.actions.CreateCategoryAction;
import org.netbeans.modules.tasks.ui.actions.CreateRepositoryAction;
import org.netbeans.modules.tasks.ui.cache.CategoryEntry;
import org.netbeans.modules.tasks.ui.cache.DashboardStorage;
import org.netbeans.modules.tasks.ui.cache.TaskEntry;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.filter.TaskFilter;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.treelist.*;
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
    private final TitleNode titleClosedNode;
    private final TitleNode titleCategoryNode;
    private final TitleNode titleRepositoryNode;
    private final Object LOCK = new Object();
    private Map<Category, AbstractCategoryNode> mapCategoryToNode;
    private List<CategoryNode> categoryNodes;
    private List<ClosedCategoryNode> closedCategoryNodes;
    private List<RepositoryNode> repositoryNodes;
    private List<ClosedRepositoryNode> closedRepositoryNodes;
    private AppliedFilters appliedFilters;
    private int taskFilterHits;
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
        mapCategoryToNode = new HashMap<Category, AbstractCategoryNode>();
        categoryNodes = new ArrayList<CategoryNode>();
        closedCategoryNodes = new ArrayList<ClosedCategoryNode>();
        repositoryNodes = new ArrayList<RepositoryNode>();
        closedRepositoryNodes = new ArrayList<ClosedRepositoryNode>();

        LinkButton btnAddCategory = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_category.png", true), new CreateCategoryAction()); //NOI18N
        btnAddCategory.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_CreateCategory")); // NOI18N
        titleCategoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Categories"), btnAddCategory); // NOI18N
        LinkButton btnAddRepo = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/add_repo.png", true), new CreateRepositoryAction()); //NOI18N
        btnAddRepo.setToolTipText(NbBundle.getMessage(DashboardViewer.class, "LBL_AddRepo")); // NOI18N
        titleRepositoryNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Repositories"), btnAddRepo); // NOI18N
        titleClosedNode = new TitleNode(NbBundle.getMessage(TitleNode.class, "LBL_Closed"), null); // NOI18N
        model.addRoot(-1, titleCategoryNode);
        model.addRoot(-1, titleRepositoryNode);

        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardViewer.class, "A11Y_TeamProjects"); //NOI18N
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        appliedFilters = new AppliedFilters();
        taskFilterHits = 0;
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
        AbstractCategoryNode destCategoryNode = mapCategoryToNode.get(category);
        TaskNode toAdd = new TaskNode(taskNode.getTask(), destCategoryNode);
        if (destCategoryNode.addTaskNode(toAdd, appliedFilters.isInFilter(toAdd.getTask()))) {
            //remove from old category
            if (taskNode.isCategorized()) {
                removeTask(taskNode);
            }
            //set new category
            toAdd.setCategory(category);
            model.contentChanged(destCategoryNode);
            destCategoryNode.refresh();
        }
        storeCategory(category);
    }

    public void removeTask(TaskNode taskNode) {
        AbstractCategoryNode categoryNode = mapCategoryToNode.get(taskNode.getCategory());
        taskNode.setCategory(null);
        categoryNode.removeTaskNode(taskNode);
        model.contentChanged(categoryNode);
        //TODO only remove that child, dont refresh all
        categoryNode.refresh();
        storeCategory(categoryNode.getCategory());
    }

    public List<Category> getCategories() {
        List<AbstractCategoryNode> cat = getCategoryNodes();
        List<Category> list = new ArrayList<Category>(cat.size());
        for (AbstractCategoryNode abstractCategoryNode : cat) {
            list.add(abstractCategoryNode.getCategory());
        }
        return list;
    }

    private List<AbstractCategoryNode> getCategoryNodes() {
        List<AbstractCategoryNode> cat = new ArrayList<AbstractCategoryNode>(categoryNodes.size() + closedCategoryNodes.size());
        cat.addAll(categoryNodes);
        cat.addAll(closedCategoryNodes);
        Collections.sort(cat);
        return cat;
    }

    public boolean isCategoryNameUnique(String categoryName) {
        List<AbstractCategoryNode> list = new ArrayList<AbstractCategoryNode>(categoryNodes.size() + closedCategoryNodes.size());
        list.addAll(categoryNodes);
        list.addAll(closedCategoryNodes);
        for (AbstractCategoryNode node : list) {
            if (node.getCategory().getName().equalsIgnoreCase(categoryName)) {
                return false;
            }
        }
        return true;
    }

    public void renameCategory(Category category, final String newName) {
        AbstractCategoryNode node = mapCategoryToNode.get(category);
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
        CategoryNode newCategoryNode = new CategoryNode(category);
        categoryNodes.add(newCategoryNode);
        int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
        mapCategoryToNode.put(category, newCategoryNode);
        addCategoryToModel(index, newCategoryNode);
        storeCategory(category);
    }

    public void deleteCategory(final Category category) {
        AbstractCategoryNode node = mapCategoryToNode.remove(category);
        model.removeRoot(node);
        if (node instanceof CategoryNode) {
            CategoryNode toDelete = (CategoryNode) node;
            categoryNodes.remove(toDelete);
        } else {
            ClosedCategoryNode toDelete = (ClosedCategoryNode) node;
            closedCategoryNodes.remove(toDelete);
            if (closedRepositoryNodes.isEmpty() && closedCategoryNodes.isEmpty()) {
                model.removeRoot(titleClosedNode);
            }
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                DashboardStorage.getInstance().deleteCategory(category.getName());
            }
        });
    }

    public void closeCategory(CategoryNode categoryNode) {
        synchronized (LOCK) {
            Category category = categoryNode.getCategory();
            categoryNodes.remove(categoryNode);

            //remove closed category from categories part of the view
            model.removeRoot(categoryNode);

            //add category to the model - sorted
            if (!model.getRootNodes().contains(titleClosedNode)) {
                model.addRoot(-1, titleClosedNode);
            }
            int index = model.getRootNodes().indexOf(titleClosedNode) + 1;
            ClosedCategoryNode closedCategoryNode = new ClosedCategoryNode(category);
            closedCategoryNodes.add(closedCategoryNode);
            Collections.sort(closedCategoryNodes);
            mapCategoryToNode.put(category, closedCategoryNode);
            addCategoryToModel(index, closedCategoryNode);
        }
        storeClosedCategories();
    }

    public void openCategory(ClosedCategoryNode closedCategoryNode) {
        synchronized (LOCK) {
            Category category = closedCategoryNode.getCategory();
            closedCategoryNodes.remove(closedCategoryNode);

            //remove opened category from closed categories part of the view
            model.removeRoot(closedCategoryNode);

            if (closedRepositoryNodes.isEmpty() && closedCategoryNodes.isEmpty()) {
                model.removeRoot(titleClosedNode);
            }
            //add category to the model - sorted
            int index = model.getRootNodes().indexOf(titleCategoryNode) + 1;
            final CategoryNode categoryNode = new CategoryNode(category);
            categoryNodes.add(categoryNode);
            Collections.sort(categoryNodes);
            mapCategoryToNode.put(category, categoryNode);
            addCategoryToModel(index, categoryNode);
            categoryNode.setExpanded(true);
        }
        storeClosedCategories();
    }

    private void addCategoryToModel(int index, AbstractCategoryNode categoryNode) {
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof AbstractCategoryNode) {
                AbstractCategoryNode displNode = (AbstractCategoryNode) node;
                if (categoryNode.compareTo(displNode) < 0) {
                    model.addRoot(model.getAllNodes().indexOf(node), categoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                model.addRoot(model.getAllNodes().indexOf(node), categoryNode);
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
        if (closedCategoryNodes.isEmpty()) {
            return;
        }
        final DashboardStorage storage = DashboardStorage.getInstance();
        final List<String> names = new ArrayList<String>(closedCategoryNodes.size());
        for (ClosedCategoryNode categoryNode : closedCategoryNodes) {
            names.add(categoryNode.getCategory().getName());
        }
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedCategories(names);
            }
        });
    }

    public void addRepository(Repository repository) {
        //add repository to the model - sorted
        RepositoryNode repositoryNode = new RepositoryNode(repository);
        repositoryNodes.add(repositoryNode);
        int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
        addRepositoryToModel(index, repositoryNode);
    }

    public void removeRepository(AbstractRepositoryNode repositoryNode) {
        if (repositoryNode instanceof RepositoryNode) {
            repositoryNodes.remove((RepositoryNode) repositoryNode);
        } else {
            closedRepositoryNodes.remove((ClosedRepositoryNode) repositoryNode);
        }
        model.removeRoot(repositoryNode);
    }

    public void closeRepository(RepositoryNode repositoryNode) {
        synchronized (LOCK) {
            Repository repository = repositoryNode.getRepository();
            repositoryNodes.remove(repositoryNode);

            //remove closed repository from repositories part of the view
            model.removeRoot(repositoryNode);

            //add repository to the model - sorted
            if (!model.getRootNodes().contains(titleClosedNode)) {
                model.addRoot(-1, titleClosedNode);
            }
            int index = model.getRootNodes().indexOf(titleClosedNode) + closedCategoryNodes.size() + 1;
            ClosedRepositoryNode closedRepositoryNode = new ClosedRepositoryNode(repository);
            closedRepositoryNodes.add(closedRepositoryNode);
            Collections.sort(closedRepositoryNodes);
            addRepositoryToModel(index, closedRepositoryNode);
        }
        storeClosedRepositories();
    }

    public void openRepository(ClosedRepositoryNode closedRepositoryNode) {
        synchronized (LOCK) {
            Repository repository = closedRepositoryNode.getRepository();
            closedRepositoryNodes.remove(closedRepositoryNode);

            //remove opened category from closed categories part of the view
            model.removeRoot(closedRepositoryNode);

            if (closedRepositoryNodes.isEmpty() && closedCategoryNodes.isEmpty()) {
                model.removeRoot(titleClosedNode);
            }
            //add category to the model - sorted
            int index = model.getRootNodes().indexOf(titleRepositoryNode) + 1;
            final RepositoryNode repositoryNode = new RepositoryNode(repository);
            repositoryNodes.add(repositoryNode);
            Collections.sort(repositoryNodes);
            addRepositoryToModel(index, repositoryNode);
            repositoryNode.setExpanded(true);
        }
        storeClosedRepositories();
    }

    private void addRepositoryToModel(int index, AbstractRepositoryNode repositoryNode) {
        int size = model.getRootNodes().size();
        boolean added = false;
        for (; index < size; index++) {
            TreeListNode node = model.getRootNodes().get(index);
            if (node instanceof AbstractRepositoryNode) {
                AbstractRepositoryNode displNode = (AbstractRepositoryNode) node;
                if (repositoryNode.compareTo(displNode) < 0) {
                    model.addRoot(model.getAllNodes().indexOf(node), repositoryNode);
                    added = true;
                    break;
                }
            } else {
                // the end of category list, add
                model.addRoot(model.getAllNodes().indexOf(node), repositoryNode);
                added = true;
                break;
            }
        }
        if (!added) {
            model.addRoot(-1, repositoryNode);
        }
    }

    private void storeClosedRepositories() {
        if (closedRepositoryNodes.isEmpty()) {
            return;
        }
        final DashboardStorage storage = DashboardStorage.getInstance();
        final List<String> ids = new ArrayList<String>(closedRepositoryNodes.size());
        for (ClosedRepositoryNode repositoryNode : closedRepositoryNodes) {
            ids.add(repositoryNode.getRepository().getId());
        }

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                storage.storeClosedRepositories(ids);
            }
        });
    }

    public AppliedFilters getAppliedFilters() {
        return appliedFilters;
    }

    public int updateTaskFilter(TaskFilter oldFilter, TaskFilter newFilter) {
        if (oldFilter != null) {
            appliedFilters.removeFilter(oldFilter);
        }
        return applyTaskFilter(newFilter);
    }

    public int applyTaskFilter(TaskFilter taskFilter) {
        appliedFilters.addFilter(taskFilter);
        taskFilterHits = 0;
        refreshContent();
        return taskFilterHits;
    }

    public int removeTaskFilter(TaskFilter taskFilter) {
        appliedFilters.removeFilter(taskFilter);
        taskFilterHits = 0;
        persistExpanded = !taskFilter.expandNodes();
        refreshContent();
        persistExpanded = true;
        return taskFilterHits;
    }

    public boolean expandNodes() {
        return appliedFilters.expandNodes();
    }

    public boolean isNodeExpanded(TreeListNode node) {
        if (appliedFilters.expandNodes()) {
            return true;
        }
        return expandedNodes.contains(node);
    }

    private void loadData() {
        //TODO not like this
        AtomicBoolean cancel = new AtomicBoolean(false);
        Runnable load = new Runnable() {
            @Override
            public void run() {
                loadRepositories();
                loadCategories();
            }
        };
        ProgressUtils.runOffEventDispatchThread(load, NbBundle.getMessage(DashboardViewer.class, "LBL_LoadingDashboard"), cancel, false);
    }

    private void loadCategories() {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<CategoryEntry> categoryEntries = storage.readCategories();
        final List<Category> categories = new ArrayList<Category>(categoryEntries.size());
        for (CategoryEntry categoryEntry : categoryEntries) {
            List<Issue> tasks = loadTasks(categoryEntry.getTaskEntries());
            categories.add(new Category(categoryEntry.getCategoryName(), tasks));
        }
        List<String> names = storage.readClosedCategories();
        final List<Category> closedCategories = new ArrayList<Category>(names.size());

        for (String name : names) {
            for (Category category : categories) {
                if (category.getName().equals(name)) {
                    categories.remove(category);
                    closedCategories.add(category);
                    break;
                }
            }
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!closedCategories.isEmpty() && !model.getRootNodes().contains(titleClosedNode)) {
                        model.addRoot(-1, titleClosedNode);
                    }
                    setCategories(categories, closedCategories);
                }
            });
        }
    }

    private List<Issue> loadTasks(List<TaskEntry> taskEntries) {
        List<Issue> tasks = new ArrayList<Issue>(taskEntries.size());
        for (TaskEntry taskEntry : taskEntries) {
            Repository repository = getRepository(taskEntry.getRepositoryId());
            if (repository != null) {
                tasks.add(repository.getIssue(taskEntry.getIssueId()));
            }
        }
        return tasks;
    }

    private Repository getRepository(String repositoryId) {
        List<Repository> repositories = new ArrayList<Repository>(Util.getRepositories());
        for (Repository repository : repositories) {
            if (repository.getId().equals(repositoryId)) {
                return repository;
            }
        }
        return null;
    }

    private void loadRepositories() {
        final List<Repository> repositories = new ArrayList<Repository>(Util.getRepositories());
        List<String> ids = DashboardStorage.getInstance().readClosedRepositories();
        final List<Repository> closedRepositories = new ArrayList<Repository>(ids.size());

        for (String id : ids) {
            for (Repository repository : repositories) {
                if (repository.getId().equals(id)) {
                    repositories.remove(repository);
                    closedRepositories.add(repository);
                    break;
                }
            }
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!closedRepositories.isEmpty() && !model.getRootNodes().contains(titleClosedNode)) {
                        model.addRoot(-1, titleClosedNode);
                    }
                    setRepositories(repositories, closedRepositories);
                }
            });
        }
    }

    private TaskNode getCategorizedTask(TaskNode taskNode) {
        List<AbstractCategoryNode> catNodes = getCategoryNodes();
        for (AbstractCategoryNode categoryNode : catNodes) {
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
        List<Category> categoryList = new ArrayList<Category>(categoryNodes.size());
        for (CategoryNode categoryNode : categoryNodes) {
            categoryList.add(categoryNode.getCategory());
        }
        List<Category> closedCategoryList = new ArrayList<Category>(closedCategoryNodes.size());
        for (ClosedCategoryNode categoryNode : closedCategoryNodes) {
            closedCategoryList.add(categoryNode.getCategory());
        }
        List<Repository> repositoryList = new ArrayList<Repository>(repositoryNodes.size());
        for (RepositoryNode repositoryNode : repositoryNodes) {
            repositoryList.add(repositoryNode.getRepository());
        }
        List<Repository> closedRepositoryList = new ArrayList<Repository>(closedCategoryNodes.size());
        for (ClosedRepositoryNode repositoryNode : closedRepositoryNodes) {
            closedRepositoryList.add(repositoryNode.getRepository());
        }
        //remove closed nodes title during filtering
        if (!appliedFilters.isEmpty()) {
            model.removeRoot(titleClosedNode);
        } else if (!model.getRootNodes().contains(titleClosedNode)) {
            model.addRoot(-1, titleClosedNode);
        }
        setRepositories(repositoryList, closedRepositoryList);
        setCategories(categoryList, closedCategoryList);
    }

    void setCategories(List<Category> categories, List<Category> closedCategories) {
        synchronized (LOCK) {
            removeNodesFromModel(AbstractCategoryNode.class);
            this.categoryNodes.clear();
            for (Category category : categories) {
                categoryNodes.add(new CategoryNode(category));
            }
            this.closedCategoryNodes.clear();
            for (Category category : closedCategories) {
                closedCategoryNodes.add(new ClosedCategoryNode(category));
            }
            mapCategoryToNode.clear();
            Collections.sort(this.categoryNodes);
            Collections.sort(this.closedCategoryNodes);
            int index = model.getAllNodes().indexOf(titleCategoryNode) + 1;
            for (CategoryNode categoryNode : categoryNodes) {
                if (appliedFilters.isEmpty() || !categoryNode.getFilteredTaskNodes().isEmpty()) {
                    taskFilterHits += categoryNode.getTotalCount();
                    mapCategoryToNode.put(categoryNode.getCategory(), categoryNode);
                    addRootToModel(index++, categoryNode);
                }
            }
            index = model.getAllNodes().indexOf(titleClosedNode) + 1;
            for (ClosedCategoryNode closedCategoryNode : closedCategoryNodes) {
                //for filtering dont show closed nodes
                if (appliedFilters.isEmpty()) {
                    mapCategoryToNode.put(closedCategoryNode.getCategory(), closedCategoryNode);
                    addRootToModel(index++, closedCategoryNode);
                }
            }
        }
    }

    void setRepositories(List<Repository> repositories, List<Repository> closedRepositories) {
        synchronized (LOCK) {
            removeNodesFromModel(AbstractRepositoryNode.class);
            this.repositoryNodes.clear();
            for (Repository repository : repositories) {
                repositoryNodes.add(new RepositoryNode(repository));
            }
            this.closedRepositoryNodes.clear();
            for (Repository repository : closedRepositories) {
                closedRepositoryNodes.add(new ClosedRepositoryNode(repository));
            }
            Collections.sort(this.repositoryNodes);
            Collections.sort(this.closedRepositoryNodes);
            int index = model.getAllNodes().indexOf(titleRepositoryNode) + 1;
            for (RepositoryNode repositoryNode : repositoryNodes) {
                if (appliedFilters.isEmpty() || !repositoryNode.getFilteredQueryNodes().isEmpty()) {
                    taskFilterHits += repositoryNode.getFilterHits();
                    addRootToModel(index++, repositoryNode);
                }
            }
            index = model.getAllNodes().indexOf(titleClosedNode) + closedCategoryNodes.size() + 1;
            for (ClosedRepositoryNode closedRepositoryNode : closedRepositoryNodes) {
                //for filtering dont show closed nodes
                if (appliedFilters.isEmpty()) {
                    addRootToModel(index++, closedRepositoryNode);
                }
            }
        }
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
