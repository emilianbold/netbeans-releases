/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.tools.ant.*;
import org.apache.tools.mail.MailMessage;

/** Task to complain (via email) to people when things fail in a build.
 * XXX In Ant 1.4 this could be better written using TaskContainer, probably.
 * XXX support fallback address for errors not otherwise handled
 * XXX do not send the same error to more than one culprit
 * XXX support ignore patterns for errors, to be useful with e.g. compile deprecations
 * @author Jesse Glick
 */
public class Kvetcher extends Task implements BuildListener {
    
    private Explanation explanation = null;
    public final class Explanation {
        StringBuffer text = new StringBuffer();
        public void addText(String s) {
            text.append(getProject().replaceProperties(s));
        }
    }
    /** Provide some explanatory text for the message. */
    public Explanation createExplanation() {
        return explanation = new Explanation();
    }
    
    private String target;
    /** Set the target to run while listening. */
    public void setTarget(String t) {
        target = t;
    }
    
    private String from = null;
    /** Set the mail address to send from. */
    public void setFrom(String f) {
        from = f;
    }
    
    private String subject="Errors in sources";
    /** Set the mail subject. */
    public void setSubject(String s) {
        subject = s;
    }
    
    private String mailhost = "localhost";
    /** Set the SMTP mailhost to send from. */
    public void setMailhost(String mh) {
        mailhost = mh;
    }
    
    private List<Culprit> culprits = new ArrayList<Culprit>(20);
    public Culprit createCulprit() {
        Culprit c = new Culprit();
        culprits.add(c);
        return c;
    }
    
    public final class Culprit {
        List<Address> to = new ArrayList<Address>(1);
        List<Address> cc = new ArrayList<Address>(1);
        List<Regexp> regexp = new ArrayList<Regexp>(5);
        public Address createTo() {
            Address a = new Address();
            to.add(a);
            return a;
        }
        public Address createCC() {
            Address a = new Address();
            cc.add(a);
            return a;
        }
        public Regexp createRegexp() {
            Regexp r = new Regexp();
            regexp.add(r);
            return r;
        }
        boolean isValid() {
            if ((to.isEmpty() && cc.isEmpty()) || regexp.isEmpty()) return false;
            Iterator it = to.iterator();
            while (it.hasNext()) {
                if (! ((Address)it.next()).isValid()) return false;
            }
            it = cc.iterator();
            while (it.hasNext()) {
                if (! ((Address)it.next()).isValid()) return false;
            }
            it = regexp.iterator();
            while (it.hasNext()) {
                if (! ((Regexp)it.next()).isValid()) return false;
            }
            return true;
        }
    }
    public final class Address {
        String name;
        public void setName(String n) {
            name = n;
        }
        boolean isValid() {
            return name != null;
        }
    }
    public final class Regexp {
        Pattern pattern;
        int group = -1;
        public void setPattern(String p) throws BuildException {
            try {
                pattern = Pattern.compile(p);
            } catch (PatternSyntaxException rese) {
                throw new BuildException(rese, getLocation());
            }
        }
        /** Set which part of the message to actually send.
         * By default, the entire message.
         * But you may set this to 0 to mean the matched portion,
         * or some number >0 to mean that parenthesized group.
         */
        public void setGroup(int g) {
            group = g;
        }
        boolean isValid() {
            return pattern != null;
        }
    }
    
    private List<String> messages = new ArrayList<String>(1000);
    
    public void execute() throws BuildException {
        if (target == null) throw new BuildException("set the target");
        if (culprits.isEmpty()) throw new BuildException("add some culprits");
        Iterator it = culprits.iterator();
        while (it.hasNext()) {
            if (! ((Culprit)it.next()).isValid()) throw new BuildException("invalid <culprit>");
        }
        getProject().addBuildListener(this);
        boolean success = false;
        try {
            getProject().executeTarget(target);
            success = true;
        } finally {
            getProject().removeBuildListener(this);
        }
        if (success) sendMail();
    }
    
    public void messageLogged(BuildEvent buildEvent) {
        int pri = buildEvent.getPriority();
        if (pri == Project.MSG_WARN || pri == Project.MSG_ERR) {
            messages.add(buildEvent.getMessage());
        }
    }
    
    private void sendMail() throws BuildException {
        for (Culprit c: culprits) {
            try {
                MailMessage mail = null;
                //boolean send = false;
                PrintStream ps = null;
                for (String msg: messages) {
                    for (Regexp r: c.regexp) {
                        Matcher m = r.pattern.matcher(msg);
                        if (m.find()) {
                            if (mail == null) {
                                // OK, time to start sending.
                                log("Sending mail to " + c.to.get(0).name);
                                mail = new MailMessage(mailhost);
                                if (from == null) from = "kvetcher@" + mailhost;
                                mail.from(from);
                                for (Address a: c.to) {
                                    mail.to(a.name);
                                }
                                for (Address a: c.cc) {
                                    mail.cc(a.name);
                                }
                                mail.setSubject(subject);
                                ps = mail.getPrintStream();
                                if (explanation != null) {
                                    ps.println(explanation.text.toString());
                                }
                                ps.println();
                            }
                            ps.println(r.group == -1 ? msg : m.group(r.group));
                            break; // this regexp matches, look for other messages
                        }
                    }
                }
                if (mail != null) {
                    mail.sendAndClose();
                }
            } catch (IOException ioe) {
                throw new BuildException("While sending mail", ioe, getLocation());
            }
        }
    }
    
    // Ignore these:
    public void taskStarted(BuildEvent buildEvent) {
    }    
    public void buildStarted(BuildEvent buildEvent) {
    }    
    public void buildFinished(BuildEvent buildEvent) {
    }
    public void taskFinished(BuildEvent buildEvent) {
    }
    public void targetFinished(BuildEvent buildEvent) {
    }
    public void targetStarted(BuildEvent buildEvent) {
    }
    
}
