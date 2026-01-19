lexer grammar StillnessLexer;

// Default mode: TEXT
TEXT: ~[#$]+ ;
DOLLAR: '$' -> pushMode(REFERENCE) ;
HASH: '#' -> pushMode(DIRECTIVE) ;

// Reference mode: $foo or ${foo.bar}
mode REFERENCE;
REF_LABEL: [a-zA-Z_][a-zA-Z0-9_]* -> popMode ;
REF_LCURL: '{' -> popMode, pushMode(EXPRESSION) ;

// Expression mode: ${...}
mode EXPRESSION;
EXPR_RCURL: '}' -> popMode ;
EXPR_DOT: '.' ;
EXPR_LBRACK: '[' ;
EXPR_RBRACK: ']' ;
EXPR_LPAREN: '(' ;
EXPR_RPAREN: ')' ;
EXPR_COMMA: ',' ;
EXPR_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
EXPR_INT: '-'? [0-9]+ ;
EXPR_FLOAT: '-'? [0-9]+ '.' [0-9]+ ;
EXPR_STRING: '"' (~["\\\r\n] | '\\' .)* '"'
           | '\'' (~['\\\r\n] | '\\' .)* '\'' ;
EXPR_PLUS: '+' ;
EXPR_MINUS: '-' ;
EXPR_MULT: '*' ;
EXPR_DIV: '/' ;
EXPR_MOD: '%' ;
EXPR_EQ: '==' ;
EXPR_NE: '!=' ;
EXPR_LT: '<' ;
EXPR_LE: '<=' ;
EXPR_GT: '>' ;
EXPR_GE: '>=' ;
EXPR_AND: '&&' ;
EXPR_OR: '||' ;
EXPR_NOT: '!' ;
EXPR_TERNARY_Q: '?' ;
EXPR_TERNARY_C: ':' ;
EXPR_PIPE: '|' ;  // For alternate values ${left|right}
EXPR_WS: [ \t\r\n]+ -> skip ;

// Directive mode: #directive(...)
mode DIRECTIVE;
DIR_IF: 'if' -> popMode, pushMode(DIR_CONDITION) ;
DIR_ELSEIF: 'elseif' -> popMode, pushMode(DIR_CONDITION) ;
DIR_ELSE: 'else' -> popMode ;
DIR_END: 'end' -> popMode ;
DIR_FOREACH: 'foreach' -> popMode, pushMode(DIR_FOREACH_ARGS) ;
DIR_SET: 'set' -> popMode, pushMode(DIR_SET_ARGS) ;
DIR_MATCH: 'match' -> popMode, pushMode(DIR_MATCH_ARGS) ;
DIR_REGEX: 'regex' -> popMode, pushMode(DIR_REGEX_ARGS) ;
DIR_OPTIONAL: 'optional' -> popMode, pushMode(DIR_MATCH_ARGS) ;
DIR_FOLLOW: 'follow' -> popMode, pushMode(DIR_FOLLOW_ARGS) ;
DIR_INCLUDE: 'include' -> popMode, pushMode(DIR_INCLUDE_ARGS) ;
DIR_PARSE: 'parse' -> popMode, pushMode(DIR_INCLUDE_ARGS) ;
DIR_MACRO: 'macro' -> popMode, pushMode(DIR_MACRO_ARGS) ;
DIR_DEFINE: 'define' -> popMode, pushMode(DIR_DEFINE_ARGS) ;
DIR_LABEL: [a-zA-Z_][a-zA-Z0-9_]* -> popMode ;  // Macro call
DIR_LINE_COMMENT: '##' ~[\r\n]* -> popMode, skip ;
DIR_BLOCK_COMMENT: '#*' .*? '*#' -> popMode, skip ;

// Condition mode: #if(expression)
mode DIR_CONDITION;
COND_LPAREN: '(' -> pushMode(EXPRESSION) ;
COND_RPAREN: ')' -> popMode ;
COND_WS: [ \t\r\n]+ -> skip ;

// Foreach mode: #foreach($item in $list)
mode DIR_FOREACH_ARGS;
FE_LPAREN: '(' ;
FE_RPAREN: ')' -> popMode ;
FE_DOLLAR: '$' ;
FE_IN: 'in' ;
FE_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
FE_DOT: '.' ;
FE_WS: [ \t\r\n]+ -> skip ;

// Set mode: #set($var = expression)
mode DIR_SET_ARGS;
SET_LPAREN: '(' -> pushMode(SET_INNER) ;
SET_RPAREN: ')' -> popMode ;
SET_WS: [ \t\r\n]+ -> skip ;

mode SET_INNER;
SET_DOLLAR: '$' ;
SET_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
SET_DOT: '.' ;
SET_EQ: '=' -> popMode, pushMode(EXPRESSION) ;
SET_I_WS: [ \t\r\n]+ -> skip ;

// Match mode: #match(string)
mode DIR_MATCH_ARGS;
MATCH_LPAREN: '(' ;
MATCH_RPAREN: ')' -> popMode ;
MATCH_STRING: '"' (~["\\\r\n] | '\\' .)* '"'
            | '\'' (~['\\\r\n] | '\\' .)* '\'' ;
MATCH_DOLLAR: '$' ;
MATCH_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
MATCH_DOT: '.' ;
MATCH_WS: [ \t\r\n]+ -> skip ;

// Regex mode: #regex(pattern, $capture1, $capture2, ...)
mode DIR_REGEX_ARGS;
REGEX_LPAREN: '(' ;
REGEX_RPAREN: ')' -> popMode ;
REGEX_STRING: '"' (~["\\\r\n] | '\\' .)* '"'
            | '\'' (~['\\\r\n] | '\\' .)* '\'' ;
REGEX_COMMA: ',' ;
REGEX_DOLLAR: '$' ;
REGEX_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
REGEX_DOT: '.' ;
REGEX_WS: [ \t\r\n]+ -> skip ;

// Follow mode: #follow(url)
mode DIR_FOLLOW_ARGS;
FOLLOW_LPAREN: '(' ;
FOLLOW_RPAREN: ')' -> popMode ;
FOLLOW_STRING: '"' (~["\\\r\n] | '\\' .)* '"'
             | '\'' (~['\\\r\n] | '\\' .)* '\'' ;
FOLLOW_DOLLAR: '$' ;
FOLLOW_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
FOLLOW_DOT: '.' ;
FOLLOW_WS: [ \t\r\n]+ -> skip ;

// Include/parse mode: #include('file')
mode DIR_INCLUDE_ARGS;
INC_LPAREN: '(' ;
INC_RPAREN: ')' -> popMode ;
INC_STRING: '"' (~["\\\r\n] | '\\' .)* '"'
          | '\'' (~['\\\r\n] | '\\' .)* '\'' ;
INC_WS: [ \t\r\n]+ -> skip ;

// Macro definition mode: #macro(name $arg1 $arg2)
mode DIR_MACRO_ARGS;
MACRO_LPAREN: '(' ;
MACRO_RPAREN: ')' -> popMode ;
MACRO_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
MACRO_DOLLAR: '$' ;
MACRO_WS: [ \t\r\n]+ -> skip ;

// Define mode: #define($var)
mode DIR_DEFINE_ARGS;
DEF_LPAREN: '(' ;
DEF_RPAREN: ')' -> popMode ;
DEF_DOLLAR: '$' ;
DEF_LABEL: [a-zA-Z_][a-zA-Z0-9_]* ;
DEF_DOT: '.' ;
DEF_WS: [ \t\r\n]+ -> skip ;
