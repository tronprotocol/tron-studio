package org.tron.studio;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRFileStream;

import org.tron.studio.antlr.SolidityLexer;
import org.tron.studio.antlr.SolidityParser;
import java.io.IOException;

public class TestAntlr {

    public static void main(String[] args)
    {
        try {
            ANTLRFileStream file = new ANTLRFileStream("/Users/wangguanghui/Documents/myproject/blockchain/TronStudio/src/main/resources/template/Ballot.sol");
            SolidityLexer lexer = new SolidityLexer(file);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SolidityParser parser = new SolidityParser(tokens);
            ParseTree t = parser.parameterList();
            System.out.println(t.toStringTree(parser));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
