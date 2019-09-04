package tortoise;

import javax.script.ScriptException;

public class Controller
{
    GUI gui;
    private Executer executer;

    private Turtle turtle;

    public Controller() throws ScriptException
    {
        gui = new GUI();
        turtle = new Turtle(0.0,0.0);
        try {executer = new Executer(this, ""); } catch(Exception ignored) {}

        doLoop();
    }

    private void doLoop()
    {
        while(true)
        {
            this.gui.awaitInput();

            try
            {
                String s = this.gui.getEditorTextRaw();
                this.displayLog("Executing...",false);
                this.gui.setEdirorText("");
                this.executer.readCommands(s);
                //TODO: Executer on a seperate thread, to kill immediately when Ctrl+C
                this.executer.run();
                this.executer.readCommands("");
            }
            catch(Parser.ParserException e)
            {
                this.gui.setErrorText(e.getMessage(),true);
                e.printStackTrace();
            }
            catch(Executer.ExecuterException e)
            {
                this.gui.setErrorText(e.getMessage(),true);
                e.printStackTrace();
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws ScriptException
    {
        Controller c = new Controller();
        System.out.println("beb");
    }

    public void displayLog(String s, boolean critical)
    {
        this.gui.setErrorText(s,false);
    }



    public void move(int steps)
    {
        this.turtle.move(steps);
        this.gui.DragTurtle((int)this.turtle.getXPos(),(int)this.turtle.getYPos());
        //Draw on graph
    }
    public void turn(int degs)
    {
        this.turtle.turn(degs);
        //Draw on graph
    }


    public void setTurtleVisible(boolean visibility)
    {
        this.turtle.setVisibility(visibility);

        //display on graph
    }

    public void wipeScreen() { System.out.println("Cleaned screen and resetter turtle position"); }

    public void setProgrammingMode(boolean flag)
    {
        if(flag == true)
            this.gui.setProgrammingMode();
        else
            this.gui.setUserMode();
    }

}
