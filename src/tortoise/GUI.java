package tortoise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GUI implements KeyListener
{

//    private final String[] CONTROL_BUTTONS = new String[] { "Reset", "Run", "Step" };

    private final Dimension WINDOW_SIZE = new Dimension( 800, 600 );
    private final Dimension WINDOW_MINIMUM_SIZE = new Dimension( 700, 320 );
    private final double GRAPHIC_EDITOR_RATIO = 0.9;
    private final int DIVIDER_THICKNESS = 5;


    private final JFrame mainFrame;
    private final JPanel centerPane;
    private final JSplitPane centerSplitPane;


    private final JLabel errorText;
    private final JPanel editorPane;
    private final JPanel graphicsOutput;
    private final DrawingBoard graphDrawingBoard;
    private final JTextArea editor;
//    private final JPanel editorButtonsPane;
//    private final JButton[] editorButtons;

//    private String lastPressedButton;

    private boolean programmingMode;


    public GUI()
    {
        this.mainFrame = new JFrame("Tortoise");
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(WINDOW_SIZE);
        this.mainFrame.setMinimumSize(WINDOW_MINIMUM_SIZE);




        //********************************************** Center Pane that holds everything
        this.centerPane = new JPanel();
//        this.centerPane.setLayout( new GridLayout( 1, 2 ) );
        this.centerPane.setLayout( new BorderLayout() );

        //********************************************** Center SPLIT Pane. Similar to the previous one
        this.centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.centerSplitPane.setResizeWeight(GRAPHIC_EDITOR_RATIO);
        this.centerSplitPane.setEnabled(true);
        this.centerSplitPane.setDividerSize(DIVIDER_THICKNESS);



        //********************************************** Graphics Panel
        this.graphicsOutput = new JPanel();
        this.graphicsOutput.setLayout( new BorderLayout() );
//        this.centerPane.add(this.graphicsOutput);
        this.centerSplitPane.add(this.graphicsOutput);

        //********************************************** Graphics drawing board
        this.graphDrawingBoard = new DrawingBoard(WINDOW_SIZE);
        this.graphicsOutput.add(  this.graphDrawingBoard );


        //********************************************** Error Console
        this.errorText = new JLabel();
        this.errorText.setBackground(Color.LIGHT_GRAY);
        this.errorText.setOpaque(true);
        this.errorText.setText("Welcome to Tortoise");
        this.graphicsOutput.add(this.errorText, BorderLayout.SOUTH);

        //********************************************** Editor Panel
        this.editorPane = new JPanel();
        this.editorPane.setLayout( new BorderLayout() );
        this.centerPane.add(this.editorPane);
        this.centerSplitPane.add(this.editorPane);


        //********************************************** TextArea
        this.editor = new JTextArea();
        this.editor.setTabSize(4);
        this.editorPane.add(new JScrollPane( this.editor), BorderLayout.CENTER);
        this.editor.addKeyListener(this);

        //For arabic/hebrew use
//        this.editor.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);


//        //********************************************** Buttons
//        this.editorButtonsPane = new JPanel();
//        this.editorButtonsPane.setLayout( new GridLayout(3,1) );
//        this.editorPane.add(this.editorButtonsPane, BorderLayout.EAST);
//
//        this.editorButtons = new JButton[this.CONTROL_BUTTONS.length];
//        for( int i = 0; i < CONTROL_BUTTONS.length; i++ )
//        {
//            this.editorButtons[i] = new JButton();
//            this.editorButtons[i].setText(this.CONTROL_BUTTONS[i]);
//            this.editorButtons[i].addActionListener(this);
//            this.editorButtonsPane.add( this.editorButtons[i] );
//        }

//        this.mainFrame.getContentPane().add( this.centerPane, BorderLayout.CENTER );
        this.mainFrame.getContentPane().add( this.centerSplitPane, BorderLayout.CENTER );
        this.mainFrame.setVisible(true);
        this.programmingMode = false;


    }

    public void setErrorText(String s,boolean critical)
    {
        if(critical)
            setErrorText(s, Color.RED,Color.PINK);
        else
            setErrorText(s, Color.BLACK,Color.LIGHT_GRAY);
    }

    public void setErrorText(String s,Color txt , Color bg)
    {
        this.errorText.setBackground(bg);
        this.errorText.setForeground(txt);
        this.errorText.setText(s);
    }



    public String getEditorTextRaw()
    {
        return this.editor.getText();
    }
    public void setEdirorText(String s)
    {
        this.editor.setText(s);
    }


    public void setProgrammingMode()
    {
        //Fill up the whole screen with text, like Notepad.

//        this.mainFrame.getContentPane().removeAll();
//        this.mainFrame.getContentPane().add( this.centerPane, BorderLayout.CENTER );
////        this.mainFrame.setContentPane(this.centerPane);
//        this.mainFrame.repaint();
//        this.mainFrame.setVisible(true);
        this.programmingMode = true;
    }

    public void setUserMode()
    {
        //Bring it back to normal
//        this.mainFrame.removeAll();
//        this.mainFrame.getContentPane().add( this.centerSplitPane, BorderLayout.CENTER );
//        this.mainFrame.setVisible(true);
        this.programmingMode = false;
    }

    public void DragTurtle(int x, int y)
    {
        this.graphDrawingBoard.dragTurtle(x,y);
    }
    



    @Override
    public void keyTyped(KeyEvent e)
    {
        if(e.getKeyChar() == KeyEvent.VK_ENTER && this.programmingMode == false
            || e.getKeyChar() == KeyEvent.VK_ESCAPE && this.programmingMode == true)
        {
            synchronized( this )
            {
                this.notifyAll();
            }
        }

        //TODO: If Ctrl+C was pressed, Stop the Executer immediately
    }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }



    public synchronized void awaitInput()
    {
//        while (this.enterPressed == false)
//        {
            try
            {
                this.wait();
            } catch (InterruptedException ex)
            {
                ex.getStackTrace();
            }
//        }
    }
}
