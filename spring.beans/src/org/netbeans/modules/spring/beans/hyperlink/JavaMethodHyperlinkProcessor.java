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

import org.netbeans.modules.spring.beans.editor.BeansEditorUtils;
import org.netbeans.modules.spring.beans.editor.BeansEditorUtils.Public;
import org.netbeans.modules.spring.beans.editor.BeansEditorUtils.Static;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class JavaMethodHyperlinkProcessor implements HyperlinkProcessor {

    private int argCount = -1;
    private BeansEditorUtils.Public publicFlag = BeansEditorUtils.Public.DONT_CARE;
    private BeansEditorUtils.Static staticFlag = BeansEditorUtils.Static.DONT_CARE;

    public JavaMethodHyperlinkProcessor(Public publicFlag, Static staticFlag, int argCount) {
        this.publicFlag = publicFlag;
        this.staticFlag = staticFlag;
        this.argCount = argCount;
    }
    
    public void process(HyperlinkEnv env) {
        String classFqn = BeansEditorUtils.getBeanClassName(env.getCurrentTag());
        BeansEditorUtils.openMethodInEditor(env.getDocument(), classFqn, env.getValueString(), 0,
                            BeansEditorUtils.Public.DONT_CARE, BeansEditorUtils.Static.NO);
    }
}
