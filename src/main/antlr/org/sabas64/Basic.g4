grammar Basic;

program: line* EOF;

line: lineNumber=intLiteral? statements+=statement? (':' statements+=statement?)* '\n';

statement
    : simpleStatement #SimpleStatementWrapper
    | 'goto' jumpTarget #GotoStatement
    | 'return' #ReturnStatement
    | 'end' #EndStatement
    | COMMENT #Comment
    | 'if' condition=expression ('then' statements+=statement? (':' statement?)* | ('then' | 'goto') jumpTarget) #IfStatement
    | 'on' value=expression jump=('goto'|'gosub') targets+=jumpTarget (',' targets+=jumpTarget)* #OnStatement
    | 'for' identifier '=' from=expression 'to' to=expression ('step' step=expression)? #ForStatement
    | 'next' identifier? #NextStatement
    | 'data' items+=dataItem (',' items+=dataItem)* #DataStatement
    ;

simpleStatement
    : 'let'? lValue '=' expression #AssignmentStatement
    | 'gosub' jumpTarget #GosubStatement
    | 'print' arguments+=printArgument* #PrintStatement
    | 'poke' target=expression ',' value=expression #PokeStatement
    | 'def' 'fn' name=identifier '(' params+=identifier (',' params+=identifier)* ')' '=' body=expression #DefStatement
    | 'read' targets+=lValue (',' targets+=lValue)* #ReadStatement
    | 'input' (STRING ';')? targets+=lValue (',' targets+=lValue)* #InputStatement
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
    | operator=('+'|'-') operand=expression #PrefixExpression
    | lhs=expression operator=('*'|'/') rhs=expression #InfixExpression
    | lhs=expression operator=('+'|'-') rhs=expression #InfixExpression
    | lhs=expression operator=('<'|'<='|'>'|'>='|'<>'|'=') rhs=expression #InfixExpression
    | operator='not' operand=expression #PrefixExpression
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

identifier: letters+=LETTER letters+=(LETTER | DIGIT)* ('%'|'$')?;

jumpTarget: intLiteral?;

intLiteral: DIGIT+;

floatLiteral: DIGIT* '.' DIGIT*;

DOLLAR: '$';
PERCENT: '%';

BUILT_IN_FUNCTION
    : 'abs' | 'asc' | 'atn' | 'chr$' | 'cos' | 'exp' | 'fre' | 'int' | 'left$' | 'len' | 'log' | 'mid$' | 'peek'
    | 'pos' | 'right$' | 'rnd' | 'sgn' | 'sin' | 'spc' | 'sqr' | 'str$' | 'tab' | 'tan' | 'usr' | 'val'
    ;

LETTER: [a-z];
DIGIT: [0-9];
STRING: '"' ~'"'* '"';
COMMENT: 'rem' ~'\n'*;

WS: [ \t\r] -> channel(HIDDEN);

INVALID: .;
