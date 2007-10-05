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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * ForTask.java
 *
 * Created on November 1, 2001, 5:00 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import java.util.Iterator;
import java.io.File;
import org.apache.tools.ant.taskdefs.CallTarget;

/** Task which behave as for command. Task has two required attributes (list,property)
 * and one optional (delimiters) or one attribute (count). 
 * It has nested elements ant and antcall.
 * 'For' task divides value of list attribute by delimiters and for every part
 * calls all nested elements with property set to the devided part of the list.
 * Name of this property is specified by property attribute of for task.
 * When 'count' attribute is used, it calls nested elements several times 
 * depending on count value.
 *
 * Example:
 *
 *  <for list="first,second,third" delimiters=",;" property="selected.part">
 *      <antcall target="{selected.part}_target"/>
 *      <ant target="sometarget">
 *        <property name="some.property" value="somevalue"/>
 *      </ant>
 *  </for>
 *
 * <!-- or -->
 * 
 *  <for count="3">
 *      <antcall target="my_target"/>
 *      <ant target="sometarget">
 *        <property name="some.property" value="somevalue"/>
 *      </ant>
 *  </for>
 * @author lm97939
 */
public class ForTask extends Task {
    
    private String list;
    private String property;
    private String delim = ",;: \t\n\r\f";
    private String count;
    private List antlist = new LinkedList();
    
    
    public void setList(String list) {
        this.list = list;
    }
    
    public void setProperty(String prop) {
        this.property = prop;
    }
    
    public void setDelimiters(String delim) {
        this.delim = delim;
    }
    
    public void setCount(String count) {
        this.count = count;
    }
    
    public static class MyAnt {
        String antfile;
        String target;
        File dir;
        List properties = new LinkedList();
        
        public void setTarget(String t) {
            target = t;
        }
        public void setAntfile(String f) {
            antfile = f;
        }
        public void setDir(File d) {
            dir = d;
        }
        public void addProperty(MyProperty p) {
            properties .add(p);
        }
    }
    
    public static class MyAntCall {
        String target;
        List properties = new LinkedList();
        
        public void setTarget(String t) {
            target = t;
        }
        
        public void addParam(MyProperty p) {
            properties .add(p);
        }
    }
    
    public static class MyProperty {
        String name;
        String value;
        
        public void setName(String n) {
            name = n;
        }
        public void setValue(String v) {
            value = v;
        }
    }
    
    public void addAnt(MyAnt a) {
        antlist.add(a);
    }
    
    public void addAntcall(MyAntCall a) {
        antlist.add(a);
    }
    
    public void execute() throws BuildException {
        
        if(list == null && count == null) {
            throw new BuildException("Attribute 'list' or 'count' must be defined.");
        }
        if(count != null && (list != null || property != null)) {
            throw new BuildException("Only attribute 'count' must be defined.");
        }
        if(list != null && property == null) {
            throw new BuildException("Attribute 'property' is empty.");
        }
        
        if(count != null) {
            int intCount;
            try {
                intCount = Integer.valueOf(count).intValue();
            } catch (NumberFormatException e) {
                throw new BuildException("Attribute 'count' must be a valid integer.");
            }
            for(int i=0; i<intCount;i++) {
                runNested(null);
            }
        } else {
            StringTokenizer tokens = new StringTokenizer(list,delim);
            while (tokens.hasMoreTokens()) {
                String name = tokens.nextToken();
                runNested(name);
            }
        }
    }
    
    public void runNested(String name) {
        Iterator it = antlist.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof MyAnt) {
                MyAnt a = (MyAnt)o;
                Iterator prop = a.properties.iterator();
                Ant ant = (Ant) getProject().createTask("ant");
                ant.init();
                ant.setLocation(getLocation());
                ant.setTarget(a.target);
                ant.setDir(a.dir);
                ant.setAntfile(a.antfile);
                if(name!=null) {
                    Property p1 = ant.createProperty();
                    p1.setName(property);
                    p1.setValue(name);
                }
                while (prop.hasNext()) {
                    MyProperty myproperty = (MyProperty) prop.next();
                    Property p = ant.createProperty();
                    StringBuffer buff = new StringBuffer(myproperty.value);
                    int index = buff.toString().indexOf("${"+property+"}");
                    if (index != -1 && name != null)
                        buff.replace(index,index+property.length()+3,name);
                    p.setName(myproperty.name);
                    p.setValue(buff.toString());
                }
                ant.execute();
            }
            else {
                if (o instanceof MyAntCall) {
                    MyAntCall a = (MyAntCall)o;
                    Iterator prop = a.properties.iterator();
                    CallTarget ant = (CallTarget) getProject().createTask("antcall");
                    ant.init();
                    ant.setLocation(getLocation());
                    ant.setTarget(a.target);
                    if(name != null) {
                        Property p1 = ant.createParam();
                        p1.setName(property);
                        p1.setValue(name);
                    }
                    while (prop.hasNext()) {
                        MyProperty myproperty = (MyProperty) prop.next();
                        Property p = ant.createParam();
                        StringBuffer buff = new StringBuffer(myproperty.value);
                        int index = buff.toString().indexOf("${"+property+"}");
                        if (index != -1 && name != null)
                            buff.replace(index,index+property.length()+3,name);
                        p.setName(myproperty.name);
                        p.setValue(buff.toString());
                    }
                    ant.execute();
                }
                else {
                    throw new BuildException("Unknown nested element.");
                }
            }
        }
    }
    
}
