package org.netbeans.modules.wsdlextensions.email.refactoring;

import org.netbeans.modules.wsdlextensions.email.imap.IMAPAddress;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPBinding;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPComponent;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPInput;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPOperation;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAttachment;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPBinding;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPInput;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPOperation;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Address;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Binding;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Component;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Input;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Operation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author skini
 */
public class EmailRenameRefactorVisitor extends ChildVisitor implements WSDLVisitor,
        SMTPComponent.Visitor, IMAPComponent.Visitor, POP3Component.Visitor {

    private Referenceable referenced;
    private String oldName;

    public EmailRenameRefactorVisitor(Referenceable referenced, String oldName) {
        this.referenced = referenced;
        this.oldName = oldName;
    }

    @Override
    public void visit(ExtensibilityElement ee) {
        if (ee instanceof IMAPInput) {
            visit((IMAPInput) ee);
        } else if (ee instanceof POP3Input) {
            visit((POP3Input) ee);
        } else if (ee instanceof SMTPInput) {
            visit((SMTPInput) ee);
        } else if (ee instanceof SMTPAttachment) {
            visit((SMTPAttachment) ee);
        }
        visitComponent(ee);
    }

    public void visit(IMAPAddress target) {
        //nothing to refactor
    }

    public void visit(IMAPBinding target) {
        //nothing to refactor
    }

    public void visit(IMAPOperation target) {
        //nothing to refactor
    }

    public void visit(POP3Address target) {
        //nothing to refactor
    }

    public void visit(POP3Binding target) {
        //nothing to refactor
    }

    public void visit(POP3Operation target) {
        //nothing to refactor
    }

    public void visit(SMTPAddress target) {
        //nothing to refactor
    }

    public void visit(SMTPBinding target) {
        //nothing to refactor
    }

    public void visit(SMTPOperation target) {
        //nothing to refactor
    }

    public void visit(IMAPInput target) {
        if (referenced instanceof Part) {
            updateParts(target, oldName, ((Part) referenced).getName());
        }
    }

    public void visit(POP3Input target) {
        if (referenced instanceof Part) {
            updateParts(target, oldName, ((Part) referenced).getName());
        }
    }

    public void visit(SMTPInput target) {
        if (referenced instanceof Part) {
            updateParts(target, oldName, ((Part) referenced).getName());
        }
    }

    public void visit(SMTPAttachment target) {
        if (referenced instanceof Part) {
            updateParts(target, oldName, ((Part) referenced).getName());
        }
    }

    private void updateParts(IMAPInput input, String oldName, String name) {
        if (oldName.equals(input.getBcc())) {
            input.setBcc(name);
        }
        if (oldName.equals(input.getTo())) {
            input.setTo(name);
        }
        if (oldName.equals(input.getCc())) {
            input.setCc(name);
        }
        if (oldName.equals(input.getFrom())) {
            input.setFrom(name);
        }
        if (oldName.equals(input.getNewsgroups())) {
            input.setNewsgroups(name);
        }
        if (oldName.equals(input.getSubject())) {
            input.setSubject(name);
        }
        if (oldName.equals(input.getMessage())) {
            input.setMessage(name);
        }
    }

    private void updateParts(POP3Input input, String oldName, String name) {
        if (oldName.equals(input.getBcc())) {
            input.setBcc(name);
        }
        if (oldName.equals(input.getTo())) {
            input.setTo(name);
        }
        if (oldName.equals(input.getCc())) {
            input.setCc(name);
        }
        if (oldName.equals(input.getFrom())) {
            input.setFrom(name);
        }
        if (oldName.equals(input.getNewsgroups())) {
            input.setNewsgroups(name);
        }
        if (oldName.equals(input.getSubject())) {
            input.setSubject(name);
        }
        if (oldName.equals(input.getMessage())) {
            input.setMessage(name);
        }
    }

    private void updateParts(SMTPInput input, String oldName, String name) {
        if (oldName.equals(input.getBcc())) {
            input.setBcc(name);
        }
        if (oldName.equals(input.getTo())) {
            input.setTo(name);
        }
        if (oldName.equals(input.getCc())) {
            input.setCc(name);
        }
        if (oldName.equals(input.getFrom())) {
            input.setFrom(name);
        }
        if (oldName.equals(input.getNewsgroups())) {
            input.setNewsgroups(name);
        }
        if (oldName.equals(input.getSubject())) {
            input.setSubject(name);
        }
        if (oldName.equals(input.getMessage())) {
            input.setMessage(name);
        }
    }

    private void updateParts(SMTPAttachment attachment, String oldName, String name) {
        if (oldName.equals(attachment.getAttachmentContentPart())) {
            attachment.setAttachmentContentPart(name);
        }
        if (oldName.equals(attachment.getAttachmentFileNamePart())) {
            attachment.setAttachmentFileNamePart(name);
        }
    }
}
