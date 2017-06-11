package com.isswhu;

public class Main
{

    public static void main(String[] args)
    {
        if(args[0].equals("-pretty"))
        {
            String inPath = args[1];
            Parser parser = new Parser(inPath);
            try
            {
                parser.startAnalyse();
                String outPath = args[1].substring(0, args[1].length() - 5) + ".pretty.json";
                Formatter formatter = new Formatter(inPath, outPath);
                formatter.startFormat();
            }
            catch (Error error)
            {

            }

        }
        else if(args[0].equals("-find"))
        {
            String inPath = args[1];
            String searchPath = args[2];
            Parser parser = new Parser(inPath);
            try
            {
                parser.startAnalyse();
                Searcher searcher = new Searcher(inPath, searchPath);
                searcher.buildTree();
                if(searcher.search())
                {
                    System.out.println("\nComplete Searching : Succeed!");
                }
                else
                {
                    System.out.println("Find: null");
                    System.out.println("Complete Searching : Failed!");
                }
            }
            catch (Error error)
            {

            }
        }
        else if(args.length == 1)
        {
            String inPath = args[0];
            Parser parser = new Parser(inPath);
            try
            {
                parser.startAnalyse();
            }
            catch (Error error)
            {
                System.out.println("Complete Analysis : Failed!");
            }
        }
        else
        {
            System.out.println("Parameter error！");
        }
    }
}
