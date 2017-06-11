package com.isswhu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by WeiZehao on 17/6/10.
 * 本类用于构造json树以及搜索
 */
public class Searcher
{
    private Token _currToken;
    private LexAnalyser _lex;
    private RootNode _root;
    private ArrayList<PathWord> _pathWords;
    private PathWord _startWord;

    public Searcher(String inPath, String searchPath)
    {
        _lex = new LexAnalyser(inPath);
        _pathWords = new ArrayList<>();
        _root = new RootNode();

        Queue<String> words = new LinkedList<>();
        words.addAll(Arrays.asList(searchPath.split("/")));

        if(words.peek().equals(""))
        {
            _startWord = new PathWord("object", "", 0);
            words.poll();
        }

        Pattern p1 = Pattern.compile("(?<=\\[).*(?=\\])");
        Pattern p2 = Pattern.compile(".*(?=\\[)");
        Matcher m1, m2;

        for(String word : words)
        {

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
    }

    /**
     * 递归下降法建立json树
     */
    public void buildTree()
    {
        getToken();

        while (!_currToken.getType().equals("EOF"))
        {
            JSON();
        }
    }

    /**
     * 对json树进行递归搜索
     * @return
     */
    public boolean search()
    {
        // json以数组开始
        if(_startWord == null)
        {
            return searchArray((ArrNode)_root.get_root(), 0);
        }
        // json以对象开始
        else
        {
            return searchObj((ObjNode)_root.get_root(), 0);
        }
    }

    /**
     * 对一个数组进行搜索
     * @param arrNode
     * @param curIdx
     * @return
     */
    private boolean searchArray(ArrNode arrNode, int curIdx)
    {
        Node node = arrNode.searchArray(_pathWords.get(curIdx).getIndex());
        if(node == null)
            return false;
        else
        {
            // 根据索引值搜到的数组Value是ValNode
            if(node.get_type().equals("valNode"))
            {
                if(curIdx == _pathWords.size() - 1)
                {
                    // 搜索目标是value节点，搜索成功
                    ((ValNode)node).show();
                    return true;
                }
                else
                    return false;
            }
            // 根据索引值搜到的数组Value是ArrNode
            else if(node.get_type().equals("array"))
            {
                if(curIdx == _pathWords.size() - 1)
                {
                    // 搜索目标是数组，搜索成功
                    ((ArrNode)node).show();
                    return true;
                }
                else
                {
                    if(_pathWords.get(curIdx + 1).getKey().equals("."))
                    {
                        // 继续搜索匿名数组
                        return searchArray((ArrNode)node, curIdx + 1);
                    }
                    else
                        return false;
                }
            }
            // 根据索引值搜到的数组Value是ObjNode
            else
            {
                if(curIdx == _pathWords.size() - 1)
                {
                    // 搜索目标是对象，搜索成功
                    ((ObjNode)node).show();
                    return true;
                }
                // 继续搜索对象
                else
                    return searchObj((ObjNode)node, curIdx + 1);
            }
        }
    }

    /**
     * 对一个对象进行搜索
     * @param objNode
     * @param curIdx
     * @return
     */
    private boolean searchObj(ObjNode objNode, int curIdx)
    {
        // 根据key找到键值对节点
        PairNode pair = objNode.searchKey(_pathWords.get(curIdx).getKey());
        if(pair == null)
            return false;
        else
        {
            // 根据键值对节点获取到value节点
            Node node = pair.get_value();
            // 键值对value是ValNode
            if(node.get_type().equals("valNode"))
            {
                // 用户参数错误
                if(_pathWords.get(curIdx).getType().equals("array"))
                    return false;

                if(curIdx == _pathWords.size() - 1)
                {
                    // 搜索目标为当前非数组非对象的value，搜索成功
                    ((ValNode)node).show();
                    return true;
                }
                else
                    return false;
            }
            // 键值对Value是ObjNode
            else if(node.get_type().equals("objNode"))
            {
                // 用户参数错误
                if(_pathWords.get(curIdx).getType().equals("array"))
                    return false;

                if(curIdx == _pathWords.size() - 1)
                {
                    // 搜索目标为当前对象，搜索成功
                    ((ObjNode)node).show();
                    return true;
                }
                else
                {
                    // 对象中不存在匿名数组，所以返回错误
                    if(_pathWords.get(curIdx + 1).getKey().equals("."))
                        return false;
                        // 继续搜索当前对象
                    else
                        return searchObj((ObjNode)node, curIdx + 1);
                }
            }
            // 键值对Value是ArrNode
            else
            {
                // 虽然搜索到的是数组，但是用户的路径里没有提供索引值
                if(_pathWords.get(curIdx).getType().equals("other"))
                {
                    // 用户想输出整个数组，所以不提供索引值
                    if(curIdx == _pathWords.size() - 1)
                    {
                        // 搜索目标为当前数组，搜索成功
                        ((ArrNode)node).show();
                        return true;
                    }
                    // 当前数组路径没有提供索引值，却不是路径最后一层
                    else
                        return false;
                }
                // 提供了索引值
                else
                    // 根据索引值继续搜索数组
                    // 注意curIdx指的是用户输入的路径的数组索引，而用户提供的索引值在PathWord的_index属性中存储
                    return searchArray((ArrNode)node, curIdx);
            }
        }
    }

    private void getToken()
    {
        _currToken = _lex.getToken();
        if (_currToken.getType().equals("EOF"))
        {
            System.out.println("Complete Searching : Succeed!");
        }
    }

    private void match(String type)
    {
        if (_currToken.getType().equals(type))
        {
            getToken();
        }
    }

    private void JSON()
    {
        if (_currToken.getType().equals("{"))
        {
            OBJECT(_root);
        }
        else if (_currToken.getType().equals("["))
        {
            ARRAY(_root);
        }
    }

    private void OBJECT(Node parent)
    {
        if (_currToken.getType().equals("{"))
        {
            match("{");
            OBJECT_2(parent);
        }
    }

    private void OBJECT_2(Node parent)
    {
        ObjNode obj = new ObjNode("objNode");
        parent.addNode(obj);

        if (_currToken.getType().equals("}"))
            match("}");
        else if (_currToken.getType().equals("string"))
        {
            MEMBERS(obj);
            match("}");
        }
    }

    private void MEMBERS(Node obj)
    {
        if (_currToken.getType().equals("string"))
        {
            PAIR(obj);
            PAIR_2(obj);
        }
    }

    private void PAIR(Node obj)
    {
        if (_currToken.getType().equals("string"))
        {
            PairNode pair = new PairNode("pairNode", _currToken.getContent());
            obj.addNode(pair);

            match("string");
            match(":");
            VALUE(pair);
        }
    }

    private void PAIR_2(Node obj)
    {
        if (_currToken.getType().equals("}"))
            ;
        else if (_currToken.getType().equals(","))
        {
            match(",");
            MEMBERS(obj);
        }
    }

    private void ARRAY(Node parent)
    {
        if (_currToken.getType().equals("["))
        {
            match("[");
            ARRAY_2(parent);
        }
    }

    private void ARRAY_2(Node parent)
    {
        ArrNode array = new ArrNode("arrNode");
        parent.addNode(array);

        if (_currToken.getType().equals("{") || _currToken.getType().equals("[") || _currToken.getType().equals("string")
                || _currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific") || _currToken.getType().equals("true")
                || _currToken.getType().equals("false") || _currToken.getType().equals("null"))
        {
            ELEMENTS(array);
            match("]");
        }
        else if (_currToken.getType().equals("]"))
            match("]");
    }

    private void ELEMENTS(Node array)
    {
        if (_currToken.getType().equals("{") || _currToken.getType().equals("[")
                || _currToken.getType().equals("string") || _currToken.getType().equals("integer")
                || _currToken.getType().equals("float") || _currToken.getType().equals("scientific")
                || _currToken.getType().equals("true") || _currToken.getType().equals("false")
                || _currToken.getType().equals("null"))
        {
            VALUE(array);
            ELEMENTS_2(array);
        }
    }

    private void ELEMENTS_2(Node array)
    {
        if (_currToken.getType().equals("]"))
            ;
        else if (_currToken.getType().equals(","))
        {
            match(",");
            ELEMENTS(array);
        }
    }

    private void VALUE(Node parent)
    {
        if (_currToken.getType().equals("{"))
            OBJECT(parent);
        else if (_currToken.getType().equals("["))
            ARRAY(parent);
        else if (_currToken.getType().equals("string"))
        {
            ValNode val = new ValNode("valNode", _currToken.getContent(), "string");
            parent.addNode(val);
            match("string");
        }
        else if (_currToken.getType().equals("integer") || _currToken.getType().equals("float")
                || _currToken.getType().equals("scientific"))
            NUMBER(parent);
        else
        {
            ValNode val = new ValNode("valNode", _currToken.getContent(), _currToken.getType());
            parent.addNode(val);
            match(_currToken.getType());
        }
    }

    private void NUMBER(Node parent)
    {
        ValNode val = new ValNode("valNode", _currToken.getContent(), _currToken.getType());
        parent.addNode(val);
        match(_currToken.getType());
    }
}

/**
 * 节点类抽象基类
 */
abstract class Node
{
    private String _type;

    public Node(String type)
    {
        _type = type;
    }

    public String get_type()
    {
        return _type;
    }

    public void addNode(Node node){};
}

/**
 * json树的根节点
 */
class RootNode extends Node
{
    private Node _root;

    public RootNode()
    {
        super("root");
    }

    public void addNode(Node node)
    {
        _root = node;
    }

    public Node get_root()
    {
        return _root;
    }
}

/**
 * 表示对象，对应文法中的object
 */
class ObjNode extends Node
{
    private ArrayList<PairNode> _pairNodes;

    public ObjNode(String type)
    {
        super(type);
        _pairNodes = new ArrayList<>();
    }

    public PairNode searchKey(String key)
    {
        for (PairNode pair : _pairNodes)
        {
            if(pair.get_key().equals(key))
            {
                return pair;
            }
        }
        return null;
    }

    public void addNode(Node node)
    {
        _pairNodes.add((PairNode)node);
    }

    public void show(){}
}

/**
 * 表示数组，对应文法中的array
 */
class ArrNode extends Node
{
    private ArrayList<Node> _Nodes;

    public ArrNode(String type)
    {
        super(type);
        _Nodes = new ArrayList<>();
    }

    public void addNode(Node node)
    {
        _Nodes.add(node);
    }

    public Node searchArray(int index)
    {
        if(index <= _Nodes.size())
        {
            return _Nodes.get(index - 1);
        }
        return null;
    }

    public void show(){}
}

/**
 * 表示键值对，对应文法中的pair，存储key和指针，指向的节点对应文法中的value
 */
class PairNode extends Node
{
    private String _key;
    private Node _value;

    public PairNode(String type, String key)
    {
        super(type);
        _key = key;
    }

    public void addNode(Node node)
    {
        _value = node;
    }

    public String get_key()
    {
        return _key;
    }

    public Node get_value()
    {
        return _value;
    }
}

/**
 * Val节点，表示number，true，false和null
 */
class ValNode extends Node
{
    private String _content;
    private String _contentType;

    public ValNode(String type, String content, String contentType)
    {
        super(type);
        _content = content;
        _contentType = contentType;
    }

    public String get_content()
    {
        return _content;
    }

    public String get_contentType() { return _contentType; }

    public void show()
    {
        System.out.println(_contentType + " : " + _content);
    }
}

/**
 * 表示搜索路径的一个路径单元，如/RECORDS[35]/countryname中的RECORDS[35]
 */
class PathWord
{
    private String _type;
    private String _key;
    private int _index;

    public PathWord(String type, String content, int index)
    {
        _type = type;
        _key = content;
        _index = index;
    }

    public String getType()
    {
        return _type;
    }

    public int getIndex()
    {
        return _index;
    }

    public String getKey()
    {
        return _key;
    }
}
