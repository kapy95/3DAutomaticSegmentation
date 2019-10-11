package AutomaticSegmentation.preProcessing;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.IntrinsicVolumes3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Reconstruction;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;
import net.haesleinhuepf.clij.CLIJ;

/**
 * 
 * @author Pedro Gómez-Gálvez, Pedro Rodríguez-Hiruela and Pablo Vicente-Munuera
 *
 */
public class SegmZebrafish implements genericSegmentation {
	
	private ImagePlus inputImp;
	private ImagePlus outputImp;
	private int strelRadius2D;
	private int strelRadius3D;
	private int toleranceWatershed;
	
	private static final int PIXELSTOOPENVOLUME = 20;

	public SegmZebrafish(ImagePlus imp) {
		strelRadius2D = 2;
		strelRadius3D = 2;
		// 10 is a good start point for 8-bit images, 2000 for 16-bits. Minor
		// tolerance more divided objects with watershed
		toleranceWatershed = 0;
		this.inputImp = imp;
	}

	public SegmZebrafish(ImagePlus imp, int radius2D, int radius3D, int tolerance) {
		strelRadius2D = radius2D;
		strelRadius3D = radius3D;
		toleranceWatershed = tolerance;
		this.inputImp = imp;
	}

	/**
	 * @return the ouputImp
	 */
	public ImagePlus getOuputImp() {
		return outputImp;
	}

	/**
	 * @return the segmentedImage
	 */
	public void segmentationProtocol(CLIJ clij, String thresholdMethod) {

		// Convert the image to 8-Bit
		if (this.inputImp.getBitDepth() != 8) {
			ImageConverter converter = new ImageConverter(this.inputImp);
			converter.convertToGray8();
		}
		// Test
		int BitD = this.inputImp.getBitDepth();
		boolean dams = false;
		// double resizeFactor = 1;

		IJ.log(BitD + "-bits convertion");
		System.out.println(BitD + "-bits convertion");

		this.inputImp = filterPreprocessing(this.inputImp, clij, strelRadius3D);

		this.inputImp.show();

		ImagePlus imp_segmented = automaticThreshold(this.inputImp, thresholdMethod);

		// create structuring element (cube of radius 'radius')
		Strel3D shape3D = Strel3D.Shape.BALL.fromRadius(strelRadius3D);

		IJ.log("1 - Fill 3D particles");
		// fill 3D particles
		// ImageStack imgFilled = Reconstruction3D.fillHoles(impClosed);

		// loop for to close hole in 2D. Dilatation + erosion + fill holes
		Strel shape2D = Strel.Shape.DISK.fromRadius(strelRadius2D);

		/***** loop for closing, binarize and filling holes in 2D *****/
		System.out.println("Closing, binarize and filling");
		IJ.log("Closing, binarize and filling");
		ImageStack imgFilled = imp_segmented.getStack().duplicate();
		for (int i = 1; i <= imp_segmented.getStackSize(); i++) {
			ImageProcessor processor = imp_segmented.getStack().getProcessor(i);
			processor = Morphology.opening(processor, shape2D);
			processor = Morphology.closing(processor, shape2D);
			processor = BinaryImages.binarize(processor);
			processor = Reconstruction.fillHoles(processor);
			imgFilled.setProcessor((ImageProcessor) processor.duplicate(), i);
		}
		// progressBar.show(0.1);

		// Volume opening
		System.out.println("Small volume opening");
		IJ.log("Small volume opening");
		ImageStack imgFilterSmall = BinaryImages.volumeOpening(imgFilled, PIXELSTOOPENVOLUME);

		ImageStack resultStack = watershedProcess(BitD, dams, imgFilterSmall, strelRadius3D, toleranceWatershed);

		/****** get array of volumes ******/
		System.out.println("Get labelled volumes");
		IJ.log("Get labelled volumes");

		LabelImages.removeLargestLabel(resultStack);
		int[] labels = LabelImages.findAllLabels(resultStack);
		int nbLabels = labels.length;
		// Filter using volumes 3 times smaller than the median
		double[] volumes = IntrinsicVolumes3D.volumes(resultStack, labels,
				new ImagePlus("", resultStack).getCalibration());
		Arrays.sort(volumes);
		double thresholdVolume = (volumes[nbLabels / 2] / 3);
		// progressBar.show(0.9);

		/******** volume opening ********/
		System.out.println("Opening using the median of volumes");
		IJ.log("Opening using the median of volumes");
		ImageStack imgFilterSize = LabelImages.volumeOpening(resultStack, (int) Math.round(thresholdVolume));

		ImagePlus imp_segmentedFinal = createColouredImageWithLabels(this.inputImp, imgFilterSize);

		// progressBar.show(1);
		this.outputImp = imp_segmentedFinal;

	}
}