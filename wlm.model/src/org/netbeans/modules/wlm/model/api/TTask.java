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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.api;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Java class for tTask complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;tTask&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;import&quot; type=&quot;{http://jbi.com.sun/wfse}tImport&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element name=&quot;title&quot; type=&quot;{http://jbi.com.sun/wfse}tExpression&quot; default=&quot;&quot;/&gt;
 *         &lt;element name=&quot;priority&quot; type=&quot;{http://jbi.com.sun/wfse}tExpression&quot;/&gt;
 *         &lt;element name=&quot;init&quot; type=&quot;{http://jbi.com.sun/wfse}tInit&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;assignment&quot; type=&quot;{http://jbi.com.sun/wfse}tAssignment&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;timeout&quot; type=&quot;{http://jbi.com.sun/wfse}tTimeout&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element name=&quot;escalation&quot; type=&quot;{http://jbi.com.sun/wfse}tEscalation&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element name=&quot;notification&quot; type=&quot;{http://jbi.com.sun/wfse}tNotification&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;2&quot;/&gt;
 *         &lt;element name=&quot;action&quot; type=&quot;{http://jbi.com.sun/wfse}tAction&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;name&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;portType&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *       &lt;attribute name=&quot;operation&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}QName&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
public interface TTask extends WLMNameable, WLMComponent,
        EndpointReference, AssignmentHolder
{
    String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
    String IMPORT_TYPE_PROPERTY = "import";
    String TITLE_PROPERTY = "title";
    String PRIORITY_PROPERTY = "priority";
    String INIT_PROPERTY = "init";
    String ASSIGNMENT_PROPERTY = "assignment";
    String TIMEOUT_PROPERTY = "timeout";
    String ESCALATION_PROPERTY = "escalation";
    String NOTIFICATION_PROPERTY = "notification";
    String ACTION_PROPERTY = "action";
    String KEYWORDS_PROPERTY = "keywords";

    String INPUT_VAR_NAME = "TaskInput";
    String OUTPUT_VAR_NAME = "TaskOutput";

    Collection<TImport> getImports();
    void addImport(TImport toAdd);
    void removeImport(TImport toRemove);
    boolean hasImports();

    String getTargetNamespace();

    void setTargetNamespace(String value);

    TTitle getTitle();
    void setTitle(TTitle title);
    void removeTitle(TTitle title);

    TPriority getPriority();
    void setPriority(TPriority priority);
    void removePriority(TPriority priority);

    TInit getInit();
    void setInit(TInit value);
    void removeInit(TInit value);

    List<TTimeout> getTimeouts();
    void addTimeOut(TTimeout timeout);
    void removeTimeOut(TTimeout timeout);
    boolean hasTimeouts();

    List<TEscalation> getEscalations();
    void addEscalation(TEscalation escalation);
    void removeEscalation(TEscalation escalation);
    boolean hasEscalations();

    List<TAction> getActions();
    void addAction(TAction action);
    void removeAction(TAction action);
    boolean hasActions();

    List<TNotification> getNotifications();
    void addNotification(TNotification notification);
    void removeNotification(TNotification notification);
    boolean hasNotifications();

    TKeywords getKeywords();
    void setKeywords(TKeywords keywords);
    void removeKeywords(TKeywords keywords);

    VariableDeclaration getInputVariable();
    VariableDeclaration getOutputVariable();

}
