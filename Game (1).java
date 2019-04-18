import java.util.ArrayList; 
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;


class Game extends World {
  int cellsWide;
  int cellsHigh;
  Integer numOfMines;
  ArrayList<ArrayList<Cell>> grid;
  int flagsPlaced; 
  int flagsOnMines; // how many flags have been properly placed on mines?
  Random rand; // Random int generator
  boolean wonGame; 
  boolean lostGame; 

  public static final int CELL_DIMENSIONS = 40;
  static final ArrayList<Color> NUMBER_COLORS = new ArrayList<>(
      Arrays.asList(Color.DARK_GRAY, Color.cyan, Color.GREEN, Color.RED, Color.blue,
          Color.pink, Color.CYAN, Color.magenta, Color.black));

  // Constructor
  Game(int cellsWide, int cellsHigh, Integer numMines) {
    this.cellsWide = cellsWide;
    this.cellsHigh = cellsHigh;
    this.numOfMines = numMines;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.flagsPlaced = 0;
    this.rand = new Random();
    this.flagsOnMines = 0;
    this.wonGame = false;
    this.lostGame = false;

    if (numMines > cellsWide * cellsHigh) {
      throw new IllegalArgumentException("Not enough cells");
    }

    this.initGrid();
    this.findNeighbors();
    this.addMines();
    this.updateNeighbors();
  }

  // Constructor that takes in Random seed
  Game(int cellsWide, int cellsHigh, Integer numMines, Random rand) {
    this.cellsWide = cellsWide;
    this.cellsHigh = cellsHigh;
    this.numOfMines = numMines;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.flagsPlaced = 0;
    this.rand = rand;
    this.flagsOnMines = 0;
    this.wonGame = false;
    this.lostGame = false;

    if (numMines > cellsWide * cellsHigh) {
      throw new IllegalArgumentException("Big no no");
    }
  }

  // Initializes minesweeper board
  public void initGrid() {
    for (int x = 0; x < this.cellsWide; x += 1) {
      this.grid.add(new ArrayList<Cell>());
      for (int y = 0; y < this.cellsHigh; y += 1) {
        this.grid.get(x).add(new Cell());
      }
    }
  }

  // Adds number of mines given to random places on board
  public void addMines() {
    int minesPlaced = 0;
    while (minesPlaced < this.numOfMines) {
      int x = this.rand.nextInt(this.cellsWide - 1);
      int y = this.rand.nextInt(this.cellsHigh - 1);

      if (!this.grid.get(x).get(y).hasMine) {
        this.grid.get(x).get(y).addMine();
        minesPlaced += 1;
      }
    }
  }

  // Finds neighbors of each cell
  public void findNeighbors() {
    for (int i = 0; i < this.cellsWide; i += 1) {
      for (int j = 0; j < this.cellsHigh; j += 1) {
        this.grid.get(i).get(j).addNeighbors(this.grid, i, j, this.cellsWide, this.cellsHigh);
      }
    }
  }

  // Draws function
  public WorldScene makeScene() {

    int width = this.cellsWide * CELL_DIMENSIONS + 40;
    int gameHeight = this.cellsHigh * CELL_DIMENSIONS + 40;

    WorldImage background = new RectangleImage(width, gameHeight, OutlineMode.SOLID,
        Color.LIGHT_GRAY);

    WorldImage winText = new TextImage("Winner Winner Chicken Dinner!", 30, Color.WHITE);
    WorldImage loseText = new TextImage("Super Loser!", 30, Color.RED);

    WorldImage endBackgroundWin = new RectangleImage(width - CELL_DIMENSIONS, 
        gameHeight - CELL_DIMENSIONS, OutlineMode.SOLID,
        Color.GREEN);
    WorldImage endBackgroundLose = new RectangleImage(width - CELL_DIMENSIONS, 
        gameHeight - CELL_DIMENSIONS, OutlineMode.SOLID,
        Color.BLACK);

    WorldImage winScene = new OverlayImage(winText, endBackgroundWin);
    WorldImage loseScene = new OverlayImage(loseText, endBackgroundLose);

    WorldScene worldScene = new WorldScene(width, gameHeight); // Game Scene
    worldScene.placeImageXY(background, width / 2, gameHeight / 2);

    for (int i = 0; i < this.cellsWide; i += 1) {
      for (int j = 0; j < this.cellsHigh; j += 1) {
        worldScene.placeImageXY(this.grid.get(i).get(j).drawCell(), (i * CELL_DIMENSIONS) + 40,
            (j * CELL_DIMENSIONS) + 40);
      }
    }
    // Winning Scene
    if (this.wonGame) {
      worldScene.placeImageXY(winScene, width / 2, gameHeight / 2);
    }
    // Losing Scene
    if (this.lostGame) {
      worldScene.placeImageXY(loseScene, width / 2, gameHeight / 2);
    }
    return worldScene;
  }


  // Mouse Event Listener
  public void onMouseClicked(Posn pos, String buttonName) {

    int row = (pos.x - 20) / CELL_DIMENSIONS;
    int col = (pos.y - 20) / CELL_DIMENSIONS;
    if (!this.validMousePos(row, col) || pos.x < 20 || pos.y < 20) {
      return;
    }
    // left click
    if ((buttonName.equals("LeftButton")) && (!this.grid.get(row).get(col).isFlagged)) {
      this.leftMouseClickHelper(row, col);
    }
    // right click
    if (buttonName.equals("RightButton")) {
      this.rightMouseClickHelper(row, col);
    }
  }

  // checks if click was in game bounds
  boolean validMousePos(int row, int col) {
    return row >= 0 && col >= 0 && row < this.cellsWide && col < this.cellsHigh;
  }

  // handles left mouse click
  void leftMouseClickHelper(int row, int col) {
    this.grid.get(row).get(col).isVisible = true;
    if (this.grid.get(row).get(col).numberOfMines == 0
        && !this.grid.get(row).get(col).hasMine) {
      this.grid.get(row).get(col).flood();
    }

    // touched mine
    if (this.grid.get(row).get(col).hasMine) {
      this.lostGame = true;
    }
  }

  // handles right mouse click
  void rightMouseClickHelper(int row, int col) {
    if (this.wonGame || this.lostGame) {
      return;
    }
    this.grid.get(row).get(col).toggleFlag();
    if (!this.grid.get(row).get(col).isFlagged) {
      this.flagsPlaced -= 1;
    }
    else {
      this.flagsPlaced += 1;
      if (this.grid.get(row).get(col).hasMine) {
        this.flagsOnMines += 1;
      }
    }

    // winning cond
    if (this.flagsOnMines == this.numOfMines && this.flagsPlaced == this.numOfMines) {
      this.grid.get(row).get(col).flood();
      this.wonGame = true;
    }
  }

  // give each cell # of neighbors
  public void updateNeighbors() {
    for (int i = 0; i < this.cellsWide; i += 1) {
      for (int j = 0; j < this.cellsHigh; j += 1) {
        this.grid.get(i).get(j).numberOfMines = this.grid.get(i).get(j)
            .countNeighbors();
      }
    }
  }
}

// Represents a Cell on the board
class Cell {
  boolean hasMine; 
  boolean isVisible; 
  boolean isFlagged;
  ArrayList<Cell> neighbors;
  int numberOfMines; 

  public static final int CELL_DIMENSION = 40;

  // Blank Constructor
  Cell() {
    this.hasMine = false;
    this.isVisible = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
    this.numberOfMines = 0;
  }

  // Draws worldImage of cell
  public WorldImage drawCell() {

    // Cell Mine Count
    WorldImage cellMineCount = new TextImage(this.numberOfMines + "", 
        CELL_DIMENSION * 3 / 4, FontStyle.BOLD, Game.NUMBER_COLORS.get(this.numberOfMines));


    //Covered cell
    WorldImage cellTopCov = new RectangleImage(CELL_DIMENSION - 2, 
        CELL_DIMENSION - 2, OutlineMode.SOLID, Color.LIGHT_GRAY);
    WorldImage cellBotCov = new RectangleImage(CELL_DIMENSION, 
        CELL_DIMENSION, OutlineMode.SOLID, Color.BLACK);
    WorldImage cellCovered = new OverlayImage(cellTopCov, cellBotCov);

    // Covered with flag
    WorldImage flag = new EquilateralTriangleImage(Cell.CELL_DIMENSION / 3, 
        "solid", Color.orange);
    WorldImage cellFlagCovered = new OverlayImage(flag, cellCovered);

    //Uncovered cell
    WorldImage cellTop = new RectangleImage(38, 38, OutlineMode.SOLID, Color.WHITE);
    WorldImage cellBottom = new RectangleImage(40, 40, OutlineMode.SOLID, Color.BLACK);
    WorldImage cellBlankUncovered = new OverlayImage(cellTop, cellBottom);
    WorldImage cellUncovered = new OverlayImage(cellMineCount, cellBlankUncovered);

    if (this.isVisible) {
      if (this.hasMine) {
        return cellUncovered;
      }
      else if (this.numberOfMines == 0) {
        return cellBlankUncovered;
      }
      else {
        return cellUncovered;
      }
    }
    else {
      if (this.isFlagged) {
        return cellFlagCovered;
      }
      else {
        return cellCovered;
      }
    }
  }

  // Converts this Cell into a Mine Cell
  public void addMine() {
    this.hasMine = true;
  }

  // Toggles state of hasFlag of this cell
  public void toggleFlag() {
    if (this.isFlagged) {
      this.isFlagged = false;
    }
    else {
      this.isFlagged = true;
    }
  }

  // Adds neighbors to cell's neighbor list, if cell exists 
  void addNeighbors(ArrayList<ArrayList<Cell>> cellList, 
      int xIndex, int yIndex, int rows, int columns) {

    // checks for individual positions to see if they exist 
    if (xIndex - 1 >= 0 && yIndex - 1 >= 0) {
      this.neighbors.add(cellList.get(xIndex - 1).get(yIndex - 1));
    }
    if (xIndex - 1 >= 0) {
      this.neighbors.add(cellList.get(xIndex - 1).get(yIndex));
    }
    if (xIndex - 1 >= 0 && yIndex + 1 < columns) {
      this.neighbors.add(cellList.get(xIndex - 1).get(yIndex + 1));
    }
    if (yIndex - 1 >= 0) {
      this.neighbors.add(cellList.get(xIndex).get(yIndex - 1));
    }
    if (yIndex + 1 < columns) {
      this.neighbors.add(cellList.get(xIndex).get(yIndex + 1));
    }
    if (xIndex + 1 < rows && yIndex - 1 >= 0) {
      this.neighbors.add(cellList.get(xIndex + 1).get(yIndex - 1));
    }
    if (xIndex + 1 < rows) {
      this.neighbors.add(cellList.get(xIndex + 1).get(yIndex));
    } 
    if (xIndex + 1 < rows && yIndex + 1 < columns) {
      this.neighbors.add(cellList.get(xIndex + 1).get(yIndex + 1));
    }
  }

  // Method to handle flood effect
  public void flood() {
    for (int i = 0; i < this.neighbors.size(); i += 1) {
      if (neighbors.get(i).numberOfMines == 0 && !neighbors.get(i).isVisible) {
        neighbors.get(i).isVisible = true;
        neighbors.get(i).flood();
      }
      else {
        neighbors.get(i).isVisible = true;
      }
    }
  }

  // Gets the # of cell's neighbors
  public int countNeighbors() {
    int mineCount = 0;
    for (int i = 0; i < this.neighbors.size(); i += 1) {
      if (this.neighbors.get(i).hasMine) {
        mineCount += 1;
      }
    }
    return mineCount;
  }

}


class ExamplesMinesweeper {

  Cell blankCell;
  Cell mineCell;

  ArrayList<Cell> mtList;
  ArrayList<Cell> blankList;
  ArrayList<Cell> mineList;

  ArrayList<Cell> list10;
  ArrayList<Cell> list11;
  ArrayList<Cell> list12;

  ArrayList<Cell> list20;
  ArrayList<Cell> list21;
  ArrayList<Cell> list22;

  // 2D array lists
  ArrayList<ArrayList<Cell>> twoD1;
  ArrayList<ArrayList<Cell>> twoD2;

  // for testing m1
  ArrayList<ArrayList<Cell>> three;
  // for testing m0
  ArrayList<ArrayList<Cell>> nine;

  Cell cellA;
  Cell cellB;
  Cell cellC;
  Cell cellD;
  
  Game easy;
  Game normal;
  Game original;
  
  Game mineTestA;
  Game mineTestB;
  Game mineTestC;

  Random r1;
  Random r2;
  Random r3;

  // initializes data
  void initData() {
    this.r1 = new Random(1);
    this.r2 = new Random(2);
    this.r3 = new Random(3);

    this.cellA = new Cell();
    this.cellB = new Cell();
    this.cellC = new Cell();
    this.cellD = new Cell();
    
    this.easy = new Game(9, 9, 10);
    this.normal = new Game(16, 16, 40);
    this.original = new Game(30, 16, 99);
    this.mineTestA = new Game(6, 8, 10, r1);
    this.mineTestB = new Game(3, 3, 4, r2);
    this.mineTestC = new Game(15, 14, 15, r3);

    this.blankCell = new Cell();
    this.mineCell = new Cell();
    this.mineCell.addMine();

    this.mtList = new ArrayList<Cell>();
    this.blankList = new ArrayList<Cell>(Arrays.asList(blankCell));
    this.mineList = new ArrayList<Cell>(Arrays.asList(mineCell));

    this.list10 = new ArrayList<Cell>(Arrays.asList(blankCell, blankCell, blankCell));
    this.list11 = new ArrayList<Cell>(Arrays.asList(mineCell, mineCell, mineCell));
    this.list12 = new ArrayList<Cell>(Arrays.asList(blankCell, mineCell, blankCell));

    this.list20 = new ArrayList<Cell>(
        Arrays.asList(blankCell, blankCell, blankCell, blankCell, blankCell));
    this.list21 = new ArrayList<Cell>(
        Arrays.asList(mineCell, mineCell, mineCell));
    this.list22 = new ArrayList<Cell>(
        Arrays.asList(blankCell, blankCell, blankCell, mineCell, blankCell, blankCell));

    this.twoD1 = new ArrayList<ArrayList<Cell>>();
    this.twoD2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(list20, list21, list22));
  }
  
  // calls Bigbang
  void testBigBang(Tester t) {
    initData();
    int worldWidth = normal.cellsWide * Game.CELL_DIMENSIONS + Game.CELL_DIMENSIONS;

    int BOARD_HEIGHT = normal.cellsHigh * Game.CELL_DIMENSIONS + Game.CELL_DIMENSIONS;

    int worldHeight = BOARD_HEIGHT;

    normal.bigBang(worldWidth, worldHeight, 1);
  }

  // tests addCells()
  boolean testAddCells(Tester t) {
    initData();
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    return t.checkExpect(this.mineTestA.grid.size(), 6)
        && t.checkExpect(this.mineTestA.grid.get(0).size(), 8)
        && t.checkExpect(this.mineTestB.grid.size(), 3)
        && t.checkExpect(this.mineTestB.grid.get(0).size(), 3);
  }

  // test plantMines()
  boolean testPlantMines(Tester t) {
    initData();
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestA.addMines();
    this.mineTestB.addMines();
    return t.checkExpect(this.mineTestA.grid.get(5).get(3).hasMine, false)
        && t.checkExpect(this.mineTestA.grid.get(4).get(4).hasMine, false)
        && t.checkExpect(this.mineTestB.grid.get(1).get(1).hasMine, true)
        && t.checkExpect(this.mineTestB.grid.get(1).get(0).hasMine, true);
  }

  // tests findNeighbors()
  boolean testFindNeighbors(Tester t) {
    initData();
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestA.findNeighbors();
    this.mineTestB.findNeighbors();

    return t.checkExpect(this.mineTestA.grid.get(0).get(0).neighbors.size(), 3)
        && t.checkExpect(this.mineTestA.grid.get(3).get(4).neighbors.size(), 8)
        && t.checkExpect(this.mineTestB.grid.get(2).get(2).neighbors.size(), 3)
        && t.checkExpect(this.mineTestB.grid.get(1).get(2).neighbors.size(), 5)
        && t.checkExpect(this.easy.grid.get(7).get(2).neighbors.size(), 8);
  }

  // tests onMouseClicked()
  boolean testOnMouseClicked(Tester t) {
    initData();

    // add cells to arraylist
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestC.initGrid();

    // give cells mines
    this.mineTestA.addMines();
    this.mineTestB.addMines();
    this.mineTestC.addMines();

    this.mineTestA.onMouseClicked(new Posn(-30, -40), "LeftButton"); 
    this.mineTestC.onMouseClicked(new Posn(30, 130), "LeftButton"); 

    return t.checkExpect(this.mineTestA.flagsPlaced, 0) 
        && t.checkExpect(this.mineTestA.wonGame, false)
        && t.checkExpect(this.mineTestB.flagsPlaced, 0)
        && t.checkExpect(this.mineTestB.lostGame, false) 
        && t.checkExpect(this.mineTestC.wonGame, false);
  }

  boolean testRightClickHelp(Tester t) {
    initData();

    // add cells to arraylist
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestC.initGrid();

    this.mineTestA.rightMouseClickHelper(0, 0);
    this.mineTestB.rightMouseClickHelper(0, 0);
    this.mineTestC.rightMouseClickHelper(0, 0);

    this.mineTestC.rightMouseClickHelper(0, 0);

    return t.checkExpect(this.mineTestA.flagsPlaced, 1) 
        && t.checkExpect(this.mineTestB.flagsPlaced, 1)
        && t.checkExpect(this.mineTestC.flagsPlaced, 0);
  }

  // tests checkIfCell method
  boolean testCheckIfCell(Tester t) {
    initData();

    // add cells to arraylist
    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestC.initGrid();

    return t.checkExpect(this.mineTestA.validMousePos(-5, -10), false)
        && t.checkExpect(this.mineTestA.validMousePos(6, 8), false)
        && t.checkExpect(this.mineTestA.validMousePos(5000, 5000), false)
        && t.checkExpect(this.mineTestC.validMousePos(-1, 0), false);
  }


  // tests updateNeighbors()
  boolean testUpdateNegihbors(Tester t) {
    initData();

    this.mineTestA.initGrid();
    this.mineTestB.initGrid();
    this.mineTestC.initGrid();

    this.mineTestA.findNeighbors();
    this.mineTestB.findNeighbors();
    this.mineTestC.findNeighbors();

    this.mineTestA.addMines();
    this.mineTestB.addMines();
    this.mineTestC.addMines();

    this.mineTestA.updateNeighbors();
    this.mineTestB.updateNeighbors();
    this.mineTestC.updateNeighbors();

    return t.checkExpect(this.mineTestA.grid.get(3).get(5).numberOfMines, 4)
        && t.checkExpect(this.mineTestA.grid.get(0).get(0).numberOfMines, 0)
        && t.checkExpect(this.mineTestB.grid.get(0).get(0).numberOfMines, 3);
  }

  // tests toggleFlag()
  boolean testSwitchFlag(Tester t) {
    initData();
    this.cellA.isFlagged = true;
    this.cellD.isFlagged = false;

    this.cellA.toggleFlag();
    this.cellD.toggleFlag();

    return t.checkExpect(this.cellA.isFlagged, false) && t.checkExpect(this.cellD.isFlagged, true);
  }

  // tests hasMine()
  boolean testHasMine(Tester t) {
    initData();
    cellA.addMine();
    return t.checkExpect(this.cellA.hasMine, true) && t.checkExpect(this.cellD.hasMine, false)
        && t.checkExpect(this.cellC.hasMine, false);
  }

  // tests addNeighbors()
  boolean testAddNeighbors(Tester t) {
    initData();
    this.cellA.addNeighbors(this.twoD1, 0, 0, 0, 0);
    this.cellC.addNeighbors(this.twoD2, 1, 1, 3, 5);
    this.cellB.addNeighbors(this.twoD2, 0, 0, 3, 5);

    return t.checkExpect(this.cellA.neighbors.size(), 0)
        && t.checkExpect(this.cellC.neighbors.size(), 8)
        && t.checkExpect(this.cellD.neighbors.size(), 0);
  }

  // tests countNeighborMineCells()
  boolean testCountNeighborMineCells(Tester t) {
    initData();
    this.cellA.addNeighbors(this.twoD1, 0, 0, 0, 0);
    this.cellC.addNeighbors(this.twoD2, 1, 1, 3, 5);
    this.cellB.addNeighbors(this.twoD2, 0, 0, 3, 5);
    return t.checkExpect(this.cellA.countNeighbors(), 0)
        && t.checkExpect(this.cellB.countNeighbors(), 2)
        && t.checkExpect(this.cellC.countNeighbors(), 2);
  }

  // tests flood()
  boolean testFlood(Tester t) {
    initData();

    this.cellA.neighbors.add(this.cellD);
    this.cellA.neighbors.add(this.cellC);
    this.cellA.isVisible = true;
    this.cellA.flood();

    return t.checkExpect(this.cellA.isVisible, true) 
        && t.checkExpect(this.cellB.isVisible, false)
        && t.checkExpect(this.cellC.isVisible, true)
        && t.checkExpect(this.cellD.isVisible, true);

  }
}