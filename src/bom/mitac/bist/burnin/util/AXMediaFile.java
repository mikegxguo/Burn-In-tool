/**   
 * @Title MediaFile.java
 * @Package com.example.cameratest
 * @author Alex feong@live.com
 * @date 2013-11-5 8:00:32pm
 * @version V1.1   
 */
package bom.mitac.bist.burnin.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The media file generated by camera
 * 
 * @ClassName MediaFile
 * @author Alex feong@live.com
 * @date 2013-11-4 11:41:44am
 */
public class AXMediaFile {

	private static final String	FEONG	= "feong";

	public static enum MediaType {
		IMAGE, VIDEO
	};

	private File		file;
	private String		path;
	private String		name;
	private String		album;

	public AXMediaFile() {
	}

	public String getAlbum() {
		return album;
	}

	/**
	 * Set and generate the album
	 * 
	 * @param album
	 *            The album name
	 * @return true if the album is generated
	 */
	public boolean setAlbum(String album) {
		this.album = album;
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), album);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				return false;
			}
		}
		path = dir.getPath();
		return true;
	}

	/**
	 * Set and generate the dir saving the picture file
	 * 
	 * @param path
	 *            The dir url
	 * @return true if the album is generated
	 */
	public boolean setPath(File path){
		if (!path.exists()) {
			if (!path.mkdirs()) {
				return false;
			}
		}
		this.path = path.toString();
		return true;
	}
	
	/**
	 * Write the media data to the media file
	 * 
	 * @param data
	 *            The media data
	 * @return
	 */
	public boolean setMedia(byte[] data) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(FEONG, "File not found " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.d(FEONG, "Error accessing file " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Set the media type and generate the file
	 * 
	 * @param type
	 *            If it's an image, use MediaType.IMAGE. If it's a video, use
	 *            MediaType.VIDEO
	 * @return
	 */
	public boolean setType(MediaType type) {
		switch (type) {
			case IMAGE :
				name = "IMG_" + TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_M_TYPE) + ".jpg";
				break;
			case VIDEO :
				name = "VID_" + TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_M_TYPE) + ".mp4";
				break;
			default :
				return false;
		}
		file = new File(path , name);
		return true;
	}
	
	/**
	 * Set and generate the album. Set the media type and generate the file.
	 * Write the media data to the media file
	 * 
	 * @param album
	 *            The album name
	 * @param type
	 *            If it's an image, use MediaType.IMAGE. If it's a video, use
	 *            MediaType.VIDEO
	 * @param data
	 *            The media data
	 * @return If no media saved or saving failed, return NULL
	 */
	public File genMedia(String album, MediaType type, byte[] data) {
		if (setAlbum(album) && setType(type) && setMedia(data)) {
			return file;
		}
		return null;
	}
	
	/**
	 * Set and generate the dir saving the picture file. Set the media type and generate the file.
	 * Write the media data to the media file
	 * 
	 * @param path
	 *            The dir url
	 * @param type
	 *            If it's an image, use MediaType.IMAGE. If it's a video, use
	 *            MediaType.VIDEO
	 * @param data
	 *            The media data
	 * @return If no media saved or saving failed, return NULL
	 */
	public File genMedia(File path, MediaType type, byte[] data) {
		if (setPath(path) && setType(type) && setMedia(data)) {
			return file;
		}
		return null;
	}
}
