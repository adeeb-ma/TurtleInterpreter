package tortoise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser
{

    private String number = "[0-9]*\\.?[0-9]+";
    private String variable = ":[a-z]+";
    private String arithOpers = "[\\/\\+\\-\\*<>]";
//    String arithWithSpaces = "(([-+]?[0-9]*\\.?[0-9]+\\s*[\\/\\+\\-\\*<>]\\s*)+([-+]?\\s*[0-9]*\\.?[0-9]+)|\\d+)";
    private String arithWithParameters = "[-+]?((" + number + "|" + variable + ")\\s*" + arithOpers + "\\s*)+" + "([-+]?\\s*" + number + "|" + variable + ")|" + "(-?(" + number + "|" + variable + "))";
    private String arithWithoutParameters = "[-+]?((" + number +  ")\\s*" + arithOpers + "\\s*)+" + "([-+]?\\s*" + number +  ")|(-?" + number + ")";

    public static class ParserException extends Exception
    {
        //Throwing parser exception on brackets-error, bad arithmetic operations, probably EOF
        //                            {2+(5*), ((2+3)} {  2**3, +2+, 1-/4  }
        public ParserException(String msg)
        {
            super (msg);
        }
    }

    public String getArithmeticRegex() { return this.arithWithParameters; }
    public String getNumbersArithmeticRegex() { return this.arithWithoutParameters; }

    public ArrayList<String> parse(String s) throws ParserException
    {
        //TODO: Fix this to also detect Paremeters. They have a colon up front. Like :param

        s = s.replaceAll("\\["," \\[ ").replaceAll("\\]"," \\] ");
        s = s.replaceAll("\\s+"," ");
        s = s.replaceAll("\\[ ","\\[").replaceAll(" \\]","\\]");


        Pattern pattern = Pattern.compile(arithWithParameters);
        Matcher matcher = pattern.matcher(s);

        while (matcher.find())
        {
            String g = matcher.group();
            s = s.replace(g, g.replaceAll(" ",""));
        }


        s = cypherBlocks(s);

        ArrayList<String> result = new ArrayList<String>(Arrays.asList(s.split(" ")));

        result = decypherBlocks(result);

        return result;
    }



    private String cypherBlocks(String s) throws ParserException
    {
        StringBuilder sb = new StringBuilder(s);

        int count = 0;
        for (int i=0 ; i < s.length() ; i++)
        {
            if(s.charAt(i) == '[') count++;
            if(s.charAt(i) == ']') count--;

            if(count < 0)   throw new ParserException("ERROR: bad bracket placements.");

            if(s.charAt(i) == ' ' && count > 0) sb.setCharAt(i,'@');
        }

        return sb.toString();
    }

    private ArrayList<String> decypherBlocks(ArrayList<String> al)
    {
        return al.stream().map(s -> s.replaceAll("@"," ")).collect(Collectors.toCollection(ArrayList::new));
    }
}
