/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.filesystems;

import java.util.Collection;
import java.util.Date;
import java.util.EventObject;

/** Event for listening on filesystem changes.
* <P>
* By calling {@link #getFile} the original file where the action occurred
* can be obtained.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class FileEvent extends EventObject {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 1028087432345400108L;

    /** Original file object where the action took place. */
    private FileObject file;

    /** time when this event has been fired */
    private long time;

    /** is expected? */
    private boolean expected;

    /***/
    private EventControl.AtomicActionLink atomActionID;
    private transient Collection<Runnable> postNotify;

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    */
    public FileEvent(FileObject src) {
        this(src, src);
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * <p>
    * Note that the two arguments of this method need not be identical
    * in cases where it is reasonable that a different file object from
    * the one affected would be listened to by other components. E.g.,
    * in the case of a file creation event, the event source (which
    * listeners are attached to) would be the containing folder, while
    * the action object would be the newly created file object.
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred */
    public FileEvent(FileObject src, FileObject file) {
        super(src);
        this.file = file;
        this.time = System.currentTimeMillis();
    }

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object. Important if FileEvent is created according to
    * existing FileEvent but with another source and file but with the same time.
    */
    FileEvent(FileObject src, FileObject file, long time) {
        this(src, file);
        this.time = time;
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * <p>
    * Note that the two arguments of this method need not be identical
    * in cases where it is reasonable that a different file object from
    * the one affected would be listened to by other components. E.g.,
    * in the case of a file creation event, the event source (which
    * listeners are attached to) would be the containing folder, while
    * the action object would be the newly created file object.
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred
    * @param expected sets flag whether the value was expected*/
    public FileEvent(FileObject src, FileObject file, boolean expected) {
        this(src, file);
        this.expected = expected;
    }

    /** @return the original file where action occurred
    */
    public final FileObject getFile() {
        return file;
    }

    /** The time when this event has been created.
    * @return the milliseconds
    */
    public final long getTime() {
        return time;
    }

    /** Getter to test whether the change has been expected or not.
    */
    public final boolean isExpected() {
        return expected;
    }

    /** Support for <em>batch</em> processing of events. In some situations
     * you may want to delay processing of received events until the last
     * known one is delivered. For example if there is a lot of operations
     * done inside {@link FileUtil#runAtomicAction(java.lang.Runnable)}
     * action, there can be valid reason to do the processing only after
     * all of them are delivered. In such situation attach your {@link Runnable}
     * to provided event. Such {@link  Runnable} is then guaranteed to be called once.
     * Either immediately (if there is no batch delivery in progress)
     * or some time later (if there is a batch delivery). You can attach
     * single runnable multiple times, even to different events in the
     * same batch section and its {@link  Runnable#run()} method will still be
     * called just once. {@link Object#equals(java.lang.Object)} is used
     * to check equality of two {@link Runnable}s.
     *
     * @since 7.24
     * @param r the runnable to execute when batch event deliver is over
     *   (can be even executed immediately)
     */
    public final void runWhenDeliveryOver(Runnable r) {
        Collection<Runnable> to = postNotify;
        if (to != null) {
            to.add(r);
        } else {
            r.run();
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName().replaceFirst(".+\\.", ""));
        b.append('[');
        FileObject src = (FileObject) getSource();
        if (src != file) {
            b.append("src=");
            b.append(FileUtil.getFileDisplayName(src));
            b.append(',');
        }
        b.append("file=");
        b.append(FileUtil.getFileDisplayName(file));
        b.append(",time=");
        b.append(new Date(time));
        b.append(",expected=");
        b.append(expected);
        insertIntoToString(b);
        b.append(']');
        return b.toString();
    }
    void insertIntoToString(StringBuilder b) {}

    /** */
    void setAtomicActionLink(EventControl.AtomicActionLink atomActionID) {
        this.atomActionID = atomActionID;
    }

    /** Tests if FileEvent was fired from atomic action.
     * @param run is tested atomic action.
     * @return true if fired from run.
     * @since 1.35
     */
    public boolean firedFrom(FileSystem.AtomicAction run) {
        EventControl.AtomicActionLink currentPropID = this.atomActionID;

        if (run == null) {
            return false;
        }

        while (currentPropID != null) {
            if (run.equals(currentPropID.getAtomicAction())) {
                return true;
            }

            currentPropID = currentPropID.getPreviousLink();
        }

        return false;
    }
    
    final boolean isAsynchronous() {
        EventControl.AtomicActionLink currentPropID = this.atomActionID;
        while (currentPropID != null) {
            final Object atomicAction = currentPropID.getAtomicAction();
            if (atomicAction != null && atomicAction.getClass().getName().indexOf("AsyncRefreshAtomicAction") != -1) {
                return true;
            }
            currentPropID = currentPropID.getPreviousLink();
        }

        return false;
    }

    void setPostNotify(Collection<Runnable> runs) {
        // cannot try to set the postNotify field twiced
        assert postNotify == null || runs == null;
        this.postNotify = runs;
    }
    
}
