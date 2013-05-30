package net.metarelate.terminology.publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class IDrenderer {

	public static void writeToFile(String registerQRImageFile, String uri) throws IOException {
		File registerQRImageFileFW= new File(registerQRImageFile);
		FileOutputStream registerQRImageFileBW = new FileOutputStream(registerQRImageFileFW);
		QRCode.from(uri).to(ImageType.GIF).withSize(120,120).writeTo(registerQRImageFileBW);
		registerQRImageFileBW.close();
		
	}

}
