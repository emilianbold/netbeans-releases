/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.mail.MailMessage;

import org.apache.regexp.*;

/** Task to complain (via email) to people when things fail in a build.
 * In Ant 1.4 this could be better written using TaskContainer, probably.
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
            text.append(s);
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
    
    private List culprits = new ArrayList(20); // List<Culprit>
    public Culprit createCulprit() {
        Culprit c = new Culprit();
        culprits.add(c);
        return c;
    }
    
    public final class Culprit {
        List to = new ArrayList(1); // List<Address>
        List cc = new ArrayList(1); // List<Address>
        List regexp = new ArrayList(5); // List<Regexp>
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
        RE pattern;
        int group = -1;
        public void setPattern(String p) throws BuildException {
            try {
                pattern = new RE(p);
            } catch (RESyntaxException rese) {
                throw new BuildException(rese, location);
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
    
    private List messages = new ArrayList(1000); // List<String>
    
    public void execute() throws BuildException {
        if (target == null) throw new BuildException("set the target");
        if (culprits.isEmpty()) throw new BuildException("add some culprits");
        Iterator it = culprits.iterator();
        while (it.hasNext()) {
            if (! ((Culprit)it.next()).isValid()) throw new BuildException("invalid <culprit>");
        }
        project.addBuildListener(this);
        boolean success = false;
        try {
            project.executeTarget(target);
            success = true;
        } finally {
            project.removeBuildListener(this);
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
        Iterator it = culprits.iterator();
        while (it.hasNext()) {
            try {
                Culprit c = (Culprit)it.next();
                MailMessage mail = null;
                boolean send = false;
                PrintStream ps = null;
                Iterator it2 = messages.iterator();
                while (it2.hasNext()) {
                    String msg = (String)it2.next();
                    Iterator it3 = c.regexp.iterator();
                    while (it3.hasNext()) {
                        Regexp r = (Regexp)it3.next();
                        if (r.pattern.match(msg)) {
                            if (mail == null) {
                                // OK, time to start sending.
                                log("Sending mail to " + ((Address)c.to.get(0)).name);
                                mail = new MailMessage(mailhost);
                                if (from == null) from = "kvetcher@" + mailhost;
                                mail.from(from);
                                Iterator it4 = c.to.iterator();
                                while (it4.hasNext()) {
                                    mail.to(((Address)it4.next()).name);
                                }
                                it4 = c.cc.iterator();
                                while (it4.hasNext()) {
                                    mail.cc(((Address)it4.next()).name);
                                }
                                mail.setSubject(subject);
                                ps = mail.getPrintStream();
                                if (explanation != null) {
                                    ps.println(explanation.text.toString());
                                }
                                ps.println();
                            }
                            ps.println(r.group == -1 ? msg : r.pattern.getParen(r.group));
                            break; // this regexp matches, look for other messages
                        }
                    }
                }
                if (mail != null) {
                    mail.sendAndClose();
                }
            } catch (IOException ioe) {
                throw new BuildException("While sending mail", ioe, location);
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
