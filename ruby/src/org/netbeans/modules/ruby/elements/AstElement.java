/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.elements;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.INameNode;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyParseResult;
import org.netbeans.modules.ruby.RubyParser;
import org.netbeans.modules.ruby.RubyType;
import org.netbeans.modules.ruby.lexer.LexUtilities;

/**
 * A Ruby element coming from a JRuby parse tree.
 *
 * @author Tor Norbye
 */
public abstract class AstElement extends RubyElement {

    protected Node node;
    protected ParserResult info;
    protected ArrayList<AstElement> children;
    protected String name;
    private String in;
    protected Set<Modifier> modifiers;
    private RubyType type;
    /**
     * Specfies whether this element should be hidden from the navigator window.
     */
    private boolean hidden;

    public AstElement(ParserResult info, Node node) {
        super();
        this.info = info;
        this.node = node;
        this.type = RubyType.createUnknown(); // by defaul unknown
    }

    public String getFqn() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public abstract String getName();

    //    public String getName() {
    //        if (name == null) {
    //            name = node.toString();
    //        }
    //
    //        return name;
    //    }

    public String getDisplayName() {
        return getName();
    }

    public String getDescription() {
        // XXX TODO
        return getName();
    }

    public List<AstElement> getChildren() {
        //        if (children == null) {
        //            children = new ArrayList<AstElement>();
        //
        //            for (Node child : node.childNodes()) {
        //                addInterestingChildren(this, children, child);
        //            }
        //        }
        //
        if (children == null) {
            return Collections.emptyList();
        }

        return children;
    }

    public void addChild(AstElement child) {
        if (children == null) {
            children = new ArrayList<AstElement>();
        }

        children.add(child);
    }

    public static AstElement create(ParserResult info, Node node) {
        switch (node.getNodeType()) {
        case DEFNNODE:
        case DEFSNODE:
            return new AstMethodElement(info, node);
        case CLASSNODE:
        case SCLASSNODE:
            return new AstClassElement(info, node);
        case MODULENODE:
            return new AstModuleElement(info, node);
        case CONSTNODE:
            return new AstNameElement(info, node, ((INameNode)node).getName(),
                    ElementKind.VARIABLE); // Why VARIABLE instead of CONSTANT?
        case CLASSVARNODE:
        case CLASSVARDECLNODE:
        case INSTASGNNODE:
        case INSTVARNODE:
            return new AstFieldElement(info, node);
        case CONSTDECLNODE:
            return new AstNameElement(info, node, ((INameNode)node).getName(),
                    ElementKind.CONSTANT);
        case SYMBOLNODE:
            return new AstAttributeElement(info, (SymbolNode)node, null);
        default:
            return null;
        }
    }

    @Override
    public String toString() {
        String clz = getClass().getName();

        return clz.substring(0, clz.lastIndexOf('.')) + ":" + node.toString();
    }

    public Image getIcon() {
        return null;
    }

    @Override
    public String getIn() {
        // TODO - compute signature via AstUtilities
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public ParserResult getInfo() {
        return info;
    }

    public void setType(final RubyType type) {
        assert type != null : "Cannot pass null to AstElement#setTypes";
        this.type = type;
    }

    public RubyType getType() {
        return type;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        RubyParseResult parserResult = AstUtilities.getParseResult(result);
        Element object = RubyParser.resolveHandle(parserResult, this);

        if (object instanceof AstElement) {
            Node target = ((AstElement) object).getNode();
            if (target != null) {
                OffsetRange range = AstUtilities.getRange(node);
                return LexUtilities.getLexerOffsets(parserResult, range);
            } else {
                return OffsetRange.NONE;
            }
        } else if (object != null) {
            Logger logger = Logger.getLogger(AstElement.class.getName());
            logger.log(Level.WARNING, "Foreign element: " + object + " of type " + //NOI18N
                    ((object != null) ? object.getClass().getName() : "null")); //NOI18N
        } else {
            if (getNode() != null) {
                OffsetRange astRange = AstUtilities.getRange(getNode());
                if (astRange != OffsetRange.NONE) {
                    ParserResult oldInfo = info;
                    if (oldInfo == null) {
                        oldInfo = parserResult;
                    }
                    return LexUtilities.getLexerOffsets(oldInfo, astRange);
                } else {
                    return OffsetRange.NONE;
                }
            }
        }

        return OffsetRange.NONE;
    }

    /**
     * @see #hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @see #hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}
