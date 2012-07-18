/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocCommentParser {

    private static Pattern pattern = Pattern.compile("[\r\n][ \\t]*[*]?[ \\t]*");

    /**
     * Tags that define a type / types
     */
    private static final List<PhpAnnotationType> PHPDocTypeTags = new ArrayList<PhpAnnotationType>();
    static {
        PHPDocTypeTags.add(PHPDocTag.Type.RETURN);
        PHPDocTypeTags.add(PHPDocTag.Type.VAR);
    }

    /**
     * Tags that define something of a type
     */
    private static final List<PhpAnnotationType> PHPDocVarTypeTags = new ArrayList<PhpAnnotationType>();
    static {
        PHPDocVarTypeTags.add(PHPDocTag.Type.PARAM);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY);
        PHPDocVarTypeTags.add(PHPDocTag.Type.GLOBAL);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY_READ);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY_WRITE);
    }

    public PHPDocCommentParser() {
    }

    /**
     *
     * @param startOffset this is offset of the comment in the document. It's used
     * for creating ASTNodes.
     * @param comment
     * @return
     */
    public PHPDocBlock parse(final int startOffset, final int endOffset, final String comment) {
        assert startOffset <= endOffset;
        List<PHPDocTag> tags = new ArrayList<PHPDocTag>(); // list of tags
        String blockDescription = "";   // description of the block
        if (comment == null || comment.length() == 0) {
            // issue #142644
            return new PHPDocBlock(startOffset, endOffset, blockDescription, tags);
        }

        Matcher matcher = pattern.matcher(comment);
        int index = 0;
        String line = "";               // one line of the blog
        String description = "";        // temporary holder for description of block description or tag
        PhpAnnotationType lastTag = null;
        int lastStartIndex = 0;
        int lastEndIndex = comment.length();

        while (matcher.find()) {
            line = comment.substring(index, matcher.start()).trim();
            if (index == 0) { // remove * from the first line
                line = removeStarAndTrim(line);
            }
            PhpAnnotationType tagType = findTagOnLine(line);
            if (tagType != null) { // is a tag defined on the line
                if (lastTag == null) { // is it the first tag in the block
                    blockDescription = description.length() > 0 && description.charAt(description.length() - 1) == '\n' ? description.substring(0, description.length() -1) : description;  // save the block description
                } else { // create last recognized tag
                    PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.substring(0, description.length() -1), comment, startOffset + 3);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
                lastTag = tagType;  // remember the recognized tag
                lastStartIndex = index;
                description = "";
                line = line.substring(tagType.getName().length() + 1); // and the first line of description of the tag
            }
            index = matcher.end();
            lastEndIndex = matcher.start();
            description = description + line + "\n";
        }
        // last line
        if (index == 0) {  // there is only one line comment
            line = removeStarAndTrim(comment);
        } else {
            line = comment.substring(index, comment.length()).trim();
        }
        PhpAnnotationType tagType = findTagOnLine(line);
        if (tagType != null) {  // is defined a tag on the last line
            if (lastTag == null) {
                blockDescription = description.trim();
            } else {
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.substring(0, description.length() -1), comment, startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            line = line.substring(tagType.getName().length() + 1).trim();
            PHPDocTag tag = createTag(startOffset + 3 + index, startOffset + 3 + comment.length(), tagType, line, comment, startOffset + 3);
            if (tag != null) {
                tags.add(tag);
            }
        } else {
            if (lastTag == null) {  // thre is not defined a tag before the last line
                blockDescription = description + line;
            } else {
                description = description + line;
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.substring(0, description.length() -1), comment, startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return new PHPDocBlock(Math.min(startOffset + 3, endOffset), endOffset, blockDescription, tags);
    }

    private PHPDocTag createTag(int start, int end, PhpAnnotationType type, String description, String originalComment, int originalCommentStart) {
        if (type.equals(PHPDocTag.Type.METHOD) || PHPDocTypeTags.contains(type) || PHPDocVarTypeTags.contains(type) || type instanceof CustomAnnotationType) {
            List<PHPDocTypeNode> docTypes = findTypes(description, start, originalComment, originalCommentStart);
            if (PHPDocVarTypeTags.contains(type)) {
                String variable = getVaribleName(description);
                PHPDocNode varibaleNode = null;
                if (variable != null) {
                    int startOfVariable = findStartOfDocNode(originalComment, originalCommentStart, variable, start);
                    varibaleNode = new PHPDocNode(startOfVariable, startOfVariable + variable.length(), variable);
                } else if (type.equals(PHPDocTag.Type.PARAM)) {
                    varibaleNode = new PHPDocNode(start, start, ""); //NOI18N
                }
                if (varibaleNode != null) {
                    return new PHPDocVarTypeTag(start, end, type, description, docTypes, varibaleNode);
                }
                return null;
            } else if (type.equals(PHPDocTag.Type.METHOD)) {
                String name = getMethodName(description);
                if (name != null) {
                    int startOfVariable = findStartOfDocNode(originalComment, originalCommentStart, name, start);
                    PHPDocNode methodNode = new PHPDocNode(startOfVariable, startOfVariable + name.length(), name);
                    List<PHPDocVarTypeTag> params = findMethodParams(description, findStartOfDocNode(originalComment, originalCommentStart, description, start));
                    return new PHPDocMethodTag(start, end, type, docTypes, methodNode, params, description);
                }
                return null;
            }
            return new PHPDocTypeTag(start, end, type, description, docTypes);
        }
        return new PHPDocTag(start, end, type, description);
    }

    private List<PHPDocTypeNode> findTypes(String description, int startDescription, String originalComment, int originalCommentStart) {
        List<PHPDocTypeNode> result = new ArrayList<PHPDocTypeNode>();

        for (String stype : getTypes(description)) {
            stype = removeHTMLTags(stype);
            int startDocNode = findStartOfDocNode(originalComment, originalCommentStart, stype, startDescription);
            int index = stype.indexOf("::");    //NOI18N
            boolean isArray = (stype.indexOf('[') > 0 && stype.indexOf(']') > 0);
            if (isArray) {
                stype = stype.substring(0, stype.indexOf('[')).trim();
            }
            PHPDocTypeNode docType;
            if (index == -1) {
                docType = new PHPDocTypeNode(startDocNode, startDocNode + stype.length(), stype, isArray);
            } else {
                String className = stype.substring(0, index);
                String constantName = stype.substring(index + 2, stype.length());
                PHPDocNode classNameNode = new PHPDocNode(startDocNode, startDocNode + className.length(), className);
                PHPDocNode constantNode = new PHPDocNode(startDocNode + className.length() + 2, startDocNode + stype.length(), constantName);
                docType = new PHPDocStaticAccessType(startDocNode, startDocNode + stype.length(), stype, classNameNode, constantNode);
            }
            result.add(docType);
        }
        return result;
    }

    private List<String> getTypes(String description) {
        String[] tokens = description.trim().split("[ ]+"); //NOI18N
        ArrayList<String> types = new ArrayList<String>();
        if (tokens.length > 0 && !tokens[0].startsWith("$")) { //NOI18N
            if (tokens[0].indexOf('|') > -1) {
                String[] ttokens = tokens[0].split("[|]"); //NOI18N
                for (String ttoken : ttokens) {
                    types.add(ttoken.trim());
                }
            } else {
                types.add(tokens[0].trim());
            }
        }

        return types;
    }

    private String getVaribleName(String description) {
        String[] tokens = description.trim().split("[ \n\t]+"); //NOI18N
        String variable = null;

        if (tokens.length > 0 && tokens[0].length() > 0 && tokens[0].charAt(0) == '$'){
            variable = tokens[0].trim();
        } else if ((tokens.length > 1) && (tokens[1].charAt(0) == '$')) {
            variable = tokens[1].trim();
        }
        return variable;
    }

    private String getMethodName(String description) {
        String name = null;
        int index = description.indexOf('(');
        if (index > 0 ) {
            name = description.substring(0, index);
            index = name.lastIndexOf(' ');
            if (index > 0) {
                name = name.substring(index + 1);
            }
        } else {
            // probably defined without () after the name
            // then we expect that the name is after the first space
            String[] tokens = description.trim().split("[ \n\t]+"); //NOI18N
            if (tokens.length > 1){
                name = tokens[1];
            }
        }
        return name;
    }

    private List<PHPDocVarTypeTag> findMethodParams(String description, int startOfDescription) {
        List<PHPDocVarTypeTag> result = new ArrayList();
        int position = startOfDescription;
        ParametersExtractor parametersExtractor = ParametersExtractorImpl.create();
        String parameters = parametersExtractor.extract(description);
        position += parametersExtractor.getPosition();
        if (parameters.length() > 0) {
            String[] tokens = parameters.split("[,]+"); //NOI18N
            String paramName;
            for(String token : tokens) {
                paramName = getVaribleName(token.trim());
                if (paramName != null) {
                    int startOfParamName = findStartOfDocNode(description, startOfDescription, paramName, position);
                    PHPDocNode paramNameNode = new PHPDocNode(startOfParamName, startOfParamName + paramName.length(), paramName);
                    List<PHPDocTypeNode> types = token.trim().indexOf(' ') > -1
                            ? findTypes(token, position, description, startOfDescription)
                            : Collections.EMPTY_LIST;
                    result.add(new PHPDocVarTypeTag(position, startOfParamName + paramName.length(), PHPDocTag.Type.PARAM, token, types, paramNameNode));
                }
                position = position + token.length() + 1;
            }
        }
        return result;
    }

    private String removeHTMLTags(String text) {
        String value = text;
        int index = value.indexOf('>');
        if (index > -1) {
            value = value.substring(index + 1);
            index = value.indexOf('<');
            if (index > -1) {
                value = value.substring(0, index);
            }
        }
        return value;
    }

    private int findStartOfDocNode(String originalComment, int originalStart, String what, int from) {
        int pos = originalComment.indexOf(what, from - originalStart);
        return originalStart + pos;
    }

    private String removeStarAndTrim(String text) {
        text = text.trim();
        if (text.length() > 0 && text.charAt(0) == '*') {
            text = text.substring(1).trim();
        }
        return text;
    }

    private PhpAnnotationType findTagOnLine(String line) {
        PhpAnnotationType type = null;
        if (line.length() > 0 && line.charAt(0) == '@') {
            String[] tokens = line.trim().split("[ \t]+");
            if (tokens.length > 0) {
                final String name = tokens[0].substring(1);
                String tag = name.toUpperCase();
                if (tag.indexOf('-') > -1) {
                    tag = tag.replace('-', '_');
                }
                try {
                    type = PHPDocTag.Type.valueOf(tag);
                } catch (IllegalArgumentException iae) {
                    // we are not able to thread such tag
                    type = new CustomAnnotationType(name);
                }
            }
        }
        return type;
    }

    private static class ParametersExtractorImpl implements ParametersExtractor {

        private int position = 0;
        private String parameters = "";
        private String subDescription = "";
        private boolean hasParameters = false;
        private int bracketBalance = 0;
        private int paramsStart = 0;
        private int paramsEnd = 0;

        public static ParametersExtractor create() {
            return new ParametersExtractorImpl();
        }

        private ParametersExtractorImpl() {
        }

        @Override
        public String extract(String description) {
            int index = description.indexOf('(');
            int possibleParamIndex = description.indexOf('$');
            if (index > -1 && possibleParamIndex > -1) {
                position += index;
                subDescription = description.substring(index);
                processSubDescription();
            }
            return parameters;
        }

        private void processSubDescription() {
            for (int i = 0; i < subDescription.length(); i++) {
                findMatchingBraces(i);
                if (!parameters.isEmpty()) {
                    break;
                }
            }
        }

        private void findMatchingBraces(int i) {
            char ch = subDescription.charAt(i);
            if (ch == '(') {
                processLeftBrace(i);
            } else if (ch == ')') {
                processRightBrace(i);
            } else if (Character.isWhitespace(ch)) {
                return;
            } else {
                processNonWhiteCharacter();
            }
            checkParameters();
        }

        private void processLeftBrace(int i) {
            bracketBalance++;
            if (bracketBalance == 1) {
                paramsStart = i + 1;
            }
        }

        private void processRightBrace(int i) {
            bracketBalance--;
            if (bracketBalance == 0) {
                paramsEnd = i;
            }
        }

        private void processNonWhiteCharacter() {
            if (bracketBalance == 0) {
                hasParameters = false;
            } else {
                hasParameters = true;
            }
        }

        private void checkParameters() {
            if (hasParameters && bracketBalance == 0) {
                parameters = subDescription.substring(paramsStart, paramsEnd);
                position += paramsStart;
            }
        }

        @Override
        public int getPosition() {
            return position;
        }

    }

    private interface ParametersExtractor {

        /**
         * Extracts part of parameters from magic method tag description.
         *
         * @param description Line of magic method tag description.
         * @return Extracted parameters part.
         */
        public String extract(String description);

        /**
         * Returns start position of parameters part from magic method tag description line.
         *
         * @return Start position of parameters part.
         */
        public int getPosition();

    }

}
