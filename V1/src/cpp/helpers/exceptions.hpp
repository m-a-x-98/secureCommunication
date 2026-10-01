class PadExhaustedException : public std::runtime_error {
public:
    PadExhaustedException(char const* const message) throw();
    virtual char const* what() const throw();
};