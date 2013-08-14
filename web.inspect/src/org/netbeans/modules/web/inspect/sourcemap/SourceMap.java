/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.sourcemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Source map.
 *
 * @author Jan Stola
 */
public class SourceMap {
    /** Source map version supported by this class. */
    private static final String SUPPORTED_VERSION = "3"; // NOI18N
    /** JSON representation of this source map. */
    private JSONObject sourceMap;
    /**
     * Mapping provided by this source map. The key is the line number
     * and the value is the list of mappings related to the line. The list
     * is ordered according to the increasing column.
     */
    private final Map<Integer,List<Mapping>> mappings = new HashMap<Integer,List<Mapping>>();

    /**
     * Creates a new {@code SourceMap}.
     * 
     * @param sourceMap {@code String} representation of the source map.
     */
    public SourceMap(String sourceMap) {
        this(toJSONObject(sourceMap));
    }

    /**
     * Creates a new {@code SourceMap}.
     * 
     * @param sourceMap JSON representation of the source map.
     */
    public SourceMap(JSONObject sourceMap) {
        this.sourceMap = sourceMap;
        String version = (String)sourceMap.get("version"); // NOI18N
        if (!SUPPORTED_VERSION.equals(version)) {
            throw new IllegalArgumentException("Unsupported version of the source map: " + version); // NOI18N
        }
        String mappingInfo = (String)sourceMap.get("mappings"); // NOI18N
        MappingTokenizer tokenizer = new MappingTokenizer(mappingInfo);
        int line = 0;
        List<Mapping> lineInfo = null;
        for (Mapping mapping : tokenizer) {
            if (mapping == Mapping.NEW_LINE) {
                if (lineInfo != null) {
                    mappings.put(line, lineInfo);
                    lineInfo = null;
                }
                line++;
            } else {
                if (lineInfo == null) {
                    lineInfo = new ArrayList<Mapping>();
                }
                lineInfo.add(mapping);
            }
        }
        if (lineInfo != null) {
            mappings.put(line, lineInfo);
        }
    }

    /**
     * Returns path of the source with the specified index. The path is relative
     * to the location of this source map.
     * 
     * @param sourceIndex source index.
     * @return path of the source with the specified index.
     */
    public String getSourcePath(int sourceIndex) {
        JSONArray sources = (JSONArray)sourceMap.get("sources"); // NOI18N
        return (String)sources.get(sourceIndex);
    }

    /**
     * Returns the mapping that corresponds to the given line and column.
     * 
     * @param line line of the location.
     * @param column column of the location.
     * @return mapping that corresponds to the given line and column
     * or {@code null} if there is no mapping known for the given position.
     */
    public Mapping findMapping(int line, int column) {
        Mapping result = null;
        List<Mapping> lineInfo = mappings.get(line);
        if (lineInfo != null) {
            for (Mapping mapping : lineInfo) {
                if (mapping.getColumn() > column) {
                    break;
                }
                result = mapping;
            }
        }
        return result;
    }

    /**
     * Parses the given {@code text} and returns the corresponding JSON object.
     * 
     * @param text text to parse.
     * @return JSON object that corresponds to the given text.
     * @throws IllegalArgumentException when the given text is not a valid
     * representation of a JSON object.
     */
    private static JSONObject toJSONObject(String text) throws IllegalArgumentException {
        try {
            JSONObject json = (JSONObject)JSONValue.parseWithException(text);
            return json;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(text);
        }
    }
    
}
