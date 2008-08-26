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

package org.netbeans.modules.web.client.tools.common.dbgp;

import java.net.URI;

/**
 *
 * @author jdeva
 */
public class CommandFactory {
    private DebuggerProxy proxy;
    
    public CommandFactory(DebuggerProxy proxy) {
        this.proxy = proxy;
    }
    
    public Break.BreakCommand breakCommand() {
        return new Break.BreakCommand(proxy.getTransactionId());
    }
    
    public Extension.OpenURICommand openURICommand(URI uri) {
        return new Extension.OpenURICommand(proxy.getTransactionId(), uri.toASCIIString());
    }
    
    public Breakpoint.LineBreakpointSetCommand lineBreakpointSetCommand(URI uri, int line) {
        return new Breakpoint.LineBreakpointSetCommand(proxy.getTransactionId(), uri.toASCIIString(), line);
    }
    
    public Breakpoint.CallBreakpointSetCommand callBreakpointSetCommand(String func) {
        return new Breakpoint.CallBreakpointSetCommand(proxy.getTransactionId(), func);
    }
    
    public Breakpoint.ConditionalBreakpointSetCommand conditionalBreakpointSetCommand(String condition) {
        return new Breakpoint.ConditionalBreakpointSetCommand(proxy.getTransactionId(), condition);
    }
    
    public Breakpoint.ExceptionBreakpointCommand exceptionBreakpointSetCommand(String exception) {
        return new Breakpoint.ExceptionBreakpointCommand(proxy.getTransactionId(), exception);
    }    
    
    public Breakpoint.BreakpointRemoveCommand breakpointRemoveCommand(String breakpointId) {
        return new Breakpoint.BreakpointRemoveCommand(proxy.getTransactionId(), breakpointId);
    }
    
    public Breakpoint.BreakpointGetCommand breakpointGetCommand(String breakpointId) {
        return new Breakpoint.BreakpointGetCommand(proxy.getTransactionId(), breakpointId);
    }    
    
    public Breakpoint.BreakpointUpdateCommand breakpointUpdateCommand(String breakpointId) {
        return new Breakpoint.BreakpointUpdateCommand(proxy.getTransactionId(), breakpointId);
    }
    
    public Breakpoint.BreakpointListCommand breakpointListCommand() {
        return new Breakpoint.BreakpointListCommand(proxy.getTransactionId());
    }    
    
    public Continue.RunCommand runCommand(){
        return new Continue.RunCommand(proxy.getTransactionId());
    }
    
    public Continue.PauseCommand pauseCommand(){
        return new Continue.PauseCommand(proxy.getTransactionId());
    }    
    
    public Continue.StepIntoCommand stepIntoCommand(){
        return new Continue.StepIntoCommand(proxy.getTransactionId());
    }
    
    public Continue.StepOutCommand stepOutCommand(){
        return new Continue.StepOutCommand(proxy.getTransactionId());
    }
    
    public Continue.StepOverCommand stepOverCommand(){
        return new Continue.StepOverCommand(proxy.getTransactionId());
    }
    
    public Continue.StopCommand stopCommand(){
        return new Continue.StopCommand(proxy.getTransactionId());
    }    
    
    public Continue.DetachCommand detachCommandCommand() {
        return new Continue.DetachCommand(proxy.getTransactionId());
    }
    
    public Status.StatusCommand statusCommand() {
        return new Status.StatusCommand(proxy.getTransactionId());
    }
    
    public Property.PropertyGetCommand propertyGetCommand(String name, int stackDepth) {
        return new Property.PropertyGetCommand(proxy.getTransactionId(), name, stackDepth);
    }
            
    public Property.PropertySetCommand propertySetCommand(String name, String value, int stackDepth) {
        return new Property.PropertySetCommand(proxy.getTransactionId(), name, value, stackDepth);
    }
    
    public Property.PropertyValueCommand propertyValueCommand(String name, int stackDepth) {
        return new Property.PropertyValueCommand(proxy.getTransactionId(), name, stackDepth);
    }
    
    public Stack.StackDepthCommand stackDepthCommand() {
        return new Stack.StackDepthCommand(proxy.getTransactionId());
    }
    
    public Stack.StackGetCommand stackGetCommand(int depth) {
        return new Stack.StackGetCommand(proxy.getTransactionId(), depth);
    }
    
    public Feature.FeatureGetCommand featureGetCommand(Feature.Name feature) {
        return new Feature.FeatureGetCommand(proxy.getTransactionId(), feature);
    }
         
    public Feature.FeatureSetCommand featureSetCommand(Feature.Name feature, String value) {
        return new Feature.FeatureSetCommand(proxy.getTransactionId(), feature, value);
    }    
    
    public Context.ContextGetCommand contextGetCommand(int stackDepth) {
        return new Context.ContextGetCommand(proxy.getTransactionId(), stackDepth);
    }
    
    public Context.ContextNamesCommand contextNamesCommand(int stackDepth) {
        return new Context.ContextNamesCommand(proxy.getTransactionId(), stackDepth);
    }
    
    public Source.SourceCommand sourceCommand(String uri) {
        return new Source.SourceCommand(proxy.getTransactionId(), uri);
    }
    
    public Eval.EvalCommand evalCommand(String data, int stackDepth) {
        return new Eval.EvalCommand(proxy.getTransactionId(), data, stackDepth);
    }
 }
