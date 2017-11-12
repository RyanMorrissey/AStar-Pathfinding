// Author: Ryan Morrissey
// Date: 10/24/2017
// CSCI 331 Project 1
// A* Algorithm

import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Project1
{
  public static final double XCOST = 10.29;
  public static final double YCOST = 7.55;
  public static final double DCOST = 12.76;

  // The class for the node object
  static class Node
  {
    String color; // The pixel color in hex
    double difficulty; // The scaling for the distance heuristic
    double elevation; // The nodes elevation
    double hCost = 0; // The heuristic cost
    double fCost = 0; // The final cost
    int i, j;
    Node parent;

    Node(String color, double elevation, int i, int j)
    {
      this.color = color;
      this.elevation = elevation;
      this.i = i;
      this.j = j;

      // Now to get the difficulty scaling
      if(color.equals("f89412")) // OPEN LAND
        difficulty = 1;
      else if(color.equals("ffc000")) // ROUGH MEADOW
        difficulty = 1.4;
      else if(color.equals("ffffff")) // EASY MOVEMENT FOREST
        difficulty = 1.15;
      else if(color.equals("02d03c")) // SLOW RUN FOREST
        difficulty = 1.35;
      else if(color.equals("028828")) // WALK FOREST
        difficulty = 1.5;
      else if(color.equals("054918")) // IMPASSIBLE VEGETATION
        difficulty = 100;
      else if(color.equals("0000ff")) // LAKE/SWAMP/MARSH
        difficulty = 50;
      else if(color.equals("473303")) // PAVED ROAD
        difficulty = 1;
      else if(color.equals("000000")) // FOOTPATH
        difficulty = 1;
      else if(color.equals("cd0065")) // OUT OF BOUNDS
        difficulty = 1000000000;
    }

    @Override
    public String toString()
    {
      return "[" + this.i + ", " + this.j + "]";
    }
  }

  // Declarations for what will be needed by the program
  static Node [][] map = new Node[500][395]; // The Board
  static boolean closed[][]; // The explored nodes
  static PriorityQueue<Node> open;  // The open nodes to be explored
  static int startI, startJ, endI, endJ;
  static BufferedImage terrain;

  // Set the starting node
  public static void setStart(int i, int j)
  {
    startI = i;
    startJ = j;
  }

  // Set the goal node to find
  public static void setGoal(int i, int j)
  {
    endI = i;
    endJ = j;
  }

  // This will check the target nodes information and update their
  // fCost and parent
  static void check(Node current, Node n, double cost)
  {
    if(n == null || closed[n.i][n.j])
      return;
    double nFCost = n.hCost + cost;
    boolean inOpen = open.contains(n);
    if(!inOpen || nFCost < n.fCost)
    {
      n.fCost = nFCost;
      n.parent = current;
      if(!inOpen)
        open.add(n);
    }
  }


  // The A* Searching algorithm!
  public static void Project1()
  {
    // Start off by adding the starting location
    open.add(map[startI][startJ]);
    Node current;

    // Where the magic happens
    while(true)
    {
      current = open.poll();
      if(current == null)
        break;

      closed[current.i][current.j] = true; // Add the node to closed

      if(current.equals(map[endI][endJ]))
        return; // We reached the goal

      Node n;
      // These are the necessary checks to prevent going out of bounds
      if(current.i - 1 >= 0) //
      {
        n = map[current.i-1][current.j]; // North neighbor
        check(current, n, current.fCost + (YCOST*n.difficulty
          *getElevationScale(current, n)));

        if(current.j - 1 >= 0)
        {
          n = map[current.i - 1][current.j - 1]; // Northwest neighbor
          check(current, n, current.fCost + (DCOST*n.difficulty
            *getElevationScale(current, n)));
        }

        if(current.j + 1 < map[0].length)
        {
          n = map[current.i - 1][current.j + 1]; // Northeast neighbor
          check(current, n, current.fCost + (DCOST*n.difficulty
            *getElevationScale(current, n)));
        }
      }

      if(current.j - 1 >= 0)
      {
        n = map[current.i][current.j - 1];  // West neighbor
        check(current, n, current.fCost + (XCOST*n.difficulty
          *getElevationScale(current, n)));
      }

      if(current.j + 1 < map[0].length)
      {
        n = map[current.i][current.j+1]; // East neighbor
        check(current, n, current.fCost + (XCOST*n.difficulty
          *getElevationScale(current, n)));
      }

      // Final if statement
      if(current.i + 1 < map.length)
      {
        n = map[current.i + 1][current.j]; // South neighbor
        check(current, n, current.fCost + (YCOST*n.difficulty
          *getElevationScale(current, n)));

        if(current.j - 1 >= 0)
        {
          n = map[current.i + 1][current.j - 1]; // Southwest neighbor
          check(current, n, current.fCost + (DCOST*n.difficulty
            *getElevationScale(current, n)));
        }

        if(current.j + 1 < map[0].length)
        {
          n = map[current.i + 1][current.j + 1]; // Southeast neighbor
          check(current, n, current.fCost + (DCOST*n.difficulty
            *getElevationScale(current, n)));
        }
      }
    }
  }

  // This method will compare two nodes elevations, and return
  // a scaling factor that will either increase or decrease
  // the weight between two nodes.
  // Going upwill will slow you down at a steady rate, capping at .5
  // Going downhill will speed you up at a steady rate, capping at 1.5
  public static double getElevationScale(Node current, Node target)
  {
    double value = current.elevation - target.elevation;
    if(value > 0) // Going down in elevation
    {
      if(value >= -1)
        return 1.1;
      else if(value >= -2)
        return 1.3;
      else
        return 1.5;
    }
    else if(value < 0) // Going up in elevation
    {
      if(value <= 1)
        return 0.9;
      else if(value <= 2)
        return 0.7;
      else
        return 0.5;
    }
    else
      return 1.0; // Same elevation
  }

  // This method will freeze the water pixels if they fulfull a certain
  // criteria.  It will change the water nodes difficulty scalar to 1.15,
  // which is a huge improvement to its old scalar of 50.
  public static void freeze(int i, int j)
  {
    Color ice = new Color(102, 255, 255);
    int rgb = ice.getRGB();
    for(int k = 0; k <= 7; k++)
    {
      if(j - k >= 0) // Check nodes to northwest, west, southwest
      {
        if(i - k >= 0) // Check northwest
        {
          if(map[i-k][j-k].color.equals("0000ff")) // Northwest neighbor
          {
            map[i-k][j-k].difficulty = 1.15;
            terrain.setRGB(j-k, i-k, rgb);
          }
        }
        if(map[i][j-k].color.equals("0000ff")) // West neighbor
        {
          map[i][j-k].difficulty = 1.15;
          terrain.setRGB(j-k, i, rgb);
        }
        if(i + k <= 499) // Check southwest
        {
          if(map[i+k][j-k].color.equals("0000ff")) // Southwest neighbor
          {
            map[i+k][j-k].difficulty = 1.15;
            terrain.setRGB(j-k, i+k, rgb);
          }
        }
      }
      if(i - k >= 0) // Check north
      {
        if(map[i-k][j].color.equals("0000ff")) // North neighbor
        {
          map[i-k][j].difficulty = 1.15;
          terrain.setRGB(j, i-k, rgb);
        }
      }
      if(i + k <= 499) // Check south
      {
        if(map[i+k][j].color.equals("0000ff")) // South neighbor
        {
          map[i+k][j].difficulty = 1.15;
          terrain.setRGB(j, i+k, rgb);
        }
      }
      if(j + k <= 394) // Check nodes to northeast, east, southeast
      {
        if(i - k >= 0) // Check northeast
        {
          if(map[i-k][j+k].color.equals("0000ff")) // Northeast neighbor
          {
            map[i-k][j+k].difficulty = 1.15;
            terrain.setRGB(j+k, i-k, rgb);
          }
        }
        if(map[i][j+k].color.equals("0000ff")) // East neighbor
        {
          map[i][j+k].difficulty = 1.15;
          terrain.setRGB(j+k, i, rgb);
        }
        if(i + k <= 499) // Check southeast
        {
          if(map[i+k][j+k].color.equals("0000ff")) // Southeast neighbor
          {
            map[i+k][j+k].difficulty = 1.15;
            terrain.setRGB(j+k, i+k, rgb);
          }
        }
      }
    }
  }

  // This method will swampify the non-water nodes next to water
  // and change their difficulty scalar to that of water.  It will go a distance
  // of 15 nodes if the elevation allows it.
  public static void swampify(int i, int j)
  {
    Color swamp = new Color(0, 0, 255);
    int rgb = swamp.getRGB();
    double waterElevation = map[i][j].elevation;
    for(int k = 1; k <= 15; k++)
    {
      if(j - k >= 0) // Check nodes to northwest, west, southwest
      {
        if(i - k >= 0) // Check northwest
        { // The && is to keep things higher than 50 unchanged, such as Impassible Vegetation and OOB
          if(map[i-k][j-k].elevation - waterElevation < 1 && map[i-k][j-k].difficulty <= 50) // Northwest neighbor
          {
            map[i-k][j-k].difficulty = 50;
            terrain.setRGB(j-k, i-k, rgb);
          }
        }
        if(map[i][j-k].elevation - waterElevation < 1 && map[i][j-k].difficulty <= 50) // West neighbor
        {
          map[i][j-k].difficulty = 50;
          terrain.setRGB(j-k, i, rgb);
        }
        if(i + k <= 499) // Check southwest
        {
          if(map[i+k][j-k].elevation - waterElevation < 1 && map[i+k][j-k].difficulty <= 50) // Southwest neighbor
          {
            map[i+k][j-k].difficulty = 50;
            terrain.setRGB(j-k, i+k, rgb);
          }
        }
      }
      if(i - k >= 0) // Check north
      {
        if(map[i-k][j].elevation - waterElevation < 1 && map[i-k][j].difficulty <= 50) // North neighbor
        {
          map[i-k][j].difficulty = 50;
          terrain.setRGB(j, i-k, rgb);
        }
      }
      if(i + k <= 499) // Check south
      {
        if(map[i+k][j].elevation - waterElevation < 1 && map[i+k][j].difficulty <= 50) // South neighbor
        {
          map[i+k][j].difficulty = 50;
          terrain.setRGB(j, i+k, rgb);
        }
      }
      if(j + k <= 394) // Check nodes to northeast, east, southeast
      {
        if(i - k >= 0) // Check northeast
        {
          if(map[i-k][j+k].elevation - waterElevation < 1 && map[i-k][j+k].difficulty <= 50) // Northeast neighbor
          {
            map[i-k][j+k].difficulty = 50;
            terrain.setRGB(j+k, i-k, rgb);
          }
        }
        if(map[i][j+k].elevation - waterElevation < 1 && map[i][j+k].difficulty <= 50) // East neighbor
        {
          map[i][j+k].difficulty = 50;
          terrain.setRGB(j+k, i, rgb);
        }
        if(i + k <= 499) // Check southeast
        {
          if(map[i+k][j+k].elevation - waterElevation < 1 && map[i+k][j+k].difficulty <= 50) // Southeast neighbor
          {
            map[i+k][j+k].difficulty = 50;
            terrain.setRGB(j+k, i+k, rgb);
          }
        }
      }
    }
  }

  // The meat of the program.  This initializes everything and calls
  // the A* Algorithm
  public static void run(String pictureFile, String terrainFile,
    String courseFile, String season) throws IOException
  {
    map = new Node[500][395];
    closed = new boolean[500][395];
    // Now to parse the info and generate the map
    terrain = ImageIO.read(new File(pictureFile));
    Scanner scanner = new Scanner(new File(terrainFile));
    for(int i = 0; i < 500; i++)
    {
      Scanner scanner2 = new Scanner(scanner.nextLine());
      for(int j = 0; j < 395; j++)
      {
        Color c = new Color(terrain.getRGB(j,i));
        map[i][j] = new Node(String.format("%02x%02x%02x", c.getRed(),
          c.getGreen(), c.getBlue()), Double.parseDouble(scanner2.next()), i, j);
      }
    }

    // Update map based on season
    if(season.equalsIgnoreCase("Fall")) // Fall, so update paths between easy-movement forest
    {
      Color forest = new Color(255, 255, 255);
      int rgbFall = forest.getRGB();
      for(int i = 0; i < 500; i++)
      {
        for(int j = 0; j < 395; j++)
        { // If map node is footpath
          if(map[i][j].color.equals("473303"))
          {
            if(j - 1 >= 0) // Check nodes to northwest, west, southwest
            {
              if(i - 1 >= 0) // Check northwest
              {
                if(map[i-1][j-1].color.equals("ffffff")) // Northwest neighbor
                {
                  map[i][j].difficulty = 1.15;
                  terrain.setRGB(j, i, rgbFall);
                }
              }
              if(map[i][j-1].color.equals("ffffff")) // West neighbor
              {
                map[i][j].difficulty = 1.15;
                terrain.setRGB(j, i, rgbFall);
              }
              if(i + 1 <= 499) // Check southwest
              {
                if(map[i+1][j-1].color.equals("ffffff")) // Southwest neighbor
                {
                  map[i][j].difficulty = 1.15;
                  terrain.setRGB(j, i, rgbFall);
                }
              }
            }
            if(i - 1 >= 0) // Check north
            {
              if(map[i-1][j].color.equals("ffffff")) // North neighbor
              {
                map[i][j].difficulty = 1.15;
                terrain.setRGB(j, i, rgbFall);
              }
            }
            if(i + 1 <= 499) // Check south
            {
              if(map[i+1][j].color.equals("ffffff")) // South neighbor
              {
                map[i][j].difficulty = 1.15;
                terrain.setRGB(j, i, rgbFall);
              }
            }
            if(j + 1 <= 394) // Check nodes to northeast, east, southeast
            {
              if(i - 1 >= 0) // Check northeast
              {
                if(map[i-1][j+1].color.equals("ffffff")) // Northeast neighbor
                {
                  map[i][j].difficulty = 1.15;
                  terrain.setRGB(j, i, rgbFall);
                }
              }
              if(map[i][j+1].color.equals("ffffff")) // East neighbor
              {
                map[i][j].difficulty = 1.15;
                terrain.setRGB(j, i, rgbFall);
              }
              if(i + 1 <= 499) // Check southeast
              {
                if(map[i+1][j+1].color.equals("ffffff")) // Southeast neighbor
                {
                  map[i][j].difficulty = 1.15;
                  terrain.setRGB(j, i, rgbFall);
                }
              }
            }
          }
        }
      }
    }
    if(season.equalsIgnoreCase("Winter")) // Winter, so freeze some water to make it easy to cross
    {
      for(int i = 0; i < 500; i++)
      {
        for(int j = 0; j < 395; j++)
        { // If map node is water
          if(map[i][j].color.equals("0000ff"))
          {
            if(j - 1 >= 0) // Check nodes to northwest, west, southwest
            {
              if(i - 1 >= 0) // Check northwest
              {
                if(map[i-1][j-1].color.equals("0000ff")) // Northwest neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  // I am going to used closed early to prevent
                  // freeze from being called multiple times for the
                  // same water pixel
                  if(closed[i][j] == false)
                  {
                    freeze(i, j); // Call the freeze method
                    closed[i][j] = true;
                  }
                }
              }
              if(map[i][j-1].color.equals("0000ff")) // West neighbor
              {
                // Do nothing
              }
              else
              {
                if(closed[i][j] == false)
                {
                  freeze(i, j);
                  closed[i][j] = true;
                }
              }
              if(i + 1 <= 499) // Check southwest
              {
                if(map[i+1][j-1].color.equals("0000ff")) // Southwest neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    freeze(i, j); // Call the freeze method
                    closed[i][j] = true;
                  }
                }
              }
            }
            if(i - 1 >= 0) // Check north
            {
              if(map[i-1][j].color.equals("0000ff")) // North neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  freeze(i, j); // Call the freeze method
                  closed[i][j] = true;
                }
              }
            }
            if(i + 1 <= 499) // Check south
            {
              if(map[i+1][j].color.equals("0000ff")) // South neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  freeze(i, j); // Call the freeze method
                  closed[i][j] = true;
                }
              }
            }
            if(j + 1 <= 394) // Check nodes to northeast, east, southeast
            {
              if(i - 1 >= 0) // Check northeast
              {
                if(map[i-1][j+1].color.equals("0000ff")) // Northeast neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    freeze(i, j); // Call the freeze method
                    closed[i][j] = true;
                  }
                }
              }
              if(map[i][j+1].color.equals("0000ff")) // East neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  freeze(i, j); // Call the freeze method
                  closed[i][j] = true;
                }
              }
              if(i + 1 <= 499) // Check southeast
              {
                if(map[i+1][j+1].color.equals("0000ff")) // Southeast neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    freeze(i, j); // Call the freeze method
                    closed[i][j] = true;
                  }
                }
              }
            }
          }
        }
      }
    }
    if(season.equalsIgnoreCase("Spring")) // Spring, so make things muddy
    {
      for(int i = 0; i < 500; i++)
      {
        for(int j = 0; j < 395; j++)
        { // If map node is water
          if(map[i][j].color.equals("0000ff"))
          {
            if(j - 1 >= 0) // Check nodes to northwest, west, southwest
            {
              if(i - 1 >= 0) // Check northwest
              {
                if(map[i-1][j-1].color.equals("0000ff")) // Northwest neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  // I am going to used closed early to prevent
                  // freeze from being called multiple times for the
                  // same water pixel
                  if(closed[i][j] == false)
                  {
                    swampify(i, j); // Call the swampify method
                    closed[i][j] = true;
                  }
                }
              }
              if(map[i][j-1].color.equals("0000ff")) // West neighbor
              {
                // Do nothing
              }
              else
              {
                if(closed[i][j] == false)
                {
                  swampify(i, j);
                  closed[i][j] = true;
                }
              }
              if(i + 1 <= 499) // Check southwest
              {
                if(map[i+1][j-1].color.equals("0000ff")) // Southwest neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    swampify(i, j);
                    closed[i][j] = true;
                  }
                }
              }
            }
            if(i - 1 >= 0) // Check north
            {
              if(map[i-1][j].color.equals("0000ff")) // North neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  swampify(i, j);
                  closed[i][j] = true;
                }
              }
            }
            if(i + 1 <= 499) // Check south
            {
              if(map[i+1][j].color.equals("0000ff")) // South neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  swampify(i, j);
                  closed[i][j] = true;
                }
              }
            }
            if(j + 1 <= 394) // Check nodes to northeast, east, southeast
            {
              if(i - 1 >= 0) // Check northeast
              {
                if(map[i-1][j+1].color.equals("0000ff")) // Northeast neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    swampify(i, j);
                    closed[i][j] = true;
                  }
                }
              }
              if(map[i][j+1].color.equals("0000ff")) // East neighbor
              {
                // Do nothing as its a water pixel
              }
              else // meaning its next to a non water pixel
              {
                if(closed[i][j] == false)
                {
                  swampify(i, j);
                  closed[i][j] = true;
                }
              }
              if(i + 1 <= 499) // Check southeast
              {
                if(map[i+1][j+1].color.equals("0000ff")) // Southeast neighbor
                {
                  // Do nothing as its a water pixel
                }
                else // meaning its next to a non water pixel
                {
                  if(closed[i][j] == false)
                  {
                    swampify(i, j);
                    closed[i][j] = true;
                  }
                }
              }
            }
          }
        }
      }
    }
    // This is for the actual iteration

    Scanner scanner3 = new Scanner(new File(courseFile));
    int startX = 0;
    int startY = 0;
    int goalX = 0;
    int goalY = 0;

    Scanner scannerStart = new Scanner(scanner3.nextLine());
    goalY = Integer.parseInt(scannerStart.next());
    goalX = Integer.parseInt(scannerStart.next());
    // Start iterating through the course file
    while(scanner3.hasNextLine())
    {
      // I initially declared closed and open at the start of run.
      // Needless to say it caused a lot of heartache until I fixed it.
      closed = new boolean[500][395];
      open = new PriorityQueue<>((Object o1, Object o2) ->
      {
        Node n1 = (Node)o1;
        Node n2 = (Node)o2;
        return n1.fCost < n2.fCost ? -1 : n1.fCost > n2.fCost ? 1 : 0;
      });
      Scanner scanner4 = new Scanner(scanner3.nextLine());
      // Make the old goal the new start
      startX = goalX;
      startY = goalY;
      goalY = Integer.parseInt(scanner4.next());
      goalX = Integer.parseInt(scanner4.next());

      // Start preparing to run the search
      setStart(startX, startY);
      setGoal(goalX, goalY);
      // Now set the hCost and reset the fCost
      for(int i = 0; i < 500; i++)
      {
        for(int j = 0; j < 395; j++)
        {
          if(map[i][j] != null)
          {
            map[i][j].hCost = Math.abs(i - goalX) + Math.abs(j - goalY);
            map[i][j].fCost = 0;
            map[i][j].parent = null;
          }
        }
      }
      System.out.println("\n\nFinding path between " + endI + "," + endJ + " and " + startI + "," + startJ);
      Project1();
      if(closed[endI][endJ])
      {
        // Print out the path
        System.out.println("Path: ");
        Node current = map[endI][endJ];
        System.out.print(current);
        Color red = new Color(255,0,0);
        int rgb = red.getRGB();
        terrain.setRGB(current.j, current.i, rgb);
        while(current.parent != null)
        {
            System.out.print(" -> "+current.parent);
            current = current.parent;
            terrain.setRGB(current.j, current.i, rgb);
        }
      }
      else System.out.println("No possible path");

      // Now save the changes to display the path.
      File outputfile = new File("Path.png");
      ImageIO.write(terrain, "png", outputfile);
      System.out.println();
    }
  }

  public static void main(String[] args) throws Exception
  {
    Scanner read = new Scanner(System.in);
    System.out.println("Im trusting you to not give me bad user input");
    System.out.print("Enter the map's file name (example - terrain.png):  ");
    String mapFile = read.next();
    System.out.print("Enter the elevation text file name (example - mpp.txt):  ");
    String elevationFile = read.next();
    System.out.print("Enter the course file name (example - white.txt):  ");
    String courseFile = read.next();
    System.out.print("Enter the season (example - Summer/Fall/Winter/Spring):  ");
    String season = read.next();
    long startTime = System.currentTimeMillis();
    // Run the program 
    run(mapFile, elevationFile, courseFile, season);
    long endTime   = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    System.out.println("Runtime:  " + totalTime + " ms");
  }
}
