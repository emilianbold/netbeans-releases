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

package org.netbeans.modules.soa.xpath.mapper.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A class, which wraps a list and contains a direction flag.
 * It helps resolve mess with direction.
 *
 * It is designed as strictly immutable.
 *
 * ATTANTION!!!!
 * FORWARD DIRECTION is a direction, where the variable is at the end
 * of the list, like in the following example:
 *     Attribute -> Element -> Element -> Part -> Variable
 *
 * @param <T>
 *
 * @author Nikita Krjukov
 */
public final class DirectedList<T> implements Iterable<T> {

    public static DirectedList EMPTY = new DirectedList(Collections.EMPTY_LIST, true);

    public static <T> DirectedList<T> empty() {
        return (DirectedList<T>)EMPTY;
    }

    private List<T> mList;
    private boolean mForwardDirection;

    public DirectedList(List<T> list, boolean forwardDirection) {
        mList = new ArrayList(list);
        mForwardDirection = forwardDirection;
    }

    public interface Filter {
        public boolean pass(Object obj);
    }

    public DirectedList(DirectedList<T> source, Filter filter) {
        Iterator<T> itr = source.getList().iterator();
        ArrayList<T> newList = new ArrayList<T>();
        while (itr.hasNext()) {
            T nextItem = itr.next();
            if (filter.pass(nextItem)) {
                newList.add(nextItem);
            }
        }
        mList = newList;
        mForwardDirection = source.isForwardDirected();
    }

    public DirectedList(DirectedList base, T additional, boolean toEnd) {
        List<T> baseList = base.getList();
        LinkedList<T> newList = new LinkedList<T>(baseList);
        if (base.isForwardDirected()) {
            if (toEnd) {
                newList.addLast(additional);
            } else {
                newList.addFirst(additional);
            }
        } else {
            if (toEnd) {
                newList.addFirst(additional);
            } else {
                newList.addLast(additional);
            }
        }
        //
        mList = newList;
        mForwardDirection = base.isForwardDirected();
    }

    public List<T> getList() {
        return mList;
    }

    public boolean isForwardDirected() {
        return mForwardDirection;
    }

    public int size() {
        return mList.size();
    }

    public boolean isEmpty() {
        return mList == null || mList.isEmpty();
    }

    public ListIterator<T> forwardIterator() {
        if (mForwardDirection) {
            return mList.listIterator();
        } else {
            return new ReversedListIterator(mList);
        }
    }

    public ListIterator<T> backwardIterator() {
        if (mForwardDirection) {
            return new ReversedListIterator(mList);
        } else {
            return mList.listIterator();
        }
    }

    public Iterator<T> iterator() {
        return forwardIterator();
    }

    public List<T> constructBackwardList() {
        if (!mForwardDirection) {
            return mList;
        } else {
            ArrayList<T> lList = new ArrayList<T>(mList.size());
            //
            Iterator<T> itr = backwardIterator();
            while(itr.hasNext()) {
                T obj = itr.next();
                lList.add(obj);
            }
            //
            return lList;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mForwardDirection) {
            sb.append("FORWARD"); // NOI18N
        } else {
            sb.append("BACKWARD"); // NOI18N
        }
        //
        String lineSeparator = System.getProperty("line.separator");
        sb.append(lineSeparator);
        //
        sb.append(mList.toString());
        //
        return sb.toString();
    }

    public static class ReversedListIterator<T> implements ListIterator<T> {
        private ListIterator<T> mItr;

        public ReversedListIterator(List<T> list) {
            mItr = list.listIterator(list.size());
        }

        public boolean hasNext() {
            return mItr.hasPrevious();
        }

        public T next() {
            return mItr.previous();
        }

        public boolean hasPrevious() {
            return mItr.hasNext();
        }

        public T previous() {
            return mItr.next();
        }

        public int nextIndex() {
            return mItr.previousIndex();
        }

        public int previousIndex() {
            return mItr.nextIndex();
        }

        public void remove() {
//            mItr.remove();
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void set(T o) {
//            mItr.set(o);
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void add(T o) {
//            mItr.add(o);
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

    }

}

