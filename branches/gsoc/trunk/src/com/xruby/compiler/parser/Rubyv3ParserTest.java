package com.xruby.compiler.parser;

import java.lang.reflect.*;
import junit.framework.TestCase;
import org.antlr.runtime.*;

public class Rubyv3ParserTest  extends TestCase {
//	/ @param[in]e	Can be null
    private String build_error_string(String program_text, Exception exception) {
        assert(null != program_text);

        //Dump text and exception(if any)
        StringBuilder debug_message = new StringBuilder();
        debug_message.append("\"");
        debug_message.append(program_text);
        debug_message.append("\" ");
        if (null != exception) {
            debug_message.append(exception.toString());
        }

        //Dump token streams;
        SymbolTableManager stm = new SymbolTableManager(null);
        Rubyv3Lexer lexter = new Rubyv3Lexer(new ANTLRStringStream(program_text), stm, false);
        debug_message.append("\nToken stream is:");

        Token token = lexter.nextToken();
        while (Rubyv3Lexer.EOF != token.getType()) {
            debug_message.append(token.getText());
            debug_message.append("(");
            debug_message.append(token.getType());
            debug_message.append(")\t");

            token = lexter.nextToken();
        }

        debug_message.append("\n");

        return debug_message.toString();
    }

    private void invoke_method_with_reflection(String program_text, String method_name) throws RecognitionException {
        assert(null != program_text);
        assert(null != method_name);

        TokenStream stm = new Rubyv3Lexer(new ANTLRStringStream(program_text), null, false);
        Rubyv3Parser parser = new Rubyv3Parser(stm);
        Class<? extends Rubyv3Parser> c = parser.getClass();

        Method method = null;
        try {
            method = c.getMethod(method_name);
        } catch (NoSuchMethodException e) {
            assertTrue("No such method: " + method_name, false);
        }

        try {
            method.invoke(parser);
        } catch (IllegalAccessException e) {
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RecognitionException) {
                throw (RecognitionException)cause;
            } else {
                cause.printStackTrace();
                assertTrue(cause.toString(), false);
            }
        }

        //Ensure there are no more tokens left
        assertEquals(program_text + " There are tokens left: " + stm.LT(1).toString() + ", ",
                Rubyv3Lexer.EOF, stm.LT(1).getType());
    }

    private void parse(String[] program_texts, String method_name) {
        for (int i = 0; i < program_texts.length; ++i) {
            try {
                invoke_method_with_reflection(program_texts[i], method_name);
            } catch (RecognitionException e) {
                assertTrue(build_error_string(program_texts[i], e), false);
            }
        }
    }

    public void test_terminal() {
        String[] program_texts = {
            ";",
            //";;;;;;;;;;;",
            //"\n\n\n\n;;;;;;;;;;;",
            //"\n",
        };

        parse(program_texts, "terminal");
    }
    
    public void test_expression() {
        String[] program_texts = {
            "1+2",
            "1*2",
            "1/2",
            "1-2"
        };

        parse(program_texts, "expression");
    }
   
    
    public void test_andorExpression() {
        String[] program_texts = {
            "1 and 2",
        };

        parse(program_texts, "andorExpression");
    }
    
    public void test_notExpression() {
        String[] program_texts = {
            "not 2",
        };

        parse(program_texts, "notExpression");
    }
    
    
    public void test_ternaryIfThenElseExpression() {
        String[] program_texts = {
            "1 ? 2 : 3",
        };

        parse(program_texts, "ternaryIfThenElseExpression");
    }

    
    public void test_assignmentExpression() {
        String[] program_texts = {
            "1 += 2",
            //to do
            //ava.lang.NullPointerException
            //[junit]     at com.xruby.compiler.parser.Rubyv3Lexer.updateSymbolTable(Rubyv3Lexer.java:324)
            //"a+=2"
        };

        parse(program_texts, "assignmentExpression");
    }
    
    public void test_rangeExpression() {
        String[] program_texts = {
            "1..3",
            "3...5"
        };

        parse(program_texts, "rangeExpression");
    }
    
    public void test_logicalOrExpression() {
    	String[] program_texts = {
                "1||2",
            };

            parse(program_texts, "logicalOrExpression");
    }
    
    public void test_equalityExpression() {
    	String[] program_texts = {
                "1<=>2",
            };

            parse(program_texts, "equalityExpression");
    }
    
    public void test_relationalExpression() {
    	String[] program_texts = {
                "1<2",
            };

            parse(program_texts, "relationalExpression");
    }
    
    public void test_orExpression() {
    	String[] program_texts = {
                "1|2",
            };

            parse(program_texts, "orExpression");
    }
    
    public void test_andExpression() {
    	String[] program_texts = {
                "1&2",
            };

            parse(program_texts, "andExpression");
    }
    
    public void test_shiftExpression() {
    	String[] program_texts = {
                "1<<2",
            };

            parse(program_texts, "shiftExpression");
    }
    
    
    public void test_additiveExpression() {
    	String[] program_texts = {
                "1+19",
            };

            parse(program_texts, "additiveExpression");
    }
    
    public void test_multiplicativeExpression() {
    	String[] program_texts = {
                "1*19",
            };

            parse(program_texts, "multiplicativeExpression");
    }
    
    public void test_powerExpression() {
    	String[] program_texts = {
                "1**19",
            };

            parse(program_texts, "powerExpression");
    }
    
    public void test_bnotExpression() {
    	String[] program_texts = {
                "~1",
            };

            parse(program_texts, "bnotExpression");
    }
    
    public void test_command() {
    	String[] program_texts = {
                "nil",
            };

            parse(program_texts, "command");
    }
    
    public void test_unaryExpression() {
    	String[] program_texts = {
                "nil",
            };

            parse(program_texts, "unaryExpression");
    }
    
    public void test_predefinedValue() {
    	String[] program_texts = {
                "nil",
            };

            parse(program_texts, "predefinedValue");
    }
    
    public void test_numeric() {
    	String[] program_texts = {
                "1213",
            };

            parse(program_texts, "numeric");
    }
    
    public void test_primaryExpressionThatCanBeMethodName() {
    	String[] program_texts = {
                "[]",
            };

            parse(program_texts, "primaryExpressionThatCanBeMethodName");
    }
    
    public void test_primaryExpressionThatCanNotBeMethodName() {
    	String[] program_texts = {
                "false",
            };

            parse(program_texts, "primaryExpressionThatCanNotBeMethodName");
    }
    
    public void test_primaryExpression() {
    	String[] program_texts = {
                "false",
                "[]",
            };

            parse(program_texts, "primaryExpression");
    }
    
    public void test_regex() {
    	String[] program_texts = {
                "/fasdf/",
                "//",
            };

            parse(program_texts, "regex");
    }
    
    public void test_symbol() {
    	String[] program_texts = {
                ":if",
            };

            parse(program_texts, "symbol");
    }
    
    public void test_string() {
    	String[] program_texts = {
                "\"a\"",
                "\'b\'"
            };

            parse(program_texts, "string");
    }
    
    public void test_operatorAsMethodname() {
    	String[] program_texts = {
                "/",
                
            };

            parse(program_texts, "operatorAsMethodname");
    }
    
    public void test_keyword() {
    	String[] program_texts = {
                "nil",
                
            };

            parse(program_texts, "keyword");
    }
    
    public void test_keywordAsMethodName() {
    	String[] program_texts = {
                "if",
                
            };

            parse(program_texts, "keywordAsMethodName");
    }
    
    public void test_literal() {
    	String[] program_texts = {
                "/fasdf/",
                "//",
            };

            parse(program_texts, "literal");
    }
    
    public void test_keyValuePair() {
    	String[] program_texts = {
                "1=>2"
            };

            parse(program_texts, "keyValuePair");
    }
    
    
    public void test_arrayReferenceArgument() {
    	String[] program_texts = {
                "1,2,3"
            };

            parse(program_texts, "arrayReferenceArgument");
    }
    
//    public void test_arrayExpression() {
//    	String[] program_texts = {
//                "［］"
//            };
//
//            parse(program_texts, "arrayExpression");
//    }
}