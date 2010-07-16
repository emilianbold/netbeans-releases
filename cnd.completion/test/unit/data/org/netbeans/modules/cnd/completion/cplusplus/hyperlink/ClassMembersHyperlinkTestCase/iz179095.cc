typedef unsigned int	size_t;

struct ACE_Allocator {
};

struct ACE_Data_Block {
};

struct ACE_Lock {
};

struct ACE_Time_Value {
    static const int zero = 0;
    static const int max_time = 10;
};

#define ACE_DEFAULT_MESSAGE_BLOCK_PRIORITY 1
#define MB_DATA 1
#define ACE_DEFAULT_MESSAGE_BLOCK_PRIORITY 1

class ACE_Message_Block {
public:
    typedef int ACE_Message_Type;
    typedef unsigned long Message_Flags;

    ACE_Message_Block(ACE_Allocator *message_block_allocator = 0);

    ACE_Message_Block(ACE_Data_Block *,
            Message_Flags *flags = 0,
            ACE_Allocator *message_block_allocator = 0);

    ACE_Message_Block(size_t size,
            ACE_Message_Type type = MB_DATA,
            ACE_Message_Block *cont = 0,
            const char *data = 0,
            ACE_Allocator *allocator_strategy = 0,
            ACE_Lock *locking_strategy = 0,
            unsigned long priority = ACE_DEFAULT_MESSAGE_BLOCK_PRIORITY,
            const ACE_Time_Value &execution_time = ACE_Time_Value::zero,
            const ACE_Time_Value &deadline_time = ACE_Time_Value::max_time,
            ACE_Allocator *data_block_allocator = 0,
            ACE_Allocator *message_block_allocator = 0);

    ACE_Message_Block(const char *data,
            size_t size = 0,
            unsigned long priority = ACE_DEFAULT_MESSAGE_BLOCK_PRIORITY);

    ACE_Message_Block(const ACE_Message_Block &mb,
            size_t align);

    ACE_Message_Block(size_t size,
            ACE_Message_Type msg_type,
            ACE_Message_Block *msg_cont,
            const char *msg_data,
            ACE_Allocator *allocator_strategy,
            ACE_Lock *locking_strategy,
            Message_Flags flags,
            unsigned long priority,
            const ACE_Time_Value &execution_time,
            const ACE_Time_Value &deadline_time,
            ACE_Data_Block *db,
            ACE_Allocator *data_block_allocator,
            ACE_Allocator *message_block_allocator);

};

ACE_Message_Block::ACE_Message_Block(const char *data,
        size_t size,
        unsigned long priority) {
}

ACE_Message_Block::ACE_Message_Block(ACE_Allocator *message_block_allocator) {
}

ACE_Message_Block::ACE_Message_Block(size_t size,
        ACE_Message_Type msg_type,
        ACE_Message_Block *msg_cont,
        const char *msg_data,
        ACE_Allocator *allocator_strategy,
        ACE_Lock *locking_strategy,
        unsigned long priority,
        ACE_Time_Value &execution_time,
        ACE_Time_Value &deadline_time,
        ACE_Allocator *data_block_allocator,
        ACE_Allocator *message_block_allocator
        ) {
}

ACE_Message_Block::ACE_Message_Block(size_t size,
        ACE_Message_Type msg_type,
        ACE_Message_Block *msg_cont,
        const char *msg_data,
        ACE_Allocator *allocator_strategy,
        ACE_Lock *locking_strategy,
        Message_Flags flags,
        unsigned long priority,
        const ACE_Time_Value &execution_time,
        const ACE_Time_Value &deadline_time,
        ACE_Data_Block *db,
        ACE_Allocator *data_block_allocator,
        ACE_Allocator *message_block_allocator) {
}

ACE_Message_Block::ACE_Message_Block(ACE_Data_Block *data_block,
        ACE_Message_Block::Message_Flags* flags,
        ACE_Allocator *message_block_allocator) {
}

ACE_Message_Block::ACE_Message_Block(const ACE_Message_Block &mb,
        size_t align) {
}
