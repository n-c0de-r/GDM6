import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GRDM_U6 implements PlugInFilter {
	static ImagePlus imp; // ImagePlus object

	public static void main(String args[]) {

		IJ.open("Component.jpg");

		GRDM_U6 pw = new GRDM_U6();
		GRDM_U6.imp = IJ.getImage();
		pw.run(imp.getProcessor());
	}

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return DOES_RGB + NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}

	public void run(ImageProcessor ip) {

		String[] dropdownmenue = { "Kopie", "Pixelwiederholung", "Bilinear" };

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode", dropdownmenue, dropdownmenue[0]);
		gd.addNumericField("Hoehe:", 500, 0);
		gd.addNumericField("Breite:", 400, 0);

		gd.showDialog();

		int height_n = (int) gd.getNextNumber(); // _n fuer das neue skalierte Bild
		int width_n = (int) gd.getNextNumber();

		int width = ip.getWidth(); // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		String choice = gd.getNextChoice();

		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild", width_n, height_n, 1, NewImage.FILL_BLACK);

		ImageProcessor ip_n = neu.getProcessor();

		int[] pix = (int[]) ip.getPixels();
		int[] pix_n = (int[]) ip_n.getPixels();

		if (choice == "Kopie") {
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					int y = y_n;
					int x = x_n;
					
					//Altes Bild zum neuen kopieren, Bounds checken
					if (y < height && x < width) {
						int pos = y * width + x;
						int pos_n = y_n * width_n + x_n;
						
						pix_n[pos_n] = pix[pos];
					}
				}
			}
		}

		if (choice == "Pixelwiederholung") {
			// Werte um 1 reduzieren, gegen Out of Bounds
			// Skalierungsfaktor zwischen altem und neuem Bild berechnen
			double scaleX = (1.0 * width - 1) / (1.0 * width_n - 1);
			double scaleY = (1.0 * height - 1) / (1.0 * height_n - 1);
						
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					// --> Nächsten neuen Nachbarpixel bestimmen <-- //
					int pos_n = y_n * width_n + x_n;
					
					//Pixel in Relation zum Faktor setzen
					//Originalpixel auslesen
					int oldX = (int) (scaleX * x_n);
					int oldY = (int) (scaleY * y_n);
					
					//Originalpixel auslesen
					int pos = oldY * width + oldX;
					
					//Neuer Pixel übernimmt den alten Wert 1:1
					pix_n[pos_n] = pix[pos];
				}
			}
		}
		
		if (choice == "Bilinear") {
			// Werte um 1 reduzieren, gegen out of bounds
			// Skalierungsfaktor zwischen altem und neuem Bild berechnen
			double scaleX = (1.0 * width - 1) / (1.0 * width_n - 1);
			double scaleY = (1.0 * height - 1) / (1.0 * height_n - 1);
			
			//4 Pixelpositionen
			int A, B, C, D = 0;
			
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					
					int pos_n = y_n * width_n + x_n;
					
					//Pixel in Relation zum Faktor setzen
					//Originalpixel auslesen
					int oldX = (int) (scaleX * x_n);
					int oldY = (int) (scaleY * y_n);
					
					//Abstand alter zu neuer Pixel bestimmen
					double h = (scaleX * x_n) - oldX;
					double v = (scaleY * y_n) - oldY;
					
					int pos = oldX + oldY * width;
					
                    //Pixel auslesen für A, B, C, D in Relation zu A
					//Einfachster Fall: alles Randpixel, daher = A
                    A = pix[pos];
                    B = pix[pos];
                    C = pix[pos];
                    D = pix[pos];
                    
                    //B, C, D mit Randbehandlung
                    if (oldX != width-1) {
                    	B = pix[pos+1];
                    	if (oldY == height-1) {
                    		D = pix[pos+1];
                    	} else {
                            D = pix[pos+1+width];
                        }
                    }
                    
                    else if (oldY != height-1) {
                        C = pix[pos+width];
                        if (oldX == width-1) {
                        	D = pix[pos+width];
                        } else {
                            D = pix[pos+1+width];
                        }
                    }
                    
                    //RGB Werte von A, B, C, D holen
                    int rA = (A >> 16) & 0xff;
                    int gA = (A >> 8) & 0xff;
                    int bA = A & 0xff;
                    
                    int rB = (B >> 16) & 0xff;
                    int gB = (B >> 8) & 0xff;
                    int bB = B & 0xff;
                    
                    int rC = (C >> 16) & 0xff;
                    int gC = (C >> 8) & 0xff;
                    int bC = C & 0xff;
                    
                    int rD = (D >> 16) & 0xff;
                    int gD = (D >> 8) & 0xff;
                    int bD = D & 0xff;
                    
                    //Formel anwenden
                    int r = (int) (rA*(1-h)*(1-v) + rB*h*(1-v) + rC*(1-h)*v + rD*h*v);
                    int g = (int) (gA*(1-h)*(1-v) + gB*h*(1-v) + gC*(1-h)*v + gD*h*v);
                    int b = (int) (bA*(1-h)*(1-v) + bB*h*(1-v) + bC*(1-h)*v + bD*h*v);
                    
                    pix_n[pos_n] = (0xff<<24) | (r<<16) | (g<<8) | (b);
				}
			}
		}
		
		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}

	void showAbout() {
		IJ.showMessage("");
	}
}
