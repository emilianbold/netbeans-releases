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

package org.netbeans.modules.xslt.core.text.completion.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xslt.core.text.completion.IllegalXsltVersionException;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionResultItem;
import org.netbeans.modules.xslt.core.text.completion.XSLTEditorComponentHolder;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionConstants;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionUtil;
import org.netbeans.modules.xslt.model.InvalidAttributeValueException;

/**
 * @author Alex Petrov (06.06.2008)
 */
public class HandlerAttributeEnumValues extends BaseCompletionHandler implements  
    XSLTCompletionConstants {
    private static Map<String, AttributeValuesHolder>
        // key - String:XSLT_version, value - AttributeValuesHolder:Holder_of_attribute_values
        mapAttributeValuesHolders = new HashMap<String, AttributeValuesHolder>(3);
    
    static {
        mapAttributeValuesHolders.put(XSLT_VERSION_1_0, new AttributeValueHolderImpl_1_0());
        mapAttributeValuesHolders.put(XSLT_VERSION_1_1, new AttributeValueHolderImpl_1_1());
        mapAttributeValuesHolders.put(XSLT_VERSION_2_0, new AttributeValueHolderImpl_2_0());
    }
    
    @Override
    public List<XSLTCompletionResultItem> getResultItemList(
        XSLTEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        // if (schemaModel == null) return Collections.emptyList();
        return getAttributeEnumValueList();
    }

    private List<XSLTCompletionResultItem> getAttributeEnumValueList() {
        if ((surroundTag == null) || (attributeName == null) || (xslModel == null)) 
            return Collections.emptyList();
        try {
            String xsltVersion = xslModel.getStylesheet().getVersion().toString().trim();
            if (! setSupportedXsltVersions.contains(xsltVersion)) {
                throw new IllegalXsltVersionException(xsltVersion);
            }
            AttributeValuesHolder attributeValuesHolder = mapAttributeValuesHolders.get(
                xsltVersion);
            if (attributeValuesHolder != null) {
                String tagName = XSLTCompletionUtil.ignoreNamespace(surroundTag.getTagName());
                List<String> attribueValues = attributeValuesHolder.getAttributeValues(
                    tagName, attributeName);
                
                List<XSLTCompletionResultItem> resultItemList = 
                    new ArrayList<XSLTCompletionResultItem>();
                for (String attributeValue : attribueValues) {
                    resultItemList.add(new XSLTCompletionResultItem(attributeValue, 
                        document, caretOffset));
                }
                return resultItemList;
            }
        } catch(IllegalXsltVersionException ixve) {
            Logger.getLogger(HandlerAttributeEnumValues.class.getName()).log(
                Level.WARNING, ixve.getMessage(), ixve);
            return Collections.emptyList();
        } catch(InvalidAttributeValueException iave) {
            Logger.getLogger(HandlerAttributeEnumValues.class.getName()).log(
                Level.WARNING, iave.getMessage(), iave);
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}

interface AttributeValuesHolder {
    String[] STRING_ARRAY_YES_OR_NO = {"yes", "no"};

    List<String> getAttributeValues(String tagName, String attributeName);
}

abstract class BaseAttributeValueHolderImpl implements AttributeValuesHolder,
    XSLTCompletionConstants {
    protected Set<TagAttributeValues> setTagAttributeValues = 
        new TreeSet<TagAttributeValues>();
    protected String xsltVersion = XSLT_VERSION_1_0;

    public List<String> getAttributeValues(String tagName, String attributeName) {
        Iterator<TagAttributeValues> iterator = setTagAttributeValues.iterator();
        while (iterator.hasNext()) {
            TagAttributeValues tagAttributeValues = iterator.next();
            if ((tagAttributeValues == null) || (! tagAttributeValues.equals(tagName)))
                continue;
            
            return tagAttributeValues.getAttributeValues(attributeName);
        }
        return (Collections.emptyList());
    }
}

class AttributeValueHolderImpl_1_0 extends BaseAttributeValueHolderImpl {
    {
        setTagAttributeValues.add(new TagAttributeValues("output", 
            new AttributeValues[] {
                new AttributeValues("method", new String[] {"xml", "html", "text"}),
                new AttributeValues("omit-xml-declaration", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("standalone", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("indent", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("message", 
            new AttributeValues[] {
                new AttributeValues("terminate", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("text", 
            new AttributeValues[] {
                new AttributeValues("disable-output-escaping", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("value-of", 
            new AttributeValues[] {
                new AttributeValues("disable-output-escaping", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("number", 
            new AttributeValues[] {
                new AttributeValues("level", new String[] {"single", "multiple", "any"}),
                new AttributeValues("letter-value", new String[] {"alphabetic", "traditional"})
            })
        );
    }
}

class AttributeValueHolderImpl_1_1 extends AttributeValueHolderImpl_1_0 {
    public AttributeValueHolderImpl_1_1() {xsltVersion = XSLT_VERSION_1_1;}
}

class AttributeValueHolderImpl_2_0 extends BaseAttributeValueHolderImpl {
    public AttributeValueHolderImpl_2_0() {
        xsltVersion = XSLT_VERSION_2_0;
    
        setTagAttributeValues.add(new TagAttributeValues("stylesheet", 
            new AttributeValues[] {
                new AttributeValues("default-validation", new String[] {"preserve", "strip"}),
                new AttributeValues("input-type-annotations", new String[] {"preserve", "strip", "unspecified"}),
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("transform", 
            new AttributeValues[] {
                new AttributeValues("default-validation", new String[] {"preserve", "strip"}),
                new AttributeValues("input-type-annotations", new String[] {"preserve", "strip", "unspecified"}),
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("output", 
            new AttributeValues[] {
                new AttributeValues("byte-order-mark", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("method", new String[] {"xml", "xhtml", "html", "text"}),
                new AttributeValues("omit-xml-declaration", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("standalone", new String[] {"yes", "no", "omit"}),
                new AttributeValues("indent", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("byte-order-mark", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("escape-uri-attributes", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("include-content-type", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("undeclare-prefixes", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("normalization-form", new String[] {"NFC", "NFD", 
                    "NFKC", "NFKD", "fully-normalized", "none"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("copy", 
            new AttributeValues[] {
                new AttributeValues("copy-namespaces", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("inherit-namespaces", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("copy-of", 
            new AttributeValues[] {
                new AttributeValues("copy-namespaces", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("element", 
            new AttributeValues[] {
                new AttributeValues("inherit-namespaces", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("function", 
            new AttributeValues[] {
                new AttributeValues("override", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("param", 
            new AttributeValues[] {
                new AttributeValues("required", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("tunnel", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("sort", 
            new AttributeValues[] {
                new AttributeValues("case-order", new String[] {"upper-first", "lower-first"}),
                new AttributeValues("data-type", new String[] {"text", "number"}),
                new AttributeValues("order", new String[] {"ascending", "descending"}),
                new AttributeValues("stable", STRING_ARRAY_YES_OR_NO)
            })
        );
      setTagAttributeValues.add(new TagAttributeValues("text", 
            new AttributeValues[] {
                new AttributeValues("disable-output-escaping", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("value-of", 
            new AttributeValues[] {
                new AttributeValues("disable-output-escaping", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("with-param", 
            new AttributeValues[] {
                new AttributeValues("tunnel", STRING_ARRAY_YES_OR_NO)
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("number", 
            new AttributeValues[] {
                new AttributeValues("level", new String[] {"single", "multiple", "any"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("attribute", 
            new AttributeValues[] {
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("document", 
            new AttributeValues[] {
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"})
            })
        );
        setTagAttributeValues.add(new TagAttributeValues("result-document", 
            new AttributeValues[] {
                new AttributeValues("method", new String[] {"xml", "html", "xhtml", "text"}),
                new AttributeValues("byte-order-mark", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("escape-uri-attributes", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("include-content-type", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("indent", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("normalization-form", new String[] {"NFC", "NFD", "NFKC", "NFKD", "fully-normalized", "none"}),
                new AttributeValues("omit-xml-declaration", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("standalone", new String[] {"yes", "no", "omit"}),
                new AttributeValues("undeclare-prefixes", STRING_ARRAY_YES_OR_NO),
                new AttributeValues("validation", new String[] {"strict", "lax", "preserve", "strip"}),
            })
        );
    }
}   
    
class AttributeValues implements Comparable {
    private String attributeName = "";
    private List<String> attributeValues = new ArrayList<String>();

    public AttributeValues(String attributeName) {
        if ((attributeName != null) && (attributeName.length() > 0)) {
            this.attributeName = attributeName;
        }
    }
    
    public AttributeValues(String attributeName, String[] attributeValues) {
        this(attributeName);
        if ((attributeValues != null) && (attributeValues.length > 0)) {
            for (String attributeValue : attributeValues) {
                addValue(attributeValue);
            }
        }
    }

    public boolean addValue(String attributeValue) {
        if ((attributeValue != null) && (attributeValue.length() > 0) &&
            (! attributeValues.contains(attributeValue))) {
            return attributeValues.add(attributeValue);
        }
        return false;
    }
    
    public String getAttributeName() {return attributeName;}
    public List<String> getAttributeValues() {return attributeValues;}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() == String.class) {
            return ((String) obj).equals(attributeName);
        }    
        if (getClass() != obj.getClass()) return false;
        
        if (this.attributeName == null) return false;
        
        AttributeValues other = (AttributeValues) obj;
        return (this.attributeName.equals(other.attributeName));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.attributeName != null ? this.attributeName.hashCode() : 0);
        return hash;
    }
    
    public int compareTo(Object obj) {
        AttributeValues other = (AttributeValues) obj;
        String thisValue = attributeName, 
               otherValue = other.attributeName;
        return (thisValue.compareTo(otherValue));
    }
    
    @Override
    public String toString() {
        return ("attribute name: [" + attributeName + "]");
    }
}

class TagAttributeValues implements Comparable {
    private String tagName = "";
    private List<AttributeValues> attributes = new ArrayList<AttributeValues>();

    public TagAttributeValues(String tagName) {
        if ((tagName != null) && (tagName.length() > 0)) {
            this.tagName = tagName;
        }
    }

    public TagAttributeValues(String tagName, AttributeValues[] attributes) {
        this(tagName);
        if ((attributes != null) && (attributes.length > 0)) {
            for (AttributeValues attribute : attributes) {
                addAttribute(attribute);
            }
        }
    }

    public boolean addAttribute(AttributeValues attribute) {
        if ((attribute != null) && (! attributes.contains(attribute))) {
            return attributes.add(attribute);
        }
        return false;
    }
    
    public List<String> getAttributeValues(String attributeName) {
        List<String> resultList = Collections.emptyList();
        for (AttributeValues attribute : attributes) {
            if ((attribute == null) || (! attribute.equals(attributeName)))
                continue;
            
            return attribute.getAttributeValues();
        }
        return resultList;
    }
    
    public String getTagName() {return tagName;}
    public List<AttributeValues> getAttributes() {return attributes;}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() == String.class) {
            return ((String) obj).equals(tagName);
        }    
        if (getClass() != obj.getClass()) return false;
        if (this.tagName == null) return false;
        
        TagAttributeValues other = (TagAttributeValues) obj;
        return (this.tagName.equals(other.tagName));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.tagName != null ? this.tagName.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object obj) {
        TagAttributeValues other = (TagAttributeValues) obj;
        String thisValue = tagName, 
               otherValue = other.tagName;
        return (thisValue.compareTo(otherValue));
    }

    @Override
    public String toString() {return ("tag name: [" + tagName + "]");}
}