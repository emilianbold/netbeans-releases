/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.javacard.project.libraries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tim Boudreau
 */
public final class LibrariesModel {

    private final List<LibraryEntry> entries = new ArrayList<LibraryEntry>();
    private final Set<LibraryEntry> removed = new HashSet<LibraryEntry>();
    private final ChangeSupport supp = new ChangeSupport(this);

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    public boolean moveEntryUp(LibraryEntry entry) {
        int ix = entries.indexOf(entry);
        if (ix > 0) {
            entries.remove(ix);
            entries.add(ix - 1, entry);
            supp.fireChange();
            return true;
        }
        return false;
    }

    public boolean moveEntryDown(LibraryEntry entry) {
        int ix = entries.indexOf(entry);
        if (ix < entries.size() - 1) {
            entries.remove(ix);
            entries.add(ix + 1, entry);
            supp.fireChange();
            return true;
        }
        return false;
    }

    public void add(LibraryEntry entry) {
        entries.add(entry);
    }

    public List<LibraryEntry> entries() {
        return new ArrayList<LibraryEntry>(entries);
    }

    public void remove(LibraryEntry e) {
        if (entries.remove(e)) {
            removed.add (e);
            supp.fireChange();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LibrariesModel other = (LibrariesModel) obj;
        if (this.entries != other.entries && (this.entries == null || !this.entries.equals(other.entries))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.entries != null ? this.entries.hashCode() : 0);
        return hash;
    }

    public Set<LibraryEntry> removed() {
        Set s = new HashSet<LibraryEntry>(removed);
        //remove anything re-added
        s.removeAll(entries);
        return s;
    }

    public void saveToProject (JCProject project) {
        ReferenceHelper h = project.refHelper();
        for (LibraryEntry e : removed()) {
            File f = e.toFile();
        }
    }
    
}
