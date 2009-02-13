template <class _Tp, class _Sequence>
class queue {
public:
  template <class _Tp1, class _Seq1>
  friend bool operator== (const queue<_Tp1, _Seq1>&,
                          const queue<_Tp1, _Seq1>&);
protected:
  int cc;
};

template <class _Tp, class _Sequence>
bool
operator==(const queue<_Tp, _Sequence>& __x, const queue<_Tp, _Sequence>& __y)
{
  return __x.cc == __y.cc;
}
