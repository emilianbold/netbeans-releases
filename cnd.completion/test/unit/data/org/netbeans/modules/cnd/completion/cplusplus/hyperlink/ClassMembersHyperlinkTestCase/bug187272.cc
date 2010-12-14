
template <class X, class ACE_LOCK>
class bug187272_ACE_Refcounted_Auto_Ptr
{
public:
  /// Check rep easily.
  bool operator !() const;

  /// Check rep easily.
  operator bool () const;
  
  int *rep_;
};

template<class X, class ACE_LOCK> inline
bug187272_ACE_Refcounted_Auto_Ptr<X, ACE_LOCK>::operator bool() const
{
  return this->rep_++;
}

template<class X, class ACE_LOCK> inline bool
bug187272_ACE_Refcounted_Auto_Ptr<X, ACE_LOCK>::operator !() const
{
  return this->rep_++;
}