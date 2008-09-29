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
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocPropertyTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocCommentParser {

    private static Pattern pattern = Pattern.compile("[\r\n][ \\t]*[*]?[ \\t]*");

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
                    PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.trim());
                    tags.add(tag);
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
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description.trim());
                tags.add(tag);
            }
            line = line.substring(tagType.name().length() + 1).trim();
            PHPDocTag tag = createTag(startOffset + 3 + index, startOffset + 3 + comment.length(), tagType, line);
            tags.add(tag);
        } else {
            if (lastTag == null) {  // thre is not defined a tag before the last line
                blockDescription = description + line;
            } else {
                description = description + line;
                PHPDocTag tag = createTag(startOffset + 3 + lastStartIndex, startOffset + 3 + lastEndIndex, lastTag, description);
                tags.add(tag);
            }
        }
        return new PHPDocBlock(startOffset + 3, endOffset, blockDescription, tags);
    }

    private PHPDocTag createTag(int start, int end, PHPDocTag.Type type, String description) {
        if (type == PHPDocTag.Type.PROPERTY
                || type == PHPDocTag.Type.PROPERTY_READ
                || type == PHPDocTag.Type.PROPERTY_WRITE) {
            String[] tokens = description.split("[ ]+"); //NOI18N
            if (tokens.length > 1) {
                String name = tokens[1].trim();
                if (name != null && name.length() > 0 && name.charAt(0) == '$') { //NOI18N
                    name = name.substring(1);
                }
                return new PHPDocPropertyTag(start, end, type, name, tokens[0].trim(), description);
            }
        }
        return new PHPDocTag(start, end, type, description);
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
