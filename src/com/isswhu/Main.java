package com.isswhu;

public class Main
{

    public static void main(String[] args)
    {
        String path = Main.class.getResource("/").toString();
        LexAnalyser lexAnalyser = new LexAnalyser("/Users/mebottle/IdeaProjects/jsonResolver/test/b/true.json");

        Parser parser = new Parser("/Users/mebottle/IdeaProjects/jsonResolver/test/e/test.pretty.json");
        try
        {
            parser.startAnalyse();
//            Formatter formatter = new Formatter("/Users/mebottle/IdeaProjects/jsonResolver/test/e/test.json",
//                    "/Users/mebottle/IdeaProjects/jsonResolver/test/e/test.pretty.json");
//            formatter.startFormat();
        }
        catch (Exception e)
        {

        }
        Searcher searcher = new Searcher("/Users/mebottle/IdeaProjects/jsonResolver/test/e/test.json",
                ".[4]/education/course[1]");
        searcher.buildTree();
        searcher.search();
    }
}
