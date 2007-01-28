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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmi.reflect.RefFeatured;

import org.netbeans.jmi.javamodel.BehavioralFeature;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.StringLiteral;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;

/*
 * This modifies the string used to lookup the bean by name.
 *
 * @author Sandip Chitale
 */
public class RenameBeanValueBindingExpressionRefactoringElement implements
        RefactoringElementImplementation {

    private StringLiteral valueBindingExpression;

    private String oldBeanName;

    private String newBeanName;

    // Enclosing callable feature such as method or constructor
    private BehavioralFeature behavioralFeature;

    private boolean enabled;

    private int status = RefactoringElementImplementation.NORMAL;

    private PositionBounds bounds;

    public RenameBeanValueBindingExpressionRefactoringElement(StringLiteral valueBindingExpression,
            String oldBeanName, String newBeanName) {
        this.valueBindingExpression = valueBindingExpression;
        this.oldBeanName = oldBeanName;
        this.newBeanName = newBeanName;

        // Enclosing callable feature such as method or constructor. This is returned from
        // getJavaElement() method so that the refactoring element appears in the right place
        // in the preview window.
        RefFeatured featured = valueBindingExpression.refImmediateComposite();
        while (featured != null) {
            if (featured instanceof BehavioralFeature) {
                behavioralFeature = (BehavioralFeature) featured;
                break;
            }
            featured = ((Element) featured).refImmediateComposite();
        }

        // initially enabled
        enabled = true;
    }

    public String getText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanValueBindingExpressionRefactoringElement.class).getString("MSG_RenameBeanValueBindingExpressionText"),
                new Object[] {oldBeanName});
    }

    public String getDisplayText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanValueBindingExpressionRefactoringElement.class).getString("MSG_RenameBeanValueBindingExpressionDisplayText"),
                new Object[] {oldBeanName});
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Looks for string literal of the form <code>"#{OldBeanName");</code>
     * and replaces the bean name with new beanname.
     */
    public void performChange() {
        String valueBindingExpressionValue = valueBindingExpression.getValue();
        valueBindingExpression.setValue(valueBindingExpressionValue.replaceAll(Pattern.quote(oldBeanName),
                Matcher.quoteReplacement(newBeanName)));
    }

    public Element getJavaElement() {
        return behavioralFeature;
    }

    public FileObject getParentFile() {
        return null;
    }

    public PositionBounds getPosition() {
        if (bounds == null) {
            bounds = JMManager.getManager().getElementPosition(getJavaElement());
        }
        return bounds;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void openInEditor() {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                new UnsupportedOperationException("New interface method not implemented, do it!")); // NOI18N
    }
    
}
