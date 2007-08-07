/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
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
 * @todo Limit this hint to Rails projects, or at least files containing
 *  rails-patterned names
 *
 * @author Tor Norbye
 */
public class RailsDeprecations implements AstRule {
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

    public boolean appliesTo(CompilationInfo info) {
        // Only perform these checks in Rails projects
        Project project = FileOwnerQuery.getOwner(info.getFileObject());
        // Ugly!!
        if (project == null || project.getClass().getName().indexOf("RailsProject") == -1) { // NOI18N
            return false;
        }

        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.ROOTNODE);
    }

    public void run(CompilationInfo info, Node root, AstPath path, int caretOffset, List<Description> result) {
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

    private void scan(CompilationInfo info, Node node, List<Description> result) {
        // Look for use of deprecated fields
        if (node.nodeId == NodeTypes.INSTVARNODE || node.nodeId == NodeTypes.INSTASGNNODE) {
            String name = ((INameNode)node).getName();

            // Skip matches in _test files, since the standard code generator still
            // spits out code which violates the deprecations
            // (such as    @request    = ActionController::TestRequest.new )
            if (deprecatedFields.contains(name) && !info.getFileObject().getName().endsWith("_test")) { // NOI18N
                // Add a warning - you're using a deprecated field. Use the
                // method/attribute instead!
                String message = NbBundle.getMessage(RailsDeprecations.class, "DeprecatedRailsField", name, name.substring(1));
                addFix(info, node, result, message);
            }
        } else if (AstUtilities.isCall(node)) {
            String name = ((INameNode)node).getName();
            
            if (deprecatedMethods.containsKey(name)) {
                // Add a warning - you're using a deprecated field. Use the
                // method/attribute instead!
                String message = NbBundle.getMessage(RailsDeprecations.class, "DeprecatedMethodUse", name, deprecatedMethods.get(name));
                addFix(info, node, result, message);
            }
        }

        @SuppressWarnings(value = "unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            scan(info, child, result);
        }
    }

    private void addFix(CompilationInfo info, Node node, List<Description> result, String displayName) {
        OffsetRange range = AstUtilities.getNameRange(node);

        Description desc = new Description(this, displayName, info.getFileObject(), range, Collections.<Fix>emptyList(), 100);
        result.add(desc);

        // TODO - add a fix to turn off this hint? - Should be a utility or infrastructure option!
    }

    public boolean getDefaultEnabled() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
