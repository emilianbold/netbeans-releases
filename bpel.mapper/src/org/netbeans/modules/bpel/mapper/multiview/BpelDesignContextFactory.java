/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.editors.api.nodes.FactoryAccess;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Vitaly Bychkov
 */
public class BpelDesignContextFactory implements DesignContextFactory {

    private static final BpelDesignContextFactory INSTANCE = new BpelDesignContextFactory();
    private final ContextCreator[] contextCreators;

    private BpelDesignContextFactory() {
        contextCreators = new ContextCreator[] {
            new AssignContextCreator(),
            new BooleanConditionContextCreator(),
            new TimeConditionContextCreator(),
            new ForEachContextCreator(),
            new EmptyContextCreator()};
    }

    public static BpelDesignContextFactory getInstance() {
        return INSTANCE;
    }

    public boolean isMappableEntity(BpelEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean isMappable = false;
        assert contextCreators != null;
        for (BpelDesignContextFactory.ContextCreator contextCreator : contextCreators) {
            if (contextCreator.accepted(entity)
                    && !(contextCreator instanceof EmptyContextCreator))
            {
                isMappable = true;
                break;
            }
        }

        return isMappable;
    }

    public BpelDesignContext createBpelDesignContext(
                    BpelEntity selectedEntity, Node node, Lookup lookup)
    {
        if (selectedEntity == null || node == null || lookup == null) {
            return null;
        }

        BpelDesignContext context = null;

        assert contextCreators != null;
        for (BpelDesignContextFactory.ContextCreator contextCreator : contextCreators) {
            if (contextCreator.accepted(selectedEntity)) {
                context = contextCreator.create(selectedEntity, node, lookup);
                break;
            }
        }

        return context;
    }

    public BpelDesignContext getActivatedContext(BpelModel currentBpelModel) {
        if (currentBpelModel == null) {
            return null;
        }

        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        BpelEntity bpelEntity = null;
        if (nodes[0] instanceof InstanceRef) {
            Object entity = ((InstanceRef) nodes[0]).getReference();
            if (entity instanceof BpelEntity
                    && currentBpelModel.equals(((BpelEntity)entity).getBpelModel()))
            {
                bpelEntity = (BpelEntity)entity;
            }
        } else {
            return null;
        }

        Lookup lookup = nodes[0].getLookup();
        BpelDesignContext bpelContext =
                createBpelDesignContext(bpelEntity, nodes[0], lookup);
        return bpelContext;
    }

    public BpelDesignContext getProcessContext(BpelModel currentBpelModel, Lookup lookup) {
        if (currentBpelModel == null) {
            return null;
        }

        BpelEntity bpelEntity = currentBpelModel.getProcess();
        Node node = null;
        if (bpelEntity != null) {
            node = FactoryAccess.getPropertyNodeFactory().createNode(NodeType.PROCESS, bpelEntity, lookup);
        }

        BpelDesignContext bpelContext =
                createBpelDesignContext(bpelEntity, node, lookup);
        return bpelContext;
    }

    private class AssignContextCreator implements ContextCreator {

        /**
         *
         * @param selectedEntity - the selected bpel entity to show mapper
         * @return true if selected Entity is Assign or Assign bpel descendant - Copy, From or To
         */
        public boolean accepted(BpelEntity selectedEntity) {
            if (selectedEntity == null) {
                return false;
            }
            //
            boolean accept = false;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Assign.class) {
                accept = true;
            } else if (entityType == Copy.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Assign.class) {
                    accept = true;
                }
            } else if (entityType == From.class || entityType ==  To.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Copy.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent != null &&
                            nextParent.getElementType() == Assign.class) {
                        accept = true;
                    }
                }
            } else if (entityType == Query.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null) {
                    entityType = parent.getElementType();
                    if (entityType == From.class || entityType == To.class) {
                        parent = parent.getParent();
                        if (parent != null && parent.getElementType() == Copy.class) {
                            BpelEntity nextParent = parent.getParent();
                            if (nextParent != null &&
                                    nextParent.getElementType() == Assign.class) {
                                accept = true;
                            }
                        }
                    }
                }
            } else if (entityType == Literal.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == From.class) {
                    parent = parent.getParent();
                    if (parent != null && parent.getElementType() == Copy.class) {
                        BpelEntity nextParent = parent.getParent();
                        if (nextParent != null &&
                                nextParent.getElementType() == Assign.class) {
                            accept = true;
                        }
                    }
                }
            }
            //
            return accept;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            //
            BpelDesignContext context =  null;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Assign.class) {
                context = new BpelDesignContextImpl(selectedEntity,
                        selectedEntity, selectedEntity, node, lookup);
            } else if (entityType == Copy.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Assign.class) {
                    context = new BpelDesignContextImpl(parent,
                            selectedEntity, selectedEntity, node, lookup);
                }
            } else if (entityType == From.class || entityType ==  To.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Copy.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent != null &&
                            nextParent.getElementType() == Assign.class) {
                        context = new BpelDesignContextImpl(nextParent,
                                parent, selectedEntity, node, lookup);
                    }
                }
            } else if (entityType == Query.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null) {
                    entityType = parent.getElementType();
                    if (entityType == From.class || entityType == To.class) {
                        parent = parent.getParent();
                        if (parent != null && parent.getElementType() == Copy.class) {
                            BpelEntity nextParent = parent.getParent();
                            if (nextParent != null &&
                                    nextParent.getElementType() == Assign.class) {
                                context = new BpelDesignContextImpl(nextParent,
                                        parent, selectedEntity, node, lookup);
                            }
                        }
                    }
                }
            } else if (entityType == Literal.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == From.class) {
                    parent = parent.getParent();
                    if (parent != null && parent.getElementType() == Copy.class) {
                        BpelEntity nextParent = parent.getParent();
                        if (nextParent != null &&
                                nextParent.getElementType() == Assign.class) {
                            context = new BpelDesignContextImpl(nextParent,
                                    parent, selectedEntity, node, lookup);
                        }
                    }
                }
            }
            //
            return context;
        }
    }

    private class TimeConditionContextCreator implements ContextCreator {

        /**
         * @param selectedEntity - the selected bpel entity to show mapper
         */
        public boolean accepted(BpelEntity selectedEntity) {
            if (selectedEntity == null) {
                return false;
            }

            boolean accept = false;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Wait.class ||
                    entityType == OnAlarmPick.class ||
                    entityType == OnAlarmEvent.class) {
                accept = true;
            } else if (entityType == For.class ||
                    entityType == RepeatEvery.class ||
                    entityType == DeadlineExpression.class) {
                BpelEntity parent = selectedEntity.getParent();
                entityType = parent.getElementType();
                if (entityType == Wait.class ||
                    entityType == OnAlarmPick.class ||
                    entityType == OnAlarmEvent.class) {
                    accept = true;
                }
            }

            return accept;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            //
            BpelDesignContext context =  null;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Wait.class ||
                    entityType == OnAlarmPick.class ||
                    entityType == OnAlarmEvent.class) {
                context = new BpelDesignContextImpl(selectedEntity,
                        selectedEntity, selectedEntity, node, lookup);
            } else if (entityType == For.class ||
                    entityType == RepeatEvery.class ||
                    entityType == DeadlineExpression.class) {
                BpelEntity parent = selectedEntity.getParent();
                entityType = parent.getElementType();
                if (entityType == Wait.class ||
                    entityType == OnAlarmPick.class ||
                    entityType == OnAlarmEvent.class) {
                    //
                    context = new BpelDesignContextImpl(parent,
                            parent, selectedEntity, node, lookup);
                }
            }
            //
            return context;
        }
    }

    private class BooleanConditionContextCreator implements ContextCreator {

        /**
         * @param selectedEntity - the selected bpel entity to show mapper
         */
        public boolean accepted(BpelEntity selectedEntity) {
            if (selectedEntity == null) {
                return false;
            }
            //
            boolean accept = false;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == If.class ||
                    entityType == ElseIf.class ||
                    entityType == While.class ||
                    entityType == RepeatUntil.class) {
                accept = true;
            } else if (entityType == BooleanExpr.class) {
                BpelEntity parent = selectedEntity.getParent();
                entityType = parent.getElementType();
                if (entityType == If.class ||
                    entityType == ElseIf.class ||
                    entityType == While.class ||
                    entityType == RepeatUntil.class) {
                    accept = true;
                }
            }
            //
            return accept;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            //
            BpelDesignContext context =  null;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == If.class ||
                    entityType == ElseIf.class ||
                    entityType == While.class ||
                    entityType == RepeatUntil.class) {
                context = new BpelDesignContextImpl(selectedEntity,
                        selectedEntity, selectedEntity, node, lookup);
            } else if (entityType == BooleanExpr.class) {
                BpelEntity parent = selectedEntity.getParent();
                entityType = parent.getElementType();
                if (entityType == If.class ||
                    entityType == ElseIf.class ||
                    entityType == While.class ||
                    entityType == RepeatUntil.class) {
                    context = new BpelDesignContextImpl(parent, parent,
                            selectedEntity, node, lookup);
                }
            }
            //
            return context;
        }
    }

    private class ForEachContextCreator implements ContextCreator {

        /**
         * @param selectedEntity - the selected bpel entity to show mapper
         */
        public boolean accepted(BpelEntity selectedEntity) {
            if (selectedEntity == null) {
                return false;
            }
            //
            boolean accept = false;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == ForEach.class) {
                accept = true;
            } else if (entityType == StartCounterValue.class ||
                    entityType == FinalCounterValue.class ||
                    entityType == CompletionCondition.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == ForEach.class) {
                    accept = true;
                }
            } else if (entityType == Branches.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null &&
                        parent.getElementType() == CompletionCondition.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent != null &&
                            nextParent.getElementType() == ForEach.class) {
                        accept = true;
                    }
                }
            }
            //
            return accept;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            //
            BpelDesignContext context =  null;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == ForEach.class) {
                context = new BpelDesignContextImpl(selectedEntity,
                        selectedEntity, selectedEntity, node, lookup);
            } else if (entityType == StartCounterValue.class ||
                    entityType == FinalCounterValue.class ||
                    entityType == CompletionCondition.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == ForEach.class) {
                    context = new BpelDesignContextImpl(parent,
                            selectedEntity, selectedEntity, node, lookup);
                }
            } else if (entityType == Branches.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null &&
                        parent.getElementType() == CompletionCondition.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent != null &&
                            nextParent.getElementType() == ForEach.class) {
                        context = new BpelDesignContextImpl(nextParent,
                            parent, selectedEntity, node, lookup);
                    }
                }
            }
            //
            return context;
        }
    }
}
