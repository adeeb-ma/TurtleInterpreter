package tortoise;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;




public class DrawingBoard extends JPanel
{

	final int xOffset;
	final int yOffset;

	ArrayList<int[]> turtlePosHistory;
	ArrayList<Color> turtleColorHistory;
	ArrayList<Boolean> turtleVisibleHistory;
	
	int direction;
	int actualCenter[] = new int [] { 0, 0};
	int actualMax[] = new int[] { 0, 0 };
	Dimension visibleSize;
	boolean isScaling;
	

	public DrawingBoard(Dimension size){
		this.turtlePosHistory = new ArrayList<int[]>();
		this.turtleColorHistory = new ArrayList<Color>();
		this.turtleVisibleHistory = new ArrayList<Boolean>();

		this.visibleSize = new Dimension( 600, 800);
		this.isScaling = false;

		this.xOffset = size.width/2;
		this.yOffset = size.height/2;

		int initPos[] = new int[2];
		initPos[0] = xOffset;
		initPos[1] = yOffset;
		this.turtlePosHistory.add(initPos);

//		int initPos2[] = new int[2];
//		initPos2[0] = 200;
//		initPos2[1] = 300;
//		this.turtlePosHistory.add(initPos2);

		this.setVisible( true );
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		this.setBackground(Color.PINK);

		for(int i=1 ; i< this.turtlePosHistory.size() ; i++)
		{
			int[] a = this.turtlePosHistory.get(i-1);
			int[] b = this.turtlePosHistory.get(i);

//			System.out.println(a[0]+" "+a[1]+" "+b[0]+" "+b[1]);

			g.drawLine(a[0],a[1],b[0],b[1]);
		}

		this.revalidate();
	}

	public void dragTurtle(int posX, int posY)
	{
		int[] newPos = new int[2];
		newPos[0] = posX + xOffset;
		newPos[1] = posY + yOffset;
		this.turtlePosHistory.add(newPos);

		this.repaint();
	}

}

	
	
