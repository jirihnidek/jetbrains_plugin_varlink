package org.intellij.sdk.language.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import static org.intellij.sdk.language.psi.VarlinkTypes.*;

%%

%public
%class VarlinkLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

// A comment runs from '#' to the end of the line.
COMMENT="#"[^\r\n]*

// White space, including new lines.
WHITE_SPACE=[ \t\r\n]+

// interface_name : reverse-domain name (lowercase, dots, hyphens, digits).
// name           : CamelCase identifier starting with an upper-case letter.
// field_name     : identifier that may contain underscores.
// A single, permissive IDENTIFIER token covers all three; the parser gives
// each occurrence its meaning by position.
IDENTIFIER=[A-Za-z][A-Za-z0-9_.\-]*

%%

<YYINITIAL> {
  {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
  {COMMENT}           { return COMMENT; }

  // Keywords.
  "interface"         { return INTERFACE_KEYWORD; }
  "type"              { return TYPE_KEYWORD; }
  "method"            { return METHOD_KEYWORD; }
  "error"             { return ERROR_KEYWORD; }

  // Basic (built-in) types.
  "bool"              { return BOOL_KEYWORD; }
  "int"               { return INT_KEYWORD; }
  "float"             { return FLOAT_KEYWORD; }
  "string"            { return STRING_KEYWORD; }
  "object"            { return OBJECT_KEYWORD; }
  "any"               { return ANY_KEYWORD; }

  // Operators and punctuation. Multi-character tokens must come before the
  // single-character ones that would otherwise match a prefix.
  "->"                { return ARROW; }
  "[string]"          { return MAP; }
  "[]"                { return ARRAY; }
  "("                 { return LPAREN; }
  ")"                 { return RPAREN; }
  ","                 { return COMMA; }
  ":"                 { return COLON; }
  "?"                 { return QUESTION; }

  {IDENTIFIER}        { return IDENTIFIER; }
}

[^]                   { return TokenType.BAD_CHARACTER; }
