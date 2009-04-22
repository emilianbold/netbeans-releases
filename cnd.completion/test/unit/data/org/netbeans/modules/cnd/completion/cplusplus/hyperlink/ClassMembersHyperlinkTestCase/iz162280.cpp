#define DEFINE_CALLBACK( parent, action ) \
class Cmd##action: public CmdGeneric \
{ \
    public: \
        virtual ~Cmd##action() {} \
        virtual void execute(); \
    private: \
        parent *m_pParent; \
\
} m_cmd##action; \
friend class Cmd##action;


/// Base class for skins commands
class CmdGeneric
{
public:
    virtual void execute() = 0;
};

class OSGraphics;

class Tooltip
{
    private:
        /// Image of the tooltip
        OSGraphics *m_pImage;

        /// Callback to show the tooltip window
        DEFINE_CALLBACK( Tooltip, Show );
};

void Tooltip::CmdShow::execute()
{
    if( m_pParent->m_pImage )
    {
    }
}
