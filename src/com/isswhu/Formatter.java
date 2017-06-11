package com.isswhu;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by WeiZehao on 17/6/10.
 * 本类用于格式化
 */
public class Formatter
{
    private Token _currToken;
    private LexAnalyser _lex;
    private FileWriter _writer;

    public Formatter(String inPath, String outPath)
    {
        _lex = new LexAnalyser(inPath);
        try
        {
            _writer = new FileWriter(outPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void startFormat()
    {
        getToken();

        try
        {
            while(!_currToken.getType().equals("EOF"))
            {
                JSON();
            }
            _writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void getToken()
    {
        _currToken = _lex.getToken();

        if(_currToken.getType().equals("EOF"))
        {
            System.out.println("Complete Formatting : Succeed!");
        }
    }

    private void match(String type)
    {
        if(_currToken.getType().equals(type))
        {
            getToken();
        }
    }

    private void writeFormat(boolean newline, int tier) throws IOException
    {
        if(newline)
        {
            _writer.write("\n");
            for(int i = 0; i < tier; i++)
                _writer.write("\t");
        }
    }

    private void JSON() throws IOException
    {
        if(_currToken.getType().equals("{"))
            OBJECT(false,0);
        else if(_currToken.getType().equals("["))
            ARRAY(false, 0);
    }

    private void OBJECT(boolean newline, int tier) throws IOException
    {
        if(_currToken.getType().equals("{"))
        {
            writeFormat(newline, tier);
            _writer.write("{");
            match("{");
            OBJECT_2(tier);
        }
    }

    private void ARRAY(boolean newline, int tier) throws IOException
    {
        if(_currToken.getType().equals("["))
        {
            writeFormat(newline, tier);
            _writer.write("[");
            match("[");
            ARRAY_2(tier);
        }
    }

    private void ARRAY_2(int tier) throws IOException
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[") || _currToken.getType().equals("string")
                || _currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific") || _currToken.getType().equals("true")
                || _currToken.getType().equals("false") || _currToken.getType().equals("null"))
        {
            ELEMENTS(tier + 1);

            writeFormat(true, tier);
            _writer.write("]");
            match("]");
        }
        else if(_currToken.getType().equals("]"))
        {
            writeFormat(true, tier);
            _writer.write("]");
            match("]");
        }
    }

    private void OBJECT_2(int tier) throws IOException
    {
        if(_currToken.getType().equals("}"))
        {
            writeFormat(true, tier);
            _writer.write("}");
            match("}");
        }
        else if(_currToken.getType().equals("string"))
        {
            MEMBERS(tier + 1);

            writeFormat(true, tier);
            _writer.write("}");
            match("}");
        }
    }

    private void MEMBERS(int tier) throws IOException
    {
        if(_currToken.getType().equals("string"))
        {
            PAIR(tier);
            PAIR_2(tier);
        }
    }

    private void PAIR(int tier) throws IOException
    {
        if(_currToken.getType().equals("string"))
        {
            writeFormat(true, tier);
            _writer.write("\""+_currToken.getContent()+"\"");
            match("string");
            _writer.write(": ");
            match(":");
            VALUE(false, tier);
        }
    }

    private void PAIR_2(int tier) throws IOException
    {
        if(_currToken.getType().equals("}"))
            ;
        else if(_currToken.getType().equals(","))
        {
            _writer.write(",");
            match(",");
            MEMBERS(tier);
        }
    }

    private void ELEMENTS(int tier) throws IOException
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[")
                || _currToken.getType().equals("string") || _currToken.getType().equals("integer")
                || _currToken.getType().equals("float") || _currToken.getType().equals("scientific")
                || _currToken.getType().equals("true") || _currToken.getType().equals("false")
                || _currToken.getType().equals("null"))
        {
            VALUE(true, tier);
            ELEMENTS_2(tier);
        }
    }

    private void ELEMENTS_2(int tier) throws IOException
    {
        if(_currToken.getType().equals("]"))
            ;
        else if(_currToken.getType().equals(","))
        {
            _writer.write(",");
            match(",");
            ELEMENTS(tier);
        }
    }

    private void VALUE(boolean newline, int tier) throws IOException
    {
        if(_currToken.getType().equals("{"))
            OBJECT(newline, tier);
        else if(_currToken.getType().equals("["))
            ARRAY(newline, tier);
        else if(_currToken.getType().equals("string"))
        {
            writeFormat(newline, tier);
            _writer.write("\""+_currToken.getContent()+"\"");
            match("string");
        }
        else if(_currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific"))
            NUMBER(newline, tier);
        else if(_currToken.getType().equals("true"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("true");
        }
        else if(_currToken.getType().equals("false"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("false");
        }
        else if(_currToken.getType().equals("null"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("null");
        }
    }

    private void NUMBER(boolean newline, int tier) throws IOException
    {
        if(_currToken.getType().equals("integer"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("integer");
        }
        else if(_currToken.getType().equals("float"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("float");
        }
        else if(_currToken.getType().equals("scientific"))
        {
            writeFormat(newline, tier);
            _writer.write(_currToken.getContent());
            match("scientific");
        }
    }
}
