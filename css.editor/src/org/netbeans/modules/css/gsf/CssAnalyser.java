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
package org.netbeans.modules.css.gsf;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.css.editor.CssPropertyValue;
import org.netbeans.modules.css.editor.Property;
import org.netbeans.modules.css.editor.PropertyModel;
import org.netbeans.modules.css.editor.properties.CustomErrorMessageProvider;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 * @todo Add some support for CSS versions
 * @todo Localize !!!!!!!
 *
 * @author marek
 */
public class CssAnalyser {

    private static final String UNKNOWN_PROPERTY = "unknown_property";
    private static final String INVALID_PROPERTY_VALUE = "invalid_property_value";
    private static final String INVALID_CONTENT = "invalid_content";
    
    public static List<Error> checkForErrors(final Snapshot snapshot, final SimpleNode node) {
        final ArrayList<Error> errors = new ArrayList<Error>();
        final PropertyModel model = PropertyModel.instance();
        NodeVisitor visitor = new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (node.kind() == CssParserTreeConstants.JJTDECLARATION) {
                    SimpleNode propertyNode = SimpleNodeUtil.getChildByType(node, CssParserTreeConstants.JJTPROPERTY);
                    SimpleNode valueNode = SimpleNodeUtil.getChildByType(node, CssParserTreeConstants.JJTEXPR);

                    if (propertyNode != null) {
                        String propertyName = propertyNode.image().trim();
                        //check for vendor specific properies - ignore them
                        Property property = model.getProperty(propertyName);
                        if (!CssGSFParser.containsGeneratedCode(propertyName) && !isVendorSpecificProperty(propertyName) && property == null) {
                            //unknown property - report
                            Error error =
                                    DefaultError.createDefaultError(UNKNOWN_PROPERTY,
                                    NbBundle.getMessage(CssAnalyser.class, UNKNOWN_PROPERTY, propertyName),
                                    null, snapshot.getSource().getFileObject(),
                                    propertyNode.startOffset(), propertyNode.endOffset(),
                                    false /* not line error */, Severity.WARNING);
                            errors.add(error);
                        }

                        //check value
                        if (valueNode != null && property != null) {
                            String valueImage = valueNode.image().trim();
                            
                            //do not check values which contains generated code
                            //we are no able to identify the templating semantic
                            if (!CssGSFParser.containsGeneratedCode(valueImage)) {
                                CssPropertyValue pv = new CssPropertyValue(property, valueImage);
                                if (!pv.success()) {
                                    String errorMsg = null;
                                    if (pv instanceof CustomErrorMessageProvider) {
                                        errorMsg = ((CustomErrorMessageProvider) pv).customErrorMessage();
                                    }

                                    //error in property 
                                    String unexpectedToken = pv.left().get(pv.left().size() - 1);

                                    if (errorMsg == null) {
                                        errorMsg = NbBundle.getMessage(CssAnalyser.class, INVALID_PROPERTY_VALUE, unexpectedToken);
                                    }

                                    Error error =
                                            DefaultError.createDefaultError(INVALID_PROPERTY_VALUE,
                                            errorMsg,
                                            null, snapshot.getSource().getFileObject(),
                                            valueNode.startOffset(), valueNode.endOffset(),
                                            false /* not line error */, Severity.WARNING);
                                    errors.add(error);
                                }
                            }
                        }

                    }

                } else if(node.kind() == CssParserTreeConstants.JJTERROR_SKIP_TO_WHITESPACE && node.image().length() > 0) {
                    Error error =
                            new DefaultError(INVALID_CONTENT,
                            NbBundle.getMessage(CssAnalyser.class, INVALID_CONTENT),
                            null, snapshot.getSource().getFileObject(),
                            node.startOffset(), node.endOffset(), Severity.ERROR);
                    errors.add(error);
                }
            }
        };
        
        if(node != null) {
            SimpleNodeUtil.visitChildren(node, visitor);
        }
        return errors;
    }

    public static boolean isVendorSpecificProperty(String propertyName) {
        return propertyName.startsWith("_") || propertyName.startsWith("-"); //NOI18N
    }
}
