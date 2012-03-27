/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks;

import java.net.URL;

/**
 * Description of a bookmark that does not have a corresponding document
 * constructed yet (or is a snapshot of a document bookmark).
 *
 * @author Miloslav Metelka
 */
public final class BookmarkInfo {
    
    public static BookmarkInfo create(int id, String name, int lineIndex, String key) {
        return create(id, name, lineIndex, key, null);
    }

    /**
     * Create new bookmark info.
     * @param name name of bookmark consisting of characters satisfying
     *   {@link Character#isJavaIdentifierPart(char) }.
     * @param lineIndex zero-based index of line on which the bookmark will be placed.
     * @param key Current implementation returns a single char [0-9a-z] used for jumping
     * to the bookmark by a keystroke in a Goto dialog or an empty string
     * when no shortcut was assigned yet.
     * @param url explicit url to which this bookmark belongs; but usually it's null
     * which means to take the URL from URLBookmarks object which contains this info.
     */
    public static BookmarkInfo create(int id, String name, int lineIndex, String key, URL url) {
        return new BookmarkInfo(id, name, lineIndex, key, url);
    }

    public static BookmarkInfo create(BookmarkInfo info, URL url) {
        return create(info, url, -1);
    }
    
    /**
     * Create new bookmark info by getting information from an original bookmark info
     * and setting a fresh line index value.
     *
     * @param info non-null existing info
     * @param url url or null to retain info's url.
     * @param lineIndex line index to be set or -1 to retain info's lineIndex.
     */
    public static BookmarkInfo create(BookmarkInfo info, URL url, int lineIndex) {
        if (url == null) {
            url = info.getURL();
        }
        if (lineIndex == -1) {
            lineIndex = info.getLineIndex();
        }
        return new BookmarkInfo(info.getId(), info.getName(), lineIndex, info.getKey(), url);
    }
    
    private final int id;
    
    private String name;

    private int lineIndex;

    private String key;

    private URL url;
    
    private BookmarkInfo(int id, String name, int lineIndex, String key, URL url) {
        this.id = id;
        this.name = name;
        this.lineIndex = lineIndex;
        this.key = key;
        this.url = url;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new IllegalArgumentException("name=\"" + name + "\": char at [" + i + "] not allowed"); // NOI18N
            }
        }
        this.name = name;
    }

    /**
     * Get line index of this info.
     * <br/>
     * Note that line index information may be obsolete in case a corresponding Bookmark instance exists
     * for this info.
     *
     * @return zero-based line index.
     */
    public int getLineIndex() {
        return lineIndex;
    }
    
    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        if (key.length() > 0) {
            this.key = key.substring(0, 1).toUpperCase();
        } else {
            this.key = "";
        }
    }

    /**
     * Get URL for this bookmark or null if it was not filled in (parent URLBookmarks define it).
     *
     * @return URL or null.
     */
    public URL getURL() {
        return url;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=\"" + name + "\", key='" + key + // NOI18N
                "' at line=" + lineIndex + ", url=" + url; // NOI18N
    }

}
