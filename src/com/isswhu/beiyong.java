package com.isswhu;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by WeiZehao on 17/6/10.
 * 本类用于查找
 */
public class beiyong
{
    private Token _currToken;
    private LexAnalyser _lex;
    private ArrayList<PathWord> _pathWords;
    private String _searchPath;

    public beiyong(String inPath, String searchPath)
    {
        _lex = new LexAnalyser(inPath);
        _pathWords = new ArrayList<>();
        _searchPath = searchPath;
    }

    public void search()
    {
        String[] words = _searchPath.split("/");
        Pattern p1 = Pattern.compile("(?<=\\[).*(?=\\])");
        Pattern p2 = Pattern.compile(".*(?=\\[)");
        Matcher m1, m2;
        for(String word : words)
        {
            if(word.equals(""))
                continue;
            m1 = p1.matcher(word);
            if(m1.find())
            {
                m2 = p2.matcher(word);
                m2.find();
                _pathWords.add(new PathWord("array", m2.group(0), Integer.parseInt(m1.group(0))));
            }
            else
            {
                _pathWords.add(new PathWord("other", word, 0));
            }
        }

        getToken();

        try
        {
            while(!_currToken.getType().equals("EOF"))
            {
                JSON();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getToken()
    {
        _currToken = _lex.getToken();

        if(_currToken.getType().equals("EOF"))
        {
            System.out.println("Complete Search : Succeed!");
        }
    }

    private void match(String type)
    {
        if(_currToken.getType().equals(type))
        {
            getToken();
        }
    }

    private void JSON()
    {
        if(_currToken.getType().equals("{"))
            OBJECT(0, _pathWords);
        else if(_currToken.getType().equals("["))
            ARRAY(0, _pathWords, _pathWords.get(0).getIndex());
    }

    private void OBJECT(int index, ArrayList<PathWord> pathWords)
    {
        if(_currToken.getType().equals("{"))
        {
            match("{");
            OBJECT_2(index, pathWords);
        }
    }

    private void ARRAY(int index, ArrayList<PathWord> pathWords, int arrayIndex)
    {
        if(_currToken.getType().equals("["))
        {
            match("[");
            ARRAY_2(index, pathWords, arrayIndex);
        }
    }

    private void ARRAY_2(int index, ArrayList<PathWord> pathWords, int arrayIndex)
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[") || _currToken.getType().equals("string")
                || _currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific") || _currToken.getType().equals("true")
                || _currToken.getType().equals("false") || _currToken.getType().equals("null"))
        {
            ELEMENTS(index, pathWords, arrayIndex, 1);//todo There is a problem!

            match("]");
        }
        else if(_currToken.getType().equals("]"))
        {
            match("]");
        }
    }

    private void OBJECT_2(int index, ArrayList<PathWord> pathWords)
    {
        if(_currToken.getType().equals("}"))
        {
            match("}");
        }
        else if(_currToken.getType().equals("string"))
        {
            MEMBERS(index, pathWords);

            match("}");
        }
    }

    private void MEMBERS(int index, ArrayList<PathWord> pathWords)
    {
        if(_currToken.getType().equals("string"))
        {
            PAIR(index, pathWords);
            PAIR_2(index, pathWords);
        }
    }

    private void PAIR(int index, ArrayList<PathWord> pathWords)
    {
        if(_currToken.getType().equals("string"))
        {
            if(pathWords.get(index).getKey().equals(_currToken.getContent()))
            {
                match("string");
                match(":");
                if(index != pathWords.size() - 1)
                {
                    if(pathWords.get(index).getType().equals("array"))
                    {
                        VALUE(index, pathWords, false);
                    }
                    else
                        VALUE(index + 1, pathWords, false);
                }
                else
                {
                    VALUE(index, pathWords, true);
                }
            }
            else
            {
                match("string");
                match(":");
                VALUE(index, pathWords, false);
            }
        }
    }

    private void PAIR_2(int index, ArrayList<PathWord> pathWords)
    {
        if(_currToken.getType().equals("}"))
            ;
        else if(_currToken.getType().equals(","))
        {
            match(",");
            MEMBERS(index, pathWords);
        }
    }

    private void ELEMENTS(int index, ArrayList<PathWord> pathWords, int arrayIndex, int times)
    {
        if(_currToken.getType().equals("{") || _currToken.getType().equals("[")
                || _currToken.getType().equals("string") || _currToken.getType().equals("integer")
                || _currToken.getType().equals("float") || _currToken.getType().equals("scientific")
                || _currToken.getType().equals("true") || _currToken.getType().equals("false")
                || _currToken.getType().equals("null"))
        {
            if(times == arrayIndex)
            {
                if(index != pathWords.size() - 1)
                {
                    VALUE(index + 1, pathWords, false);
                }
                else
                {
                    VALUE(index, pathWords, true);
                }
                ELEMENTS_2(index, pathWords, arrayIndex, times + 1);
            }
            else
            {
                VALUE(index, pathWords, false);
                ELEMENTS_2(index, pathWords, arrayIndex, times + 1);
            }
        }
    }

    private void ELEMENTS_2(int index, ArrayList<PathWord> pathWords, int arrayIndex, int times)
    {
        if(_currToken.getType().equals("]"))
            ;
        else if(_currToken.getType().equals(","))
        {
            match(",");
            ELEMENTS(index, pathWords, arrayIndex, times);
        }
    }

    private void VALUE(int index, ArrayList<PathWord> pathWords, boolean isTarget)
    {
        if(isTarget)
        {
            if(_currToken.getType().equals("{"))
                ;//OBJECT();
            else if(_currToken.getType().equals("["))
                ;//ARRAY();
            else if(_currToken.getType().equals("string"))
            {
                System.out.println(_currToken.getType() + " : " + _currToken.getContent());
                match("string");
            }
            else if(_currToken.getType().equals("integer") || _currToken.getType().equals("float")
                    || _currToken.getType().equals("scientific"))
                NUMBER(true);
            else if(_currToken.getType().equals("true"))
            {
                System.out.println(_currToken.getType() + " : " + _currToken.getContent());
                match("true");
            }
            else if(_currToken.getType().equals("false"))
            {
                System.out.println(_currToken.getType() + " : " + _currToken.getContent());
                match("false");
            }
            else if(_currToken.getType().equals("null"))
            {
                System.out.println(_currToken.getType() + " : " + _currToken.getContent());
                match("null");
            }
        }
        else
        {
            if(_currToken.getType().equals("{"))
                OBJECT(index, pathWords);
            else if(_currToken.getType().equals("["))
                ARRAY(index, pathWords, pathWords.get(index).getIndex());
            else if(_currToken.getType().equals("string"))
            {
                match("string");
            }
            else if(_currToken.getType().equals("integer") || _currToken.getType().equals("float")
                    || _currToken.getType().equals("scientific"))
                NUMBER(false);
            else if(_currToken.getType().equals("true"))
            {
                match("true");
            }
            else if(_currToken.getType().equals("false"))
            {
                match("false");
            }
            else if(_currToken.getType().equals("null"))
            {
                match("null");
            }
        }
    }

    private void NUMBER(boolean isTarget)
    {
        if(isTarget)
        {
            System.out.println(_currToken.getType() + " : " + _currToken.getContent());
        }
        else
        {
            if(_currToken.getType().equals("integer"))
            {
                match("integer");
            }
            else if(_currToken.getType().equals("float"))
            {
                match("float");
            }
            else if(_currToken.getType().equals("scientific"))
            {
                match("scientific");
            }
        }
    }
}

//class PathWord
//{
//    private String _type;
//    private String _content;
//    private int _index;
//
//    public PathWord(String type, String content, int index)
//    {
//        _type = type;
//        _content = content;
//        _index = index;
//    }
//
//    public String getType()
//    {
//        return _type;
//    }
//
//    public int getIndex()
//    {
//        return _index;
//    }
//
//    public String getContent()
//    {
//        return _content;
//    }
//}