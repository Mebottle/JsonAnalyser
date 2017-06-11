package com.isswhu;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by WeiZehao on 17/5/27.
 * 本类用于词法分析
 */

public class LexAnalyser
{
    private FileReader _reader;
    private char _candidate;
    private int _row = 0;
    private int _col = 0;
    private static final char EOF = (char)4;  // 控制字符EOT，表示传输结束

    public LexAnalyser(String path)
    {
        try
        {
            _reader = new FileReader(path);
            // 读第一个字符
            nextChar();
            _row++;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void close()
    {
        try
        {
            _reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取下一个字符
     */
    private void nextChar()
    {
        try
        {
            int num = _reader.read();
            _col++;
            if(num != -1)
            {
                _candidate = (char)num;
            }
            else
                _candidate = EOF;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取一个词法单元
     * 每次调用结束后，_candidate通过nextChar()方法，均指向下一个将被验证的字符，故直接进入判断
     * 每次调用结束后，返回读取的token，且此时读取指针已经指向下一个token的开头字符
     * @return token
     */
    public Token getToken()
    {
        int offset = 0;
        StringBuffer strb;
        while(true)
        {
            // 如果为空格或TAB键或回车符继续读下一个
            if(_candidate == ' ' || _candidate == '\t' || _candidate == '\r')
            {
                nextChar();
            }
            // 如果读到行尾，换行
            else if(_candidate == '\n')
            {
                nextChar();
                _row++;
                _col = 1;
            }
            else
                break;
        }// end while
        switch (_candidate)
        {
            case '{':
                nextChar();
                return new Token("{","{",_row,_col - 1);
            case '}':
                nextChar();
                return new Token("}","}",_row,_col - 1);
            case '[':
                nextChar();
                return new Token("[","[",_row,_col - 1);
            case ']':
                nextChar();
                return new Token("]","]",_row,_col - 1);
            case ',':
                nextChar();
                return new Token(",",",",_row,_col - 1);
            case ':':
                nextChar();
                return new Token(":", ":",_row,_col - 1);
            default:
                break;
        }
        // 识别"true" "false" "null"
        if(Character.isAlphabetic(_candidate))
        {
            strb = new StringBuffer();
            do{
                strb.append(_candidate);
                nextChar();
                offset++;
            }while(Character.isAlphabetic(_candidate));
            String str = strb.toString();
            if(str.equals("true")|str.equals("false")|str.equals("null"))
            {
                // 识别成功
                return new Token(str, str, _row, _col - offset);
            }
            else
            {
                int oldCol = _col;
                int oldRow = _row;
                String wrongChar = String.format("<%s>", str);
                treatWrongWord();
                return new Token("ERROR", "invalid word" + wrongChar , oldRow, oldCol - offset);
            }
        }
        // 识别String
        else if (_candidate == '\"')
        {
            strb = new StringBuffer();
            nextChar();
            offset++;
            // 一次循环识别一个字符
            while(_candidate != '\"' && !isControlCharacter(_candidate))
            {
                // 识别转义符
                if(_candidate == '\\')
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                    switch (_candidate)
                    {
                        // 识别形如'/n'的字符
                        case '\"':
                        case '\\':
                        case '/':
                        case 'b':
                        case 'f':
                        case 'n':
                        case 'r':
                        case 't':
                            strb.append(_candidate);
                            nextChar();
                            offset++;
                            break;
                        // 识别形如'/u0A9F'的字符
                        case 'u':
                            strb.append(_candidate);
                            nextChar();
                            offset++;
                            for (int i = 0; i < 4; i++)
                            {
                                if((_candidate >= 'A' && _candidate <= 'F')
                                        | (_candidate >= 'a' && _candidate <= 'f')
                                        | (_candidate >= '0' && _candidate <= '9'))
                                {
                                    strb.append(_candidate);
                                    nextChar();
                                    offset++;
                                }
                                else
                                {
                                    int oldCol = _col;
                                    int oldRow = _row;
                                    String wrongChar = String.format("<%s>", strb.toString());
                                    treatWrongWord();
                                    return new Token("ERROR", "invalid unicode character" + wrongChar, oldRow, oldCol - offset);
                                }
                            }
                        default:
                            int oldCol = _col;
                            int oldRow = _row;
                            treatWrongWord();
                            return new Token("ERROR", "invalid String, unexpected<\\>", oldRow, oldCol - offset);
                    }
                }
                // 识别普通字符
                else
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
            }
            if(isControlCharacter(_candidate))
            {
                int oldCol = _col;
                int oldRow = _row;
                String wrongChar = String.format("<%s>", String.valueOf(_candidate));
                treatWrongWord();
                return new Token("ERROR", "unexpected control character" + wrongChar, oldRow, oldCol - offset);
            }
            // 识别String成功
            else if (_candidate == '\"')
            {
                nextChar();
                offset++;
                return new Token("string", strb.toString(), _row, _col - offset);
            }
            // _candidate == EOF
            else
            {
                int oldCol = _col;
                int oldRow = _row;
                String wrongChar = String.format("<%s>", strb.toString());
                treatWrongWord();
                return new Token("ERROR", "invalid string" + wrongChar, oldRow, oldCol - offset);
            }
        }
        // 识别number
        else if(Character.isDigit(_candidate) | _candidate == '-')
        {
            strb = new StringBuffer();
            boolean hasDot = false;
            boolean hasE = false;

            /*
             * 识别'-' 或空
             */

            if(_candidate == '-')
            {
                strb.append(_candidate);
                nextChar();
                offset++;
            }

            /*
             * 识别'[1-9][0-9]*'或'0'
             */

            // 识别'[1-9][0-9]'
            if(_candidate != '0')
            {
                while(Character.isDigit(_candidate))
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
            }
            // 识别'0'
            else if(_candidate == '0')
            {
                strb.append(_candidate);
                nextChar();
                offset++;
            }
            else
            {
                int oldCol = _col;
                int oldRow = _row;
                String wrongChar = String.format("<%s>", String.valueOf(_candidate));
                treatWrongWord();
                return new Token("ERROR", "invalid number, unexpected" + wrongChar, oldRow, oldCol - offset);
            }

            /*
             * 识别'.[0-9]+'或空
             */

            if(_candidate == '.')
            {
                strb.append(_candidate);
                nextChar();
                offset++;
                hasDot = true;
                // 小数点后至少有一位数字
                if(!Character.isDigit(_candidate))
                {
                    int oldCol = _col;
                    int oldRow = _row;
                    treatWrongWord();
                    return new Token("ERROR", "invalid float, unexpected<.> ", oldRow, oldCol - offset);
                }
                while(Character.isDigit(_candidate))
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
            }

            /*
             * 识别'(e|E)(+|-|空)[0-9]+'或识别数字结束
             */

            if(_candidate == 'e' | _candidate == 'E')
            {
                strb.append(_candidate);
                nextChar();
                offset++;
                hasE = true;
                if (_candidate == '+' | _candidate == '-')
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
                // 至少要有一位数字
                if(!Character.isDigit(_candidate))
                {
                    int oldCol = _col;
                    int oldRow = _row;
                    String wrongChar = String.format("<%s>", String.valueOf(_candidate));
                    treatWrongWord();
                    return new Token("ERROR","invalid scientific notation number, unexpected" + wrongChar, oldRow, oldCol - offset);
                }
                while(Character.isDigit(_candidate))
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
            }

            // 识别number成功
            if(hasE)
            {
                return new Token("scientific", strb.toString(), _row, _col - offset);
            }
            else if(hasDot)
            {
                return new Token("float", strb.toString(), _row, _col - offset);
            }
            else
            {
                return new Token("integer", strb.toString(), _row, _col - offset);
            }
        }
        // 正常读到文件结尾
        else if(_candidate == EOF)
        {
            return new Token("EOF", "EOF", _row, _col - offset);
        }
        else
        {
            int oldCol = _col;
            int oldRow = _row;
            String wrongChar = String.format("<%s>", String.valueOf(_candidate));
            treatWrongWord();
            return new Token("ERROR", "unexpected word, unexpected" + wrongChar, oldRow, oldCol - offset);
        }
    }

    /**
     * 错误恢复
     * 当出现词法错误时，忽略下一个','前的所有元素
     */
    private void treatWrongWord()
    {
        while(_candidate != ',')
        {
            // 如果读到行尾，换行
            if(_candidate == '\n')
            {
                nextChar();
                _row++;
                _col = 1;
            }
            else if(_candidate == EOF)
                break;
            else
            {
                nextChar();
            }
        }
    }

    /**
     * 判断字符是否为控制字符或者通讯专用字符
     * @param c 字符
     * @return 判断结果
     */
    private boolean isControlCharacter(char c)
    {
        // 第0～31号及第127号(共33个)是控制字符或通讯专用字符
        if(c <= 31 | c == 127)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
