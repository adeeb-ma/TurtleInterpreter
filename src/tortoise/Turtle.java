package tortoise;

import java.awt.*;


public class Turtle
{
	private final int CIRCLE_DEGREES = 360;
	private final int STARTING_DEGREE = 90;
	private final Color[] TURTLE_COLOR = new Color[] { Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW };
	
	private double xStart;
	private double yStart;
	private double xPos;
	private double yPos;
	private int degree;
	private int actualColor;
	private boolean isDrawing;
	private boolean isVisible;
	

	public Turtle(double xStartingPos, double yStartingPos ){
		this.xPos = xStartingPos;
		this.yPos = yStartingPos;
		this.degree = STARTING_DEGREE;
		this.actualColor = 0;
		this.isDrawing = true;
		this.isVisible = true;
	}
	

	public void move( int steps ){
		this.xPos += ( Math.cos( Math.PI/180 * degree ) * steps );
		this.yPos += ( Math.sin( Math.PI/180 * degree ) * steps );
		System.out.println( "Turtle :: move\nManaged to move " + steps + " steps.\ny=" + this.yPos + ", x=" + this.xPos + "\n" );
	}
	

	public void turn( int degree ){
		this.degree = this.degree + degree;
		this.degree = this.degree % CIRCLE_DEGREES;
		
		System.out.println( "Turtle :: turn\nTurning " + degree + " Degrees.\nNow facing " +this.degree + "\n" );
	}
	

	public void changeColor( int choice)
	{
		if( choice >= 0 && choice < this.TURTLE_COLOR.length )
		{
			this.actualColor = choice;	
		}
	}

	public Color getColor(){
		return this.TURTLE_COLOR[ actualColor ];
	}
	

	public boolean getPen(){
		return this.isDrawing;
	}
	

	public double getXPos(){
		return this.xPos;
	}


	public double getYPos(){ return this.yPos; }
	

	public int getDirection(){
		return this.degree;
	}
	

	public void setPen( boolean isDrawing ){
		this.isDrawing = isDrawing;
	}

	public boolean getVisibility() { return this.isVisible; }

	public void setVisibility(boolean visibility) { this.isVisible = visibility; }


	public void reset(){
		this.xPos = xStart;
		this.yPos = yStart;
		this.degree = STARTING_DEGREE;
		this.actualColor = 0;
	}


}
