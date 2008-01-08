/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.hyperlink;

import org.netbeans.modules.spring.beans.editor.ContextUtilities;

/**
 * Hyperlink Processor for p-namespace stuff. Delegates to beanref processor 
 * and property processor for computation
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PHyperlinkProcessor implements HyperlinkProcessor {

    private BeansRefHyperlinkProcessor beansRefHyperlinkProcessor
            = new BeansRefHyperlinkProcessor(true);
    private PropertyHyperlinkProcessor propertyHyperlinkProcessor
            = new PropertyHyperlinkProcessor();
    public PHyperlinkProcessor() {
        
    }

    public void process(HyperlinkEnv env) {
        String attribName = env.getAttribName();
        if(env.getType() == BeansContextHyperlinkProvider.Type.ATTRIB_VALUE) {
            if(attribName.endsWith("-ref")) {
                beansRefHyperlinkProcessor.process(env);
            }
        } else if(env.getType() == BeansContextHyperlinkProvider.Type.ATTRIB) {
            String temp = ContextUtilities.getLocalNameFromTag(attribName);
            if(temp.endsWith("-ref")) {
                temp = temp.substring(0, temp.indexOf("-ref"));
            }
            
            HyperlinkEnv newEnv = new HyperlinkEnv(env.getDocument(), 
                    env.getCurrentTag(), env.getTagName(), env.getAttribName(), temp, env.getType());
            propertyHyperlinkProcessor.process(newEnv);
        }
    }

}
