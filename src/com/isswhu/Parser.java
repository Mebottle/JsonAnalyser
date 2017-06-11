package com.isswhu;

/**
 * Created by WeiZehao on 17/6/8.
 * 本类用于语法分析
 */

public class Parser
{
    private Token _currToken;
    private LexAnalyser _lex;

    public Parser(String path)
    {
        _lex = new LexAnalyser(path);
    }

    public void startAnalyse() throws Error
    {
        getToken();

        while(!_currToken.getType().equals("EOF"))
        {
            JSON();
        }
    }

    private void getToken() throws LexError
    {
        _currToken = _lex.getToken();
        if(_currToken.getType().equals("ERROR"))
        {

            System.out.printf("line %d, position %d : %s\n",
                    _currToken.getRow(), _currToken.getCol(), _currToken.getContent());
            treatWithLexError();
        }
        else if(_currToken.getType().equals("EOF"))
        {
            System.out.println("Complete Analysis : Succeed!");
        }
    }

    private void treatWithLexError() throws LexError
    {
        Token token;
        while(true)
        {
            token = _lex.getToken();
            if(token.getType().equals("EOF"))
                break;
            if(token.getType().equals("ERROR"))
            {
                System.out.printf("line %d, position %d : %s\n",token.getRow(),token.getCol(),token.getContent());
            }
        }
        _lex.close();
        throw new LexError();
    }

    private void match(String type) throws Error
    {
        if(_currToken.getType().equals(type))
        {
            getToken();
        }
        else
            error();
    }

    private void JSON() throws Error
    {
        if(_currToken.getType().equals("{"))
            OBJECT();
        else if(_currToken.getType().equals("["))
            ARRAY();
        else
            error();
    }

    private void OBJECT() throws Error
    {
        if(_currToken.getType().equals("{"))
        {
            match("{");
            OBJECT_2();
        }
        else
            error();
    }

    private void ARRAY() throws Error
    {
        if(_currToken.getType().equals("["))
        {
            match("[");
            ARRAY_2();
        }
        else
            error();
    }

    private void ARRAY_2() throws Error
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[") || _currToken.getType().equals("string")
                || _currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific") || _currToken.getType().equals("true")
                || _currToken.getType().equals("false") || _currToken.getType().equals("null"))
        {
            ELEMENTS();
            match("]");
        }
        else if(_currToken.getType().equals("]"))
            match("]");
        else
            error();
    }

    private void OBJECT_2() throws Error
    {
        if(_currToken.getType().equals("}"))
            match("}");
        else if(_currToken.getType().equals("string"))
        {
            MEMBERS();
            match("}");
        }
        else
            error();
    }

    private void MEMBERS() throws Error
    {
        if(_currToken.getType().equals("string"))
        {
            PAIR();
            PAIR_2();
        }
        else
            error();
    }

    private void PAIR() throws Error
    {
        if(_currToken.getType().equals("string"))
        {
            match("string");
            match(":");
            VALUE();
        }
        else
            error();
    }

    private void PAIR_2() throws Error
    {
        if(_currToken.getType().equals("}"))
            ;
        else if(_currToken.getType().equals(","))
        {
            match(",");
            MEMBERS();
        }
        else
            error();
    }

    private void ELEMENTS() throws Error
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[")
                || _currToken.getType().equals("string") || _currToken.getType().equals("integer")
                || _currToken.getType().equals("float") || _currToken.getType().equals("scientific")
                || _currToken.getType().equals("true") || _currToken.getType().equals("false")
                || _currToken.getType().equals("null"))
        {
            VALUE();
            ELEMENTS_2();
        }
        else
            error();
    }

    private void ELEMENTS_2() throws Error
    {
        if(_currToken.getType().equals("]"))
            ;
        else if(_currToken.getType().equals(","))
        {
            match(",");
            ELEMENTS();
        }
        else
            error();
    }

    private void VALUE() throws Error
    {
        if(_currToken.getType().equals("{"))
            OBJECT();
        else if(_currToken.getType().equals("["))
            ARRAY();
        else if(_currToken.getType().equals("string"))
            match("string");
        else if(_currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific"))
            NUMBER();
        else if(_currToken.getType().equals("true"))
            match("true");
        else if(_currToken.getType().equals("false"))
            match("false");
        else if(_currToken.getType().equals("null"))
            match("null");
        else
            error();
    }

    private void NUMBER() throws Error
    {
        if(_currToken.getType().equals("integer"))
            match("integer");
        else if(_currToken.getType().equals("float"))
            match("float");
        else if(_currToken.getType().equals("scientific"))
            match("scientific");
        else
            error();
    }

    private void error() throws SyntaxError
    {
        System.out.printf("line %d, position %d : Syntax error<%s>\n",
                _currToken.getRow(), _currToken.getCol(), _currToken.getContent());
        throw new SyntaxError();
    }
}

/**
 * 错误类
 */
class Error extends Exception
{
    public Error()
    {
        super();
    }
}

/**
 * 语法错误类
 */
class SyntaxError extends Error
{
}

/**
 * 词法错误类
 */
class LexError extends Error
{
}

