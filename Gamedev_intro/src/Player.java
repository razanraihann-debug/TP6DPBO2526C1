import java.awt.*;

public class Player {
    private int posX;
    private int posY;
    private int width;
    private int height;
    private Image image;
    private int velocityY = -0;
    private double angle = 0; // sudut rotasi (derajat), + = hidung ke bawah

    public Player(int posX, int posY, int width, int height, Image image) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.image = image;

        this.velocityY = -0;
    }

    //getter
    public int getPosX() {return posX;}
    public int getPosY() {return posY;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public int getVelocityY() {return velocityY;}
    public Image getImage() {return image;}
    public double getAngle() {return angle;}

    //setter
    public void setPosX(int posX) {this.posX = posX;}
    public void setPosY(int posY) {this.posY = posY;}
    public void setWidth(int width) {this.width = width;}
    public void setHeight(int height) {this.height = height;}
    public void setVelocityY(int velocityY) {this.velocityY = velocityY;}
    public void setImage(Image image) {this.image = image;}
    public void setAngle(double angle) {this.angle = angle;}
}
