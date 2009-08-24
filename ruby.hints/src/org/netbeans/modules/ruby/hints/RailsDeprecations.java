/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IScopingNode;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * A hint which looks at Rails files and scans for usages of deprecated
 * Rails constructs; it adds warnings (and in some cases fixes) for these.
 * <p>
 * Source:
 * <a href="http://www.rubyonrails.org/deprecation">
 * http://www.rubyonrails.org/deprecation
 * </a>
 * The above deprecations are (mostly) covered. However, my googling also found
 * the following lists which need to be evaluated and covered:
 * <ul>
 *  <li> http://rubythis.blogspot.com/2006/12/ruby-on-rails-deprecations-part-1.html
 *  <li> http://rubythis.blogspot.com/2006/12/ruby-on-rails-deprecations-part-2.html
 *  <li> http://rubythis.blogspot.com/2006/12/ruby-on-rails-deprecations-part-3-of-3.html
 *  <li> http://i.nfectio.us/articles/2006/11/02/deprecations-in-rails-1-2
 * </ul>
 * 
 * @todo Infer deprecations dynamically; Rails seems to annotate them - see
 *   for example has_many_association.rb
 *   <pre>
 *       deprecate :find_all => "use find(:all, ...) instead"
 *   </pre>
 *
 * @author Tor Norbye
 */
public class RailsDeprecations extends RubyAstRule {
    static Set<String> deprecatedFields = new HashSet<String>();
    static Map<String,String> deprecatedMethods = new HashMap<String,String>();
    static {
        deprecatedFields.add("@params"); // NOI18N
        deprecatedFields.add("@session"); // NOI18N
        deprecatedFields.add("@flash"); // NOI18N
        deprecatedFields.add("@request"); // NOI18N
        deprecatedFields.add("@cookies"); // NOI18N
        deprecatedFields.add("@headers"); // NOI18N
        deprecatedFields.add("@response"); // NOI18N
        
        deprecatedMethods.put("find_first", "find :first"); // NOI18N
        deprecatedMethods.put("find_all", "find :all"); // NOI18N
        deprecatedMethods.put("push_with_attributes", "has_many :through"); // NOI18N
        deprecatedMethods.put("redirect_to_path", "redirect_to"); // NOI18N
        deprecatedMethods.put("redirect_to_url", "redirect_to"); // NOI18N
        deprecatedMethods.put("start_form_tag", "form_tag with a block"); // TODO - I18n?
        deprecatedMethods.put("end_form_tag", "form_tag with a block");
        deprecatedMethods.put("update_element_function", "RJS"); // NOI18N
        deprecatedMethods.put("link_to_image", "link_to(image_tag(..), url)"); // NOI18N
        deprecatedMethods.put("link_image_to", "link_to(image_tag(..), url)"); // NOI18N
        deprecatedMethods.put("human_size", "number_to_human_size"); // NOI18N
        deprecatedMethods.put("post_format", "respond_to or request.format");
        deprecatedMethods.put("formatted_post?", "respond_to or request.format");
        deprecatedMethods.put("xml_post?", "respond_to or request.format");
        deprecatedMethods.put("yaml_post?", "respond_to or request.format");
        deprecatedMethods.put("render_text", "render :text => ..."); // NOI18N
        deprecatedMethods.put("render_template", "render :template => ..."); // NOI18N
        // TODO - the above list for render_X was not exhaustive - look up the API and complete it!
        // TODO url_for(:symbol, *args), redirect_to(:symbol, *args)
        // TODO components
        // TODO *association*_count
    }

    public RailsDeprecations() {
    }

    public boolean appliesTo(RuleContext context) {
        ParserResult info = context.parserResult;
        // Only perform these checks in Rails projects
        Project project = FileOwnerQuery.getOwner(RubyUtils.getFileObject(info));
        // Ugly!!
        if (project == null || project.getClass().getName().indexOf("RailsProject") == -1) { // NOI18N
            return false;
        }

        return true;
    }

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.ROOTNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node root = context.node;
        ParserResult info = context.parserResult;
        AstPath path = context.path;

        if (root == null) {
            return;
        }
        
        // This rule should only be called on the root node itself
        assert path.leaf() == root;
        
        scan(info, root, result);
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "Rails_Deprecations"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(RailsDeprecations.class, "RailsDeprecation");
    }

    public String getDescription() {
        return NbBundle.getMessage(RailsDeprecations.class, "RailsDeprecationDesc");
    }

    private void scan(ParserResult info, Node node, List<Hint> result) {
        // Look for use of deprecated fields
        if (node.getNodeType() == NodeType.INSTVARNODE || node.getNodeType() == NodeType.INSTASGNNODE) {
            String name = ((INameNode)node).getName();
            if (!inActionController(info, node)) {
                return;
            }
            // Skip matches in _test files, since the standard code generator still
            // spits out code which violates the deprecations
            // (such as    @request    = ActionController::TestRequest.new )
            if (deprecatedFields.contains(name) && !RubyUtils.getFileObject(info).getName().endsWith("_test")) { // NOI18N
                // Add a warning - you're using a deprecated field. Use the
                // method/attribute instead!
                String message = NbBundle.getMessage(RailsDeprecations.class, "DeprecatedRailsField", name, name.substring(1));
                addFix(info, node, result, message);
            }
        } else if (AstUtilities.isCall(node)) {
            String name = ((INameNode)node).getName();
            
            if (deprecatedMethods.containsKey(name)) {
                // #121418: render_template is not just a deprecated Rails method,
                // it's also an RSpec method
                if ("render_template".equals(name)) { // NOI18N
                    // Filter from RSpec modules
                    if (RubyUtils.getFileObject(info).getName().endsWith("_spec")) { // NOI18N
                        return;
                    }
                }

                // find_all is not only a deprecated Rails active record method,
                // it's also a common method on Enumerable! Only warn about
                // this when you're calling it as a static method!
                // (It would be better to check the actual types here and
                // make sure that the class on the left is actually a model,
                // but that's costly and much less likely to be a problem
                if (name.startsWith("find_")) { // NOI18N
                    if (node.getNodeType() == NodeType.CALLNODE) {    
                        Node receiver = ((CallNode)node).getReceiverNode();
                        if (receiver.getNodeType() != NodeType.CONSTNODE && 
                                receiver.getNodeType() != NodeType.COLON2NODE) {
                            return;
                        }
                    }
                }
                
                // Add a warning - you're using a deprecated field. Use the
                // method/attribute instead!
                String message = NbBundle.getMessage(RailsDeprecations.class, "DeprecatedRailsMethodUse", name, deprecatedMethods.get(name));
                addFix(info, node, result, message);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            scan(info, child, result);
        }
    }

    /**
     * @return true if the given node is within a class that is a subclass
     * of <code>ActionController::Base</code>.
     */
    private boolean inActionController(ParserResult info, Node node) {
        Node root = AstUtilities.getRoot(info);
        if (root == null) {
            return false;
        }
        AstPath path = new AstPath(root, node);
        IScopingNode clazz = AstUtilities.findClassOrModule(path);
        if (clazz == null) {
            return false;
        }
        String className = AstUtilities.getClassOrModuleName(clazz);
        IndexedClass superClass = RubyIndex.get(info).getSuperclass(className);
        while (superClass != null) {
            String superClassName = superClass.getName();
            if (superClassName.equals("ActionController::Base")) {  //NOI18N
                return true;
            }
            superClass = RubyIndex.get(info).getSuperclass(superClassName);
        }
        return false;
    }

    private void addFix(ParserResult info, Node node, List<Hint> result, String displayName) {
        OffsetRange range = AstUtilities.getNameRange(node);

        range = LexUtilities.getLexerOffsets(info, range);
        if (range != OffsetRange.NONE) {
            Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, Collections.<HintFix>emptyList(), 100);
            result.add(desc);
        }
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public boolean showInTasklist() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
