/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.CVSRoot;

import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

/**
 * This admin handler represents virtual reality, it
 * does not touch any disk file. Instead it pretends
 * that it represents empty local folder corresponding
 * to given repository path.
 *
 * <p>It supports only non-recursive commands.
 *
 * @author Petr Kuzel
 */
final class VirtualAdminHandler implements AdminHandler {

    private final CVSRoot root;

    private final String repositoryPath;

    /**
     * Creates AdminHandler for a non-existing local folder that represents
     * root.getRepository() + "/" + repositoryPath repository folder.
     *
     * @param root repository
     * @param repositoryPath local path representation in repository (always <tt>/</tt> separated)
     */
    public VirtualAdminHandler(CVSRoot root, String repositoryPath) {
        this.root = root;
        this.repositoryPath = repositoryPath;
    }

    public void updateAdminData(String localDirectory, String repositoryPath, Entry entry, GlobalOptions globalOptions) throws IOException {
    }

    public boolean exists(File file) {
        return false;
    }

    public Entry getEntry(File file) throws IOException {
        return null;
    }

    public Iterator getEntries(File directory) throws IOException {
        return Collections.EMPTY_SET.iterator();
    }

    public void setEntry(File file, Entry entry) throws IOException {
    }

    public String getRepositoryForDirectory(String directory, String repository) throws IOException {
        String repo = root.getRepository();
        assert repo.equals(repository) : "Mismatch! Expected " + repo + " but got " + repository;  // NOI18N
        return repository + "/" + repositoryPath; // NOI18N
    }

    public void removeEntry(File file) throws IOException {
    }

    public Set getAllFiles(File directory) throws IOException {
        return Collections.EMPTY_SET;
    }

    public String getStickyTagForDirectory(File directory) {
        return null; // trunk
    }
}
