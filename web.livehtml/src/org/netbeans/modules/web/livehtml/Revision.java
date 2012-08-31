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

package org.netbeans.modules.web.livehtml;

import org.netbeans.modules.web.domdiff.Change;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Single document revision with stacktrace, data and list of changes.
 */
public final class Revision {
    
    private int index;
    private String timeStamp;
    
    private StringBuilder content;
    private StringBuilder reformattedContent;
    
    private List<Change> changes;
    private List<Change> reformattedChanges;
    
    final private StringBuilder previewContent;
        
    private JSONArray stacktrace;
    private StringBuilder data;
    
    public Revision(int index, String timeStamp, StringBuilder content, 
                StringBuilder reformattedContent, List<Change> changes, 
                List<Change> reformattedChanges, StringBuilder stacktrace, 
                StringBuilder data, StringBuilder previewContent) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.content = content;
        this.reformattedContent = reformattedContent;
        this.changes = changes;
        this.reformattedChanges = reformattedChanges;
        if (stacktrace != null) {
            this.stacktrace = (JSONArray) JSONValue.parse(stacktrace.toString());
        }
        this.data = data;
        this.previewContent = previewContent;
        
        addRemovedContent();
    }

    public String getContent() {
        if (content == null) {
            return null;
        }
        return content.toString();
    }

    public JSONArray getStacktrace() {
        return stacktrace;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public StringBuilder getData() {
        return data;
    }

    public int getIndex() {
        return index;
    }

    private void addRemovedContent() {
        int increment = 0;
        for (Change ch : changes) {
            ch.increment(increment);
            if (!ch.isAdd() && ch.getRevisionIndex() == -1) {
                try {
                    content.insert(ch.getOffset(), ch.getRemovedText());
                } catch (java.lang.StringIndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
                increment += ch.getRemovedText().length();
            }
        }
    }
    
    public String getTimeStamp() {
        return timeStamp;
    }
    
    public List<Object> getStackTraceValues(String valueName) {
        List<Object> objects = new ArrayList<Object>();
        if (valueName == null || getStacktrace() == null) {
            return objects;
        }
        for (Object object : getStacktrace()) {
            if (object instanceof JSONObject) {
                JSONObject jSONObject = (JSONObject) object;
                final Object value = jSONObject.get(valueName);
                if (value != null) {
                    objects.add(value);
                }
            }
        }
        return objects;
    }

    public String getReformattedContent() {
        return reformattedContent == null ? null : reformattedContent.toString();
    }

    public List<Change> getReformattedChanges() {
        return reformattedChanges;
    }
    
    public boolean hasEmptyChanges() {
        if (getChanges() == null || getChanges().isEmpty()) {
            return true;
        }
        for (Change change : changes) {
            if (!change.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public StringBuilder getPreviewContent() {
        return previewContent;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.timeStamp != null ? this.timeStamp.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Revision other = (Revision) obj;
        if ((this.timeStamp == null) ? (other.timeStamp != null) : !this.timeStamp.equals(other.timeStamp)) {
            return false;
        }
        return true;
    }

}
