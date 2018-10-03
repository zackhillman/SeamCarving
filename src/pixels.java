import java.awt.*;

// represents a pixel in a grid
class Pixel {
  Color c;
  Pixel up;
  Pixel down;
  Pixel left;
  Pixel right;
  SeamInfo seamInfo;
  double brightness;

  // creates a pixel leaving all other pixels empty;
  public Pixel(Color c) {
    this.c = c;
    double avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    brightness = avg / 255;
  }

  // return the pixel downRight to this
  public Pixel downRight() {
    if (this.down == null) {
      return null;
    }
    else {
      return this.down.right;
    }
  }

  // return the pixel topRight to this
  public Pixel topRight() {
    if (this.up == null) {
      return null;
    }
    else {
      return this.up.right;
    }
  }

  // return the pixel downLeft to this
  public Pixel downLeft() {
    if (this.down == null) {
      return null;
    }
    else {
      return this.down.left;
    }
  }

  // return the pixel topLeft to this
  public Pixel topLeft() {
    if (this.up == null) {
      return null;
    }
    else {
      return this.up.left;
    }
  }


  // return the energy of this pixel
  double energy() {

    // brightness values
    double brU = 0;
    double brD = 0;
    double brR = 0;
    double brL = 0;
    double brDR = 0;
    double brTR = 0;
    double brDL = 0;
    double brTL = 0;
    if (this.up != null) {
      brU = this.up.brightness;
    }
    if (this.down != null) {
      brD = this.down.brightness;
    }

    if (this.right != null) {
      brR = this.right.brightness;
    }

    if (this.left != null) {
      brL = this.left.brightness;
    }

    if (this.downRight() != null) {
      brDR = this.downRight().brightness;
    }

    if (this.downLeft() != null) {
      brDL = this.downLeft().brightness;
    }

    if (this.topLeft() != null) {
      brTL = this.topLeft().brightness;
    }

    if (this.topRight() != null) {
      brTR = this.topRight().brightness;
    }


    double vertEnergy = (brTL + brL + brDL) - (brTR + brR + brDR);
    double horizEnergy = (brTL + brU + brTR) - (brDL + brD + brDR);

    return Math.sqrt((vertEnergy * vertEnergy) + (horizEnergy * horizEnergy));

  }

  // setup the Vertical seam info for this pixel
  // assumes that the pixels above have seam info calculated
  void calcVertSeamInfo() {
    SeamInfo tr = null;
    SeamInfo tl = null;
    SeamInfo t = null;
    if (this.topRight() != null) {
      tr = topRight().seamInfo;
    }
    if (this.topLeft() != null) {
      tl = topLeft().seamInfo;
    }
    if (this.up != null) {
      t = up.seamInfo;
    }

    double min = Double.MAX_VALUE;
    SeamInfo cameFrom = null;

    if (tr != null && min > tr.totalWeight) {
      min = tr.totalWeight;
      cameFrom = tr;
    }
    if (tl != null && min > tl.totalWeight) {
      min = tl.totalWeight;
      cameFrom = tl;
    }
    if (t != null && min > t.totalWeight) {
      min = t.totalWeight;
      cameFrom = t;
    }

    // for top border cases
    if (cameFrom == null) {
      min = 0;
    }

    this.seamInfo = new SeamInfo(this, min + this.energy(), cameFrom, true);
  }

  // setup the Horizontal seam info for this pixel
  // assumes that the pixels to the right have seam info calculated
  void calcHorizSeamInfo() {
    SeamInfo tl = null;
    SeamInfo dl = null;
    SeamInfo l = null;
    if (this.topLeft() != null) {
      tl = topLeft().seamInfo;
    }
    if (this.downLeft() != null) {
      dl = downLeft().seamInfo;
    }
    if (this.left != null) {
      l = left.seamInfo;
    }

    double min = Double.MAX_VALUE;
    SeamInfo cameFrom = null;

    if (tl != null && min > tl.totalWeight) {
      min = tl.totalWeight;
      cameFrom = tl;
    }
    if (dl != null && min > dl.totalWeight) {
      min = dl.totalWeight;
      cameFrom = dl;
    }
    if (l != null && min > l.totalWeight) {
      min = l.totalWeight;
      cameFrom = l;
    }

    // for top border cases
    if (cameFrom == null) {
      min = 0;
    }

    this.seamInfo = new SeamInfo(this, min + this.energy(), cameFrom, false);
  }

  // remove the given pixel
  // expects this to be the next in a vertical seam
  //EFFECT : mutates the directions of toRemove and its neighbors
  void removeVert(Pixel toRemove) {

    // is the given below this
    if (this.down == toRemove) {
      if (toRemove.right != null) {
        toRemove.right.left = toRemove.left;
      }
      if (toRemove.left != null) {
        toRemove.left.right = toRemove.right;
      }
    }

    // is the given downright to this
    else if (this.downRight() == toRemove) {
      if (toRemove.right != null) {
        toRemove.right.left = toRemove.left;
      }
      if (toRemove.left != null) {
        toRemove.left.right = toRemove.right;
        toRemove.left.up = toRemove.up;
      }
      if (toRemove.up != null) {
        toRemove.up.down = toRemove.left;
      }
    }

    //is the given downleft to this
    else if (this.downLeft() == toRemove) {
      if (toRemove.right != null) {
        toRemove.right.left = toRemove.left;
      }
      if (toRemove.left != null) {
        toRemove.left.right = toRemove.right;
      }
      if (toRemove.right != null) {
        toRemove.right.up = toRemove.up;
      }
      if (toRemove.up != null) {
        toRemove.up.down = toRemove.right;
      }
    }
    // if the connections were incorrect
    else {
      throw new RuntimeException("Illegal vertical seam");
    }
  }

  // remove the given pixel
  // expects this to be the next in a horizontal seam
  //EFFECT : mutates the directions of toRemove and its neighbors
  void removeHoriz(Pixel toRemove) {

    if (this.right == toRemove) {
      if (toRemove.up != null) {
        toRemove.up.down = toRemove.down;
      }
      if (toRemove.down != null) {
        toRemove.down.up = toRemove.up;
      }
    }

    else if (this.topRight() == toRemove) {
      if (toRemove.up != null) {
        toRemove.up.down = toRemove.down;
      }
      if (toRemove.down != null) {
        toRemove.down.up = toRemove.up;
      }
      if (toRemove.down != null) {
        toRemove.down.left = toRemove.left;
      }
      if (toRemove.left != null) {
        toRemove.left.right = toRemove.down;
      }
    }
    else if (this.downRight() == toRemove) {
      if (toRemove.up != null) {
        toRemove.up.down = toRemove.down;
      }
      if (toRemove.down != null) {
        toRemove.down.up = toRemove.up;
      }
      if (toRemove.up != null) {
        toRemove.up.left = toRemove.left;
      }
      if (toRemove.left != null) {
        toRemove.left.right = toRemove.up;
      }
    }
    else {
      throw new RuntimeException("Illegal disconnected seam");
    }
  }
}

//Represents a seam Info
class SeamInfo {
  Pixel p;
  double totalWeight;
  SeamInfo cameFrom;
  // represents the direction of the seam
  boolean vertical;

  public SeamInfo(Pixel p, double totalWeight, SeamInfo cameFrom, boolean vertical) {
    this.p = p;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
    this.vertical = vertical;
  }

  int length() {
    if (this.cameFrom == null) {
      return 1;
    }
    else {
      return 1 + this.cameFrom.length();
    }
  }

  void colorSeam() {
    this.p.c = Color.RED;
    if (this.cameFrom != null) {
      this.cameFrom.colorSeam();
    }
  }
}
