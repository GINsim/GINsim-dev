package org.ginsim.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ginsim.exception.GsException;

import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.managerresources.Translator;

public class IOUtils {

	/**
	 * Produces a FileInputStream initialized with the given path
	 * 
	 * @param path the path for which the InputSTream is desired
	 * @return a FileInputStream initialized with the given path
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static InputStream getStreamForPath(String path) throws IOException,	FileNotFoundException {
		
		URL url = Tools.class.getResource(path);
		if (url != null) {
			return url.openStream();
		}
		return new FileInputStream(path);
	}

	/**
	 * Produces a StringBuffer initialized with the given path
	 * 
	 * @param file_path the path for which the StringBuffer is desired
	 * @return a StringBuffer initialized with the given path
	 * @throws IOException
	 */
	public static StringBuffer readFromFile(String file_path) throws IOException {
		
		StringBuffer sb = new StringBuffer(1024);
		readFromFile(file_path, sb);
		return sb;
	}

	/**
	 * Fill the given StringBuffer with the content of the file located at the given path
	 * 
	 * @param file_path the path to the file to read
	 * @param sb the StringBuffer to fill
	 * @throws IOException
	 */
	public static void readFromFile(String file_path, StringBuffer sb) throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(file_path));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			sb.append(buf, 0, numRead);
		}
		reader.close();
	}
	
	/**
	 * Verify if the file at the given path can be opened in write mode
	 * 
	 * @param path the path of the file to test
	 * @return true if file exists and is writable or file does not exists and can be created
	 * @throws GsException
	 */
	public static boolean isFileWritable( String path) throws GsException {
		
		if (path == null || path.equals("")) {
			return false;
		}
		
		File file = new File(path);
		if (file.exists()) {

			if (file.isDirectory()) {
				throw new GsException(GsException.GRAVITY_ERROR,
						Translator.getString("STR_error_isdirectory"));
			}
			if (!file.canWrite()) {
				throw new GsException(GsException.GRAVITY_ERROR,
						Translator.getString("STR_error_notWritable"));
			}

		}
		try {
			if (!file.createNewFile()) {
				throw new GsException(GsException.GRAVITY_ERROR,
						Translator.getString("STR_error_cantcreate"));
			}
			file.delete();
			return true;
		} catch (Exception e) {
			throw new GsException(GsException.GRAVITY_ERROR, Translator.getString("STR_error_io"));
		}
	}
	

}
