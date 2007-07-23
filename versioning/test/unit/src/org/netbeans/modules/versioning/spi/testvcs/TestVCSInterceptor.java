/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.spi.testvcs;

import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Maros Sandor
 */
public class TestVCSInterceptor extends VCSInterceptor {

    private final List<File>    beforeCreateFiles = new ArrayList<File>();
    private final List<File>    doCreateFiles = new ArrayList<File>();
    private final List<File>    createdFiles = new ArrayList<File>();
    private final List<File>    beforeDeleteFiles = new ArrayList<File>();
    private final List<File>    doDeleteFiles = new ArrayList<File>();
    private final List<File>    deletedFiles = new ArrayList<File>();
    private final List<File>    beforeMoveFiles = new ArrayList<File>();
    private final List<File>    afterMoveFiles = new ArrayList<File>();
    private final List<File>    beforeEditFiles = new ArrayList<File>();
    private final List<File>    beforeChangeFiles = new ArrayList<File>();
    private final List<File>    afterChangeFiles = new ArrayList<File>();

    public TestVCSInterceptor() {
    }

    public boolean beforeCreate(File file, boolean isDirectory) {
        beforeCreateFiles.add(file);
        return true;
    }

    public void beforeChange(File file) {
        beforeChangeFiles.add(file);
    }

    public void afterChange(File file) {
        afterChangeFiles.add(file);
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        doCreateFiles.add(file);
        if (!file.exists()) {
            if (isDirectory) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        }
    }

    public void afterCreate(File file) {
        createdFiles.add(file);
    }

    public boolean beforeDelete(File file) {
        beforeDeleteFiles.add(file);
        return true;
    }

    public void doDelete(File file) throws IOException {
        doDeleteFiles.add(file);
        if (file.getName().endsWith("do-not-delete")) return;
        file.delete();
    }

    public void afterDelete(File file) {
        deletedFiles.add(file);
    }

    public boolean beforeMove(File from, File to) {
        beforeMoveFiles.add(from);
        return true;
    }

    public void doMove(File from, File to) throws IOException {
        from.renameTo(to);
    }

    public void afterMove(File from, File to) {
        afterMoveFiles.add(from);
    }

    public void beforeEdit(File file) {
        beforeEditFiles.add(file);
    }

    public List<File> getBeforeCreateFiles() {
        return beforeCreateFiles;
    }

    public List<File> getDoCreateFiles() {
        return doCreateFiles;
    }

    public List<File> getCreatedFiles() {
        return createdFiles;
    }

    public List<File> getBeforeDeleteFiles() {
        return beforeDeleteFiles;
    }

    public List<File> getDoDeleteFiles() {
        return doDeleteFiles;
    }

    public List<File> getDeletedFiles() {
        return deletedFiles;
    }

    public List<File> getBeforeMoveFiles() {
        return beforeMoveFiles;
    }

    public List<File> getAfterMoveFiles() {
        return afterMoveFiles;
    }

    public List<File> getBeforeEditFiles() {
        return beforeEditFiles;
    }

    public List<File> getBeforeChangeFiles() {
        return beforeChangeFiles;
    }

    public List<File> getAfterChangeFiles() {
        return afterChangeFiles;
    }

    public void clearTestData() {
        beforeCreateFiles.clear();
        doCreateFiles.clear();
        createdFiles.clear();
        beforeDeleteFiles.clear();
        doDeleteFiles.clear();
        deletedFiles.clear();
        beforeMoveFiles.clear();
        afterMoveFiles.clear();
        beforeEditFiles.clear();
        beforeChangeFiles.clear();
        afterChangeFiles.clear();
    }
}
