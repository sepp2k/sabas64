grammar Basic;

program: line* EOF;

line: lineNumber=intLiteral? statements+=statement? (':' statements+=statement?)* '\n';

statement
    : 'let'? lValue '=' expression #AssignmentStatement
    | 'print' arguments+=printArgument* #PrintStatement
    | 'goto' intLiteral? #GotoStatement
    | 'gosub' intLiteral? #GosubStatement
    | 'return' #ReturnStatement
    | 'poke' expression ',' expression #PokeStatement
    | COMMENT #Comment
    | 'def' 'fn' name=identifier '(' params+=identifier (',' params+=identifier)* ')' '=' body=expression #DefStatement
    | 'if' condition=expression ('then' statements+=statement? (':' statement?)* | ('then' | 'goto') intLiteral?) #IfStatement
    | 'for' identifier '=' start=expression 'to' to=expression ('step' step=expression)? #ForStatement
    | 'next' identifier? #NextStatement
    | 'data' items+=dataItem (',' items+=dataItem)* #DataStatement
    | 'read' targets+=lValue (',' targets+=lValue)* #ReadStatement
    | 'input' (STRING ',')? targets+=lValue (',' targets+=lValue)* #InputStatement
    | 'dim' identifier '(' dimensions+=expression (',' dimensions+=expression)* ')' #DimStatement
    ;

expression
    : identifier #VariableExpression
    | ('π' | '~') #PiExpression
    | (intLiteral | floatLiteral) #NumberExpression
    | STRING #StringExpression
    | identifier '(' indices+=expression (',' indices+=expression)* ')' #ArrayExpression
    | function '(' arguments+=expression (',' arguments+=expression)* ')' #FunctionCallExpression
    | lhs=expression operator='^' rhs=expression #InfixExpression
    | operator=('+'|'-') expression #PrefixExpression
    | lhs=expression operator=('*'|'/') rhs=expression #InfixExpression
    | lhs=expression operator=('+'|'-') rhs=expression #InfixExpression
    | lhs=expression operator=('<'|'<='|'>'|'>='|'<>'|'=') rhs=expression #InfixExpression
    | operator='not' expression #PrefixExpression
    | lhs=expression operator='and' rhs=expression #InfixExpression
    | lhs=expression operator='or' rhs=expression #InfixExpression
    ;

printArgument: expression terminator=(';' | ',')?;

dataItem: ~(',' | ':' | '\n')*;

function
    : 'fn' identifier #UserDefinedFunction
    | BUILT_IN_FUNCTION #BuiltInFunction
    ;

lValue
    : identifier #VariableLValue
    | identifier '(' indices+=expression (',' indices+=expression)* ')' #ArrayLValue
    ;

identifier: letters+=LETTER letters+=(LETTER | DIGIT)* sigil=('%'|'$')?;

intLiteral: DIGIT+;

floatLiteral: DIGIT* '.' DIGIT*;

DOLLAR: '$';
PERCENT: '%';

BUILT_IN_FUNCTION
    : 'peek' | 'sin' | 'cos' | 'tan' | 'cot' | 'si' | 'sqr'
    ;

LETTER: [a-z];
DIGIT: [0-9];
STRING: '"' ~'"'* '"';
COMMENT: 'rem' ~'\n'*;

WS: [ \t\r] -> channel(HIDDEN);
