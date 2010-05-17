package org.netbeans.modules.wsdlextensions.email.refactoring;

import java.util.List;
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
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author skini
 */
public class EmailReferenceFinderVisitor extends ChildVisitor implements WSDLVisitor,
        SMTPComponent.Visitor, IMAPComponent.Visitor, POP3Component.Visitor {

    private Referenceable referenced;
    private List<Component> components;

    public EmailReferenceFinderVisitor(Referenceable referenced, List<Component> components) {
        this.referenced = referenced;
        this.components = components;
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
        //visitComponent(ee);
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
            Part part = (Part) referenced;
            if (matchesAnyAttributesWithPart(part.getName(), target)) {
                WSDLComponent parent = target.getParent();
                if (parent != null) {
                    NamedComponentReference<Message> messageRef = null;
                    if (parent instanceof BindingInput) {
                        Reference<Input> input = ((BindingInput) parent).getInput();
                        if (input != null && input.get() != null) {
                            messageRef = input.get().getMessage();
                        }
                    } else if (parent instanceof BindingOutput) {
                        Reference<Output> output = ((BindingOutput) parent).getOutput();
                        if (output != null && output.get() != null) {
                            messageRef = output.get().getMessage();
                        }
                    }

                    if (messageRef != null && messageRef.get() != null) {
                        if (messageRef.get().equals(part.getParent())) {
                            components.add(target);
                        }
                    }
                }
            }
        }

    }

    public void visit(SMTPInput target) {
        if (referenced instanceof Part) {
            Part part = (Part) referenced;
            if (matchesAnyAttributesWithPart(part.getName(), target)) {
                WSDLComponent parent = target.getParent();
                if (parent != null) {
                    NamedComponentReference<Message> messageRef = null;
                    if (parent instanceof BindingInput) {
                        Reference<Input> input = ((BindingInput) parent).getInput();
                        if (input != null && input.get() != null) {
                            messageRef = input.get().getMessage();
                        }
                    } else if (parent instanceof BindingOutput) {
                        Reference<Output> output = ((BindingOutput) parent).getOutput();
                        if (output != null && output.get() != null) {
                            messageRef = output.get().getMessage();
                        }
                    }

                    if (messageRef != null && messageRef.get() != null) {
                        if (messageRef.get().equals(part.getParent())) {
                            components.add(target);
                        }
                    }
                }
            }
        }
    }

    public void visit(POP3Input target) {
        if (referenced instanceof Part) {
            Part part = (Part) referenced;
            if (matchesAnyAttributesWithPart(part.getName(), target)) {
                WSDLComponent parent = target.getParent();
                if (parent != null) {
                    NamedComponentReference<Message> messageRef = null;
                    if (parent instanceof BindingInput) {
                        Reference<Input> input = ((BindingInput) parent).getInput();
                        if (input != null && input.get() != null) {
                            messageRef = input.get().getMessage();
                        }
                    } else if (parent instanceof BindingOutput) {
                        Reference<Output> output = ((BindingOutput) parent).getOutput();
                        if (output != null && output.get() != null) {
                            messageRef = output.get().getMessage();
                        }
                    }

                    if (messageRef != null && messageRef.get() != null) {
                        if (messageRef.get().equals(part.getParent())) {
                            components.add(target);
                        }
                    }
                }
            }
        }
    }

    private boolean matchesAnyAttributesWithPart(String name, IMAPInput input) {
        return name.equals(input.getBcc()) || name.equals(input.getCc()) ||
                name.equals(input.getFrom()) || name.equals(input.getTo()) ||
                name.equals(input.getSubject()) || name.equals(input.getMessage()) ||
                name.equals(input.getNewsgroups());
    }

    private boolean matchesAnyAttributesWithPart(String name, POP3Input input) {
        return name.equals(input.getBcc()) || name.equals(input.getCc()) ||
                name.equals(input.getFrom()) || name.equals(input.getTo()) ||
                name.equals(input.getSubject()) || name.equals(input.getMessage()) ||
                name.equals(input.getNewsgroups());
    }

    private boolean matchesAnyAttributesWithPart(String name, SMTPInput input) {
        return name.equals(input.getBcc()) || name.equals(input.getCc()) ||
                name.equals(input.getFrom()) || name.equals(input.getTo()) ||
                name.equals(input.getSubject()) || name.equals(input.getMessage()) ||
                name.equals(input.getNewsgroups());
    }

    private boolean matchesAnyAttributesWithPart(String name, SMTPAttachment attachment) {
        return name.equals(attachment.getAttachmentContentPart()) ||
                name.equals(attachment.getAttachmentFileNamePart());
    }

    public void visit(SMTPAttachment target) {
        if (referenced instanceof Part) {
            Part part = (Part) referenced;
            if (matchesAnyAttributesWithPart(part.getName(), target)) {
                WSDLComponent parent = target.getParent();
                if (parent != null) {
                    parent = parent.getParent();
                    if (parent != null) {
                        NamedComponentReference<Message> messageRef = null;
                        if (parent instanceof BindingInput) {
                            Reference<Input> input = ((BindingInput) parent).getInput();
                            if (input != null && input.get() != null) {
                                messageRef = input.get().getMessage();
                            }
                        } else if (parent instanceof BindingOutput) {
                            Reference<Output> output = ((BindingOutput) parent).getOutput();
                            if (output != null && output.get() != null) {
                                messageRef = output.get().getMessage();
                            }
                        }

                        if (messageRef != null && messageRef.get() != null) {
                            if (messageRef.get().equals(part.getParent())) {
                                components.add(target);
                            }
                        }
                    }
                }
            }
        }
    }
}
