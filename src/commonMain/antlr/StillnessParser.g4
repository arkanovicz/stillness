parser grammar StillnessParser;

options { tokenVocab = StillnessLexer; }

// Entry point
template: templatePart* EOF ;

templatePart:
    text                    # TextPart
  | simpleReference         # SimpleRefPart
  | complexReference        # ComplexRefPart
  | directive               # DirectivePart
  ;

// Text content (to be matched literally)
text: TEXT ;

// Simple reference: $foo
simpleReference: DOLLAR REF_LABEL ;

// Complex reference: ${foo.bar} or ${foo.bar|default}
complexReference: DOLLAR REF_LCURL expression (EXPR_PIPE expression)? EXPR_RCURL ;

// Expression (used in ${...} and #if, #set)
expression:
    primary                                           # PrimaryExpr
  | expression EXPR_DOT EXPR_LABEL                    # PropertyExpr
  | expression EXPR_DOT EXPR_LABEL EXPR_LPAREN argumentList? EXPR_RPAREN  # MethodCallExpr
  | expression EXPR_LBRACK expression EXPR_RBRACK    # IndexExpr
  | EXPR_NOT expression                               # NotExpr
  | expression (EXPR_MULT | EXPR_DIV | EXPR_MOD) expression  # MultExpr
  | expression (EXPR_PLUS | EXPR_MINUS) expression   # AddExpr
  | expression (EXPR_LT | EXPR_LE | EXPR_GT | EXPR_GE) expression  # CompareExpr
  | expression (EXPR_EQ | EXPR_NE) expression        # EqualityExpr
  | expression EXPR_AND expression                    # AndExpr
  | expression EXPR_OR expression                     # OrExpr
  | expression EXPR_TERNARY_Q expression EXPR_TERNARY_C expression  # TernaryExpr
  ;

primary:
    EXPR_LABEL                    # IdentifierPrimary
  | EXPR_INT                      # IntPrimary
  | EXPR_FLOAT                    # FloatPrimary
  | EXPR_STRING                   # StringPrimary
  | EXPR_LPAREN expression EXPR_RPAREN  # ParenPrimary
  ;

argumentList: expression (EXPR_COMMA expression)* ;

// Directives
directive:
    ifDirective
  | foreachDirective
  | setDirective
  | matchDirective
  | regexDirective
  | optionalDirective
  | followDirective
  | includeDirective
  | parseDirective
  | macroDefDirective
  | defineDirective
  | macroCallDirective
  | endDirective
  | elseDirective
  | elseifDirective
  ;

ifDirective: HASH DIR_IF COND_LPAREN expression COND_RPAREN ;

elseifDirective: HASH DIR_ELSEIF COND_LPAREN expression COND_RPAREN ;

elseDirective: HASH DIR_ELSE ;

endDirective: HASH DIR_END ;

foreachDirective:
    HASH DIR_FOREACH FE_LPAREN FE_DOLLAR loopVar=FE_LABEL FE_IN FE_DOLLAR listRef FE_RPAREN ;

listRef: FE_LABEL (FE_DOT FE_LABEL)* ;

setDirective:
    HASH DIR_SET SET_LPAREN SET_DOLLAR setTarget SET_EQ expression EXPR_RCURL ;

setTarget: SET_LABEL (SET_DOT SET_LABEL)* ;

matchDirective:
    HASH DIR_MATCH MATCH_LPAREN matchContent MATCH_RPAREN ;

matchContent: MATCH_STRING | MATCH_DOLLAR MATCH_LABEL (MATCH_DOT MATCH_LABEL)* ;

regexDirective:
    HASH DIR_REGEX REGEX_LPAREN REGEX_STRING (REGEX_COMMA regexCapture)* REGEX_RPAREN ;

regexCapture: REGEX_DOLLAR REGEX_LABEL (REGEX_DOT REGEX_LABEL)* ;

optionalDirective:
    HASH DIR_OPTIONAL MATCH_LPAREN matchContent MATCH_RPAREN ;

followDirective:
    HASH DIR_FOLLOW FOLLOW_LPAREN followUrl FOLLOW_RPAREN ;

followUrl: FOLLOW_STRING | FOLLOW_DOLLAR FOLLOW_LABEL (FOLLOW_DOT FOLLOW_LABEL)* ;

includeDirective: HASH DIR_INCLUDE INC_LPAREN INC_STRING INC_RPAREN ;

parseDirective: HASH DIR_PARSE INC_LPAREN INC_STRING INC_RPAREN ;

macroDefDirective:
    HASH DIR_MACRO MACRO_LPAREN macroName=MACRO_LABEL (MACRO_DOLLAR MACRO_LABEL)* MACRO_RPAREN ;

defineDirective:
    HASH DIR_DEFINE DEF_LPAREN DEF_DOLLAR DEF_LABEL (DEF_DOT DEF_LABEL)* DEF_RPAREN ;

macroCallDirective: HASH DIR_LABEL ;
