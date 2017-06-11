package com.isswhu;

import java.util.ArrayList;

/**
 * Created by WeiZehao on 17/5/27.
 * 本类用于定义Token
 */

//enum Type
//{
//    STRING,
//
//    L_BRACKET,
//    R_BRACKET,
//    RESERVED,
//    ERROR,
//    EOF
//}

/**
 * 将多个枚举类型归为一个大类，用于判断某个枚举类型是否属于该大类
 * @param <E>
 */
class IdSet<E>
{
    private ArrayList<E> _list;

    public IdSet()
    {
        _list = new ArrayList();
    }

    public void add(E...es)
    {
        for (E e : es)
        {
            _list.add(e);
        }
    }

    public boolean isTypeContain(E e)
    {
        return _list.contains(e);
    }
}

public class Token
{
    private int _col;
    private int _row;
    private String _type;       // 终结符类型
    private String _content;    // 终结符实际内容

    public Token(String type, String content, int row, int col)
    {
        _type = type;
        _content = content;
        _row = row;
        _col = col;
    }

    public String getContent()
    {
        return _content;
    }

    public String getType()
    {
        return _type;
    }

    public int getCol()
    {
        return _col;
    }

    public int getRow()
    {
        return _row;
    }
}
