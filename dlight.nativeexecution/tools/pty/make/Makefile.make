OBJS = $(OBJFILES:%=$(OBJ_DIR)/%)
$(OBJS) := DEP = $(@:$(OBJ_DIR)/%.o=$(SRC_DIR)/%.c)

clean_deps:
	$(RM) .make.state.$(CONF)

$(OBJS): $(OBJ_DIR) $$(DEP)
	$(COMPILE.c) -o $@ $(DEP)

.KEEP_STATE:
.KEEP_STATE_FILE: .make.state.$(CONF)

