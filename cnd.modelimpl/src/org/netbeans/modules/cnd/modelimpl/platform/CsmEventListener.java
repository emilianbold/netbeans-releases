/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
/*package*/ final class CsmEventListener {

    private final ProjectBase project;
    private final NativeProject nativeProject;
    private volatile boolean enabledEventsHandling = true;

    //private static final RequestProcessor RP = new RequestProcessor(CsmEventListener.class.getSimpleName());
    private final RequestProcessor.Task task;
    private final Object eventsLock = new Object();
    private int suspendCount = 0;

    private HashMap<String, CsmEvent> events = new LinkedHashMap<>();

    private static final CsmEvent NULL = CsmEvent.createEmptyEvent(CsmEvent.Kind.NULL);

    public CsmEventListener(ProjectBase project) {
        assert project.getPlatformProject() instanceof NativeProject;
        this.project = project;
        this.nativeProject = (NativeProject) project.getPlatformProject();
        this.task = ModelSupport.instance().getModel().createModelTask(new Worker(), getClass().getSimpleName());
    }

    public ProjectBase getProject() {
        return project;
    }

    public NativeProject getNativeProject() {
        return nativeProject;
    }
    
    public void enableListening(boolean enable) {
        if (TraceFlags.TIMING) {
            CsmEvent.LOG.log(Level.INFO, "\n%{0} ProjectListeners {1}...", new Object[] {enable ? "enable" : "disable",
                    nativeProject.getProjectDisplayName()});
        }
        enabledEventsHandling = enable;
    }

    void fireEvent(CsmEvent event) {
        switch (event.getKind()) {
            case FILE_INDEXED:
            case FOLDER_CREATED:
            case FILE_ATTRIBUTE_CHANGED:
            case NULL:
                return;
        }
        CsmEvent.trace("%s dispatched to %s", event, project);
        checkEvent(event);
        String path = getPath(event);
        synchronized (eventsLock) {
            CsmEvent prev = events.get(path);
            events.put(path, convert(prev, event));
        }
        task.schedule(0); // fe.runWhenDeliveryOver(taskScheduler); ???
    }

    String getPath(CsmEvent event) {
        FileObject fo = event.getFileObject();
        if (fo != null) {
            return fo.getPath();
        }
        NativeFileItem item = event.getNativeFileItem();
        if (item != null) {
            return item.getAbsolutePath();
        }
        NativeProject np = event.getNativeProject();
        if (np != null) {
            return np.getProjectRoot();
        }
        return "/"; //NOI18N
    }

    void checkEvent(CsmEvent event) {
        FileObject fo = event.getFileObject();
        if (fo != null) {
            try {
                assert fo.getFileSystem().equals(nativeProject.getFileSystem());
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        NativeProject np = event.getNativeProject();
        if (np == null) {
            NativeFileItem item = event.getNativeFileItem();
            if (item != null) {
                np = item.getNativeProject();
            }
        }
        if (np != null) {
            assert np.equals(nativeProject);
        }
    }

    void resume() {
        boolean schedule;
        synchronized (eventsLock) {
            CndUtils.assertTrue(suspendCount > 0, "resume without suspend " + suspendCount);
            suspendCount--;
            if (suspendCount < 0) {
                suspendCount = 0;
            }
            schedule = (suspendCount == 0);
        }
        if (schedule) {
            task.schedule(0);
        }
    }

    void suspend() {
        synchronized (eventsLock) {
            CndUtils.assertTrue(suspendCount >= 0, "suspend with " + suspendCount);
            suspendCount++;
        }
    }

    /*package*/void flush() {
        task.schedule(0);
        task.waitFinished();
    }

    //<editor-fold defaultstate="collapsed" desc="old impl">
    /***
   private void processEvents(Collection<CsmEvent> events) {
        if (!enabledEventsHandling) {
            CsmEvent.trace("events processing disabled, skipping %d events", events.size());
            return;
        }
        CsmEvent.trace("processing %d events", events.size());
        boolean checkForRemoved = false;
        for (CsmEvent event : events) {
            CsmEvent.trace("processing %s", event);
            switch (event.getKind()) {
                case FILE_DELETED:
                case ITEM_REMOVED:
                case FILE_RENAMED_DELETED:
                case ITEM_RENAMED_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_CREATED:
                case FILE_RENAMED_CREATED:
                    project.onFileObjectExternalCreate(event.getFileObject());
                    break;
                case ITEM_ADDED:
                    project.onFileItemsAdded(Arrays.asList(event.getNativeFileItem()));
                    break;
                case FOLDER_CREATED:
                    // nothing
                    break;
                case FILE_CHANGED:
                    project.findFile(event.getPath(), false, false);
                    break;
                case FILE_ATTRIBUTE_CHANGED:
                    // nothing
                    break;
                case ITEM_PROPERTY_CHANGED:
                    project.onFileItemsPropertyChanged(Arrays.asList(event.getNativeFileItem()), false);
                    break;
                case ITEMS_ALL_PROPERTY_CHANGED:
                    List<NativeFileItem> items = new ArrayList<>();
                    for (NativeFileItem item : nativeProject.getAllFiles()) {
                        if (!item.isExcluded()) {
                            switch (item.getLanguage()) {
                                case C:
                                case CPP:
                                case FORTRAN:
                                    items.add(item);
                                    break;
                                default:
                                    break;
                            }
                        }
                        project.onFileItemsPropertyChanged(items, true);
                    }
                    break;
                case ITEM_RENAMED_CREATED:
                    project.onFileItemRenamed(event.getOldPath(), event.getNativeFileItem());
                    break;
                case PROJECT_DELETED:
                    RepositoryUtils.onProjectDeleted(nativeProject);
                    break;
                case FILES_IN_SOURCE_ROOT_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_INDEXED:
                    CndUtils.assertTrue(false, "FILE_INDEXED event should never reach end listener"); //NOI18N
                    break;
                case NULL:
                    // nothing
                    break;
                default:
                    throw new AssertionError(event.getKind().name());
            }
            if (checkForRemoved) {
                project.checkForRemoved();
            }
        }
    }
     */
    //</editor-fold>

    private void processEvents(Collection<CsmEvent> events) {
        if (!enabledEventsHandling) {
            CsmEvent.trace("events processing disabled, skipping %d events", events.size());
            return;
        }
        CsmEvent.trace("processing %d events", events.size());

        boolean projectDeleted = false;
        boolean checkForRemoved = false;
        boolean projectRemoved = false;
        boolean allPropertiesChanged = false;
        List<FileObject> createdFiles = new ArrayList<>();
        List<NativeFileItem> addedItems = new ArrayList<>();
        List<NativeFileItem> changedItemProps = new ArrayList<>();
        List<String> changedFiles = new ArrayList<>();
        List<CsmEvent> renamedItems = new ArrayList<>();

        for (CsmEvent event : events) {
            CsmEvent.trace("processing %s", event);
            switch (event.getKind()) {
                case FILE_DELETED:
                case ITEM_REMOVED:
                case FILE_RENAMED_DELETED:
                case ITEM_RENAMED_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_CREATED:
                case FILE_RENAMED_CREATED:
                    createdFiles.add(event.getFileObject());
                    break;
                case ITEM_ADDED:
                    addedItems.add(event.getNativeFileItem());
                    break;
                case FOLDER_CREATED:
                    // nothing
                    break;
                case FILE_CHANGED:
                    changedFiles.add(event.getPath());
                    break;
                case FILE_ATTRIBUTE_CHANGED:
                    // nothing
                    break;
                case ITEM_PROPERTY_CHANGED:
                    changedItemProps.add(event.getNativeFileItem());
                    break;
                case ITEMS_ALL_PROPERTY_CHANGED:
                    allPropertiesChanged = true;
                    break;
                case ITEM_RENAMED_CREATED:
                    renamedItems.add(event);
                    break;
                case PROJECT_DELETED:
                    projectDeleted = true;
                    break;
                case FILES_IN_SOURCE_ROOT_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_INDEXED:
                    CndUtils.assertTrue(false, "FILE_INDEXED event should never reach end listener"); //NOI18N
                    break;
                case NULL:
                    // nothing
                    break;
                default:
                    throw new AssertionError(event.getKind().name());
            }

            if (projectDeleted) {
                RepositoryUtils.onProjectDeleted(nativeProject);
                // all other events aren't relevant any more
                return;
            }

            if (!renamedItems.isEmpty()) {
                for (CsmEvent e : renamedItems) {
                    project.onFileItemRenamed(e.getOldPath(), e.getNativeFileItem());
                }
            }

            if (allPropertiesChanged) {
                List<NativeFileItem> items = new ArrayList<>();
                for (NativeFileItem item : nativeProject.getAllFiles()) {
                    if (!item.isExcluded()) {
                        switch (item.getLanguage()) {
                            case C:
                            case CPP:
                            case FORTRAN:
                                items.add(item);
                                break;
                            default:
                                break;
                        }
                    }
                }
                project.onFileItemsPropertyChanged(items, true);
                changedItemProps.clear();
            }

            if(!changedFiles.isEmpty()) {
                for (String path : changedFiles) {
                    CsmFile csmFile = project.findFile(path, false, false);
                    if (csmFile != null) {
                        project.onFileImplExternalChange((FileImpl) csmFile);
                    }
                }
            }
            if (!changedItemProps.isEmpty()) {
                project.onFileItemsPropertyChanged(changedItemProps, false);
            }
            if (!createdFiles.isEmpty()) {
                project.onFileObjectExternalCreate(createdFiles);
            }
            if (!addedItems.isEmpty()) {
                project.onFileItemsAdded(addedItems);
            }
            if (checkForRemoved) {
                project.checkForRemoved();
            }
        }
    }

// the table below is incomplete: to me, it's easier to just fill the switch/case and add some comments there
// but I left it just in case I decide to fill it later :)
/*-------------------------------------------------------------------------------------------------------------------------------------------
                                     (prevKind)
(curKind) | F/DEL  | F/CR    | F/REN_CR | F/REN_DL |   F/CH   | I/ADD  | I/RM   | I/PROP | I/ALPROP | I/REN_CR | I/REN_DL | P/DEL  | F/RT_DEL |
----------------------------------------------------------------------------------------------------------------------------------------------
F/DEL     | F/DEL  | null    | null     | assert   | F/DEL    | null   | F/DEL  | F/DEL  | assert   | null     | assert   | P/DEL  | assert   |
I/RM      | I/RM   | null    | null     | assert   | I/RM     | null   | I/RM   | I/RM   | assert   | null     | assert   | P/DEL  | assert   |
F/RT_DEL  | assert | assert  | assert   | assert   | assert   | assert | assert | assert | assert   | assert   | assert   | P/DEL  | F/RT_DEL |
F/CR      | F/CH   | F/CR    | assert   | F/CH     | F/CH     | I/ADD  | ?      | ?      | assert   |          |          | P/DEL  |          |
I/ADD     | I/ADD  | I/ADD?  | I/ADD    | I/ADD    | I/ADD?   | I/ADD  | F/CH   |        | assert   |          |          | P/DEL  | assert   |
F/REN_CR  | F/CH   | assert  | assert   | F/CH     | assert   |        |        |        | assert   |          |          | P/DEL  |          |
I/REN_CR  |        |         |          |          |          |        |        |        | assert   |          |          | P/DEL  |          |
F/REN_DL  | assert | null (?)| null (?) | assert   | F/REN_DL |        |        |        | assert   |          |          | P/DEL  |          |
I/REN_DL  |        |         |          |          |          |        |        |        | assert   |          |          | P/DEL  |          |
F/CH      | assert | F/CR    | F/REN_CR | assert   | F/CH     |        |        |        | assert   |          |          | P/DEL  |          |
I/PROP    | null   | F/CR    | null     | assert   | I/PROP   | I/ADD  | I/RM   | I/PROP | assert   | I/REN_CR | assert   | P/DEL  | assert   |
I/ALPROP  | assert | assert  | assert   | assert   | assert   | assert | assert | assert | assert   | assert   | assert   | P/DEL  |          |
P/DEL     | P/DEL  | P/DEL   | P/DEL    | P/DEL    | P/DEL    | P/DEL  | P/DEL  | P/DEL  | P/DEL    | P/DEL    | P/DEL    | P/DEL  | P/DEL    |
----------------------------------------------------------------------------------------------------------------------------------------------*/

    private static CsmEvent convert(CsmEvent prev, CsmEvent cur) {
        if (prev == null || prev.getKind() == CsmEvent.Kind.NULL) {
            return cur;
        }

        if (prev.getKind() == CsmEvent.Kind.PROJECT_DELETED) {
            return prev;
        } else if (cur.getKind() == CsmEvent.Kind.PROJECT_DELETED) {
            return cur;
        }

        switch (cur.getKind()) {
            case FILE_DELETED: //<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur;
                    case ITEM_REMOVED:                  return cur; // doesn't matter, processing is the same
                    case FILE_CREATED:                  return doNull();
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // does that mean that the project is deleted?
                    case FILES_IN_SOURCE_ROOT_DELETED:  return cur; // doesn't matter, processing is thesame
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }   //</editor-fold>   
            case FILE_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doChanged(cur);
                    case ITEM_REMOVED:                  return doChanged(cur);
                    case FILE_CREATED:                  return prev;
                    case ITEM_ADDED:                    return prev; // item events are stronger
                    case FILE_RENAMED_CREATED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_CREATED:          return prev; // ITEM_RENAMED_CREATED will finally cause checkForRemove and nativeItemAdded
                    case FILE_RENAMED_DELETED:          return doChanged(cur);
                    case ITEM_RENAMED_DELETED:          return doChanged(cur);
                    case FILE_CHANGED:                  return prev;
                    case ITEM_PROPERTY_CHANGED:         return prev; // ?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_RENAMED_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doChanged(cur);
                    case ITEM_REMOVED:                  return doChanged(cur);
                    case FILE_CREATED:                  return doAssert(prev, cur);
                    case ITEM_ADDED:                    return prev;
                    case FILE_RENAMED_CREATED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_CREATED:          return doAssert(prev, cur);
                    case FILE_RENAMED_DELETED:          return doChanged(cur);
                    case ITEM_RENAMED_DELETED:          return doChanged(cur);
                    case FILE_CHANGED:                  return doAssert(prev, cur);
                    case ITEM_PROPERTY_CHANGED:         return cur; //?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_RENAMED_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doAssert(prev, cur);
                    case ITEM_REMOVED:                  return doNullOnRename(prev, cur); //?
                    case FILE_CREATED:                  return doNullOnRename(prev, cur);
                    case ITEM_ADDED:                    return doNullOnRename(prev, cur);
                    case FILE_RENAMED_CREATED:          return doNullOnRename(prev, cur);
                    case ITEM_RENAMED_CREATED:          return doNullOnRename(prev, cur);
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doAssert(prev, cur);
                    case ITEM_REMOVED:                  return cur;
                    case FILE_CREATED:                  return prev;
                    case FILE_RENAMED_CREATED:          return prev;
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_ADDED:                    return cur;
                    case ITEM_PROPERTY_CHANGED:         return prev;
                    case ITEM_RENAMED_CREATED:          return prev;
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur);
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_ADDED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur;
                    case ITEM_REMOVED:                  return doChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED,  cur);
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return cur;
                    case FILE_RENAMED_CREATED:          return cur;
                    case ITEM_RENAMED_CREATED:          return cur; //?
                    case FILE_RENAMED_DELETED:          return cur; //?
                    case ITEM_RENAMED_DELETED:          return cur; //?
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur; //?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_REMOVED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur; // procssing is the same
                    case ITEM_REMOVED:                  return cur;
                    case FILE_CREATED:                  return cur; //?
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return cur; // processing is the same
                    case ITEM_RENAMED_DELETED:          return cur; // processing is the same
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur; //???
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_PROPERTY_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doNull();
                    case ITEM_REMOVED:                  return doNull();
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return prev;
                    case FILE_RENAMED_CREATED:          return prev;
                    case ITEM_RENAMED_CREATED:          return prev;
                    case FILE_RENAMED_DELETED:          return doNull();
                    case ITEM_RENAMED_DELETED:          return doNull();
                    case FILE_CHANGED:                  return cur; // item event is stronger
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEMS_ALL_PROPERTY_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  // fallthrough
                    case ITEM_REMOVED:                  // fallthrough
                    case FILE_CREATED:                  // fallthrough
                    case FILE_RENAMED_CREATED:          // fallthrough
                    case FILE_RENAMED_DELETED:          // fallthrough
                    case FILE_CHANGED:                  // fallthrough
                    case ITEM_ADDED:                    // fallthrough
                    case ITEM_PROPERTY_CHANGED:         // fallthrough
                    case ITEM_RENAMED_CREATED:          // fallthrough
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur); // cur path is project, prev path is file
                    case ITEMS_ALL_PROPERTY_CHANGED:    return cur;
                    case FILES_IN_SOURCE_ROOT_DELETED:  return cur; // should we create a synthetic event for this?
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_RENAMED_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doNull();
                    case ITEM_REMOVED:                  return doNull();
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return cur;
                    case ITEM_RENAMED_DELETED:          return cur;
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur; //???
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_RENAMED_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doChanged(cur);
                    case ITEM_REMOVED:                  return doChanged(cur);
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return prev;// or cur... doesn/t really matter
                    case FILE_RENAMED_CREATED:          return cur;
                    case ITEM_RENAMED_CREATED:          return cur;
                    case FILE_RENAMED_DELETED:          return doChanged(cur);
                    case ITEM_RENAMED_DELETED:          return doChanged(cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case FILES_IN_SOURCE_ROOT_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  // fallthrough
                    case ITEM_REMOVED:                  // fallthrough
                    case FILE_CREATED:                  // fallthrough
                    case ITEM_ADDED:                    // fallthrough
                    case FILE_RENAMED_CREATED:          // fallthrough
                    case ITEM_RENAMED_CREATED:          // fallthrough
                    case FILE_RENAMED_DELETED:          // fallthrough
                    case ITEM_RENAMED_DELETED:          // fallthrough
                    case FILE_CHANGED:                  // fallthrough
                    case ITEM_PROPERTY_CHANGED:         return doAssert(prev, cur); // cur path is project, prev path is item ?!
                    case ITEMS_ALL_PROPERTY_CHANGED:    return prev; // ??? should we create a combined event?
                    case FILES_IN_SOURCE_ROOT_DELETED:  return cur;
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            default:    throw new AssertionError(prev.getKind());
        }
    }

    private static CsmEvent doAssert(CsmEvent prev, CsmEvent cur) {
        return doAssert(prev, cur, cur);
    }

    private static CsmEvent doAssert(CsmEvent prev, CsmEvent cur, CsmEvent correct ){
        CndUtils.assertTrueInConsole(false, "invalid states " + prev + " " + cur);
        return correct;
    }

    private static CsmEvent doChanged(CsmEvent.Kind kind, CsmEvent cur) {
        return CsmEvent.create(kind, cur);
    }

    private static CsmEvent doChanged(CsmEvent cur) {
        return CsmEvent.create(CsmEvent.Kind.FILE_CHANGED, cur);
    }

    private static CsmEvent doNull() {
        return NULL;
    }

    private static CsmEvent doNullOnRename(CsmEvent prev, CsmEvent cur) {
        return NULL;
    }

    private static boolean toBeSuspended(CsmEvent value) {
        switch (value.getKind()) {
            case FILE_DELETED:
            case ITEM_REMOVED:
            case FILES_IN_SOURCE_ROOT_DELETED:
                return true;
            case FILE_CREATED:
            case FILE_RENAMED_CREATED:
            case FILE_RENAMED_DELETED:
            case FOLDER_CREATED:
            case FILE_CHANGED:
            case FILE_ATTRIBUTE_CHANGED:
            case ITEM_ADDED:
            case ITEM_PROPERTY_CHANGED:
            case ITEMS_ALL_PROPERTY_CHANGED:
            case ITEM_RENAMED_DELETED:
            case ITEM_RENAMED_CREATED:
            case PROJECT_DELETED:
            case FILE_INDEXED:
            case NULL:
                return false;
            default:
                throw new AssertionError(value.getKind().name());
        }
    }

    private final class Worker implements Runnable {
        @Override
        public void run() {
            HashMap<String, CsmEvent> curEvents;
            synchronized (eventsLock) {
                if (events.isEmpty()) {
                    return;
                }
                curEvents = events;
                if (suspendCount == 0) {
                    events = new LinkedHashMap<>();
                } else {
                    HashMap<String, CsmEvent> suspendedRemoves = new LinkedHashMap<>();
                    for (Iterator<Map.Entry<String, CsmEvent>> it = curEvents.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, CsmEvent> entry = it.next();
                        CsmEvent value = entry.getValue();
                        // hold on with delete events and delete/create pair from rename event
                        if (toBeSuspended(value)) {
                            //(value.getKind() == CsmEvent.Kind.FILE_CREATED && value.event instanceof FileRenameEvent)) {
                            suspendedRemoves.put(entry.getKey(), value);
                            it.remove();
                        }
                    }
                    events = suspendedRemoves;
                }
            }
            processEvents(curEvents.values());
        }
    }
}
