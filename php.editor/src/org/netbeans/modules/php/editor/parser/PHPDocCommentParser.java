/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocCommentParser {

    private static Pattern pattern = Pattern.compile("[\r\n][ \\t]*[*]?[ \\t]*");
    private static final List<PHPDocTag.Type> PHPDocTypeTags = new ArrayList<PHPDocTag.Type>();
    static {
        PHPDocTypeTags.add(PHPDocTag.Type.RETURN);
        PHPDocTypeTags.add(PHPDocTag.Type.THROWS);
        PHPDocTypeTags.add(PHPDocTag.Type.VAR);
        PHPDocTypeTags.add(PHPDocTag.Type.SEE);
    }

    private static final List<PHPDocTag.Type> PHPDocVarTypeTags = new ArrayList<PHPDocTag.Type>();
    static {
        PHPDocVarTypeTags.add(PHPDocTag.Type.PARAM);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY_READ);
        PHPDocVarTypeTags.add(PHPDocTag.Type.PROPERTY_WRITE);
    }

    public PHPDocCommentParser() {
    }

    /**
     * 
     * @param startOffset thi is offset of the comment in the document. It's used 
     * for creating ASTNodes.
     * @param comment
     * @return
     */
    public PHPDocBlock parse(final int startOffset, final int endOffset, final String comment) {
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
        PHPDocTag.Type lastTag = null;
        int lastStartIndex = 0;
        int lastEndIndex = comment.length();

        while (matcher.find()) {
            line = comment.substring(index, matcher.start()).trim();
            if (index == 0) { // remove * from the first line
                line = removeStarAndTrim(line);
            }
            PHPDocTag.Type tagType = findTagOnLine(line);
            if (tagType != null) { // is a tag defined on the line
                if (lastTag == null) { // is it the first tag in the block
                    blockDescription = description.trim();  // save the block description
                } else { // create last recognized tag
                    PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.trim(), comment, startOffset + 3);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
                lastTag = tagType;  // remember the recognized tag
                lastStartIndex = index;
                description = "";
                line = line.substring(tagType.name().length() + 1).trim(); // and the first line of description of the tag
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
        PHPDocTag.Type tagType = findTagOnLine(line);
        if (tagType != null) {  // is defined a tag on the last line
            if (lastTag == null) {
                blockDescription = description.trim();  
            } else {
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.trim(), comment, startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            line = line.substring(tagType.name().length() + 1).trim();
            PHPDocTag tag = createTag(startOffset + 3 + index, startOffset + 3 + comment.length(), tagType, line, comment, startOffset + 3);
            if (tag != null) {
                tags.add(tag);
            }
        } else {
            if (lastTag == null) {  // thre is not defined a tag before the last line
                blockDescription = description + line;
            } else {
                description = description + line;
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description, comment, startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return new PHPDocBlock(startOffset + 3, endOffset, blockDescription, tags);
    }

    private PHPDocTag createTag(int start, int end, PHPDocTag.Type type, String description, String originalComment, int originalCommentStart) {
        List<PHPDocNode> docTypes = new ArrayList<PHPDocNode>();
        if (PHPDocTypeTags.contains(type) || PHPDocVarTypeTags.contains(type)) {
            for (String stype : getTypes(description)) {
                stype = removeHTMLTags(stype);
                int startDocNode = findStartOfDocNode(originalComment, originalCommentStart, stype, start);
                int index = stype.indexOf("::");    //NOI18N
                PHPDocNode docType;
                if (index == -1) {
                    docType = new PHPDocNode(startDocNode, startDocNode + stype.length(), stype);
                }
                else {
                    String className = stype.substring(0, index);
                    String constantName = stype.substring(index+2,stype.length());
                    PHPDocNode classNameNode = new PHPDocNode(startDocNode, startDocNode + className.length(), className);
                    PHPDocNode constantNode = new PHPDocNode(startDocNode + className.length()+2, startDocNode + stype.length(), constantName);
                    docType = new PHPDocStaticAccessType(startDocNode, startDocNode + stype.length(), stype, classNameNode, constantNode);
                }
                docTypes.add(docType);
            }
            if (PHPDocVarTypeTags.contains(type)) {
                String variable = getVaribleName(description);
                if (variable != null) {
                    int startOfVariable = findStartOfDocNode(originalComment, originalCommentStart, variable, start);
                    PHPDocNode varibaleNode = new PHPDocNode(startOfVariable, startOfVariable + variable.length(), variable);
                    return new PHPDocVarTypeTag(start, end, type, description, docTypes, varibaleNode);
                }
                return null;
            }
            return new PHPDocTypeTag(start, end, type, description, docTypes);
        }
        return new PHPDocTag(start, end, type, description);
    }

    private List<String> getTypes(String description) {
        String[] tokens = description.split("[ ]+"); //NOI18N
        ArrayList<String> types = new ArrayList<String>();
        if (tokens.length > 0) {
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
        String[] tokens = description.split("[ ]+"); //NOI18N
        String variable = null;

        if (tokens.length > 0 && tokens[0].length() > 0 && tokens[0].charAt(0) == '$'){
            variable = tokens[0].trim();
        } else if ((tokens.length > 1) && (tokens[1].charAt(0) == '$')) {
            variable = tokens[1].trim();
        }
        return variable;
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

    private PHPDocTag.Type findTagOnLine(String line) {
        PHPDocTag.Type type = null;
        if (line.length() > 0 && line.charAt(0) == '@') {
            String[] tokens = line.split("[ ]+");
            if (tokens.length > 0) {
                String tag = tokens[0].substring(1).toUpperCase();
                if (tag.indexOf('-') > -1) {
                    tag = tag.replace('-', '_');
                }
                try {
                    type = PHPDocTag.Type.valueOf(tag);
                }
                catch (IllegalArgumentException iae) {
                    // we are not able to thread such tag
                    type = null;
                }
            }
        }
        return type;
    }
}
