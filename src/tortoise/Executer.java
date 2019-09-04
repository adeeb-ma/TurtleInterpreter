package tortoise;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class Executer
{
	private enum CMD {MOVE, TURN, VISIBILITY, CLEAR_SCREEN, PEN, REPEAT, DECL, FUNC, END, ADD_VAR, COND, STOP, PRINT}
    private enum ArithmeticOperations {ADD,SUB,MULT,DIV,RAND,SIN,COS,TAN,PI,EQ,NEQ,AND,OR,XOR}


    private static class FunctionInfo
    {
        int argsNum;
        String commandsRaw;
        ArrayList<String> argumentsName;

        public FunctionInfo(int argsNum, String commandsRaw, ArrayList<String> argumentsName)
        {
            this.argsNum = argsNum;
            this.commandsRaw = commandsRaw;
            this.argumentsName = argumentsName;
        }
    }

    public static class ExecuterException extends Exception
    {
        public ExecuterException( String msg )
        {
            super( msg );
        }
    }

    private Parser p;

    private HashMap<String, String> variables;

    private HashMap<String, FunctionInfo> functions;
    private HashSet<String> returnablePrimitives;
    private HashMap<String, ArithmeticOperations> stringsToArithmetic;

    private HashMap<String, Method> primitives;
    private HashMap<String, Object[]> primitiveParameters;
    private HashMap<String, String> synonyms;

    private int currentCmdIndex;
    private String currentCmdName;
    private ArrayList<String> commands;

    Controller controller;

    private boolean programmingMode;
    private boolean prevConditionFlow;
    private boolean hasDoneCheck;

    private ScriptEngineManager sem;
    private ScriptEngine scriptEngine;


    public Executer(HashMap<String, String> variables, HashMap<String, FunctionInfo> functions, String commandsRaw, Controller controller) throws Parser.ParserException
    {
//        this.p = new Parser();
//
//        this.variables = variables;
//        this.functions = functions;
//
//        this.commands = p.parse(commandsRaw);
//        if(this.commands.equals( new ArrayList<String>(Arrays.asList("") ) ))
//            this.commands = new ArrayList<String>();
//
//        this.controller = controller;
//
//        this.currentCmdIndex = 0;
//        this.programmingMode = false;

        this.preSetup(variables,functions,commandsRaw,controller);
        this.setupEverything();
        this.setupRest();
//        this.setupPrimitives();
//        this.setupSynonims();
    }

    public Executer(Controller controller,String commandsRaw) throws Parser.ParserException
    {
//        this.p = new Parser();
//
//        this.variables = new HashMap<>();
//        this.functions = new HashMap<>();
//
//        this.commands = p.parse(commandsRaw);
//        if(this.commands.equals( new ArrayList<String>(Arrays.asList("") ) ))
//            this.commands = new ArrayList<String>();
//
//        this.controller = controller;
//
//        this.currentCmdIndex = 0;
//        this.programmingMode = false;

        this.preSetup(null,null,commandsRaw,controller);
        this.setupEverything();
        this.setupRest();
//        this.setupPrimitives();
//        this.setupSynonims();
    }

    private void preSetup(HashMap<String, String> variables, HashMap<String, FunctionInfo> functions, String commandsRaw, Controller controller) throws Parser.ParserException
    {
        this.p = new Parser();

        this.variables = variables; if(variables == null) this.variables = new HashMap<>();
        this.functions = functions; if(functions == null) this.functions = new HashMap<>();
        this.primitives = new HashMap<>();
        this.primitiveParameters = new HashMap<>();
        this.returnablePrimitives = new HashSet<>();
        this.synonyms = new HashMap<>();
        this.stringsToArithmetic = new HashMap<>();

        this.commands = p.parse(commandsRaw);
        if(this.commands.equals( new ArrayList<String>(Arrays.asList("") ) ))
            this.commands = new ArrayList<String>();

        this.controller = controller;

        this.currentCmdIndex = 0;
        this.programmingMode = false;
        this.prevConditionFlow = false;
        this.hasDoneCheck = false;

        this.sem = new ScriptEngineManager();
        this.scriptEngine = sem.getEngineByName("JavaScript");
    }



	//Probably the most important function to exist
	private void setupEverything()
	{
		this.addNewCommand("_primitive_move",new Object[]{false},"امام","مم","forward","fd");

		this.addNewCommand("_primitive_move",new Object[]{true},"وراء","ور","backward", "back","bk");

		this.addNewCommand("_primitive_turn",new Object[]{false},"يمين","يم","right","rt");

		this.addNewCommand("_primitive_turn",new Object[]{true},"شمال","شم","left","lf");

		this.addNewCommand("_primitive_repeat", null,"كرر","repeat");

		this.addNewCommand("_primitive_toggle_programming_mode", null,"حرر","حر","decl");

		this.addNewCommand("_primitive_add_variable", null,"دع","make");

		this.addNewCommand("_primitive_condition", new Object[]{"if"},"اذا","if");

		this.addNewCommand("_primitive_stop", null,"قف","stop");

		this.addNewCommand("_primitive_print", new Object[]{false},"اطبع","طب","print");

		this.addNewCommand("_primitive_print", new Object[]{true},"اطبع1","طب1","print1");

		//Probably a bad idea
//        this.addNewCommand("_primitive_arithmetic", new Object[]{"add"},"جمع","add");
//
//        this.addNewCommand("_primitive_arithmetic", new Object[]{"sub"},"طرح","sub");
//
//        this.addNewCommand("_primitive_arithmetic", new Object[]{"multiply"},"ضرب","mul");
//
//        this.addNewCommand("_primitive_arithmetic", new Object[]{"divide"},"قسمة","div");
	}



    private void addNewCommand(String methodName, Object[] primitiveParams, String command, String ...synonyms)
    {
        addSynonims(command,synonyms);

        this.addPrimitive(command, methodName, primitiveParams);
    }

    private void addPrimitive(String command, String methodName, Object[] parameters)
    {
        try
        {
//            Method m = Arrays.stream(this.getClass().getDeclaredMethods()).filter( x -> x.getName().equals(methodName)).findFirst().get();
            Method m = this.getClass().getDeclaredMethod(methodName);

//            if(m.getReturnType() == String.class)
//            {
//                this.returnablePrimitives.add(methodName);
//            }
//            else
//            {
                if (this.primitives.containsKey(command))
                    throw new Exception(command + " already exists as primitive.");
                this.primitives.put(command, m);
                this.primitiveParameters.put(command, parameters);
//            }

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSynonims(String command, String ...synonyms)
    {
        this.synonyms.put(command,command);

        for(String s : synonyms)
            this.synonyms.put(s,command);
    }

    private void setupRest()
    {
        this.addSynonims("يا","func");
        this.addSynonims("نهاية","end");

//        this.addReturnables("add","sub","mul","div","rand","sin","cos","tan","pi");

        this.addArithmeticOperation(ArithmeticOperations.ADD, "add");
        this.addArithmeticOperation(ArithmeticOperations.SUB, "sub");
        this.addArithmeticOperation(ArithmeticOperations.MULT, "mul", "mult");
        this.addArithmeticOperation(ArithmeticOperations.DIV, "div");
        this.addArithmeticOperation(ArithmeticOperations.RAND, "rand");
        this.addArithmeticOperation(ArithmeticOperations.SIN, "sin");
        this.addArithmeticOperation(ArithmeticOperations.COS, "cos");
        this.addArithmeticOperation(ArithmeticOperations.TAN, "tan");
        this.addArithmeticOperation(ArithmeticOperations.PI, "pi");
        this.addArithmeticOperation(ArithmeticOperations.EQ, "eq");
        this.addArithmeticOperation(ArithmeticOperations.NEQ, "neq");
        this.addArithmeticOperation(ArithmeticOperations.AND, "and");
        this.addArithmeticOperation(ArithmeticOperations.OR, "or");
        this.addArithmeticOperation(ArithmeticOperations.XOR, "xor");

    }

    private void addReturnables(String ...funcs)
    {
        this.returnablePrimitives.addAll(Arrays.asList(funcs));
    }

    private void addArithmeticOperation(ArithmeticOperations ao, String ...opers)
    {
        this.addReturnables(opers);

        for(String s : opers)
            this.stringsToArithmetic.put(s,ao);
    }



	//OBSOLETE
	private void setupPrimitives()
	{


		this.addPrimitive("امام","_primitive_move", new Object[]{false});

		this.addPrimitive("وراء","_primitive_move", new Object[]{true});

		this.addPrimitive("يمين","_primitive_turn", new Object[]{true});

		this.addPrimitive("شمال","_primitive_turn", new Object[]{false});
	}

	//OBSOLETE
	private void setupSynonims()
	{
		this.synonyms.put("forward","امام");
		this.synonyms.put("fd","امام");
		this.synonyms.put("امام","امام");
		this.synonyms.put("مم","امام");

		this.synonyms.put("backward","وراء");
		this.synonyms.put("back","وراء");
		this.synonyms.put("وراء","وراء");
		this.synonyms.put("ور","وراء");

		this.synonyms.put("right","يمين");
		this.synonyms.put("rt","يمين");
		this.synonyms.put("يمين","يمين");
		this.synonyms.put("يم","يمين");

		this.synonyms.put("left","شمال");
		this.synonyms.put("lf","شمال");
		this.synonyms.put("شمال","شمال");
		this.synonyms.put("شم","شمال");
	}




    private Object evaluateExpression(String s) throws ScriptException, ExecuterException
    {
        //Replace all variables with their actual number representations
//        for (String variable : this.variables.keySet())
//        {
//            s = s.replaceAll(variable,this.variables.get(variable));
//        }

        s = replaceVarsWithValues(s);

        if(isNumberArithmeticOperation(s))
            return this.scriptEngine.eval(s);


        throw new ExecuterException("ERROR: asjkfnqejkrg");
    }


    private boolean isBoolean(String s)
    {
        return s.equals("true") || s.equals("false");
    }

    private boolean isArithmeticOperation(String s)
    {
       return s.matches(this.p.getArithmeticRegex());
    }

    private boolean isNumberArithmeticOperation(String s)
    {
        return s.matches(this.p.getNumbersArithmeticRegex()) ;
    }

    private boolean isReturnableFunction(String s) { return this.returnablePrimitives.contains(s); }

    private boolean isNumeric(String s)
    {
        try { Integer.parseInt(s);} catch(Exception e) {return false;}
        return true;
    }

    private boolean isString(String arg)
    {
        if(arg.charAt(0) == '\"')   return true;

        return false;
    }

    private boolean isVariable(String s)
    { return s.charAt(0) == ':'; }

    private boolean hasVariable(String s)
    {
        return this.variables.containsKey(s);
    }

    private boolean hasFunction(String s)
    {
        return this.functions.containsKey(s);
    }



    private void checkAmountArguments(int n) throws ExecuterException
    {
        //For example: fd 100.
        //ArrayList is size 2, and we Extracted fd. thus 1 + n=1 <= 2
        //TODO: probably find a better boolean expression
        if (this.currentCmdIndex + n > this.commands.size())
            throw new ExecuterException("ERROR: in " + this.currentCmdName + " arguments too short. Needed " + 1 +", passed " + 0);
    }

    private boolean isBlock(String s)
    {
        return (s.charAt(0) == '[' && s.charAt(s.length()-1) == ']');
    }

    private String[] extractArguments(int n) throws ExecuterException, ScriptException
    {
        String[] args = null;
        if(n == 0)  return args;

        args = new String[n];

//        this.checkAmountArguments(n);

        for(int i=0 ; i<n ; i++)
        {
            //TODO: Always check here if we still have arguments available
            if(isDone())
                throw new ExecuterException("ERROR: in " + this.currentCmdName + " arguments too short. Needed " + n +", passed " + i);

            args[i] = this.peekCurrCommand();
            if(isReturnableFunction(args[i]))
            {
//                this.currentCmdIndex--;         //So we can calculate the return value properly
                args[i] = this.tryParsingValue(args[i]).toString();
            }
            else
            {
                this.extract();
            }
        }

        return args;
    }

//    private String extractNextArgument()
//    {
//        String res = null;
//
//
//
//        return res;
//    }

    private Object[] extractParams(String s)
    {
        return this.primitiveParameters.get(s);

    }

    private Object tryParsingValue(String s) throws ExecuterException, ScriptException
    {
        //This handles both singular numbers and singular variables, or combined somehow
        if(isArithmeticOperation(s))
        {
            return evaluateExpression(s);
        }
        else if(isReturnableFunction(s))
        {
            //We assume that currentCmdIndex points to the very returnable function.
            return evaluateReturnFunction();
//            return "";
        }
        else if(isString(s))
        {
            return tryParseString(s);
        }
        else if(isBoolean(s))
        {
            return Boolean.parseBoolean(s);
        }
        else if(isBlock(s))
        {
            return tryParsingBlock(s);
        }
        else
        {
            throw new ExecuterException("ERROR: in" + this.currentCmdName + " symbol " + s +" not a variable or number.");
        }
    }

    private String tryParsingBlock(String s) throws ExecuterException
    {
        if(isBlock(s) == false) throw new ExecuterException("ERROR: in command " + this.currentCmdName + " expected block of code, got " + s + "instead.");
        return s.substring(1,s.length()-1);
    }

    private String tryParseString(String s) throws ExecuterException
    {
        if(isString(s) == false) throw new ExecuterException("ERROR: in command " + this.currentCmdName + " expected String, got " + s + "instead.");
        return s.substring(1,s.length());
    }

    private void ConcatBlockToCommands(String block) throws ExecuterException, Parser.ParserException
    {
        //Don't change tryParsingBlock to tryParsingValue. We have to be guaranteed that a bloc is passed here.
        ArrayList<String> newCommands = this.p.parse( this.tryParsingBlock(block) );

        for (int j=newCommands.size()-1 ; j>=0 ; j--)
        {
            this.commands.add(this.currentCmdIndex,newCommands.get(j));
        }
    }

    private String replaceVarsWithValues(String s)
    {
        for (String variable : this.variables.keySet())
        {
            s = s.replaceAll(variable,this.variables.get(variable));
        }
        return s;
    }




    private void done() { this.currentCmdIndex = this.commands.size(); }

    private boolean isDone()
    {
        return this.currentCmdIndex == this.commands.size();
    }

    private String peekCurrCommand()
    {
        return this.commands.get(this.currentCmdIndex);
    }

    private String extract()
    {
        String s = this.commands.get(this.currentCmdIndex);
        this.currentCmdIndex++;
        return s;
    }

    private String CurrentCmd()
    {
        return this.currentCmdName;
    }

    private String CypherCmd(String s) throws ExecuterException
    {
        String res = this.synonyms.get(s);
        if(res == null) throw new ExecuterException("ERROR: command " + s + " not implemented.");

        return res;
    }

    private String CypherCmdNoExp(String s)
    {
        try
        {
            return CypherCmd(s);
        }
        catch(Exception ignored)
        {

        }

        return "";
    }

    private ArithmeticOperations StringToArithmetic(String s) throws ExecuterException
    {
//        ArithmeticOperations res;
//        switch(s)
//        {
//            case "add": res = ArithmeticOperations.ADD; break;
//            case "sub": res = ArithmeticOperations.SUB; break;
//            case "mul": res = ArithmeticOperations.MULT; break;
//            case "div": res = ArithmeticOperations.DIV; break;
//            case "rand": res = ArithmeticOperations.RAND; break;
//            case "sin": res = ArithmeticOperations.SIN; break;
//            case "cos": res = ArithmeticOperations.COS; break;
//            case "tan": res = ArithmeticOperations.TAN; break;
//            case "pi": res = ArithmeticOperations.PI; break;
//            default: throw new ExecuterException("ERROR: dejnfbvijwe");
//        }
//        return res;

        return this.stringsToArithmetic.get(s);
    }




    //TODO: you havent checked about worst case scenarios. Meaning do unit testing.
    private Object evaluateReturnFunction() throws ScriptException, ExecuterException
    {
        Stack<String> instructionsStack = new Stack<>();
        Stack<String> numbersStack = new Stack<>();
        String s;

        while   (this.isDone() == false &&
                (this.isReturnableFunction( this.peekCurrCommand() ) ||
                        this.isArithmeticOperation(this.peekCurrCommand())))

        {
            s = this.extract();
            instructionsStack.push(s);
        }

        //From the top to bottom, if its a returnable function command, execute it on the numbers stack.
        while(instructionsStack.empty() == false)
        {
            s = instructionsStack.peek();
            instructionsStack.pop();

            if(this.isArithmeticOperation(s))
            {
                numbersStack.push(s);
            }
            else if(this.isReturnableFunction(s))
            {
                performEvaluationReturnFunction(s,numbersStack);
            }
            else
            {
                //We probably shouldn't enter here
            }
        }

        if(numbersStack.size() != 1)    throw new ExecuterException("ERROR: Bad arithmetic Arguments.");
        return this.tryParsingValue( numbersStack.peek() );
    }

    //OBSOLETE
//    private String reevaluateReturnFunction(Stack<String> instructionsStack,Stack<String> numbersStack) throws ScriptException, ExecuterException
//    {
//        String s;
//
//        //From the top to bottom, if its a returnable function command, execute it on the numbers stack.
//        while(instructionsStack.empty() == false)
//        {
//            s = instructionsStack.peek();
//            instructionsStack.pop();
//
//            if(this.isArithmeticOperation(s))
//            {
//                numbersStack.push(s);
//            }
//            else if(this.isReturnableFunction(s))
//            {
//                performEvaluationReturnFunction(s,numbersStack);
//            }
//            else
//            {
//                //We probably shouldn't enter here
//            }
//        }
//
//        if(numbersStack.size() != 1)    throw new ExecuterException("ERROR: Bad arithmetic Arguments.");
//        Object o = this.tryParsingValue( numbersStack.peek() );
//        numbersStack.pop();
//        return o.toString();
//    }

    private void performEvaluationReturnFunction(String instruction, Stack<String> numbers) throws ExecuterException, ScriptException
    {
        switch(this.StringToArithmetic(instruction))
        {
            case PI:    nonaryOperation(ArithmeticOperations.PI, numbers); break;

            case DIV:   binaryOperation(ArithmeticOperations.DIV, numbers);  break;
            case RAND:  unaryOperation(ArithmeticOperations.RAND, numbers); break;
            case SIN:   unaryOperation(ArithmeticOperations.SIN, numbers); break;
            case COS:   unaryOperation(ArithmeticOperations.COS, numbers); break;
            case TAN:   unaryOperation(ArithmeticOperations.TAN, numbers); break;

            case ADD:   binaryOperation(ArithmeticOperations.ADD, numbers);  break;
            case SUB:   binaryOperation(ArithmeticOperations.SUB, numbers);  break;
            case MULT:  binaryOperation(ArithmeticOperations.MULT, numbers);  break;


            case EQ:    binaryConditions(ArithmeticOperations.EQ, numbers);    break;
            case NEQ:   binaryConditions(ArithmeticOperations.NEQ, numbers);    break;
            case AND:   binaryConditions(ArithmeticOperations.AND, numbers);    break;
            case OR:    binaryConditions(ArithmeticOperations.OR, numbers);    break;
            case XOR:   binaryConditions(ArithmeticOperations.XOR, numbers);    break;
            default:    break;
        }
    }

    private void nonaryOperation(ArithmeticOperations ao, Stack<String> numbers) throws ScriptException, ExecuterException
    {
        Number res = 0;
        switch(ao)
        {
            case PI: res = Math.PI; break;
            default: break;
        }

        numbers.push(res.toString());
    }

    private void unaryOperation(ArithmeticOperations ao, Stack<String> numbers) throws ScriptException, ExecuterException
    {
        Number x, res = 0;

        x = (Number) this.tryParsingValue(numbers.peek()); numbers.pop();

        switch (ao)
        {
            case RAND: res = new Random().nextInt(x.intValue());    break;
            case SIN:  res = Math.sin(x.doubleValue());             break;
            case COS:  res = Math.cos(x.doubleValue());             break;
            case TAN:  res = Math.tan(x.doubleValue());             break;
            default: break;
        }

        numbers.push(res.toString());
    }

    private void binaryOperation(ArithmeticOperations ao, Stack<String> numbers) throws ScriptException, ExecuterException
    {
        Number a,b,res;
        String op = "";

        a = (Number) this.tryParsingValue(numbers.peek()); numbers.pop();
        b = (Number) this.tryParsingValue(numbers.peek()); numbers.pop();

        switch (ao)
        {
            case ADD:   op = "+"; break;
            case SUB:   op = "-"; break;
            case MULT:  op = "*"; break;
            case DIV:   op = "/"; break;
            default: break;
        }

        res = (Number) this.tryParsingValue(a + op + b);
        numbers.push(res.toString());
    }

    //TODO: Try mushing this into the binaryOperation function. To clean up code
    private void binaryConditions(ArithmeticOperations ao, Stack<String> numbers) throws ScriptException, ExecuterException
    {
        String a,b,res = "";

        a = numbers.peek(); numbers.pop();
        b = numbers.peek(); numbers.pop();

        //TODO: and, or, xor can take 2+ arguments.

        switch (ao)
        {
            case EQ:    res = String.valueOf((a.equals(b))); break;
            case NEQ:   res = String.valueOf(!(a.equals(b))); break;
            case AND:   res = String.valueOf(Boolean.parseBoolean(a) && Boolean.parseBoolean(b)); break;
            case OR:    res = String.valueOf(Boolean.parseBoolean(a) || Boolean.parseBoolean(b)); break;
            case XOR:   res = String.valueOf(Boolean.parseBoolean(a) ^ Boolean.parseBoolean(b)); break;
            default: break;
        }

        numbers.push(res);
    }

    private void trinaryOperation(ArithmeticOperations ao, Stack<String> numbers)
    {

    }




    private void addVariable(String s, String val)
    {
        this.variables.put(s,val);
    }


    private void tryAddFunction() throws ExecuterException
    {
        if(isValidFuncDeclaration() == false)
            throw new ExecuterException("ERROR: Invalid programming event");

        String s = this.createNewFunctionInfo();
        this.controller.displayLog("Learned: " + s, false);
    }

    private boolean isValidFuncDeclaration()
    {
        ArrayList<String> cyphered = this.commands.stream().map(this::CypherCmdNoExp).collect(Collectors.toCollection(ArrayList::new));
        if(cyphered.get(0).equals(CypherCmdNoExp("func")) == false ||
                cyphered.get(cyphered.size() - 1).equals(CypherCmdNoExp("end")) == false ||
                this.functions.containsKey(cyphered.get(1)))
            return false;

        return true;
    }

    private String createNewFunctionInfo()
    {
        this.commands.remove(this.commands.size()-1);
        this.commands.remove(0);
        String s = this.commands.get(0);
        this.commands.remove(0);

        ArrayList<String> arguments = new ArrayList<>();
        while(isVariable(this.commands.get(0)))
        {
            arguments.add(this.commands.get(0));
            this.commands.remove(0);
        }

        String rawCommands = String.join(" ", this.commands);

        //This is actually fine. Being java, it passes these things by reference.
        //So whatever happens here, before or after, also happens in ex, and vise versa.
        //Cool beans.
        //Executer ex = new Executer(variables,functions,rawCommands, this.controller);

        FunctionInfo fi = new FunctionInfo(arguments.size(),rawCommands,arguments);

        this.functions.put(s,fi);

        return s;
    }






    public void readCommands(String s) throws Parser.ParserException
    {
        this.commands = this.p.parse(s);
    }

    public void run() throws Throwable
    {
        this.currentCmdIndex = 0;
        while(!this.isDone())
        {
            this.step();
        }

//        if(this.programmingMode == false)
        this.controller.displayLog("Finished.", false);
    }

    public void step() throws Throwable
    {
        if(!isDone())
            ExecuteCommand();
    }

    private void ExecuteCommand() throws Throwable
    {
        if(this.programmingMode == true)
        {
            //Order is set that way because tryAddFunction might throw
            this.programmingMode = false;
            this.controller.setProgrammingMode(false);
            this.tryAddFunction();
            this.done();
            return;
        }



        String cmd = extract();
        if( this.checkIfFunction(cmd) == true) return;
        cmd = CypherCmd(cmd);
        this.currentCmdName = cmd;

        //This is for primitive commands only
        this.attemptCommandExecution(this.currentCmdName);

    }

    private boolean checkIfFunction(String cmd) throws Throwable
    {
        if(hasFunction(cmd) == false)
            return false;

        FunctionInfo fi = this.functions.get(cmd);
        String s = fi.commandsRaw;

        //We don't evaluate the vals. Dynamic Programming Trait.
        String[] vals = extractArguments(fi.argsNum);
        for (int i=0 ; i<fi.argsNum ; i++)
        {
            s = s.replaceAll(fi.argumentsName.get(i),tryParsingValue(vals[i]).toString());
        }

        Executer ex = new Executer(this.variables, this.functions,s,this.controller);
        ex.run();
        return true;
    }

    private void attemptCommandExecution(String command) throws Throwable
    {
        Method m = this.primitives.get(command);
        try {m.invoke(this);} catch (InvocationTargetException ite) { throw ite.getCause(); }
    }






    /*
        INSTRUCTIONS:

        - We assign each command with its corresponding method to invoke. For example:
            fd/forward/امام... all have one principle: move turtle forward. Thus we write a function,
            that whenever we read one of these names, that function is invoked.

        - Instead of assigning each and every String their similar function, we define ONE determinant string
            that will invoke the function, thus any other String with the same function will be replaced with that
            very string, then invokes the function. For example:
            fd/امام/مم/forward all have the same purpose. So instead of assigning all of these the same function, we
            choose "forward" being the one invoking it. all other strings will be recognized as "forward", even by
            overriding their values. That's called "Cyphering".

        - For a case where multiple commands have similar goals, we make one function that does the principle of that goal.
            As for achieving each ones goal individually, we have primitiveParameters that tells us how differentiated it is.
            NOT to be confused with command arguments in the turtle app, primitiveParameters are for configuring primitive
            functions only. Example:
            assume we have the following commands, forward/back/north/south/east/west. Rather than defining a function
            for each and every one of them (primitive_forward, primitive_back, primitive_north...) we make one function
            called "move", and gets parameters according to the command. Lets say its sufficient with only one parameter
            called "Direction". depending on the command, we can trigger Direction to be appropriate:
                forward:    Direction = turtle.directionFacing
                backward:   Direction = - turtle.directionFacing
                north:      Direction = up
                ...
            NOTE: We DO NOT pass a parameter for DISTANCE. Because that comes in the COMMAND ARGUMENTS.

        - Due to irritating Java bugs/implementations, passing a parameter for a primitive function is done by so:
            We take note what the function receives as parameters (could be more than one), and store the correct ones
            to their corresponding commands (so basically Dictionary). EVERY primitive function then gathers the
            parameters from the Dictionary, and then use them accordingly.
     */


//    private void forward() throws ExecuterException
//    { this.move(false); }
//
//    private void backward() throws ExecuterException
//    { this.move(true); }

    private void _primitive_move() throws ExecuterException, ScriptException
    {
        Object[] params = this.extractParams(this.currentCmdName);
        boolean reverse = (Boolean) params[0];

        String[] args = extractArguments(1);
        int steps = (int) tryParsingValue(args[0]);

        if(reverse) steps = -steps;
        this.controller.move(steps);
    }

    private void _primitive_turn() throws ExecuterException, ScriptException
    {
        Object[] params = this.extractParams(this.currentCmdName);
        boolean counterClockwise = (Boolean) params[0];

        String[] args = extractArguments(1);

        int steps = (int) tryParsingValue(args[0]);

        if(counterClockwise) steps = -steps;
        this.controller.turn(steps);
    }

    private void _primitive_turtle_visibility() throws ExecuterException
    {
        Object[] params = this.extractParams(this.currentCmdName);
        boolean visibility = (Boolean) params[0];

        this.controller.setTurtleVisible(visibility);
    }

    private void _primitive_clearscreen() throws ExecuterException
    {
        this.controller.wipeScreen();
    }

    private void _primitive_repeat() throws Throwable
    {
        String[] args = extractArguments(2);
        int loops = (int) tryParsingValue(args[0]);


//        String newArgs = tryParsingBlock(args[1]);

//        Executer exec = new Executer(this.variables,this.functions,newArgs,this.controller);
//        for (int i=0 ; i<loops ; i++)
//        {
//            exec.run();
//        }


        //Instead of running an Executer the repeated code, when it comes to [stop] command things get more complicated.
        //so basically, every Executer does the repeat ON HIS OWN (by concatenating the commands). This will help us with
        //the [stop] in function recursive calls
        for (int i = 0; i < loops; i++)
        {
            this.ConcatBlockToCommands(args[1]);
        }
    }

    private void _primitive_toggle_programming_mode()
    {
        this.programmingMode = true;
        this.controller.setProgrammingMode(true);
    }

    private void _primitive_add_variable() throws ExecuterException, ScriptException
    {
        String[] args = extractArguments(2);

        if(isString(args[0]) == false)
            throw new ExecuterException("ERROR: Expected string, got " + args[0]);

//        if(isNumeric(args[1]) == false)
//            throw new ExecuterException("ERROR: Expected number, got " + args[1]);
//        int val = tryParsingValue(args[1]);

        args[0] = args[0].replace('\"',':');
        args[1] = tryParsingValue(args[1]).toString();
        addVariable(args[0],args[1]);
    }

    private void _primitive_condition() throws ExecuterException, ScriptException, Parser.ParserException
    {
        Object[] params = this.extractParams(this.currentCmdName);
        String flow = (String) params[0];

        String[] args = extractArguments(2);
        boolean cond = (boolean) tryParsingValue(args[0]);

        if(flow.equals("if"))
        {
            String trueBlock = args[1];
            String falseBlock = null;
            if(this.isDone() == false && this.isBlock(this.peekCurrCommand()))
                falseBlock = this.extractArguments(1)[0];

            if(cond)
                ConcatBlockToCommands(trueBlock);
            else if(falseBlock != null)
                ConcatBlockToCommands(falseBlock);
        }
        else if(flow.equals("check"))
        {

        }
        else if (flow.equals(("if_true")))
        {
            if(this.hasDoneCheck == false)
                throw new ExecuterException("bleb");

            if(this.prevConditionFlow == true)
            {

            }
        }
        else if(flow.equals("if_false"))
        {
            if(this.hasDoneCheck == false)
                throw new ExecuterException("bleb");

            if(this.prevConditionFlow == false)
            {

            }
        }
        else
        {

        }
    }

    private void _primitive_stop()
    {
        this.done();
    }

    private void _primitive_print() throws ScriptException, ExecuterException
    {
        Object[] params = this.extractParams(this.currentCmdName);
        boolean inline = (Boolean) params[0];

        String[] args = extractArguments(1);
        String toPrint = tryParsingValue(args[0]).toString();


        System.out.print(toPrint);
        if(inline == false)
            System.out.println();
    }


}

/*

func tree :size :factor
if :size<1 [stop]
fd :size*:factor
rt 45
tree :size-1 :factor
lf 90
tree :size-1 :factor
rt 45
bk :size*:factor
end

 */