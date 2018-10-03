import javalib.worldimages.ComputedPixelImage;
import javalib.worldimages.FromFileImage;

import java.awt.*;
import java.util.ArrayList;

// a wrapper class for the pixel grid
class PixelGrid {
  // the width and height of the image originally
  int width;
  int height;
  // represents an image using the topleft corner of the image;
  private Pixel image;

  // the constructor
  PixelGrid(FromFileImage f) {

    this.width = (int) f.getWidth();
    this.height = (int) f.getHeight();
    ArrayList<ArrayList<Pixel>> pixels;
    pixels = new ArrayList<>();

    // make all the pixels
    for (int x = 0; x < width; x++) {
      ArrayList<Pixel> col = new ArrayList<>();
      for (int y = 0; y < height; y++) {
        col.add(new Pixel(f.getColorAt(x, y)));
      }
      pixels.add(col);
    }

    // make all the connections
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (x < width - 1) {
          pixels.get(x).get(y).right = pixels.get(x + 1).get(y);
        }
        if (x > 0) {
          pixels.get(x).get(y).left = pixels.get(x - 1).get(y);
        }
        if (y > 0) {
          pixels.get(x).get(y).up = pixels.get(x).get(y - 1);
        }
        if (y < height - 1) {
          pixels.get(x).get(y).down = pixels.get(x).get(y + 1);
        }
      }
    }

    // give the 0,0 pixel to the image
    this.image = pixels.get(0).get(0);
  }

  // calculate the minimum vertical seam
  SeamInfo minVertSeam() {
    Pixel p = image;

    // calculate the seam info of all the pixels
    while (p.down != null) {
      Pixel p1 = p;
      while (p1.right != null) {
        p1.calcVertSeamInfo();
        p1 = p1.right;
      }
      p = p.down;
    }
    p = p.up;

    // now all seam infos have been calculated and p points to (0, height)
    SeamInfo min = p.seamInfo;
    while (p.right != null) {
      if (min.totalWeight > p.seamInfo.totalWeight) {
        min = p.seamInfo;
      }
      p = p.right;
    }

    return min;
  }

  // calculate the minimum horizontal seam
  SeamInfo minHorizSeam() {

    Pixel p = image;
    // Calculate the seam info for all the pixels
    while (p.right != null) {
      Pixel p1 = p;
      while (p1.down != null) {
        p1.calcHorizSeamInfo();
        p1 = p1.down;
      }
      p = p.right;
    }
    p = p.left;

    // now all seam infos have been calculated and p points to (width, 0)
    SeamInfo min = p.seamInfo;
    while (p.down != null) {
      if (min.totalWeight > p.seamInfo.totalWeight) {
        min = p.seamInfo;
      }
      p = p.down;
    }
    return min;

  }

  // remove the given seam
  void removeSeam(SeamInfo si) {
    if (si.vertical) {
      removeVertSeamAcc(si.cameFrom, si.p);
    }
    else {
      removeHorizSeamAcc(si.cameFrom, si.p);
    }
  }

  // remove a given vertical seam,
  // private and called only by removeSeam
  // which makes sure that the seam is vertical type
  private void removeVertSeamAcc(SeamInfo si, Pixel prev) {
    si.p.removeVert(prev);

    if (si.cameFrom != null) {
      removeVertSeamAcc(si.cameFrom, si.p);
    }
    if (si.p == this.image) {
      this.image = si.p.right;
      this.image.left = null;
    }
    else {
      if (si.p.right != null) {
        si.p.right.left = si.p.left;
      }
      if (si.p.left != null) {
        si.p.left.right = si.p.right;
      }
    }
  }

  // remove the given horizontal seam from the image
  // private and called only by removeSeam
  // which makes sure that the seam is horizontal type
  private void removeHorizSeamAcc(SeamInfo si, Pixel prev) {
    /* code for removing ahorizontal seam goes here*/
    si.p.removeHoriz(prev);

    if (si.cameFrom != null) {
      removeHorizSeamAcc(si.cameFrom, si.p);
    }
    if (si.p == this.image) {
      this.image = si.p.down;
      this.image.up = null;

    }
    else {
      if (si.p.up != null) {
        si.p.up.down = si.p.down;
      }
      if (si.p.down != null) {
        si.p.down.up = si.p.up;
      }
    }
  }

  // draws the grid of pixels
  ComputedPixelImage draw() {
    ComputedPixelImage cp = new ComputedPixelImage(width, height);

    Pixel p = image;
    int row = 0;
    while (p.down != null) {
      Pixel p1 = p;
      int col = 0;
      while (p1.right != null) {
        cp.setPixel(col, row, p1.c);
        p1 = p1.right;
        col += 1;
      }
      p = p.down;
      row += 1;
    }
    return cp;
  }

  // calculate the maximum energy in the image
  double maxEnergy() {
    Pixel p = image;
    double max = p.energy();
    // calculate the seam info of all the pixels
    while (p.down != null) {
      Pixel p1 = p;
      while (p1.right != null) {
        if (max < p.energy()) {
          max = p.energy();
        }
        p1 = p1.right;
      }
      p = p.down;
    }
    return max;
  }


  // draws the grid of pixels in energy view
  ComputedPixelImage drawEnergies() {
    ComputedPixelImage cp = new ComputedPixelImage(width, height);
    double max = maxEnergy();
    Pixel p = image;
    int row = 0;
    while (p.down != null) {
      Pixel p1 = p;
      int col = 0;
      while (p1.right != null) {
        int val = (int) ((p1.energy() / max) * 255);
        Color c = new Color(val, val, val);
        cp.setPixel(col, row, c);
        p1 = p1.right;
        col += 1;
      }
      p = p.down;
      row += 1;
    }
    return cp;
  }


  // calculate the maximum vertical seam
  SeamInfo maxVertSeam() {
    Pixel p = image;

    // calculate the seam info of all the pixels
    while (p.down != null) {
      Pixel p1 = p;
      while (p1.right != null) {
        p1.calcVertSeamInfo();
        p1 = p1.right;
      }
      p = p.down;
    }
    p = p.up;

    // now all seam infos have been calculated and p points to (0, height)
    SeamInfo max = p.seamInfo;
    while (p.right != null) {
      if (max.totalWeight < p.seamInfo.totalWeight) {
        max = p.seamInfo;
      }
      p = p.right;
    }

    return max;
  }

  // calculate the maximum horizontal seam
  SeamInfo maxHorizSeam() {

    Pixel p = image;
    // Calculate the seam info foar all the pixels
    while (p.right != null) {
      Pixel p1 = p;
      while (p1.down != null) {
        p1.calcHorizSeamInfo();
        p1 = p1.down;
      }
      p = p.right;
    }
    p = p.left;

    // now all seam infos have been calculated and p points to (width, 0)
    SeamInfo max = p.seamInfo;
    while (p.down != null) {
      if (max.totalWeight < p.seamInfo.totalWeight) {
        max = p.seamInfo;
      }
      p = p.down;
    }
    return max;

  }

  // draws the grid of pixels in total energy view
  ComputedPixelImage drawTotalEnergies(double maxWeight) {
    ComputedPixelImage cp = new ComputedPixelImage(width, height);
    //double maxWeight = maxVertSeam().totalWeight;

    Pixel p = image;
    int row = 0;
    while (p.down != null) {
      Pixel p1 = p;
      int col = 0;
      while (p1.right != null) {
        int val = (int) ((p1.seamInfo.totalWeight / maxWeight) * 200);
        Color c = new Color(val, val, val);
        cp.setPixel(col, row, c);
        p1 = p1.right;
        col += 1;
      }
      p = p.down;
      row += 1;
    }
    return cp;
  }


}



