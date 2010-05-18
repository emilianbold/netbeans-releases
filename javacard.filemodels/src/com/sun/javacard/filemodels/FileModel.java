/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javacard.filemodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model representing an XML file, or data which can be output to an XML file.
 *
 * Typical usage in the IDE is to construct a model from a file, and then apply it to a user
 * interface in a project properties customizer.  The UI later constructs a new model from the
 * UI and uses equals() to determine if anything needs to be written to disk or not.
 *
 * Also generally useful for reading the various types of XML files involved in Java Card&trade; projects.
 *
 * @param <T> The type of entry, a subclass of FileModelEntry, which can be returned by getData()
 */
public abstract class FileModel<T extends FileModelEntry> {
    private volatile boolean closed;
    private final List<T> data = new ArrayList<T>();
    private volatile boolean error;
    private volatile boolean hasUnknownTags;
    private final Object dataLock = new Object();

    /**
     * Should return true if the model is valid but the XML it was created from (if any) contained
     * tags that are not expected.  Some models only handle a limited subset of the entire XML
     * spec they model, and so the user should be warned that hand-edits would be deleted if this
     * model were written to disk.
     * @return True if the parsing process (if any) encountered unknown tags
     */
    public final boolean hasUnknownTags() {
        return hasUnknownTags;
    }

    final void unknownTagEncountered() {
        hasUnknownTags = true;
    }

    protected void remove (T t) {
        synchronized (dataLock) {
            data.remove(t);
        }
    }

    /**
     * Return a valid XML representation of this model as a string.
     *
     * @return The xml version of this model
     */
    public abstract String toXml();

    /**
     * Returns a copy of the data in this model.
     * @return A copy of the data in this model.
     */
    public final List<? extends T> getData() {
        List<T> result = new ArrayList<T>();
        synchronized (dataLock) {
            result.addAll(data);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Add an entry to this model.  Note that if close() has already been
     * called (and clear() has not since then), then this method will throw
     * an IllegalStateException.
     * @param entry The entry
     */
    public final void add(T entry) {
        if (closed) {
            throw new IllegalStateException("Adding to completed model"); //NOI18N
        }
        synchronized (dataLock) {
            data.add(entry);
        }
    }

    /**
     * Clear all data in this model and return it to an uninitialized state.
     */
    public final void clear() {
        synchronized (dataLock) {
            data.clear();
        }
        closed = false;
    }

    /**
     * Close this model, blocking any further calls to add().  Any code which
     * builds a model from an XML file or a UI or similar should call close() when
     * it has completed its work.
     */
    public final void close() {
        closed = true;
    }

    /**
     * Determine if this model can still be added to.
     * @return Whether or not the model is open to additions.
     */
    public final boolean isClosed() {
        return closed;
    }

    /**
     * Determine whether this model has any contents.
     * @return True if this model is empty
     */
    public final boolean isEmpty() {
        synchronized (dataLock) {
            return data.isEmpty();
        }
    }

    /**
     * There was some kind of parsing error or IOException when this
     * model was being created.
     * @return
     */
    public final boolean isError() {
        return error;
    }

    /**
     * Set the error flag, indicating that an error was encountered during
     * the creation of this model and its contents are not to be trusted.
     */
    protected final void error() {
        error = true;
    }

    /**
     * Useful for subclasses which want to specify a problem the user
     * needs to correct.  Return null if the model is valid (i.e. it could
     * safely be written to disk and re-read without errors).
     * @return Null if this model is valid, or a localized error string if not
     */
    protected String getProblemInternal() {
        return null;
    }

    /**
     * Get a problem string that can be shown to the user indicating why this
     * model is not in a valid state.  This method first calls getProblemInternal(),
     * and then iterates this model's contents, returning the first non-null result
     * from FileModelEntry.getProblem(), if any.
     * @return A localized string
     */
    public final String getProblem() {
        String problem = getProblemInternal();
        if (problem == null) {
            for (T entry : getData()) {
                problem = entry.getProblem();
                if (problem != null) {
                    break;
                }
            }
        }
        return problem;
    }

    @Override
    public boolean equals (Object o) {
        boolean result = o == null ? false : o.getClass() == getClass() ? true : false;
        if (result) {
            FileModel<?> other = (FileModel<?>) o;
            List <?> myData = getData();
            List <?> otherData = other.getData();
            result = myData.equals(otherData);
        }
        return result;
    }

    public int hashCode() {
        synchronized (dataLock) {
            return data.hashCode();
        }
    }
}
