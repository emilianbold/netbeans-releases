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

package org.netbeans.modules.web.domdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Single change in DOM document translated into offset, length of change and
 * whether it was ADDITION, REMOVAL, or whether it is a change from previous revision.
 */
public class Change {
    
    private boolean added;
    private int offset;
    private int originalOffset;
    private int length;
    private String removedText;
    private String addedText; // this is here only for diagnostic purposes
    private int revisionIndex;

    // TODO: refactor this; it got too complicated over the time and single class is handling tree
    //   different cases which results into ugly conditional programming
    private Change(boolean added, int offset, int length, String removedText, String addedText, int originalOffset, int revisionIndex) {
        this.added = added;
        this.offset = offset;
        this.length = length;
        this.removedText = removedText;
        this.addedText = addedText;
        this.originalOffset = originalOffset;
        this.revisionIndex = revisionIndex;
    }

    public static Change added(int startOffset, int endOffset, String addedText) {
        return new Change(true, startOffset, endOffset - startOffset, null, addedText, -1, -1);
    }

    public static Change removed(int offset, int originalOffset, String text) {
        return new Change(false, offset, -1, text, null, originalOffset, -1);
    }

    public static Change origin(int startOffset, int endOffset, int revisionIndex) {
        return new Change(false, startOffset, endOffset - startOffset, null, null, -1, revisionIndex);
    }

    /**
     * This change represents ADDITION of a new text into document
     */
    public boolean isAdd() {
        return added;
    }

    /**
     * This change represents REMOVAL of a text from document
     */
    public boolean isRemove() {
        return !added && revisionIndex == -1;
    }

    /**
     * This change represents some change in previous revisions
     */
    public boolean isOrigin() {
        return revisionIndex != -1;
    }
    
    public boolean isEmpty() {
        String trimmedAddedText = addedText == null ? "" : addedText.trim();
        String trimmedRemovedText = removedText == null ? "" : removedText.trim();
        return !added && trimmedAddedText.isEmpty() && trimmedRemovedText.isEmpty() && originalOffset == -1;
    }

    public int getRevisionIndex() {
        return revisionIndex;
    }

    public int getOffset() {
        return offset;
    }

    public int getOriginalOffset() {
        return originalOffset;
    }

    public int getLength() {
        return length;
    }

    public int getLengthOfNewText() {
        assert added;
        return length;
    }

    public int getEndOffsetOfNewText() {
        assert added;
        if (length == -1) {
            return -1;
        }
        return offset + length;
    }

    public String getRemovedText() {
        assert !added;
        return removedText;
    }

    public int getEndOffsetOfRemovedText() {
        assert !added;
        return offset + removedText.length();
    }

    public String getAddedText() {
        return addedText;
    }

    public void increment(int inc) {
        offset += inc;
    }
    
    public void incrementLength(int inc) {
        length += inc;
    }

    public static String encodeToJSON(List<Change> changes) {
        JSONArray a = new JSONArray();
        for (Change c : changes) {
            JSONObject j = new JSONObject();
            j.put("offset", c.offset);
            j.put("offsetForInsertion", c.originalOffset);
            j.put("length", c.length);
            j.put("removed", c.removedText);
            j.put("added", c.added);
            j.put("addedText", c.addedText);
            j.put("origin", c.revisionIndex);
            a.add(j);
        }
        return a.toJSONString();
    }
    
    public static List<Change> decodeFromJSON(String s) {
        if (s == null) {
            return Collections.emptyList();
        }
        List<Change> res = new ArrayList<Change>();
        JSONArray a = (JSONArray)JSONValue.parse(s);
        for (Object o : a) {
            JSONObject j = (JSONObject)o;
            Number offset = (Number)j.get("offset");
            assert offset != null : j.toJSONString();
            Number offsetForInsertion = (Number)j.get("offsetForInsertion");
            assert offsetForInsertion != null : j.toJSONString();
            Boolean added = (Boolean)j.get("added");
            assert added != null : j.toJSONString();
            String removed = (String)j.get("removed");
            String addedText = (String)j.get("addedText");
            Number len = (Number)j.get("length");
            Number origin = (Number)j.get("origin");
            Change c = new Change(added.booleanValue(), offset.intValue(), 
                    len != null ? len.intValue() : -1, removed, addedText, offsetForInsertion.intValue(),
                    origin != null ? origin.intValue() : -1);
            res.add(c);
        }
        return res;
    }

//    @Override
//    public String toString() {
//        if (added) {
//            return "ChangeADD{offset=" + offset + ", len=" + length + ", '" + addedText + "'}";
//        } else if (revisionIndex != -1) {
//            return "ChangeORG{offset=" + offset + ", len=" + length + "}";
//        } else {
//            return "ChangeRMV{offset=" + offset + ", originalOffset="+originalOffset +", len=" + removedText.length() + ", '" + removedText + "'}";
//        }
//    }

    @Override
    public String toString() {
        return "Change{" + "added=" + added + ", offset=" + offset + ", originalOffset=" + originalOffset + ", length=" + length + ", removedText=" + removedText + ", addedText=" + addedText + ", revisionIndex=" + revisionIndex + '}';
    }
    
}
