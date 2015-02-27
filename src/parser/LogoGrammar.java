package parser;

public class LogoGrammar extends edu.hendrix.grambler.Grammar {
    public LogoGrammar() {
        super();
        addProduction("lines", new String[]{"lines", "line"}, new String[]{"line"});
        addProduction("line", new String[]{"expr", "endl"}, new String[]{"expr"}, new String[]{"endl"});
        addProduction("expr", new String[]{"assignment"}, new String[]{"addSubOp"}, new String[]{"loop"}, new String[]{"functionDecl"}, new String[]{"functionCall"}, new String[]{"if"}, new String[]{"ifelse"});
        addProduction("assignment", new String[]{"variable", "space", "\"=\"", "space", "addSubOp"});
        addProduction("loop", new String[]{"'repeat'", "space", "addSubOp", "space", "block"});
        addProduction("funcVars", new String[]{"funcVars", "space", "variable"}, new String[]{"variable"});
        addProduction("functionDecl", new String[]{"'to'", "space", "fname", "space", "funcVars", "space", "block"});
        addProduction("functionCall", new String[]{"functionCall", "space", "fname", "space", "fvars"}, new String[]{"fname", "space", "fvars"}, new String[]{"fname"});
        addProduction("fvars", new String[]{"fvars", "space", "addSubOp"}, new String[]{"addSubOp"});
        addProduction("fname", new String[]{"\"[a-zA-Z]+\""});
        addProduction("boolOp", new String[]{"\">\""}, new String[]{"\"<\""}, new String[]{"\">=\""}, new String[]{"\"<=\""}, new String[]{"\"==\""});
        addProduction("boolNor", new String[]{"boolNor", "space", "\"nor\"", "space", "boolOr"}, new String[]{"boolOr"});
        addProduction("boolOr", new String[]{"boolOr", "space", "\"or\"", "space", "boolAnd"}, new String[]{"boolAnd"});
        addProduction("boolAnd", new String[]{"boolAnd", "space", "\"and\"", "space", "boolCond"}, new String[]{"boolCond"});
        addProduction("boolCond", new String[]{"addSubOp", "space", "boolOp", "space", "addSubOp"}, new String[]{"addSubOp"});
        addProduction("if", new String[]{"\"if\"", "space", "boolNor", "space", "block"});
        addProduction("ifelse", new String[]{"\"ifelse\"", "space", "boolCond", "space", "block", "space", "elseBlock"});
        addProduction("block", new String[]{"'['", "space", "lines", "space", "']'"}, new String[]{"'['", "lines", "']'"});
        addProduction("elseBlock", new String[]{"'['", "space", "expr", "space", "']'"}, new String[]{"'['", "expr", "']'"});
        addProduction("addSubD", new String[]{"'+'"}, new String[]{"'-'"});
        addProduction("multDivD", new String[]{"'*'"}, new String[]{"'/'"});
        addProduction("addSubOp", new String[]{"addSubOp", "space", "addSubD", "space", "multDivOp"}, new String[]{"multDivOp"});
        addProduction("multDivOp", new String[]{"multDivOp", "space", "multDivD", "space", "addSubOp"}, new String[]{"paren"});
        addProduction("paren", new String[]{"'('", "space", "addSubOp", "space", "')'"}, new String[]{"'['", "space", "addSubOp", "space", "']'"}, new String[]{"num"});
        addProduction("num", new String[]{"\"\\d+\""}, new String[]{"variable"});
        addProduction("variable", new String[]{"\":[a-zA-Z]+\""});
        addProduction("space", new String[]{"singleSpace"}, new String[]{"space", "singleSpace"});
        addProduction("endl", new String[]{"\"\\r\\n\""}, new String[]{"\"\\n\""});
        addProduction("singleSpace", new String[]{"\" \""});
    }
}

