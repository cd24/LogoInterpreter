package parser;

public class LogoGrammar extends edu.hendrix.grambler.Grammar {
    public LogoGrammar() {
        super();
        addProduction("lines", new String[]{"lines", "line"}, new String[]{"line"});
        addProduction("line", new String[]{"expr", "\"\\r\\n\""}, new String[]{"expr", "\"\\n\""}, new String[]{"expr"});
        addProduction("expr", new String[]{"assignment"}, new String[]{"addSubOp"}, new String[]{"cmd"}, new String[]{"loop"});
        addProduction("cmd", new String[]{"cmd", "space", "fd"}, new String[]{"cmd", "space", "bk"}, new String[]{"cmd", "space", "lt"}, new String[]{"cmd", "space", "rt"}, new String[]{"cmd", "space", "pd"}, new String[]{"cmd", "space", "pu"}, new String[]{"cmd", "space", "home"}, new String[]{"cmd", "space", "cs"}, new String[]{"cmd", "space", "st"}, new String[]{"cmd", "space", "ht"}, new String[]{"fd"}, new String[]{"bk"}, new String[]{"lt"}, new String[]{"rt"}, new String[]{"pd"}, new String[]{"pu"}, new String[]{"home"}, new String[]{"cs"}, new String[]{"st"}, new String[]{"ht"});
        addProduction("assignment", new String[]{"variable", "space", "\"=\"", "space", "addSubOp"});
        addProduction("loop", new String[]{"'repeat'", "space", "addSubOp", "space", "'['", "space", "cmd", "space", "']'"});
        addProduction("addSubD", new String[]{"'+'"}, new String[]{"'-'"});
        addProduction("multDivD", new String[]{"'*'"}, new String[]{"'/'"});
        addProduction("addSubOp", new String[]{"addSubOp", "space", "addSubD", "space", "multDivOp"}, new String[]{"multDivOp"});
        addProduction("multDivOp", new String[]{"multDivOp", "space", "multDivD", "space", "addSubOp"}, new String[]{"paren"});
        addProduction("paren", new String[]{"'('", "space", "addSubOp", "space", "')'"}, new String[]{"'['", "space", "addSubOp", "space", "']'"}, new String[]{"num"});
        addProduction("fd", new String[]{"\"forward\"", "space", "addSubOp"}, new String[]{"\"fd\"", "space", "addSubOp"});
        addProduction("bk", new String[]{"\"backward\"", "space", "addSubOp"}, new String[]{"\"bk\"", "space", "addSubOp"});
        addProduction("lt", new String[]{"\"left\"", "space", "addSubOp"}, new String[]{"\"lt\"", "space", "addSubOp"});
        addProduction("rt", new String[]{"\"right\"", "space", "addSubOp"}, new String[]{"\"rt\"", "space", "addSubOp"});
        addProduction("pd", new String[]{"\"pendown\""}, new String[]{"\"pd\""});
        addProduction("pu", new String[]{"\"penup\""}, new String[]{"\"pu\""});
        addProduction("home", new String[]{"\"home\""});
        addProduction("cs", new String[]{"\"clearscreen\""}, new String[]{"\"cs\""});
        addProduction("st", new String[]{"\"showturtle\""}, new String[]{"\"st\""});
        addProduction("ht", new String[]{"\"hideturtle\""}, new String[]{"\"ht\""});
        addProduction("num", new String[]{"\"\\d+\""}, new String[]{"variable"});
        addProduction("variable", new String[]{"\"[a-zA-Z]+\""});
        addProduction("space", new String[]{"\"\\s*\""});
    }
}

