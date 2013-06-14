/* 
 (C) British Crown Copyright 2011 - 2013, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

package net.metarelate.terminology.publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class IDrenderer {
	/**
	 * Generates a QR representation for a URI
	 * @param registerQRImageFile
	 * @param uri
	 * @throws IOException
	 */
	public static void writeToFile(String registerQRImageFile, String uri) throws IOException {
		File registerQRImageFileFW= new File(registerQRImageFile);
		FileOutputStream registerQRImageFileBW = new FileOutputStream(registerQRImageFileFW);
		QRCode.from(uri).to(ImageType.GIF).withSize(120,120).writeTo(registerQRImageFileBW);
		registerQRImageFileBW.close();
		
	}

}
