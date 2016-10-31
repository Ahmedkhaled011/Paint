package tryingJavaFX;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import ShapeModels.EllipseModel;
import ShapeModels.LineModel;
import ShapeModels.RectangleModel;
import ShapeModels.TriangleModel;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import ShapeModels.*;

/**
 * @web http://java-buddy.blogspot.com/
 */
public class JavaFX_DrawOnCanvas extends Application {

	/** Color Picker to choose colors from. */
	private ColorPicker colorPicker;
	/** Button to insert a new Rectangle. */
	private Button rectangle;
	/** Button to insert a new Line. */
	private Button line;
	/** Button to do free sketching. */
	private Button free;
	/** Button to insert a new Ellipse. */
	private Button ellipse;
	/** Button to insert a new Triangle. */
	private Button triangle;
	/** Button to perform Dynamic Class Loading. */
	private Button dynamicLoad;
	/** Button to delete Shapes. */
	private Button delete;
	/** Saves the objects drawn.*/
	private Button save;
	/** Loads the objects drawn.*/
	private Button load;
	/** previous coordinates of the mouse on the canvas. */
	private Point previous;
	/** point before the previous coordinates of the mouse on the canvas. */
	private Point befPrevious;
	/** array of counters of clicks made in each mode. */
	private int[] actionsCounter;
	/** number of buttons available. */
	private final int NoOfButtons = 5;
	/** Pane to draw shapes on */
	Pane paintPane;
	/** canvas to draw in */
	Canvas canvas;
	/**
	 * state of the current drawing mode(rectangle, line, free sketching, etc.).
	 */
	private char state;
	private SimpleDoubleProperty linefx;
	private SimpleDoubleProperty linefy;
	private SimpleDoubleProperty linesx;
	private SimpleDoubleProperty linesy;
	private SimpleDoubleProperty firstX, firstRX;
	private SimpleDoubleProperty firstY, firstRY;
	private SimpleDoubleProperty secondX, secondRX;
	private SimpleDoubleProperty secondY, secondRY;
	private SimpleDoubleProperty width, widthR;
	private SimpleDoubleProperty length, lengthR;
	private Rectangle previewRect;
	private Line previewLine;
	private Ellipse previewEllipse;

	/**
	 * Initializes the drawing environment.
	 * 
	 * @param primaryStage
	 *            stage at which all components are appended
	 **/
	public void start(Stage primaryStage) {
		canvas = new Canvas(700, 800);
		final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		initDraw(graphicsContext);
		LineController lineCtrl = new LineController();
		RectangleController rectangleCtrl = new RectangleController();
		EllipseController ellipseCtrl = new EllipseController();
		TriangleController triangleCtrl = new TriangleController();
		actionsCounter = new int[NoOfButtons];
		clearActions(actionsCounter);
		// Initially as a line segment.
		paintPane = new Pane();
		state = 'f';
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				switch (state) {
				case 'l': {
					previous = new Point();
					previous.setLocation(event.getX(), event.getY());
					lineCtrl.setPreviousPoint(previous);
					linefx.setValue(event.getX());
					linefy.setValue(event.getY());
					linesx.setValue(event.getX());
					linesy.setValue(event.getY());
					System.out.println(previewRect.getX() + " " + previewRect.getY());
					break;
				}
				case 'r': {
					firstRX.setValue(event.getX());
					firstRY.setValue(event.getY());
					secondRX.setValue(event.getX());
					secondRY.setValue(event.getY());
					// .setX(event.getX());
					// previewRect.setY(event.getY());
					previous = new Point();
					previous.setLocation(event.getX(), event.getY());
					rectangleCtrl.setFirstPt(previous);
					// System.out.println(previewRect.getX() + " " +
					// previewRect.getY());
					break;
				}
				case 'e': {
					firstX.setValue(event.getX());
					firstY.setValue(event.getY());
					secondX.setValue(event.getX());
					secondY.setValue(event.getY());
					previous = new Point();
					previous.setLocation(event.getX(), event.getY());
					break;
				}
				case 't': {
					actionsCounter[3]++;
					if (actionsCounter[3] == 1) {
						befPrevious = new Point();
						befPrevious.setLocation(event.getX(), event.getY());
					} else if (actionsCounter[3] == 2) {
						previous = new Point();
						previous.setLocation(event.getX(), event.getY());
					} else {
						Point last = new Point();
						last.setLocation(event.getX(), event.getY());
						triangleCtrl.setDimensions(befPrevious, previous, last);
						triangleCtrl.draw(paintPane, colorPicker);

						befPrevious = null;
						previous = null;
						actionsCounter[3] = 0;
					}
				}
				case 'd': {
					if (event.getSource() instanceof Rectangle) {
						paintPane.getChildren().remove((Node) event.getSource());
					}
				}
				default: {
					graphicsContext.beginPath();
					graphicsContext.moveTo(event.getX(), event.getY());
					graphicsContext.stroke();
					break;
				}
				}
			}
		});

		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// free sketching mode only while free state.
				if (state == 'f') {
					graphicsContext.lineTo(event.getX(), event.getY());
					graphicsContext.stroke();
				} else if (state == 't') {
					if (befPrevious == null) {
						befPrevious = new Point();
						befPrevious.setLocation(event.getX(), event.getY());
					} else if (previous == null) {
						previous = new Point();
						previous.setLocation(event.getX(), event.getY());
					}
				} else if (state == 'l') {
					linesx.setValue(event.getX());
					linesy.setValue(event.getY());
				} else if (state == 'r') {
					secondRX.setValue(event.getX());
					secondRY.setValue(event.getY());
					widthR.setValue(Math.abs(secondRX.doubleValue() - firstRX.doubleValue()));
					lengthR.setValue(Math.abs(secondRY.doubleValue() - firstRY.doubleValue()));
				} else if (state == 'e') {
					secondX.setValue(event.getX());
					secondY.setValue(event.getY());
					width.setValue(Math.abs(secondX.doubleValue() - firstX.doubleValue()));
					length.setValue(Math.abs(secondY.doubleValue() - firstY.doubleValue()));
				}
			}
		});

		canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (state == 'r') {
					Point current = new Point();
					current.setLocation(event.getX(), event.getY());
					rectangleCtrl.setLastPoint(current);
					rectangleCtrl.drawRectangle(paintPane, canvas, colorPicker);
					firstRX.setValue(0);
					firstRY.setValue(0);
					secondRX.setValue(0);
					secondRY.setValue(0);
					widthR.setValue(0);
					lengthR.setValue(0);
				} else if (state == 'e') {
					Point current = new Point();
					current.setLocation(event.getX(), event.getY());
					ellipseCtrl.setDimensions(previous, current);
					ellipseCtrl.draw(paintPane, canvas, colorPicker);
					firstX.setValue(0);
					firstY.setValue(0);
					secondX.setValue(0);
					secondY.setValue(0);
					width.setValue(0);
					length.setValue(0);
					previous = null;
				} else if (state == 'l') {
					Point end = new Point();
					end.setLocation(event.getX(), event.getY());
					lineCtrl.setEndPoint(end);
					lineCtrl.drawLine(paintPane);
					firstX.setValue(0);
					firstY.setValue(0);
					secondX.setValue(0);
					secondY.setValue(0);
					width.setValue(0);
					length.setValue(0);
					linefx.setValue(0);
					linefy.setValue(0);
					linesx.setValue(0);
					linesy.setValue(0);
					previous = null;
				} else if (state == 'f') {
					graphicsContext.moveTo(event.getX(), event.getY());
				} else if (state == 'd') {
					if (event.getSource() instanceof Rectangle) {
						paintPane.getChildren().remove((Node) event.getSource());
					}
				}
			}
		});
		construct();
		setCanvas(canvas, paintPane, primaryStage);
		paintPane.getChildren().add(previewLine);
		paintPane.getChildren().add(previewRect);
		paintPane.getChildren().add(previewEllipse);
	}

	/**
	 * sets the preview tools.
	 */
	private void construct() {
		firstX = new SimpleDoubleProperty();
		firstY = new SimpleDoubleProperty();
		secondX = new SimpleDoubleProperty();
		secondY = new SimpleDoubleProperty();
		width = new SimpleDoubleProperty();
		length = new SimpleDoubleProperty();
		firstRX = new SimpleDoubleProperty();
		firstRY = new SimpleDoubleProperty();
		secondRX = new SimpleDoubleProperty();
		secondRY = new SimpleDoubleProperty();
		widthR = new SimpleDoubleProperty();
		lengthR = new SimpleDoubleProperty();
		linefy = new SimpleDoubleProperty();
		linefx = new SimpleDoubleProperty();
		linesx = new SimpleDoubleProperty();
		linesy = new SimpleDoubleProperty();
		firstX.setValue(0);
		linefx.setValue(0);
		firstY.setValue(0);
		linefy.setValue(0);
		secondX.setValue(0);
		linesy.setValue(0);
		secondY.setValue(0);
		linesx.setValue(0);
		firstRX.setValue(0);
		firstRY.setValue(0);
		secondRX.setValue(0);
		secondRY.setValue(0);
		previewRect = new Rectangle();
		previewEllipse = new Ellipse();
		previewLine = new Line();
		previewLine.startXProperty().bind(linefx);
		previewLine.startYProperty().bind(linefy);
		previewLine.endXProperty().bind(linesx);
		previewLine.endYProperty().bind(linesy);
		width.setValue(0);
		length.setValue(0);
		widthR.setValue(0);
		lengthR.setValue(0);
		previewRect.setStroke(Color.BLACK);
		previewRect.setFill(Color.WHITE);
		previewRect.xProperty().bind(firstRX);
		previewRect.yProperty().bind(firstRY);
		previewRect.widthProperty().bind(widthR);
		previewRect.heightProperty().bind(lengthR);
		previewEllipse.centerXProperty().bind(firstX);
		previewEllipse.centerYProperty().bind(firstY);
		previewEllipse.radiusXProperty().bind(width);
		previewEllipse.radiusYProperty().bind(length);
		previewEllipse.setStroke(Color.BLACK);
		previewEllipse.setFill(Color.WHITE);
	}

	/**
	 * sets the canvas and other layouts for drawing.
	 * 
	 * @param cvs
	 *            canvas for free sketching
	 * @param pntPne
	 *            pane for shapes addition
	 * @param primaryStage
	 *            Application Stage
	 */
	private void setCanvas(Canvas cvs, Pane pntPne, Stage primaryStage) {
		Group root = new Group();
		initializeButtons();
		buttonActions(cvs);
		pntPne.getChildren().add(cvs);
		HBox hBox = new HBox();
		hBox.getChildren().add(colorPicker);
		hBox.getChildren().addAll(free, line, ellipse, rectangle, triangle);
		VBox vBox = new VBox();
		vBox.getChildren().addAll(hBox, pntPne);
		root.getChildren().addAll(vBox);
		Scene scene = new Scene(root, 700, 725);
		primaryStage.setTitle("Vector Drawing");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Clears the actions array.
	 * 
	 * @param array
	 *            the array of actions to be cleared.
	 */
	private void clearActions(int[] array) {
		Arrays.fill(array, 0);
	}

	/**
	 * Initializes all buttons on the drawing pane.
	 */
	public void initializeButtons() {
		rectangle = new Button("Rectangle");
		line = new Button("Line");
		ellipse = new Button("Ellipse");
		free = new Button("Free");
		triangle = new Button("triangle");
		delete = new Button("Delete");
		save = new Button("Save");
		load = new Button("Load");
		Image rec = new Image("file:rect.png");
	}

	/**
	 * sets action handlers for buttons.
	 */
	public void buttonActions(Canvas paint) {
		free.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 'f';
				clearActions(actionsCounter);
				paint.toFront();
			}
		});
		line.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 'l';
				clearActions(actionsCounter);
				previous = null;
				previewLine = new Line();
				previewLine.startXProperty().bind(linefx);
				previewLine.startYProperty().bind(linefy);
				previewLine.endXProperty().bind(linesx);
				previewLine.endYProperty().bind(linesy);
				// previewRect = null;
			}
		});
		rectangle.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 'r';
				clearActions(actionsCounter);
				previous = null;
				previewRect = new Rectangle();
				previewRect.setStroke(Color.BLACK);
				previewRect.setFill(Color.WHITE);
				previewRect.xProperty().bind(firstRX);
				previewRect.yProperty().bind(firstRY);
				previewRect.widthProperty().bind(widthR);
				previewRect.heightProperty().bind(lengthR);
			}
		});
		ellipse.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 'e';
				clearActions(actionsCounter);
				previous = null;
				previewEllipse = new Ellipse();
				previewEllipse.centerXProperty().bind(firstX);
				previewEllipse.centerYProperty().bind(firstY);
				previewEllipse.radiusXProperty().bind(width);
				previewEllipse.radiusYProperty().bind(length);
				// previewRect = null;
				previewLine = null;
			}
		});
		triangle.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 't';
				clearActions(actionsCounter);
				previous = null;
			}
		});
		delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				state = 'd';
				clearActions(actionsCounter);
				paint.toBack();
			}
		});
		save.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					saveData(state, actionsCounter, previous, befPrevious);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
			
		});
	}
	public void saveData(char state, int[] counters, Point prev, Point befPrev) throws FileNotFoundException {
		try {
			FileOutputStream sav = new FileOutputStream("draw.sav");
			ObjectOutputStream save =
					new ObjectOutputStream(sav);
			((ObjectOutput) sav).writeObject(state);
			((ObjectOutput) sav).writeObject(counters);
			((ObjectOutput) sav).writeObject(prev);
			((ObjectOutput) sav).writeObject(befPrev);
		} catch (Exception exc) {
			throw new FileNotFoundException();
		}
	}
	/**
	 * sets the Color picker and the default colors for sketching.
	 * 
	 * @param gc
	 *            the graphics content of the Scene
	 */
	private void initDraw(GraphicsContext gc) {
		colorPicker = new ColorPicker();
		colorPicker.setValue(Color.BLACK);
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();
		colorPicker.setOnAction(new EventHandler() {
			public void handle(Event t) {
				gc.setStroke(colorPicker.getValue());
			}
		});
		gc.setLineWidth(5);

		gc.fill();
		gc.strokeRect(0, // x of the upper left corner
				0, // y of the upper left corner
				canvasWidth, // width of the rectangle
				canvasHeight); // height of the rectangle

		gc.setFill(colorPicker.getValue());
		gc.setStroke(colorPicker.getValue());
		gc.setLineWidth(1);
	}

	/**
	 * main method.
	 * 
	 * @args needed of called from outside
	 */
	public static void main(String[] args) {
		launch(args);
	}

}