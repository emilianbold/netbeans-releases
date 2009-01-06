class ExtractTest:
    def extract_test1(self, param_read_in_block, param_read_after_block, default_param=4):
        local1 = 1
        local2 = local1
        not_used_in_block = 1
        reassigned_in_block_before_read = 2
        read_in_block_only = 3
        read_after_block_only = 4

        # Beginning of extraction segment
        read_after_block = 5
        reassigned_after_block = 6
        reassigned_in_block_before_read = 7
        print reassigned_in_block_before_read
        print read_in_block_only
        print param_read_in_block
        # End of extraction segment

        print local1
        print read_after_block
        reassigned_after_block = 7
        print reassigned_after_block
        print not_used_in_block
        print param_read_after_block

