import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GRDM_U6_s0577683 implements PlugInFilter {
	static ImagePlus imp; // ImagePlus object

	public static void main(String args[]) {

		IJ.open("Component.jpg");

		GRDM_U6_s0577683 pw = new GRDM_U6_s0577683();
		GRDM_U6_s0577683.imp = IJ.getImage();
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

					if (y < height && x < width) {
						int pos_n = y_n * width_n + x_n;
						int pos = y * width + x;

						pix_n[pos_n] = pix[pos];
					}
				}
			}
		}

		if (choice == "Pixelwiederholung") {
			// Werte -1, damit nicht über den Rand gelaufen wird
			double scaleX = (1.0 * width - 1) / (1.0 * width_n - 1);
			double scaleY = (1.0 * height - 1) / (1.0 * height_n - 1);

			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {

					// nearest neighbor bestimmen
					int pos_n = y_n * width_n + x_n;

					int oldX = (int) (scaleX * x_n);
					int oldY = (int) (scaleY * y_n);

					int pos = oldY * width + oldX;

					pix_n[pos_n] = pix[pos];
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
