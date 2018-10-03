import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.FromFileImage;

/* Instructions

Spacebar to pause deletion
t        to togle between view modes
r        to toggle random deletion mode
v        to change to vertical deletion
h        to change to horizontal deletion

 */


// represents a world where the seam carving will occur
class SeamCarvingWorld extends World {

  // the image
  PixelGrid image;
  // epresents the mode of the current tick
  boolean colorTick;
  // represents weather the carving is playing
  boolean play;
  // represents which mode the carving is occuring in
  boolean verticalMode;
  boolean randomMode;
  // the next seam to be removed
  SeamInfo next;
  int view = 0;


  public static void main(String[] args) {
    new SeamCarvingWorld(new FromFileImage("2.jpg")).bigBang(1000, 1000, .01);
  }



  // the constructor
  public SeamCarvingWorld(FromFileImage f) {
    image = new PixelGrid(f);
    colorTick = true;
    play = true;
    verticalMode = true;
    next = image.minVertSeam();
    randomMode = false;
  }


  // flips the flags based on keyinput
  public void onKeyEvent(String s) {
    if (s.equals(" ")) {
      play = !play;
    }

    if (s.equals("r")) {
      randomMode = !randomMode;
    }

    if (!randomMode) {
      if (s.equals("v")) {
        verticalMode = true;
      }

      if (s.equals("h")) {
        verticalMode = false;
      }
    }

    if (s.equals("t")) {
      view += 1;
      view %= 3;
    }

  }

  // the onTick handler
  // EFFECT: mutates the image and colortick fields
  public void onTick() {
    // Am i playing
    if (play && image.width >= 1 && image.height >= 1) {
      // is this colorTick
      if (colorTick) {
        next.colorSeam();
      }
      else {
        // delete the next seam
        image.removeSeam(next);

        // randomly select the mode
        if (randomMode) {
          double rand = Math.random();
          verticalMode = rand < 0.5;
        }
        // calculate the next  seam based on mode
        if (verticalMode) {
          next = image.minVertSeam();
        }
        else if (!verticalMode) {
          next = image.minHorizSeam();
        }
      }
      // flip the tick mode
      colorTick = !colorTick;
    }
  }


  // draws the scene based on view mode
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(1000, 1000);
    if (view == 0) {
      ws.placeImageXY(image.draw(), 500, 500);
    }
    else if (view == 1) {
      double max;
      if (verticalMode) {
        max = image.maxVertSeam().totalWeight;
      }
      else {
        max = image.maxHorizSeam().totalWeight;
      }
      ws.placeImageXY(image.drawTotalEnergies(max), 500, 500);
    }
    else {
      ws.placeImageXY(image.drawEnergies(), 500, 500);
    }
    return ws;
  }
}
