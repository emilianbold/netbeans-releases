/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.html.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Implementation of {@link Description} for html parse tree elements.
 * 
 * @author marekfukala
 */
public class HtmlElementDescription extends SourceDescription {

    private final String elementPath;
    private final Map<String, String> attributes;
    private final int from, to;
    private final ElementType type;
    private final FileObject file;
    private List<HtmlElementDescription> children;
    private final boolean isLeaf;
    private final String name;

    private HtmlElementDescription parent;
    
    public HtmlElementDescription(HtmlElementDescription parent, Element element, FileObject file) {
        this.parent = parent;
        this.file = file;
        this.type = element.type();
        this.from = element.from();
        
        this.elementPath = ElementUtils.encodeToString(new TreePath(element));
        
        //acceptable, not 100% correct - may say it is not leaf, but then there 
        //won't be children if all children are virtual with no non-virtual ancestors
        this.isLeaf = element instanceof Node ? ((Node)element).children(OpenTag.class).isEmpty() : true;
        
        OpenTag openTag = element instanceof OpenTag ? (OpenTag)element : null;
        if(openTag != null) {
            //init attributes map
            Collection<Attribute> attrs = openTag.attributes();
            if(attrs.isEmpty()) {
                attributes = Collections.emptyMap();
            } else {
                attributes = new HashMap<String, String>();
                for(Attribute a : attrs) {
                    String key = a.name().toString().toLowerCase();
                    String val = a.unquotedValue() != null ? a.unquotedValue().toString() : null;
                    attributes.put(key, val);
                }
            }
            
        } else {
            attributes = Collections.emptyMap();
        }
        
        this.to = openTag != null ? openTag.semanticEnd() : element.to();
        this.name = openTag != null ? openTag.name().toString() : null;
        
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof HtmlElementDescription)) {
            return false;
        }
        HtmlElementDescription descr = (HtmlElementDescription)obj;
        return Diff.equals(this, descr, false);
    }

    @Override
    public int hashCode() {
        return Diff.hashCode(this, false);
    }

    @Override
    protected String getElementPath() {
        return elementPath;
    }

    @Override
    public Description getParent() {
        return parent;
    }
    
    public FileObject getFileObject() {
        return file;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public boolean isLeaf() {
        return isLeaf;
    }
    
    public ElementType getElementType() {
        return type;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public int getTo() {
        return to;
    }

    @Override
    protected Map<String, String> getAttributes() {
        return attributes;
    }

    public Node resolve(ParserResult result) {
        if(!(result instanceof HtmlParserResult)) {
            return null;
        }
        
        HtmlParserResult htmlParserResult = (HtmlParserResult)result;
        Node root = htmlParserResult.root();
        
        if(getElementType() == ElementType.ROOT) {
            return root;
        } else {
            return ElementUtils.query(root, elementPath);
        }
    }
        
    public OffsetRange getOffsetRange(ParserResult result) {
        Node node = resolve(result);
        if(node == null) {
            return OffsetRange.NONE;
        }
        
        Snapshot snapshot = result.getSnapshot();
        int dfrom = snapshot.getOriginalOffset(node.from());
        int dto = snapshot.getOriginalOffset(
                    node instanceof OpenTag 
                    ? ((OpenTag)node).semanticEnd() 
                    : node.to());
        
        return dfrom != -1 && dto != -1 ? new OffsetRange(dfrom, dto) : OffsetRange.NONE;
    }
    
     public void runTask(final Task task) throws ParseException {
        Source source = Source.create(file);
        if (source == null) {
            //file deleted
            return;
        }
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                task.run((HtmlParserResult) resultIterator.getParserResult());
            }
        });

    }

    @Override
    public synchronized List<HtmlElementDescription> getChildren() {
        if (children == null) {
            //lazy load the nested items
            //we need a parser result to be able to find Element for the ElementHandle
            try {
                runTask(new Task() {
                    @Override
                    public void run(HtmlParserResult result) {
                        Node node = resolve(result);
                        if (node != null) {
                            children = new ArrayList<HtmlElementDescription>();
                            List<OpenTag> nonVirtualChildren = gatherNonVirtualChildren(node);
                            for (OpenTag child : nonVirtualChildren) {
                                children.add(new HtmlElementDescription(HtmlElementDescription.this, child, file));
                            }
                        } else {
                            //cannot resolve the node
                            children = Collections.emptyList();
                        }
                    }
                });

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return children;
    }
    
     private List<OpenTag> gatherNonVirtualChildren(Node element) {
        List<OpenTag> collected = new LinkedList<OpenTag>();
        for (OpenTag child : element.children(OpenTag.class)) {
            if (child.type() == ElementType.OPEN_TAG) {
                if (!ElementUtils.isVirtualNode(child)) {
                    collected.add(child);
                } else {
                    collected.addAll(gatherNonVirtualChildren(child));
                }
            }
        }
        return collected;
    }
    
    public static interface Task {

        public void run(HtmlParserResult result);
        
    }
    
}
