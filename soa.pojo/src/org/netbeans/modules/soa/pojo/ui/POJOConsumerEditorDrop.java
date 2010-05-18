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

package org.netbeans.modules.soa.pojo.ui;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.pojo.resources.DestinationPaletteDrop;
import org.netbeans.modules.soa.pojo.schema.POJOConsumer;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.modules.soa.pojo.wizards.POJOConsumerPalleteWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author sgenipudi
 */
public class POJOConsumerEditorDrop extends DestinationPaletteDrop implements ActiveEditorDrop {
    private POJOConsumerNode mConsumerNode = null;
    
    public POJOConsumerEditorDrop(POJOConsumerNode consumer) {
        destType = DestinationType.CONSUMER;
        mConsumerNode = consumer;
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeType!=null && ("text/x-java".equals(mimeType) || "text/x-jsp".equals(mimeType) )) { //NOI18N
            POJOConsumerPalleteWizardIterator powiz = new POJOConsumerPalleteWizardIterator();
             WizardDescriptor wd = new WizardDescriptor(
                    powiz
             );
            this.mWizDesc = wd;
            this.wizInstItr = powiz;
            POJOConsumer pjc = this.mConsumerNode.getConsumer();
            QName inMsgType = pjc.getInputMessageType();
            QName intf = pjc.getInterface();
            QName opn = pjc.getOperation();
            String invokeType = pjc.getInvokeInputType();
            Boolean bInvokeType = Boolean.TRUE;
            if (! invokeType.equals(GeneratorUtil.SYNCH_CONST)) {
                bInvokeType = Boolean.FALSE;
            }
            
            String inputType = pjc.getInvokeInputType();
            String outputType = pjc.getInvokeReturnType();
            POJOSupportedDataTypes inPjd = null;
            POJOSupportedDataTypes outPjd = null;
            if ( inputType != null ) {
                inPjd = POJOSupportedDataTypes.valueOf(inputType);
            }
            if ( outputType != null) {
                outPjd = POJOSupportedDataTypes.valueOf(outputType);
            }
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE, inMsgType.getLocalPart());
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS, inMsgType.getNamespaceURI());
            
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_OPERATION_NAME, opn.getLocalPart());
            
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NAME, intf.getLocalPart());
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NS, intf.getNamespaceURI());
                        
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INVOKE_TYPE, bInvokeType);
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE, inPjd);
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE, outPjd);
            this.mWizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_DROP, Boolean.TRUE);
            
            destinationAction(targetComponent);
            return true;
        }
        return false;
    }
    
    
}
